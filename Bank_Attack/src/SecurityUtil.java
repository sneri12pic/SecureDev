import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public final class SecurityUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String CSRF_SESSION_KEY = "csrfToken";

    private SecurityUtil() {
    }

    public static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#x27;");
                    break;
                default:
                    escaped.append(c);
            }
        }
        return escaped.toString();
    }

    public static String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String ensureCsrfToken(HttpSession session) {
        Object existing = session.getAttribute(CSRF_SESSION_KEY);
        if (existing instanceof String && !((String) existing).isEmpty()) {
            return (String) existing;
        }

        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        session.setAttribute(CSRF_SESSION_KEY, token);
        return token;
    }

    public static boolean isValidCsrfRequest(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        }

        Object expected = session.getAttribute(CSRF_SESSION_KEY);
        String provided = req.getParameter("csrfToken");
        if (provided == null || provided.isEmpty()) {
            provided = req.getHeader("X-CSRF-Token");
        }

        return expected instanceof String && ((String) expected).equals(provided);
    }

    public static boolean isSafeStoredPath(String path) {
        return path != null && path.matches("uploads/[A-Za-z0-9._-]+\\.(png|jpg|jpeg|gif|webp)");
    }
}
