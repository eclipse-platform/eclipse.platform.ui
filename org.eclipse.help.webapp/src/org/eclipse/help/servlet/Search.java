/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;
import java.io.*;

import javax.servlet.ServletContext;
import org.apache.xerces.parsers.DOMParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.search.SearchManager;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
/**
 * Helper class for search jsp initialization
 */
public class Search {

	private ServletContext context;
	private EclipseConnector connector;
	private ProgressMonitor pm;
	private boolean isIndexing = false;

	/**
	 * Constructor
	 */
	public Search(ServletContext context) {
		this.context = context;
		this.connector = new EclipseConnector(context);
		this.pm = new ProgressMonitor();
	}

	/**
	 * Generates the html for the search results based on input xml data
	 */
	public void generateResults(String query, Writer out) {
		try {
			if (query == null || query.trim().length() == 0)
				return;

			if (isIndexing()) {
				displayProgressMonitor(out);
				return;
			}

			//System.out.println("search:"+query);
			String urlString = "search:/?" + query;
			InputSource xmlSource = new InputSource(connector.openStream(urlString));
			DOMParser parser = new DOMParser();
			parser.parse(xmlSource);
			Element toc = parser.getDocument().getDocumentElement();
			genToc(toc, out);
		} catch (Exception e) {
		}
	}
	private void genToc(Element toc, Writer out) throws IOException {

		NodeList topics = toc.getChildNodes();
		if (topics.getLength() == 0)
		{
			out.write("Nothing found");
			return;
		}
		out.write("<ul class='expanded'>");
		for (int i = 0; i < topics.getLength(); i++) {
			Node n = topics.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE)
				genTopic((Element) n, out);
		}
		out.write("</ul>");
	}
	private void genTopic(Element topic, Writer out) throws IOException {
		out.write("<li class=");
		out.write(topic.hasChildNodes() ? "'node'>" : "'leaf'>");
		out.write("<a href=");
		String href = topic.getAttribute("href");
		if (href != null && href.length() > 0) {
			// external href
			if (href.charAt(0) == '/')
				href = "content/help:" + href;
		} else
			href = "javascript:void 0";
		out.write("'" + href + "'>");
		// do this for IE5.0 only. Mozilla and IE5.5 work fine with nowrap css
		out.write("<nobr>");
		out.write(topic.getAttribute("label") + "</nobr></a>");
		if (topic.hasChildNodes()) {
			out.write("<ul class='collapsed'>");
			NodeList topics = topic.getChildNodes();
			for (int i = 0; i < topics.getLength(); i++) {
				Node n = topics.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE)
					genTopic((Element) n, out);
			}
			out.write("</ul>");
		}
		out.write("</li>");
	}


	public ProgressMonitor getProgressMonitor()
	{
		return pm;
	}

	public synchronized boolean isIndexing() {
		// First check if we need to index...
		if (!isIndexing) {
			index();
		}
		return isIndexing;
	}
	
	private synchronized void index() {
		final SearchManager sm = HelpSystem.getSearchManager();
		if (sm.isIndexingNeeded("en_US")) {
			isIndexing = true;
			Thread indexer = new Thread(new Runnable() {
				public void run() {
					try {
						sm.updateIndex(pm, "en_US");
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						isIndexing = false;
					}
				}
			});
			indexer.start();
		}
	}

	private void displayProgressMonitor(Writer out) throws IOException {
		out.write("<script>window.open('progress.jsp', null, 'height=200,width=400,status=no,toolbar=no,menubar=no,location=no'); </script>");
	}

	/**
	 * Internal progress monitor implementation.
	 */
	public class ProgressMonitor implements IProgressMonitor {
		private boolean isCancelled;
		private int totalWork;
		private int currWork;

		public ProgressMonitor() {
		}
		public void beginTask(String name, int totalWork) {
			this.totalWork = totalWork;;
		}

		public void done() {
		}

		public void setTaskName(String name) {
		}

		public boolean isCanceled() {
			return isCancelled;
		}

		public void setCanceled(boolean b) {
			isCancelled = b;
		}

		public void subTask(String name) {
		}

		public void worked(int work) {
			currWork += work;
			if (currWork > totalWork)
				currWork = totalWork;
			else if (currWork < 0)
				currWork = 0;

			internalWorked(work);
		}

		public void internalWorked(double work) {
		}

		public int getPercentage() {
			if(currWork==totalWork || totalWork<=0)
				return 100;			
			return (int)(100*currWork/totalWork);
		}
	}
}