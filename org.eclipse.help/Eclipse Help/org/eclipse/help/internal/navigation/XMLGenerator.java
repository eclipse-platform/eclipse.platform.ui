package org.eclipse.help.internal.navigation;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.contributions.*;

/**
 * Visitor class to walk the help structure and generate navigation xml
 */
public abstract class XMLGenerator {

	protected File outputDir = null;
	protected PrintWriter out = null;
	// XML escaped characters mapping
	private static final String invalidXML[] = { "&", ">", "<", "\"", "\'" };
	private static final String escapedXML[] =
		{ "&amp;", "&gt;", "&lt;", "&quot;", "&apos;" };
	/**
	 * HTMLGenerator constructor comment.
	 */
	public XMLGenerator(File outputDir) {
		super();
		this.outputDir = outputDir;
	}

	// returns a String that is a valid XML string
	// by XML escaping special characters
	protected static String xmlEscape(String cdata) {
		for (int i = 0; i < invalidXML.length; i++)
			cdata = TString.change(cdata, invalidXML[i], escapedXML[i]);
		return cdata;
	}
}