package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
/**
 * Helper class to generate xml files.
 */
public class XMLGenerator {
	private File outFile = null;
	private PrintWriter out = null;
	public int pad = 0;
	// XML escaped characters mapping
	private static final String invalidXML[] = { "&", ">", "<", "\"", "\'" };
	private static final String escapedXML[] =
		{ "&amp;", "&gt;", "&lt;", "&quot;", "&apos;" };
	/**
	 * Constructor.
	 */
	public XMLGenerator(Writer writer) {
		this.out = new PrintWriter(writer);
	}
	/**
	 * Constructor.
	 */
	public XMLGenerator(File outFile) {
		super();
		this.outFile = outFile;
		try {
			out =
				new PrintWriter(
					new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(outFile), "UTF8")),
					false /* no aotoFlush */
			);
			println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		} catch (IOException ioe) {
			Logger.logError(Resources.getString("E014", outFile.getAbsolutePath()), ioe);
		}
	}

	// returns a String that is a valid XML string
	// by XML escaping special characters
	public static String xmlEscape(String cdata) {
		for (int i = 0; i < invalidXML.length; i++)
			cdata = TString.change(cdata, invalidXML[i], escapedXML[i]);
		return cdata;
	}
	public void close() {
		out.flush();
		out.close();
		if (out.checkError())
			if (outFile != null)
				Logger.logError(Resources.getString("E015", outFile.getAbsolutePath()), null);
		out = null;
	}
	public void print(Object o) {
		if (out != null)
			out.print(o);
	}
	public void println(Object o) {
		print(o);
		print("\n");
	}
	public void printPad() {
		for (int i = 0; i < pad; i++)
			print(" ");
	}
}