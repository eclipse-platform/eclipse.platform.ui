package org.eclipse.help.internal.navigation;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.io.*;
import java.util.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.contributions.xml.HelpContribution;

/**
 * This generates the XML file for the help navigation.
 */
public class XMLNavGenerator extends XMLGenerator {
	protected StringBuffer pad = new StringBuffer();
	protected final static String indent = "  "; // two blanks indentation 
	protected Vector viewNames = new Vector();

	protected static final String NAV_XML_FILENAME = "_nav.xml";

	/**
	 * @param viewSet com.ibm.itp.ua.view.ViewSet
	 * @param outputDir java.io.File
	 */
	public XMLNavGenerator(InfoSet infoSet, File outputDir) {
		super(infoSet, outputDir);
		viewChar = 'a';
	}
	/**
	 * @param view com.ibm.itp.ua.view.View
	 */
	public void visit(InfoSet infoSet) {
		try {
			File outputFile = new File(outputDir, NAV_XML_FILENAME);
			out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));

			out.println("<?xml version=\"1.0\"?>");
			out.println("<?xml-stylesheet href=\"tree.xsl\" type=\"text/xsl\"?>");

			out.println();

			String href = infoSet.getHref();
			String id = infoSet.getID();
			String plugin = id.substring(0, id.lastIndexOf("."));
			boolean standalone = infoSet.isStandalone();

			StringBuffer options = new StringBuffer();
			options.append("id=\"");
			options.append(infoSet.getID());
			options.append("\" label=\"");
			options.append(((HelpContribution) infoSet).getRawLabel());
			options.append("\" ");
			if (!"".equals(href)) {
				options.append("href=\"");
				options.append(href);
				options.append("\" ");
			}
			if (standalone) {
				options.append("standalone=\"true\" ");
			}

			out.print("<infoset ");
			out.print(options);
			out.println(">");

			out.println();

			visitChildren(infoSet);

			out.println("</infoset>");
			out.println();

			out.flush();
			out.close();

		} catch (IOException ioe) {
			Logger.logError(Resources.getString("Error_in_generating_navigation"), ioe);
		}
	}
	/**
	 * @param view com.ibm.itp.ua.view.View
	 */
	public void visit(InfoView view) {

		StringBuffer options = new StringBuffer();
		options.append("id=\"");
		options.append(generateID(view));
		options.append("\" label=\"");
		options.append(((HelpContribution) view).getRawLabel());
		options.append("\" ");

		out.println(
			"<!-- "
				+ ((HelpContribution) view).getRawLabel()
				+ " infoview ["
				+ view.getID()
				+ "] -->");
		out.print("<infoview ");
		out.print(options.toString());
		out.println(">");

		// Generate the content of this view
		visitChildren(view);

		out.println("</infoview>"); // close the view division 
		out.println(
			"<!-- End of " + ((HelpContribution) view).getRawLabel() + " infoview -->");
		out.println();

		// Use a new character for the identifiers in next view
		viewChar++;
	}
	/**
	 * @param viewNode com.ibm.itp.ua.view.ViewNode
	 */
	public void visit(Topic topic) {
		try {
			String href = reduceURL(topic.getHref());
			String id = topic.getID();
			String plugin = id.substring(0, id.lastIndexOf("."));
			/*
			if (href == null || "".equals(href)) { 
				href = "";
			} else {
				href = "/" + plugin + "/" + href; 
			}
			*/
			StringBuffer anchor = new StringBuffer();
			pad.append(indent);
			anchor
				.append(pad)
				.append("<topic href=\"")
				.append(href)
				.append("\" id=\"")
				.append(id)
				.append("\" label=\"")
				.append(((HelpContribution) topic).getRawLabel())
				.append("\" >");

			if (topic.getChildren().hasNext()) {
				out.println(anchor.toString());

				// Generate nested topics
				visitChildren(topic);
			} else
				out.print(anchor.toString());

			out.print(pad);
			out.println("</topic>");

			pad.setLength(pad.length() - indent.length());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
