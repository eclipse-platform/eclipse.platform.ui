package org.eclipse.help.internal.protocols;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.*;

import org.eclipse.help.IToc;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.toc.*;
import org.eclipse.help.internal.util.XMLGenerator;

/**
 * URL to files in the plugin's working directory, as well as
 * to temporary files that might be generated on the fly.
 * One instance of this is a "Table of Contents" URL.
 * Example: http://localhost:80/temp/pluginID/toc.xml
 */
public class TocURL extends HelpURL {
	/**
	 * @url "/pluginid/tocfile.xml"
	 * or "/"
	 */
	public TocURL(String url, String query) {
		super(url, query);
	}
	/**
	 * Returns the path prefix that identifies the URL. 
	 */
	public static String getPrefix() {
		return "toc";
	}
	/**
	 * Opens a stream for reading.
	 * 
	 * @return java.io.InputStream
	 */
	public InputStream openStream() {
		if ("/".equals(url)) {
			return serializeTocs();
		}
		return serializeToc(url);
	}
	/**
	 * @return InputStream from XML representation of TOC
	 */
	private InputStream serializeToc(String tocID) {
		Toc toc =
			(Toc) HelpSystem.getTocManager().getToc(tocID, getLocale().toString());
		if (toc == null)
			return null;
		StringWriter stWriter = new StringWriter();
		new TocWriter(toc, stWriter).generate();
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
		XMLGenerator gen = new XMLGenerator(stWriter);
		gen.println("<tocs>");
		gen.pad++;
		for (int i = 0; i < tocs.length; i++) {
			gen.printPad();
			gen.print("<toc href=\"");
			gen.print(tocs[i].getHref());
			gen.print("\" label=\"");
			gen.print(tocs[i].getLabel());
			gen.println("\"/>");
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
}