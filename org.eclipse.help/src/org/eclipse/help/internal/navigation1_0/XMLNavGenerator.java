package org.eclipse.help.internal.navigation1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.contributions1_0.*;
import org.eclipse.help.internal.contributions.xml1_0.HelpContribution;

/**
 * This generates the XML file for the help navigation.
 */
public class XMLNavGenerator extends XMLGenerator implements Visitor {
	protected Vector viewNames = new Vector();
	protected InfoSet infoSet = null;

	/**
	 * @param viewSet com.ibm.itp.ua.view.ViewSet
	 * @param outputDir java.io.File
	 */
	public XMLNavGenerator(InfoSet infoSet, File outputFile) {
		super(outputFile);
		this.infoSet = infoSet;
	}
	/**
	 */
	public void generate() {
		// The XMLNavGenerator is a visitor that needs to start from the InfoSet
		// and will descend to children, etc....
		infoSet.accept(this);
		super.close();
	}
	/**
	 */
	public void visit(InfoSet infoSet) {
		//println("<?xml-stylesheet href=\"tree.xsl\" type=\"text/xsl\"?>");

		println("");

		String href = infoSet.getHref();
		String id = infoSet.getID();
		String plugin = id.substring(0, id.lastIndexOf("."));
		boolean standalone = infoSet.isStandalone();

		StringBuffer options = new StringBuffer();
		options.append("id=\"");
		options.append(infoSet.getID());
		options.append("\" label=\"");
		String infosetLabel = ((HelpContribution) infoSet).getLabel();
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

		print("<infoset ");
		print(options);
		println(">");

		println("");

		visitChildren(infoSet);

		println("</infoset>");
		println("");
	}
	/**
	 */
	public void visit(InfoView view) {

		StringBuffer options = new StringBuffer();
		options.append("id=\"");
		options.append(view.getID());
		options.append("\" label=\"");
		String viewLabel = ((HelpContribution) view).getLabel();
		viewLabel = xmlEscape(viewLabel);
		options.append(viewLabel);
		options.append("\" ");

		println("<!-- " + viewLabel + " infoview [" + view.getID() + "] -->");
		print("<infoview ");
		print(options);
		println(">");

		// Generate the content of this view
		visitChildren(view);

		println("</infoview>"); // close the view division 
		println("<!-- End of " + viewLabel + " infoview -->");
		println("");

		// Use a new character for the identifiers in next view
	}
	/**
	 */
	public void visit(Topic topic) {
		String href = reduceURL(topic.getHref());
		String id = topic.getID();
		String plugin = id.substring(0, id.lastIndexOf("."));

		pad++;
		StringBuffer anchor = new StringBuffer();
		String topicLabel = xmlEscape(((HelpContribution) topic).getLabel());
		anchor
			.append("<topic href=\"")
			.append(href)
			.append("\" id=\"")
			.append(id)
			.append("\" label=\"")
			.append(topicLabel)
			.append("\" >");
		printPad();
		println(anchor);
		if (topic.getChildren().hasNext())
			// Generate nested topics
			visitChildren(topic);

		printPad();
		println("</topic>");

		pad--;
	}
	public void visitChildren(Contribution con) {
		for (Iterator e = con.getChildren(); e.hasNext();) {
			Contribution c = (Contribution) e.next();
			c.accept(this);
		}
	}
	/**
	 * Simplifies url path by removing "string/.." from the path
	 * @return reduced url String
	 * @param url String
	 */
	protected static String reduceURL(String url) {
		if (url == null)
			return url;
		while (true) {
			int index = url.indexOf("/..", 1);
			if (index <= 0)
				break; //there is no "/.." or nothing before "/.." to simplify
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