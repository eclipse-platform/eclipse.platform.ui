package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Iterator;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributions.xml1_0.*;
import org.eclipse.help.internal.contributions1_0.*;
import org.eclipse.help.internal.contributors.xml1_0.*;
import org.eclipse.help.internal.util.Logger;
import org.xml.sax.*;
/**
 * Context contributor
 */
public class XMLContextContributor
	extends XMLContributor
	implements IContextContributor {
	static class ContextContributionFactory extends ContributionFactory {
		// Override the super class static field
		protected static final ContextContributionFactory instance =
			new ContextContributionFactory();
		/**
		 * ContributionFactory constructor comment.
		 */
		public ContextContributionFactory() {
			super();
		}
		public Contribution createContribution(String name, Attributes atts) {
			Contribution e = null;
			// NOTE: we don't create an element for the description
			if (name.equals(IContextContributor.CONTEXTS_ELEM))
				e = new HelpContribution(atts);
			else if (name.equals(IContextContributor.CONTEXT_ELEM))
				e = new ContextContribution(atts);
			else if (name.equals(IContextContributor.RELATED_ELEM))
				e = new HelpContextTopic(atts);
			else
				return null;
			return e;
		}
		public static ContributionFactory instance() {
			return instance;
		}
	}
	static class ContextContributionParser extends ContributionParser {
		StringBuffer buffer = new StringBuffer();
		boolean seenDescription = false;
		public ContextContributionParser() {
			super(ContextContributionFactory.instance());
		}
		public void characters(char ch[], int start, int length) throws SAXException {
			if (seenDescription)
				buffer.append(ch, start, length);
			if (Logger.DEBUG)
				Logger.logDebugMessage(
					"XMLContextContributor",
					"got char from parser= "
						+ new StringBuffer().append(ch, start, length).toString());
		}
		public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
			// make sure that no error has already occurred before adding to stack.
			if (!FATAL_ERROR_OCCURRED) {
				// We don't create a description element
				if (qName.equals(IContextContributor.DESC_ELEM)) {
					seenDescription = false;
					((ContextContribution) elementStack.peek()).setDescription(buffer.toString());
					buffer.setLength(0);
				} else if (qName.equals(IContextContributor.DESC_TXT_BOLD)) {
					// pop the starting bold tag
					elementStack.pop();
					if (!(elementStack.peek()).equals(IContextContributor.BOLD_TAG))
						buffer.append(IContextContributor.BOLD_CLOSE_TAG);
				} else
					super.endElement(namespaceURI, localName, qName);
			}
		}
		public void startElement(
			String namespaceURI,
			String localName,
			String qName,
			Attributes atts)
			throws SAXException {
			// make sure that no error has already occurred before adding to stack.
			if (!FATAL_ERROR_OCCURRED) {
				// We don't create a description element
				if (qName.equals(IContextContributor.DESC_ELEM))
					seenDescription = true;
				else if (qName.equals(IContextContributor.DESC_TXT_BOLD)) {
					// peek into stack to findout if a bold tag element already
					// exists. If we find one, then we do not add the bold tag to
					// the current StringBuffer of description.
					// ie: there are many bold start tags in the stack, but we appended
					// the tag only once to the description string.
					// eg: (b) some text (b) more test (/b) more text (/b) will result 
					// in all of the sentence being bold.
					if (!(elementStack.peek()).equals(IContextContributor.BOLD_TAG))
						buffer.append(IContextContributor.BOLD_TAG);
					elementStack.push(IContextContributor.BOLD_TAG);
				} else
					super.startElement(namespaceURI, localName, qName, atts);
			}
		}
	}
	/**
	 * XMLViewContributor constructor comment.
	 * @param plugin com.ibm.itp.core.api.plugins.IPluginDescriptor
	 * @param configuration com.ibm.itp.core.api.plugins.IConfigurationElement
	 */
	public XMLContextContributor(
		IPluginDescriptor plugin,
		IConfigurationElement configuration) {
		super(plugin, configuration);
	}
	/**
	 * getViews method comment.
	 */
	public Contribution getContribution() {
		return getContribution(NAME_ATTR);
	}
	/**
	 * Returns the contribution factory.
	 * Use the default contribution factory, but certain contributors can
	 * provide specialized subclasses.
	 */
	protected ContributionParser getContributionParser() {
		if (contributionParser == null)
			contributionParser = new ContextContributionParser();
		return contributionParser;
	}
	/**
	 * @return String
	 */
	public String getType() {
		return IContextContributor.CONTEXTS_ELEM;
	}
	/**
	 */
	protected void preprocess(Contribution contrib) {
		// update the ids and href only for child Topics (ie: related topics).
		// we know that we a Context contributrion to start with.
		// this is stored as a HelpContribution)
		for (Iterator children = contrib.getChildren(); children.hasNext();) {
			// update the id and href only for Topic node.
			// may need to revist! we may need to update the ids of Context also.
			Object child = children.next();
			if (child instanceof Topic) {
				updateIDs((Contribution) child);
				updateHrefs((Topic) child);
			} else if (child instanceof ContextContribution) {
				((ContextContribution) child).setContributor(this);
				preprocess((Contribution) child);
			}
		}
	}
	/**
	 * Utility method that scans the topics for all href attributes and update them
	 * to include the plugin id (i.e. create a help url).
	 */
	protected void updateHrefs(Topic topic) {
		// set the href on the input contribution   
		String href = topic.getHref();
		if (href == null)
			 ((HelpTopic) topic).setHref("");
		else {
			if (!href.equals("") // no empty link
				&& !href.startsWith("/") // no help url
				&& href.indexOf(':') == -1) // no other protocols
				{
				((HelpTopic) topic).setHref("/" + plugin.getUniqueIdentifier() + "/" + href);
			}
		}
		// recurse to children
		for (Iterator topics = topic.getChildren(); topics.hasNext();) {
			updateHrefs((Topic) topics.next());
		}
	}
}