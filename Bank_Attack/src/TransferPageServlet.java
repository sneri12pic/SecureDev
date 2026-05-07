import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;

/**
 * Java servlet for displaying the transfer page, get user input, and call doTransfer servlet.
 *
 * @author Emma He
 */
public class TransferPageServlet extends HttpServlet {
        private static final int TRANSFER_FEE = 2;
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
         * javax.servlet.http.HttpServletResponse)
         * 
         * Retrieve the session from HttpServletRequest.
         * Display the transfer result if it can be retreived from the response.
         * Display the transfer page to get three parameters for transferring: 
         * to, the user transferred to,
         * balance, the amount of balance to transfer, 
         * and submit, to call doTransfer servlet to do the transfer. 
         * 
         */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
                // Get session information
		HttpSession session = req.getSession(false);

		// if no session, go to welcome page; otherwise, write out transfer page
		if(session == null){
			res.sendRedirect("/welcome");
		} else {
			// Get the response message from session
			String result = (String) session.getAttribute("result");
			String username = (String) session.getAttribute("username");
			Integer credits = (Integer) session.getAttribute("credit");
                        String csrfToken = SecurityUtil.ensureCsrfToken(session);
		
			// Set up the response content	
			PrintWriter content = res.getWriter();
			res.setContentType("text/html; charset=utf-8");
			res.setStatus(HttpServletResponse.SC_OK);
	
			// Header of the HTML page, declare the title and CSS files
			content.println("<!DOCTYPE html>");
                        content.println("<html lang='en'>");
                        content.println("<head>");
                        content.println("<meta charset='UTF-8'>");
                        content.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
			content.println("<title>Transfer Page</title>");
                	content.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"./Transfer.css\">");
                	content.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"./Navbar.css\">");
                	content.println("</head>");

			// Body of the HTML page
                	content.println("<body>");

			// Header Section
                        content.println("<header id='headerNav'>");
                        content.println("<div class='header-container'>");
                        content.println("<img src='./logo.png' alt='Logo' class='logo' width='150' height='50'>");
                        content.println("<span class='hello'>Welcome, " + SecurityUtil.escapeHtml(username) + "</span>");
                        content.println("<nav class='navbar'>");
                        content.println("<a class='nav' href='account'>My Account</a>");
                        content.println("<a class='nav' href='transfer'>Transfer</a>");
                        content.println("<a class='nav' href='balance'>Customers</a>");
                        content.println("<form action='logout' method='POST' class='logoutForm'>");
                        content.println("<input type='hidden' name='csrfToken' value='" + csrfToken + "'>");
                        content.println("<input value='Log Out' type='submit' class='logoutInput nav'>");
                        content.println("</form>");
                        content.println("</nav>");
                        content.println("</div>");
                        content.println("</header>");
	
			// Headline
                        content.println("<h1>Transfer Your Balance</h1>");

                        // Form for getting transfer information
                        content.println("<div id=\"container\">");

                        // Display balance based on credits or default from database
                        if (credits != null && credits != -1) {
                            content.println("<div class=\"balance\"><h2>My Balance: " + credits + "$</h2></div>");
                            session.setAttribute("credit", -1);
                        } else {
                            content.println("<div class=\"balance\"><h2>My Balance: " + Database.getBalance(username) + "$</h2></div>");
                        }

                        // Begin the form
                        content.println("<form action=\"transfer\" method=\"POST\" accept-charset=\"utf-8\">");
                        content.println("<input type=\"hidden\" name=\"csrfToken\" value=\"" + csrfToken + "\">");
                        content.println("<input type=\"hidden\" name=\"action\" value=\"preview\">");

                        // Display response message if available
                        if (result != null && !result.isEmpty()) {
                            content.println("<div class=\"message\">" + SecurityUtil.escapeHtml(result) + "</div>");
                            session.setAttribute("result", "");
                        } else {
                            content.println("<div class=\"message\"></div>");
                        }

                        // Display the 'To Card Number' and 'Amount' inputs
                        content.println("<div class=\"form-group\">");
                        content.println("<label for=\"to\">To Card Number:</label>");
                        content.println("<input type=\"text\" name=\"to\" id=\"to\" />");
                        content.println("</div>");

                        content.println("<div class=\"form-group\">");
                        content.println("<label for=\"transferAmount\">Amount:</label>");
                        content.println("<input type=\"text\" name=\"transferAmount\" id=\"transferAmount\" />");
                        content.println("</div>");

                        // Submit Button
                        content.println("<button id=\"submitButton\" type=\"submit\">Submit</button>");

                        content.println("</form>"); // Close the form
                        content.println("</div>"); // Close the container

			
			// Close the tag for the HTML page
			content.println("</body>");
			content.println("</html>");
		}
	}

	/*
         * (non-Javadoc)
         * 
         * @see
         * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
         * javax.servlet.http.HttpServletResponse)
         * 
         * Retrieve the data from the request
         * Connect to the database to transfer the point 
         * If success, send back success message
         * If fail, send back fail message
	 * 
         */
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
		// Get session information
                HttpSession session = req.getSession(false);

                res.setContentType("text/html; charset=utf-8");
                res.setStatus(HttpServletResponse.SC_OK);

                // Get two parameters: the user to whom credits are trasferred and the number of credits to transfer
                String to = req.getParameter("to");
                String transferAmount = req.getParameter("transferAmount");
                int addBalance = parseIntOrZero(transferAmount);

                // If session is not valid, go back to welcome page
                if(session == null){
                        res.sendRedirect("/welcome");
                } else if (!SecurityUtil.isValidCsrfRequest(req)) {
                        res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token.");
                } else if ("confirm".equals(req.getParameter("action"))) {
                        processConfirmedTransfer(req, res, session, to, addBalance);
                } else {
                        renderTransferConfirmation(res, session, to, addBalance);
                }

	}

        private void renderTransferConfirmation(HttpServletResponse res, HttpSession session, String to, int addBalance) throws IOException {
                String username = (String) session.getAttribute("username");
                String fromCard = Database.getCardId(username);
                int fromBalance = Database.getBalance(username);
                boolean validTransfer = Database.isCardExist(to) && addBalance > 0 && !fromCard.equals(to)
                        && fromBalance >= addBalance + TRANSFER_FEE;

                PrintWriter content = res.getWriter();
                content.println("<!DOCTYPE html>");
                content.println("<html lang='en'>");
                content.println("<head>");
                content.println("<meta charset='UTF-8'>");
                content.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                content.println("<title>Confirm Transfer</title>");
                content.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"./Transfer.css\">");
                content.println("</head>");
                content.println("<body>");
                content.println("<h1>Confirm Transfer</h1>");

                if (!validTransfer) {
                        content.println("<div id=\"container\">");
                        content.println("<div class=\"message\">Transfer details are invalid.</div>");
                        content.println("<a href=\"transfer\">Back to transfer</a>");
                        content.println("</div>");
                        content.println("</body></html>");
                        return;
                }

                content.println("<div id=\"container\">");
                content.println("<p>To card: " + SecurityUtil.escapeHtml(to) + "</p>");
                content.println("<p>Transfer amount: " + addBalance + "$</p>");
                content.println("<p>Transfer fee: " + TRANSFER_FEE + "$</p>");
                content.println("<p>Total debit: " + (addBalance + TRANSFER_FEE) + "$</p>");
                content.println("<form action=\"transfer\" method=\"POST\">");
                content.println("<input type=\"hidden\" name=\"csrfToken\" value=\"" + SecurityUtil.ensureCsrfToken(session) + "\">");
                content.println("<input type=\"hidden\" name=\"action\" value=\"confirm\">");
                content.println("<input type=\"hidden\" name=\"to\" value=\"" + SecurityUtil.escapeHtml(to) + "\">");
                content.println("<input type=\"hidden\" name=\"transferAmount\" value=\"" + addBalance + "\">");
                content.println("<input type=\"hidden\" name=\"fee\" value=\"" + TRANSFER_FEE + "\">");
                content.println("<button id=\"submitButton\" type=\"submit\">Confirm Transfer</button>");
                content.println("</form>");
                content.println("</div>");
                content.println("</body></html>");
        }

        private void processConfirmedTransfer(HttpServletRequest req, HttpServletResponse res, HttpSession session, String to, int addBalance) throws IOException {
                session.setAttribute("result", "Fail to transfer.");
                String from = (String) session.getAttribute("username");
                String fromCard = Database.getCardId(from);
                int fromBalance = Database.getBalance(from);

                // Intentionally vulnerable for Part 2: do not trust hidden form fields.
                int fee = parseIntOrZero(req.getParameter("fee"));
                int totalDebit = addBalance + fee;

                if(Database.isCardExist(to) && addBalance > 0 && to != null && from != null
                                && !fromCard.equals(to) && fromBalance >= totalDebit){
                        int result = Database.transferBalanceWithFee(fromCard, to, fromBalance, addBalance, fee);
                        if(result >= 0){
                                session.setAttribute("result", "Success! Fee charged: " + fee + "$");
                                session.setAttribute("credit", result);
                        }
                }
                res.sendRedirect("/transfer");
        }

        private int parseIntOrZero(String value) {
                if(value != null){
                        try {
                                return Integer.parseInt(value);
                        } catch (Exception ex){
                                return 0;
                        }
                }
                return 0;
        }
}
