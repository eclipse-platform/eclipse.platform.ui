import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;
/**
 * Servlet to interface client with remote Eclipse
 */
public class EclipseServlet extends HttpServlet {
	private static final String RESOURCE_BUNDLE = EclipseServlet.class.getName();
	private ResourceBundle resBundle;
	/**
	 */
	public void init() throws ServletException {
		try {
			resBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
		} catch (Throwable e) {
			//log(resBundle.getString("problemInit"), e); //$NON-NLS-1$
			throw new ServletException(e);
		}
	}
	
	/**
	 *
	 * Called by the server (via the <code>service</code> method) to
	 * allow a servlet to handle a GET request. 
	 *
	 * <p>If the request is incorrectly formatted, <code>doGet</code>
	 * returns an HTTP "Bad Request" message.
	 * @param req   an {@link HttpServletRequest} object that
	 *          contains the request the client has made
	 *          of the servlet
	 * @param resp  an {@link HttpServletResponse} object that
	 *          contains the response the servlet sends
	 *          to the client
	 * @exception IOException   if an input or output error is 
	 *              detected when the servlet handles
	 *              the GET request
	 * @exception ServletException  if the request for the GET
	 *                  could not be handled
	 * @see javax.servlet.ServletResponse#setContentType
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		try {
			String url = req.getPathInfo();
			String query = req.getQueryString();
			if (query != null)
				url += "?" + query;
				
			URL helpURL = new URL("help:" + url);
			transfer(helpURL.openConnection(), resp);
		} catch (Throwable e) {
			log(resBundle.getString("problemGet"), e); //$NON-NLS-1$
			throw new ServletException(e);
		}
	}
	/**
	 *
	 * Called by the server (via the <code>service</code> method)
	 * to allow a servlet to handle a POST request.
	 *
	 * Handle the search requests,
	 *
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		try {
			String query = ""; //$NON-NLS-1$
			boolean firstParam = true;
			for (Enumeration params = req.getParameterNames(); params.hasMoreElements();) {
				String param = (String) params.nextElement();
				String[] values = req.getParameterValues(param);
				for (int i = 0; i < values.length; i++)
					if (firstParam) {
						query += "?" + param + "=" + values[i]; //$NON-NLS-2$ //$NON-NLS-1$
						firstParam = false;
					} else
						query += "&" + param + "=" + values[i]; //$NON-NLS-2$ //$NON-NLS-1$
			}
			String url = req.getPathInfo();
			url += query;
			URL helpURL = new URL("help:"+ url);
			transfer(helpURL.openConnection(), resp);
		} catch (Throwable e) {
			log(resBundle.getString("problemPost"), e); //$NON-NLS-1$
			throw new ServletException(e);
		}
	}
	
	private void transfer(URLConnection con, HttpServletResponse resp)
		throws IOException {
		con.setAllowUserInteraction(false);
		con.setDoInput(true);
		con.connect();
		resp.setContentType(con.getContentType());
		resp.setHeader(
			"Cache-Control",
			"max-age=" + (con.getExpiration() - System.currentTimeMillis()));
		InputStream is = con.getInputStream();
		if (is != null) {
			OutputStream os = resp.getOutputStream();
			byte buf[] = new byte[4096];
			int n = is.read(buf);
			while (n > -1) {
				if (n > 0)
					os.write(buf, 0, n);
				n = is.read(buf);
			}
			os.flush();
			is.close();
		}
	}
}