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
public class XMLNavGenerator extends XMLGenerator implements Visitor {
	protected StringBuffer pad = new StringBuffer();
	protected final static String indent = "  "; // two blanks indentation 
	protected Vector viewNames = new Vector();
	protected InfoSet infoSet = null;

	protected static final String NAV_XML_FILENAME = "_nav.xml";

	/**
	 * @param viewSet com.ibm.itp.ua.view.ViewSet
	 * @param outputDir java.io.File
	 */
	public XMLNavGenerator(InfoSet infoSet, File outputDir) {
		super(outputDir);
		this.infoSet = infoSet;
	}
	/**
	 */
	public void generate() {
		// The html generator is a visitor that needs to start from the view set
		// and will descend to children, etc....
		infoSet.accept(this);
	}
	/**
	 * @param view com.ibm.itp.ua.view.View
	 */
	public void visit(InfoSet infoSet) {
		try {
			File outputFile = new File(outputDir, NAV_XML_FILENAME);
			out =
				new PrintWriter(
					new BufferedWriter(
						new OutputStreamWriter(
							new BufferedOutputStream(new FileOutputStream(outputFile)),
							"UTF-8")));

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
			options.append(xmlEscape(infosetLabel));
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
		viewLabel = xmlEscape(viewLabel);
		options.append(viewLabel);
		options.append("\" ");

		out.println("<!-- " + viewLabel + " infoview [" + view.getID() + "] -->");
		out.print("<infoview ");
		out.print(options.toString());
		out.println(">");

		// Generate the content of this view
		visitChildren(view);

		out.println("</infoview>"); // close the view division 
		out.println("<!-- End of " + viewLabel + " infoview -->");
		out.println();

		// Use a new character for the identifiers in next view
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
			topicLabel = xmlEscape(topicLabel);

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
	public void visitChildren(Contribution con) {
		for (Iterator e = con.getChildren(); e.hasNext();) {
			Contribution c = (Contribution) e.next();
			c.accept(this);
		}
	}
	/**
	 * Simplifies url path by removing "/.." with the parent directory from the path
	 * @return java.lang.String
	 * @param url java.lang.String
	 */
	protected static String reduceURL(String url) {
		if (url == null)
			return url;
		while (true) {
			int index = url.lastIndexOf("/../");
			if (index <= 0)
				break; //there is no "/../" or nothing before "/../" to simplify
			String part1 = url.substring(0, index);
			String part2 = url.substring(index + "/..".length());
			index = part1.lastIndexOf("/");
			if (index >= 0)
				url = part1.substring(0, index) + part2;
			else
				url = part2;
		}
		return url;
	}

}