import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Coursework 2 additional vulnerability: Missing Authentication for Critical
 * Function (CWE-306). This servlet is intentionally vulnerable for Part 2
 * because it allows password reset without checking for a logged-in admin.
 */
public class AdminPasswordResetServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        if (!isAdminSession(session)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access required.");
            return;
        }

        res.setContentType("text/html; charset=utf-8");
        res.setStatus(HttpServletResponse.SC_OK);
        String csrfToken = SecurityUtil.ensureCsrfToken(session);

        PrintWriter content = res.getWriter();
        content.println("<!DOCTYPE html>");
        content.println("<html lang='en'>");
        content.println("<head>");
        content.println("<meta charset='UTF-8'>");
        content.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        content.println("<title>Admin Password Reset</title>");
        content.println("<style>");
        content.println("body { font-family: Arial, sans-serif; margin: 0; background: #f5f7fb; color: #1f2937; }");
        content.println("main { max-width: 480px; margin: 80px auto; padding: 32px; background: #fff; border: 1px solid #d8dee9; border-radius: 8px; }");
        content.println("h1 { margin: 0 0 8px; font-size: 24px; }");
        content.println("p { margin: 0 0 24px; color: #4b5563; }");
        content.println("label { display: block; margin: 16px 0 6px; font-weight: bold; }");
        content.println("input { width: 100%; box-sizing: border-box; padding: 10px; border: 1px solid #b8c0cc; border-radius: 4px; font-size: 15px; }");
        content.println("button { margin-top: 22px; padding: 10px 16px; border: 0; border-radius: 4px; background: #1d4ed8; color: white; font-weight: bold; cursor: pointer; }");
        content.println("</style>");
        content.println("</head>");
        content.println("<body>");
        content.println("<main>");
        content.println("<h1>Admin Password Reset</h1>");
        content.println("<p>Reset a user password from this administrative screen.</p>");
        content.println("<form action=\"admin-reset\" method=\"POST\" accept-charset=\"utf-8\">");
        content.println("<input type=\"hidden\" name=\"csrfToken\" value=\"" + csrfToken + "\">");
        content.println("<label for=\"username\">Username</label>");
        content.println("<input id=\"username\" type=\"text\" name=\"username\">");
        content.println("<label for=\"newPassword\">New password</label>");
        content.println("<input id=\"newPassword\" type=\"password\" name=\"newPassword\">");
        content.println("<button type=\"submit\">Reset Password</button>");
        content.println("</form>");
        content.println("</main>");
        content.println("</body>");
        content.println("</html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (!isAdminSession(session)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access required.");
            return;
        }
        if (!SecurityUtil.isValidCsrfRequest(req)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token.");
            return;
        }

        res.setContentType("text/html; charset=utf-8");
        res.setStatus(HttpServletResponse.SC_OK);

        String username = req.getParameter("username");
        String newPassword = req.getParameter("newPassword");

        boolean result = false;
        if (username != null && newPassword != null && !username.isEmpty() && newPassword.length() >= 8) {
            result = Database.updatePassword(username, newPassword);
        }

        PrintWriter content = res.getWriter();
        content.println("<!DOCTYPE html>");
        content.println("<html lang='en'>");
        content.println("<head>");
        content.println("<meta charset='UTF-8'>");
        content.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        content.println("<title>Password Reset Result</title>");
        content.println("<style>");
        content.println("body { font-family: Arial, sans-serif; margin: 0; background: #f5f7fb; color: #1f2937; }");
        content.println("main { max-width: 480px; margin: 80px auto; padding: 32px; background: #fff; border: 1px solid #d8dee9; border-radius: 8px; }");
        content.println("h1 { margin: 0 0 20px; font-size: 24px; }");
        content.println("a { color: #1d4ed8; }");
        content.println("</style>");
        content.println("</head>");
        content.println("<body>");
        content.println("<main>");
        if (result) {
            content.println("<h1>Password reset for " + SecurityUtil.escapeHtml(username) + "</h1>");
        } else {
            content.println("<h1>Password reset failed</h1>");
        }
        content.println("<a href=\"welcome\">Back to login</a>");
        content.println("</main>");
        content.println("</body>");
        content.println("</html>");
    }

    private boolean isAdminSession(HttpSession session) {
        if (session == null) {
            return false;
        }

        String username = (String) session.getAttribute("username");
        return username != null && "admin".equals(Database.getType(username));
    }
}
