/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;
import java.io.*;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
/**
 * Helper class for jsp initialization
 */
public class ContentUtil {
	private final static String TOC_PROTOCOL = "help:/toc";
	private final static String PREF_PROTOCOL = "help:/prefs";
	private final static String SEARCH_PROTOCOL = "search:/";
	private final static String LINKS_PROTOCOL = "links:/";	
	private ServletContext context;
	private HttpServletRequest request;
	
	/**
	 * Constructor
	 */
	public ContentUtil(ServletContext context, HttpServletRequest request) {
		try {
			this.context = context;
			this.request = request;
			//connector = new EclipseConnector(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads all the TOC's
	 */
	public Element loadTocs() {
		try {
			return loadXML(TOC_PROTOCOL + "/");
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Loads the specified toc.
	 */
	public Element loadTOC(String tocHref) {
		if (tocHref == null || tocHref.trim().length() == 0)
			return null;
		try {
			return loadXML(TOC_PROTOCOL + tocHref);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Loads the toc containing specified topic
	 */
	public Element loadTOCcontainingTopic(String topicHref) {
		if (topicHref == null || topicHref.trim().length() == 0)
			return null;
		try {
			return loadXML(TOC_PROTOCOL + "/?topic=" +topicHref);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Loads the search results for specified query
	 */
	public Element loadSearchResults(String searchQuery) {
		if (searchQuery == null || searchQuery.trim().length() == 0)
			return null;
		try {
			return loadXML(SEARCH_PROTOCOL + "?" + searchQuery);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Loads the list of related links for specified context query.
	 */
	public Element loadLinks(String contextQuery) {
		if (contextQuery == null || contextQuery.trim().length() == 0)
			return null;
		try {
			return loadXML(LINKS_PROTOCOL + "?" + contextQuery);
		} catch (Exception e) {
			return null;
		}
	}	

	/**
	 * Load help preferences.
	 */
	public Element loadPreferences()
	{
		try {
			return loadXML(PREF_PROTOCOL);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Reads and parses an XML stream from specified url
	 */
	public Element loadXML(String url) {
		try {
			EclipseConnector connector = new EclipseConnector(context);
			InputSource xmlSource = new InputSource(connector.openStream(url, request));
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			if (parser.getDocument() == null)
				return null;
			else
				return parser.getDocument().getDocumentElement();
		} catch (Exception e) {
			//e.printStackTrace();
			return null;
		}
	}
	
}