package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.Iterator;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.util.*;
import org.xml.sax.InputSource;
/**
 * Context contributor
 */
public class ContextContributor {
	public static final String NAME_ATTR = "name";
	public static final String PLUGIN_ATTR = "plugin";
	public static final String CONTEXT_ELEM = "context";
	public static final String CONTEXTS_ELEM = "contexts";
	public static final String DESC_ELEM = "description";
	public static final String RELATED_ELEM = "topic";
	public static final String RELATED_HREF = "href";
	public static final String RELATED_LABEL = "label";
	public static final String BOLD_CLOSE_TAG =
		"</" + Resources.getString("bold_tag_name") + ">";
	public static final String BOLD_TAG =
		"<" + Resources.getString("bold_tag_name") + ">";
	public static final String DESC_TXT_BOLD = Resources.getString("bold_tag_name");
	protected IPluginDescriptor plugin = null;
	protected IConfigurationElement configuration = null;
	protected IContextContributionNode contribution = null;
	// always call the getContributionParser() to use this....
	protected ContextContributionParser contributionParser = null;
	/**
	 * XMLViewContributor constructor comment.
	 * @param plugin com.ibm.itp.core.api.plugins.IPluginDescriptor
	 * @param configuration com.ibm.itp.core.api.plugins.IConfigurationElement
	 */
	public ContextContributor(
		IPluginDescriptor plugin,
		IConfigurationElement configuration) {
		this.plugin = plugin;
		this.configuration = configuration;
	}
	public IContextContributionNode getContribution() {
		return getContribution(NAME_ATTR);
	}
	/**
	 * @return org.w3c.dom.Document
	 * @param contributionType java.lang.String
	 * @param idAttributeName java.lang.String
	 */
	protected IContextContributionNode getContribution(String contributionNameAttribute) {
		if (contribution == null) {
			contribution = load(contributionNameAttribute);
			if (contribution != null) {
				preprocess(contribution);
			}
		}
		return contribution;
	}
	/**
	 * Returns the contribution parser
	 */
	protected ContextContributionParser getContributionParser() {
		if (contributionParser == null)
			contributionParser = new ContextContributionParser();
		return contributionParser;
	}
	/**
	 * getPluginID method comment.
	 */
	public IPluginDescriptor getPlugin() {
		return plugin;
	}
	/**
	 * @return String
	 */
	public String getType() {
		return ContextContributor.CONTEXTS_ELEM;
	}
	protected void preprocess(IContextContributionNode contrib) {
		// update the plugin IDs and href only for child Toc (ie: related topics).
		// we know that we a Context contributrion to start with.
		// this is stored as a HelpContribution)
		for (Iterator children = contrib.getChildren(); children.hasNext();) {
			// update the id and href only for Topic node.
			// may need to revist! we may need to update the ids of Context also.
			Object child = children.next();
			if (child instanceof HelpContextTopic) {
				((HelpContextTopic) child).setPlugin(plugin.getUniqueIdentifier());
				updateHrefs((HelpContextTopic) child);
			} else if (child instanceof ContextContribution) {
				((ContextContribution) child).setContributor(this);
				preprocess((ContextContribution) child);
			}
		}
	}
	protected IContextContributionNode load(String nameAttribute) {
		IContextContributionNode contribution = null;
		String file =
			plugin.getUniqueIdentifier() + "/" + configuration.getAttribute(nameAttribute);
		try {
			InputStream stream =
				ResourceLocator.openFromPlugin(
					plugin.getUniqueIdentifier(),
					configuration.getAttribute(nameAttribute));
			if (stream == null)
				return null;
			InputSource source = new InputSource(stream);
			// set id info for parser exceptions.
			// use toString method to capture protocol...etc
			// source.setSystemId(xmlURL.toString());
			source.setSystemId(file);
			ContextContributionParser parser = getContributionParser();
			parser.parse(stream, file);
			stream.close();
			contribution = parser.getContribution();
		} catch (IOException ioe) {
			String msg = Resources.getString("E009", file);
			Logger.logError(msg, ioe);
			// now pass it to the RuntimeHelpStatus object explicitly because we
			// still need to display errors even if Logging is turned off.
			RuntimeHelpStatus.getInstance().addParseError(msg, file);
		}
		return contribution;
	}
	/**
	 * Utility method that scans the topics for all href attributes and update them
	 * to include the plugin id (i.e. create a help url).
	 */
	protected void updateHrefs(HelpContextTopic topic) {
		// set the href on the input contribution   
		String href = topic.getHref();
		if (href == null)
			 ((HelpContextTopic) topic).setHref("");
		else {
			if (!href.equals("") // no empty link
				&& !href.startsWith("/") // no help url
				&& href.indexOf(':') == -1) // no other protocols
				{
				((HelpContextTopic) topic).setHref(
					"/" + plugin.getUniqueIdentifier() + "/" + href);
			}
		}
	}
}