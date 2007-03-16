/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import java.io.*;

import org.eclipse.help.internal.base.util.*;
import org.eclipse.help.internal.webapp.*;

/**
 * Helper class to generate xml files.
 */
public class XMLGenerator {
	private File outFile = null;

	private PrintWriter out = null;

	public int pad = 0;

	// XML escaped characters mapping
	private static final String invalidXML[] = { "&", ">", "<", "\"", "\'" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	private static final String escapedXML[] = {
			"&amp;", "&gt;", "&lt;", "&quot;", "&apos;" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	/**
	 * Constructor.
	 */
	public XMLGenerator(Writer writer) {
		if (writer instanceof PrintWriter)
			this.out = (PrintWriter) writer;
		else
			this.out = new PrintWriter(writer);
	}

	/**
	 * Constructor.
	 */
	public XMLGenerator(File outFile) {
		super();
		this.outFile = outFile;
		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outFile), "UTF8")), //$NON-NLS-1$
					false /* no aotoFlush */
			);
			println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
		} catch (IOException ioe) {
			HelpWebappPlugin.logError("Error accessing file: " //$NON-NLS-1$
					+ outFile.getAbsolutePath() + "", ioe); //$NON-NLS-1$
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
				HelpWebappPlugin.logError("Errors occurred generating file: " //$NON-NLS-1$
						+ outFile.getAbsolutePath() + "", null); //$NON-NLS-1$
		out = null;
	}

	public void print(Object o) {
		if (out != null)
			out.print(o);
	}

	public void println(Object o) {
		print(o);
		print("\n"); //$NON-NLS-1$
	}

	public void printPad() {
		for (int i = 0; i < pad; i++)
			print(" "); //$NON-NLS-1$
	}
}
