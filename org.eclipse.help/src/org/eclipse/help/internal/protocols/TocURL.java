package org.eclipse.help.internal.protocols;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.*;

import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.toc.*;
import org.eclipse.help.internal.util.XMLGenerator;


/**
 * URL-like description of help table of contents. This is part of the help protocol (help:/toc)
 * <ul>
 * <li>toc/pluginid/tocfile.xml: the toc defined by the specified toc xml</li> 
 * <li>toc/: all the toc's </li>
 * <li>toc/topic=/pluginid/topic.html: the toc that contains the specified topic </li>
 * </ul>
 */
public class TocURL extends HelpURL {
	public final static String TOC = "toc";
	
	/**
	 * @url "/pluginid/tocfile.xml"
	 * or "/"
	 * or "/?topic=/pluginid/topic.html"
	 */
	public TocURL(String url, String query) {
		super(url, query);
	}
	/**
	 * Returns the path prefix that identifies the URL. 
	 */
	public static String getPrefix() {
		return TOC;
	}
	/**
	 * Opens a stream for reading.
	 * 
	 * @return java.io.InputStream
	 */
	public InputStream openStream() {
		if ("/".equals(url)) {
			if (getValue("topic") == null)
				return serializeTocs();
			else
				return serializeToc(findTocContainingTopic(getValue("topic")));
		} 
		return serializeToc(url);
	}
	/**
	 * @return InputStream from XML representation of TOC
	 */
	private InputStream serializeToc(String tocID) {
		IToc toc =
			(Toc) HelpSystem.getTocManager().getToc(tocID, getLocale().toString());
		return serializeToc(toc);
	}
	/**
	 * @return InputStream from XML representation of TOC
	 */
	private InputStream serializeToc(IToc toc) {
		if (toc == null)
			return null;
		StringWriter stWriter = new StringWriter();
		TocWriter tocWriter = new TocWriter(stWriter);
		tocWriter.generate(toc, true);
		tocWriter.close();
		try {
			return new ByteArrayInputStream(stWriter.toString().getBytes("UTF8"));
		} catch (UnsupportedEncodingException uee) {
			return null;
		}
	}
	
	/**
	 * @return InputStream from XML representation of TOC list
	 */
	private InputStream serializeTocs() {
		TocManager tocManager = HelpSystem.getTocManager();
		IToc[] tocs = tocManager.getTocs(getLocale().toString());
		StringWriter stWriter = new StringWriter();
		TocWriter gen = new TocWriter(stWriter);
		gen.println("<tocs>");
		gen.pad++;
		for (int i = 0; i < tocs.length; i++) {
			gen.printPad();
			gen.generate(tocs[i], false);
		}
		gen.pad--;
		gen.println("</tocs>");
		gen.close();
		try {
			return new ByteArrayInputStream(stWriter.toString().getBytes("UTF8"));
		} catch (UnsupportedEncodingException uee) {
			return null;
		}
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

		IToc[] tocs = HelpSystem.getTocManager().getTocs(getLocale().toString());
		for (int i=0; i<tocs.length; i++)
			if (tocs[i].getTopic(topic) != null)
				return tocs[i];

		// nothing found
		return null;
	}

}