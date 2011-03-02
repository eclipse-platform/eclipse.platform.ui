/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.webapp.HelpWebappPlugin;
import org.eclipse.help.internal.webapp.WebappResources;
import org.eclipse.help.internal.webapp.data.UrlUtil;
import org.eclipse.help.internal.webapp.utils.Utils;

/*
 * Class is responsible for implementing security protection.  All servlets
 * who use the org.eclipse.help.webapp.validatedServlet extension point will
 * will be processed for security failures by this class.
 * 
 * Any URL that starts with <path>/vs<etc> will be redirected here for further
 * processing.  If the validatedServlet extension point has an alias that 
 * matches the URL passed here, it will finish the processing and return 
 * results here for validation.  If there are no malicious threats detected,
 * this class will return the output to the client.
 *
 */
public class ValidatorServlet extends HttpServlet {

	private static final long serialVersionUID = -3783758607845176051L;
	private Hashtable servletTable = new Hashtable();
	
	protected void process(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String baseURL = req.getRequestURL().toString();		
		baseURL = baseURL.substring(0, baseURL.indexOf(req.getServletPath()));
		
		Locale locale = UrlUtil.getLocaleObj(req,resp);

		String service = req.getRequestURL().toString().substring(
				(baseURL).length()+("/vs".length())); //$NON-NLS-1$
		
		try {
			HttpServletResponseAdv response = new HttpServletResponseAdv(resp);
			
			HttpServlet servlet = getServlet(service);
			ServletConfig config = getServletConfig();
			servlet.init(config);
			servlet.service(req, response);
			
			if (isSecure(req, response))
				response.commitOutput();
			
		} catch(Exception ex) {
			
			String errorMsg = WebappResources.getString("cantCreateServlet", //$NON-NLS-1$
					locale, service);
			HelpWebappPlugin.logError(errorMsg, ex);
			
			PrintWriter writer = resp.getWriter();
			writer.println(errorMsg);
			ex.printStackTrace(writer);
			
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private HttpServlet getServlet(String name) 
			throws CoreException {
		
		HttpServlet servlet = (HttpServlet)servletTable.get(name);
		
		if (servlet == null) {
			
			IConfigurationElement[] configs = 
				Platform.getExtensionRegistry().getConfigurationElementsFor(HelpWebappPlugin.PLUGIN_ID+".validatedServlet"); //$NON-NLS-1$
			
			for (int c=0; c < configs.length; c++) {
				
				String alias = configs[c].getAttribute("alias"); //$NON-NLS-1$
				if (alias != null) {
					
					if (isMatch(alias, name)) {
						servlet = (HttpServlet)configs[c].createExecutableExtension("class"); //$NON-NLS-1$
						servletTable.put(name, servlet);
						break;
					}
				}
			}
		}
		
		return servlet;
	}
	
	private boolean isMatch(String alias, String name) {
		
		int index = name.indexOf(alias);
		if (index == 0) {
			int offset = alias.length();
			if (name.length() == offset)
				return true;
			char ch = name.charAt(offset);
			if (ch == '/' || ch == '?')
				return true;
		}
		return false;
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		process(req, resp);
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		process(req, resp);
	}
	
	public boolean isSecure(HttpServletRequest req,HttpServletResponseAdv resp) 
			throws SecurityException {
		
		Enumeration names = req.getParameterNames();
		List values = new ArrayList();
		List scripts = new ArrayList();
		
		while (names.hasMoreElements()) {
			
			String name = (String)names.nextElement();
			String val = req.getParameter(name);
			values.add(val);
			if (replaceAll(val, '+', "").indexOf("<script")>-1) //$NON-NLS-1$ //$NON-NLS-2$
				scripts.add(val);
		}
		
		if (resp.getWriter() != null) {
			
			String data = resp.getString();
			for (int s=0; s < scripts.size(); s++)
				if (data.indexOf((String)scripts.get(s)) > -1)
					throw new SecurityException("Potential cross-site scripting detected."); //$NON-NLS-1$
		}
		
		return true;
	}

	public void isScript(List params,OutputStream out) {
		
//		ByteArrayOutputStream bOut = new ByteArrayOutputStream(out);
	}

	
	public String replaceAll(String str, char remove, String add) {
		
		StringBuffer buffer = new StringBuffer();
		for (int s=0; s < str.length(); s++) {
			
			char ch = str.charAt(s);
			if (ch == remove)
				buffer.append(add);
			else
				buffer.append(ch);
		}
		return buffer.toString();
	}
	
	private class HttpServletResponseAdv extends HttpServletResponseWrapper {
		
		private HttpServletResponse response;
		private ByteArrayOutputStream out;
		private ServletPrintWriter writer;
		private SecureServletOutputStream stream;
		
		public HttpServletResponseAdv(HttpServletResponse response) {
			super(response);
			out = new ByteArrayOutputStream();
			this.response = response;
		}

		public PrintWriter getWriter() {
			
			if (writer == null && stream == null)
				writer = new ServletPrintWriter();
			return writer;
		}
		
		public ServletOutputStream getOutputStream() {
			
			if (stream == null && writer == null)
				stream = new SecureServletOutputStream(out);
			return stream;
		}
		
		public void commitOutput() throws IOException {
			
			OutputStream os = response.getOutputStream();
			InputStream is = getInputStream();
			
			Utils.transferContent(is, os);
			
			os.flush();
		}
		
		public InputStream getInputStream() {
			
			if (stream != null) {
			
				try {
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new ByteArrayInputStream(out.toByteArray());
			}
			if (writer != null) {
				
				try {
					return new ByteArrayInputStream(writer.toString().getBytes(getCharacterEncoding()));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
		public String getString() {
			
			if (writer != null)
				return writer.toString();
			
			return null;
		}
	}
	
	
	private class SecureServletOutputStream extends ServletOutputStream {

		private OutputStream out;

		public SecureServletOutputStream(OutputStream out) {
			this.out = out;
		}
		
		public void write(int b) throws IOException {
			out.write(b);
		}
	}
}
