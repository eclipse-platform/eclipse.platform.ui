package org.eclipse.help.internal.context;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.util.*;
import org.xml.sax.InputSource;
/**
 * Context contributor
 */
public class ContextContributor {
	public static final String FILE_ATTR = "file";
	public static final String FILE_ATTR_V1 = "name";
	public static final String PLUGIN_ATTR = "plugin";
	public static final String CONTEXTS_ELEM = "contexts";
	public static final String CONTEXT_ELEM = "context";
	public static final String DESC_ELEM = "description";
	public static final String RELATED_ELEM = "topic";
	public static final String RELATED_HREF = "href";
	public static final String RELATED_LABEL = "label";
	public static final String BOLD_CLOSE_TAG =
		"</" + Resources.getString("bold_tag_name") + ">";
	public static final String BOLD_TAG =
		"<" + Resources.getString("bold_tag_name") + ">";
	public static final String DESC_TXT_BOLD = Resources.getString("bold_tag_name");
	protected IPluginDescriptor plugin;
	protected IConfigurationElement configuration;
	protected IContextContributionNode contribution;
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
	/**
	 * @return IContextContributionNode
	 */
	public IContextContributionNode getContribution() {
		if (contribution == null) {
			contribution = load();
			if (contribution != null) {
				preprocess(contribution);
			}
		}
		return contribution;
	}
	/**
	 * getPluginID method comment.
	 */
	public IPluginDescriptor getPlugin() {
		return plugin;
	}
	protected void preprocess(IContextContributionNode contrib) {
		// update the plugin IDs and href only for related topics.
		// we know that we have a Context contributrion to start with.
		for (Iterator children = contrib.getChildren().iterator();
			children.hasNext();
			) {
			// update the id and href only for Topic node.
			// may need to revist! we may need to update the ids of Context also.
			Object child = children.next();
			if (child instanceof RelatedTopic) {
				((RelatedTopic) child).setPlugin(plugin.getUniqueIdentifier());
				updateHrefs((RelatedTopic) child);
			} else if (child instanceof ContextContribution) {
				((ContextContribution) child).setContributor(this);
				preprocess((ContextContribution) child);
			}
		}
	}
	protected IContextContributionNode load() {
		IContextContributionNode contribution = null;
		String fileName = configuration.getAttribute(FILE_ATTR);
		if (fileName == null)
			fileName = configuration.getAttribute(FILE_ATTR_V1);
		String file = plugin.getUniqueIdentifier() + "/" + fileName;
		try {
			InputStream stream =
				ResourceLocator.openFromPlugin(plugin.getUniqueIdentifier(), fileName);
			if (stream == null)
				return null;
			InputSource source = new InputSource(stream);
			// set id info for parser exceptions.
			// use toString method to capture protocol...etc
			// source.setSystemId(xmlURL.toString());
			source.setSystemId(file);
			ContextContributionParser parser = new ContextContributionParser();
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
	protected void updateHrefs(RelatedTopic topic) {
		// set the href on the input contribution   
		String href = topic.getHref();
		if (href == null)
			 ((RelatedTopic) topic).setHref("");
		else {
			if (!href.equals("") // no empty link
				&& !href.startsWith("/") // no help url
				&& href.indexOf(':') == -1) // no other protocols
				{
				((RelatedTopic) topic).setHref(
					"/" + plugin.getUniqueIdentifier() + "/" + href);
			}
		}
	}
	/**
	 * Filters out the duplicate topics from an array
	 */
	private IHelpResource[] removeDuplicates(IHelpResource links[]) {
		if (links == null || links.length <= 0)
			return links;
		ArrayList filtered = new ArrayList();
		for (int i = 0; i < links.length; i++) {
			IHelpResource topic1 = links[i];
			if (!isValidTopic(topic1))
				continue;
			boolean dup = false;
			for (int j = 0; j < filtered.size(); j++) {
				IHelpResource topic2 = (IHelpResource) filtered.get(j);
				if (!isValidTopic(topic2))
					continue;
				if (equal(topic1, topic2)) {
					dup = true;
					break;
				}
			}
			if (!dup)
				filtered.add(links[i]);
		}
		return (IHelpResource[]) filtered.toArray(new IHelpResource[filtered.size()]);
	}
	/**
	 * Checks if topic labels and href are not null and not empty strings
	 */
	private boolean isValidTopic(IHelpResource topic) {
		return topic != null
			&& topic.getHref() != null
			&& !"".equals(topic.getHref())
			&& topic.getLabel() != null
			&& !"".equals(topic.getLabel());
	}
	/**
	 * Check if two context topic are the same.
	 * They are considered the same if both labels and href are equal
	 */
	private boolean equal(IHelpResource topic1, IHelpResource topic2) {
		return topic1.getHref().equals(topic2.getHref())
			&& topic1.getLabel().equals(topic2.getLabel());
	}
}