package org.eclipse.help.internal.navigation;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
	
	// XML escape constants
	protected static final int AMP			= '&';
	protected static final int GT			= '>';
	protected static final int LT			= '<';
	protected static final int QUOT			= '\"';
	protected static final int APOS			= '\'';
	
	protected static final String XML_AMP	= "&amp;";
	protected static final String XML_GT	= "&gt;";
	protected static final String XML_LT	= "&lt;";
	protected static final String XML_QUOT	= "&quot;";
	protected static final String XML_APOS	= "&apos;";


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

			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
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
			String infosetLabel = ((HelpContribution) infoSet).getRawLabel();
			options.append(getValidXMLString(infosetLabel));
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
		options.append(view.getID());
		options.append("\" label=\"");
		String viewLabel = ((HelpContribution) view).getRawLabel();
		viewLabel = getValidXMLString(viewLabel);
		options.append(viewLabel);
		options.append("\" ");

		out.println(
			"<!-- "
				+ viewLabel
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
			"<!-- End of " + viewLabel + " infoview -->");
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
			
			String topicLabel = ((HelpContribution) topic).getRawLabel();
			topicLabel = getValidXMLString(topicLabel);
			
			anchor
				.append(pad)
				.append("<topic href=\"")
				.append(href)
				.append("\" id=\"")
				.append(id)
				.append("\" label=\"")
				.append(topicLabel)
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
	
	
	// returns a String that is a valid XML string
	public static String getValidXMLString (String aLabel) {
		StringBuffer buffer = new StringBuffer(aLabel);
		updateXMLBuffer(buffer, AMP,  XML_AMP);
		updateXMLBuffer(buffer, GT,  XML_GT);
		updateXMLBuffer(buffer, LT,  XML_LT);
		updateXMLBuffer(buffer, APOS, XML_APOS);
		updateXMLBuffer(buffer, QUOT, XML_QUOT);
		
		return buffer.toString();
	} 
	
	
	private static void updateXMLBuffer(StringBuffer buffer, int invalidXMLChar, String validXMLString ) {
		String label = buffer.toString();
		int x = label.indexOf(invalidXMLChar);
		while (x != -1) {
			buffer.deleteCharAt(x);
			buffer.insert(x, validXMLString);
			label = buffer.toString();
			x = label.indexOf(invalidXMLChar, x+1);
		}
		
		return;		
	
	}

	
	
}
