/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;
import java.io.*;
import java.text.NumberFormat;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
/**
 * Helper class for search jsp initialization
 */
public class Search {

	private ServletContext context;
	private EclipseConnector connector;

	/**
	 * Constructor
	 */
	public Search(ServletContext context) {
		this.context = context;
		this.connector = new EclipseConnector(context);
	}

	/**
	 * Generates the html for the search results based on input xml data
	 */
	public void generateResults(
		String searchQuery,
		Writer out,
		HttpServletRequest request) {
		try {
			if (searchQuery == null || searchQuery.trim().length() == 0)
				return;

			String urlString = "search:/";
			if (searchQuery != null && searchQuery.length() >= 0)
				urlString += "?" + searchQuery;

			//System.out.println("search:"+query);
			InputSource xmlSource =
				new InputSource(connector.openStream(urlString, request));
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Element elem = parser.getDocument().getDocumentElement();
			if (elem.getTagName().equals("toc"))
				genToc(elem, out, request);
			else
				displayProgressMonitor(out, elem.getAttribute("indexed"), request);
		} catch (Exception e) {
		}
	}

	private void genToc(Element toc, Writer out, HttpServletRequest request)
		throws IOException {
		NodeList topics = toc.getChildNodes();
		if (topics.getLength() == 0) {
			// TO DO: get the correct locale
			out.write(WebappResources.getString("Nothing_found", request));
			return;
		}

		out.write("<table id='list' cellspacing='0'>");
		for (int i = 0; i < topics.getLength(); i++) {
			Node n = topics.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE)
				genTopic((Element) n, out, request);
		}
		out.write("</table>");
	}

	private void genTopic(Element topic, Writer out, HttpServletRequest request)
		throws IOException {
		out.write("<tr class='list'>");

		// obtain document score
		String scoreString = topic.getAttribute("score");
		try {
			float score = Float.parseFloat(scoreString);
			NumberFormat percentFormat =
				NumberFormat.getPercentInstance(request.getLocale());
			scoreString = percentFormat.format(score);
		} catch (NumberFormatException nfe) {
			// will display original score string
		}
		out.write("<td align='right' class='score'>");
		out.write(scoreString);
		out.write("</td><td align='left' class='label' nowrap>");
		out.write("<a href=");
		String href = topic.getAttribute("href");
		if (href != null && href.length() > 0) {
			// external href
			if (href.charAt(0) == '/')
				href = "content/help:" + href;
		} else
			href = "javascript:void 0";
		out.write("'" + href + "'>");

		out.write(topic.getAttribute("label"));

		out.write("</a></td></tr>");
	}

	private void displayProgressMonitor(
		Writer out,
		String indexed,
		HttpServletRequest request)
		throws IOException {
		//out.write("<script>window.open('progress.jsp?indexed="+indexed+"', null, 'height=200,width=400,status=no,toolbar=no,menubar=no,location=no'); </script>");
		//out.flush();
		int percentage = 0;
		try {
			percentage = Integer.parseInt(indexed);
		} catch (Exception e) {
		}

		StringBuffer sb = new StringBuffer();
		sb
			.append("<CENTER>")
			.append("<TABLE BORDER='0'>")
			.append(
				"    <TR><TD>" + WebappResources.getString("Indexing", request) + "</TD></TR>")
			.append("    <TR>")
			.append("    	<TD ALIGN='LEFT'>")
			.append("			<DIV STYLE='width:100px;height:16px;border:1px solid black;'>")
			.append(
				"				<DIV ID='divProgress' STYLE='width:"
					+ percentage
					+ "px;height:100%;background-color:Highlight'>")
			.append("				</DIV>")
			.append("			</DIV>")
			.append("		</TD>")
			.append("	</TR>")
			.append("	<TR>")
			.append(
				"		<TD>"
					+ indexed
					+ "% "
					+ WebappResources.getString("complete", request)
					+ "</TD>")
			.append("	</TR>")
			.append("</TABLE>")
			.append("</CENTER>")
			.append("<script language='JavaScript'>")
			.append("setTimeout('refresh()', 2000);")
			.append("</script>");

		out.write(sb.toString());
		out.flush();
	}
}