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
package org.eclipse.help.internal.webapp.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.internal.webapp.HelpWebappPlugin;
import org.eclipse.help.internal.webapp.WebappResources;
import org.eclipse.help.internal.webapp.data.UrlUtil;
import org.eclipse.help.internal.webapp.parser.AboutParser;
import org.eclipse.help.internal.webapp.servlet.AboutServlet;
import org.eclipse.help.internal.webapp.servlet.PreferenceWriter;
import org.eclipse.help.internal.webapp.servlet.XMLGenerator;
import org.eclipse.help.internal.webapp.utils.Utils;
import org.osgi.framework.Bundle;

/**
 * Generates either xml, json or html page having informations about either 
 * <code>User-Agent</code>, Help system <code>preferences</code> or the available 
 * plug-ins in Help system like Provider, Plugin name, Version and PluginId.
 * 
 * <p>Extends the {@link org.eclipse.help.internal.webapp.servlet.AboutServlet}
 * servlet.
 * 
 * @param show			- (Optional) specifying either <code>agent</code>
 * 						to view the request's <code>User-Agent</code> info, else
 * 						<code>preferences</code> to view the Help system preferences.
 * 						Do not specify any value to show the available plugins in
 * 						Help web application.
 * @param sortColumn	- (Optional) specifying the column number of the available 
 * 						plug-ins in Help system over which displayed output needs 
 * 						to be sorted. Applicable only if <code>show</code> parameter 
 * 						is <code>null</code>.
 * @param returnType	- (Optional) specifies the return type of the servlet.
 * 						  Accepts either <code>xml</code> (default) or <code>html</code>
 * 						  or <code>json</code>
 * 
 * @return		Informations about either <code>User-Agent</code>, <code>preferences</code>
 * 				or the available plug-ins, either as <code>xml</code> (default) or
 * 				<code>html</code> or <code>json</code>
 * 
 * @version	$Version$
 * 
 **/
public class AboutService extends AboutServlet {

	private static final long serialVersionUID = 1L;
	
	private long service;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		
		String returnType = req.getParameter(Utils.RETURN_TYPE);
		boolean boolIsHTML = (returnType != null 
				&& returnType.equalsIgnoreCase(Utils.HTML));
		// If HTML output is required, call AboutServlet class
		if (boolIsHTML) {
			super.doGet(req, resp);
			return;
		}
		
		// Set standard HTTP/1.1 no-cache headers.
		resp.setHeader("Cache-Control",  //$NON-NLS-1$
				"no-store, no-cache, must-revalidate"); //$NON-NLS-1$
		resp.setContentType("application/xml; charset=UTF-8"); //$NON-NLS-1$
		locale = UrlUtil.getLocaleObj(req, resp);
		
		// create XML response
		String response = processRequest(req, resp);
		
		boolean boolIsJSON = (returnType != null 
				&& returnType.equalsIgnoreCase(Utils.JSON));
		
		// If JSON output is required
		if (boolIsJSON) {
			resp.setContentType("text/plain"); //$NON-NLS-1$
			response = getJSONResponse(response);
		}
		
		resp.getWriter().write(response);
	}

	protected String getJSONResponse(String response)
			throws IOException {
		AboutParser aboutParser = new AboutParser(service);
		InputStream is = null;
		try {
			if (response != null) {
				is = new ByteArrayInputStream(response.getBytes("UTF-8")); //$NON-NLS-1$
				aboutParser.parse(is);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (is != null)
			is.close();

		// Call after the catch.
		// An empty JSON is created if any Exception is thrown
		// Else returns the complete JSON
		return aboutParser.toJSON();
	}

	private String processRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		
		String showParam = req.getParameter("show"); //$NON-NLS-1$
		if ("agent".equalsIgnoreCase(showParam)) { //$NON-NLS-1$
			getAgent(buf, req, resp);
		} else if ("preferences".equalsIgnoreCase(showParam)) { //$NON-NLS-1$
			getPreferences(buf, resp);
		} else {
			getAboutPlugins(buf, req, resp);
		}
		
		return buf.toString();
	}

	private void getAgent(StringBuffer buf, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		service = Utils.AGENT;
		
		String title = WebappResources.getString("userAgent", locale); //$NON-NLS-1$
		String agent = req.getHeader("User-Agent"); //$NON-NLS-1$
		buf.append("<userAgent\n      title=\""); //$NON-NLS-1$
		buf.append(XMLGenerator.xmlEscape(title));
		buf.append("\">"); //$NON-NLS-1$
		buf.append(agent);
		buf.append("</userAgent>"); //$NON-NLS-1$
	}

	private void getPreferences(StringBuffer buf, HttpServletResponse resp)
			throws IOException {
		service = Utils.PREFERENCE;
		
		String title = WebappResources.getString("preferences", locale); //$NON-NLS-1$
		buf.append("<preferences\n      title=\""); //$NON-NLS-1$
		buf.append(XMLGenerator.xmlEscape(title));
		buf.append("\">"); //$NON-NLS-1$
		PreferenceWriter writer = new PreferenceWriter(buf, locale, true);
		writer.writePreferences();
		buf.append("\n</preferences>"); //$NON-NLS-1$
	}

	private void getAboutPlugins(StringBuffer buf, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		service = Utils.ABOUT_PLUGIN;
		
		String sortParam = req.getParameter("sortColumn"); //$NON-NLS-1$
		int sortColumn = 3;
		if (sortParam != null) {
			try {
				sortColumn = Integer.parseInt(sortParam);
			} catch (NumberFormatException e) {}	
		}
		
		String title = WebappResources.getString("aboutPlugins", locale); //$NON-NLS-1$
		buf.append("<aboutPlugins\n      title=\""); //$NON-NLS-1$
		buf.append(XMLGenerator.xmlEscape(title));
		buf.append("\""); //$NON-NLS-1$
		
		List plugins = new ArrayList();
		
		Bundle[] bundles = HelpWebappPlugin.getContext().getBundles();
		for (int k = 0; k < bundles.length; k++) {
         	plugins.add(pluginDetails(bundles[k]));
        }
		
        Comparator pluginComparator = new PluginComparator(sortColumn);
		Collections.sort(plugins, pluginComparator );
		
		String[] headerColumns = new String[]{
		    "provider", //$NON-NLS-1$
		    "pluginName", //$NON-NLS-1$
		    "version", //$NON-NLS-1$
		    "pluginId" //$NON-NLS-1$
		};
		
		for (int i = 0; i < headerColumns.length; i++) {
			buf.append("\n          "); //$NON-NLS-1$
			buf.append(headerColumns[i]);
			buf.append("=\""); //$NON-NLS-1$
			buf.append(XMLGenerator.xmlEscape(WebappResources.getString(headerColumns[i], locale)));
			buf.append("\""); //$NON-NLS-1$
		}
		buf.append(">"); //$NON-NLS-1$
		
		for (Iterator iter = plugins.iterator(); iter.hasNext();) {
			PluginDetails details = (PluginDetails) iter.next();
			buf.append("\n        <plugin"); //$NON-NLS-1$
			for (int i = 0; i < headerColumns.length; i++) {
				buf.append("\n          "); //$NON-NLS-1$
				buf.append(headerColumns[i]);
				buf.append("=\""); //$NON-NLS-1$
				buf.append(XMLGenerator.xmlEscape(details.columns[i]));
				buf.append("\""); //$NON-NLS-1$
			}
			buf.append(">\n        </plugin>"); //$NON-NLS-1$
		}
		buf.append("\n</aboutPlugins>"); //$NON-NLS-1$
	}

}
