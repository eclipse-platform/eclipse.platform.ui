/* $Id: SnoopServlet.java,v 1.2 1999/10/15 21:31:48 duncan Exp $
 *
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 *
 * @author James Duncan Davidson <duncan@eng.sun.com>
 * @author Jason Hunter <jch@eng.sun.com>
 */

public class SnoopServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        PrintWriter out = response.getWriter();
        response.setContentType("text/plain");

        out.println("Snoop Servlet");
	out.println();
	out.println("Servlet init parameters:");
	Enumeration e = getInitParameterNames();
	while (e.hasMoreElements()) {
	    String key = (String)e.nextElement();
	    String value = getInitParameter(key);
	    out.println("   " + key + " = " + value); 
	}
	out.println();

	out.println("Context init parameters:");
	ServletContext context = getServletContext();
	Enumeration enum = context.getInitParameterNames();
	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
            Object value = context.getInitParameter(key);
            out.println("   " + key + " = " + value);
	}
	out.println();

	out.println("Context attributes:");
	enum = context.getAttributeNames();
	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
            Object value = context.getAttribute(key);
            out.println("   " + key + " = " + value);
	}
	out.println();
	
        out.println("Request attributes:");
        e = request.getAttributeNames();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            Object value = request.getAttribute(key);
            out.println("   " + key + " = " + value);
        }
        out.println();
        out.println("Servlet Name: " + getServletName());
        out.println("Protocol: " + request.getProtocol());
        out.println("Scheme: " + request.getScheme());
        out.println("Server Name: " + request.getServerName());
        out.println("Server Port: " + request.getServerPort());
        out.println("Server Info: " + context.getServerInfo());
        out.println("Remote Addr: " + request.getRemoteAddr());
        out.println("Remote Host: " + request.getRemoteHost());
        out.println("Character Encoding: " + request.getCharacterEncoding());
        out.println("Content Length: " + request.getContentLength());
        out.println("Content Type: "+ request.getContentType());
        out.println("Locale: "+ request.getLocale());
        out.println("Default Response Buffer: "+ response.getBufferSize());
        out.println();
        out.println("Parameter names in this request:");
        e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            String[] values = request.getParameterValues(key);
            out.print("   " + key + " = ");
            for(int i = 0; i < values.length; i++) {
                out.print(values[i] + " ");
            }
            out.println();
        }
        out.println();
        out.println("Headers in this request:");
        e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            String value = request.getHeader(key);
            out.println("   " + key + ": " + value);
        }
        out.println();  
        out.println("Cookies in this request:");
        Cookie[] cookies = request.getCookies();
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            out.println("   " + cookie.getName() + " = " + cookie.getValue());
        }
        out.println();

        out.println("Request Is Secure: " + request.isSecure());
        out.println("Auth Type: " + request.getAuthType());
        out.println("HTTP Method: " + request.getMethod());
        out.println("Remote User: " + request.getRemoteUser());
        out.println("Request URI: " + request.getRequestURI());
        out.println("Context Path: " + request.getContextPath());
        out.println("Servlet Path: " + request.getServletPath());
        out.println("Path Info: " + request.getPathInfo());
	out.println("Path Trans: " + request.getPathTranslated());
        out.println("Query String: " + request.getQueryString());

        out.println();
        HttpSession session = request.getSession();
        out.println("Requested Session Id: " +
                    request.getRequestedSessionId());
        out.println("Current Session Id: " + session.getId());
	out.println("Session Created Time: " + session.getCreationTime());
        out.println("Session Last Accessed Time: " +
                    session.getLastAccessedTime());
        out.println("Session Max Inactive Interval Seconds: " +
                    session.getMaxInactiveInterval());
        out.println();
        out.println("Session values: ");
        Enumeration names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            out.println("   " + name + " = " + session.getAttribute(name));
        }
    }
}

