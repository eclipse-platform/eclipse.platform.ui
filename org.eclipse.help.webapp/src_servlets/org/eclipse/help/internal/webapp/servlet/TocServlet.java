/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.webapp.servlet;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.eclipse.help.IToc;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.toc.*;
/**
 * URL-like description of help table of contents. 
 * <ul>
 * <li>toc/pluginid/tocfile.xml: the toc defined by the specified toc xml</li> 
 * <li>toc/: all the toc's </li>
 * <li>toc/?topic=/pluginid/topic.html: a list of toc that contain the specified topic </li>
 * </ul>
 */
public class TocServlet extends HttpServlet {
	private String locale;

	/**
	 * Called by the server (via the <code>service</code> method) to
	 * allow a servlet to handle a GET request. 
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		
		locale = UrlUtil.getLocale(req);
		req.setCharacterEncoding("UTF-8");
		
		resp.setContentType("application/xml; charset=UTF-8");
		resp.setHeader("Cache-Control",	"max-age=10000");
						
		if ("/".equals(req.getPathInfo())) {
			if (req.getParameter("topic") == null)
				serializeTocs(resp);
			else	
				serializeTocs(findTocContainingTopic(req.getParameter("topic")), resp);
		} else {
			serializeToc(req.getPathInfo(), resp);
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
		doGet(req, resp);
	}
	
	
	/**
	 * XML representation of TOC
	 */
	private void serializeToc(String tocID, HttpServletResponse resp) throws ServletException, IOException {
		IToc toc =
			(Toc) HelpSystem.getTocManager().getToc(tocID, locale);
		serializeToc(toc, resp);
	}
	/**
	 * XML representation of TOC
	 */
	private void serializeToc(IToc toc, HttpServletResponse resp) throws ServletException, IOException {
		if (toc == null)
			throw new ServletException();
		
		TocWriter tocWriter = new TocWriter(resp.getWriter());
		tocWriter.generate(toc, true);
		tocWriter.close();
	}
	
	/**
	 * XML representation of TOC list
	 */
	private void serializeTocs(HttpServletResponse resp) throws ServletException, IOException{
		TocManager tocManager = HelpSystem.getTocManager();
		IToc[] tocs = tocManager.getTocs(locale);

		TocWriter gen = new TocWriter(resp.getWriter());
		gen.println("<tocs>");
		gen.pad++;
		for (int i = 0; i < tocs.length; i++) {
			gen.printPad();
			gen.generate(tocs[i], false);
		}
		gen.pad--;
		gen.println("</tocs>");
		gen.close();
	}
	
	/**
	 * @return InputStream from XML representation of TOC list
	 */
	private void serializeTocs(IToc toc, HttpServletResponse resp) throws ServletException, IOException{
		if (toc == null)
			throw new ServletException();

		TocWriter gen = new TocWriter(resp.getWriter());
		gen.println("<tocs>");
		gen.pad++;
		gen.printPad();
		gen.generate(toc, false);
		gen.pad--;
		gen.println("</tocs>");
		gen.close();
	}
	
	/**
	 * Finds a TOC that contains specified topic
	 * @param topic the topic href
	 */
	public IToc findTocContainingTopic(String topic) {
		if (topic == null || topic.equals(""))
			return null;

		int index = topic.indexOf("help:/");
		if (index != -1)
			topic = topic.substring(index + 5);
		index = topic.indexOf('?');
		if (index != -1)
			topic = topic.substring(0, index);

		if (topic == null || topic.equals(""))
			return null;

		IToc[] tocs = HelpSystem.getTocManager().getTocs(locale);
		for (int i=0; i<tocs.length; i++)
			if (tocs[i].getTopic(topic) != null)
				return tocs[i];

		// nothing found
		return null;
	}
}