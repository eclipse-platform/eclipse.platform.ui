package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import org.xml.sax.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.contributions.xml.*;
import org.eclipse.help.internal.contributors.xml.*;
import org.eclipse.help.internal.util.Logger;

/**
 * Creates a search model for the search results
 * that were presented as XML
 */
public class XMLSearchContributor implements Contributor {
	private Contribution resultsNode = null;
	private String results;
	static class SearchContributionFactory extends ContributionFactory {
		// Override the super class static field
		protected static final SearchContributionFactory instance =
			new SearchContributionFactory();
		/**
		 * ContributionFactory constructor comment.
		 */
		public SearchContributionFactory() {
			super();
		}

		public Contribution createContribution(String name, Attributes atts) {
			Contribution e = null;
			if (name.equals(TopicContributor.TOPICS_ELEM))
				e = new HelpContribution(atts);
			else
				if (name.equals(TopicContributor.TOPIC_ELEM))
					e = new HelpTopic(atts);
				else
					return null;

			return e;
		}

		public static ContributionFactory instance() {
			return instance;
		}
	}

	/**
	 * XMLSearchContributor constructor comment.
	 */
	public XMLSearchContributor(String xmlResultsAsString) {
		super();
		this.results = xmlResultsAsString;
	}
	/**
	 */
	public Contribution getContribution() {
		if (resultsNode == null) {
			try {
				InputSource input = new InputSource(new StringReader(results));
				ContributionParser parser =
					new ContributionParser(SearchContributionFactory.instance());
				parser.parse(input);
				resultsNode = parser.getContribution();
			} catch (SAXException se) {
				Logger.logError("", se);
			} catch (IOException ioe) {
				Logger.logError("", ioe);
			}
		}
		return resultsNode;
	}
	/**
	 */
	public org.eclipse.core.runtime.IPluginDescriptor getPlugin() {
		return null;
	}
	/**
	 */
	public String getType() {
		return null;
	}
}
