package org.eclipse.help.internal.contributors.xml;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.contributions.xml.HelpTopic;

/**
 * Topic contributor
 */
public class XMLTopicContributor
	extends XMLContributor
	implements TopicContributor {
	/**
	 * @param plugin com.ibm.itp.core.api.plugins.IPluginDescriptor
	 * @param configuration com.ibm.itp.core.api.plugins.IConfigurationElement
	 */
	public XMLTopicContributor(
		IPluginDescriptor plugin,
		IConfigurationElement configuration) {
		super(plugin, configuration);

	}
	/**
	 * @return org.w3c.dom.Document
	 */
	public Contribution getContribution() {
		return getContribution(TOPICS_NAME_ATTR);
	}
	/**
	 * @return java.lang.String
	 */
	public String getType() {
		return TopicContributor.TOPICS_ELEM;
	}
	/**
	 * @param doc org.w3c.dom.Document
	 */
	protected void preprocess(Contribution contrib) {
		updateIDs(contrib);
		for (Iterator topics = contrib.getChildren(); topics.hasNext();)
			updateHrefs((Contribution) topics.next());
	}
	/**
	 * Utility method that scans the topics for all href attributes and update them
	 * to include the plugin id (i.e. create a help url).
	 */
	protected void updateHrefs(Contribution topic) {
		if (topic instanceof Topic)
		{
			HelpTopic helpTopic = (HelpTopic)topic;
			// set the href on the input contribution   
			String href = helpTopic.getHref();
			if (href == null)
				 helpTopic.setHref("");
			else {
				if (!href.equals("") // no empty link
					&& !href.startsWith("/") // no help url
					&& href.indexOf(':') == -1) // no other protocols
				{
					helpTopic.setHref("/" + plugin.getUniqueIdentifier() + "/" + href);
				}
			}
		}

		// recurse to children
		for (Iterator topics = topic.getChildren(); topics.hasNext();) {
			updateHrefs((Contribution) topics.next());
		}
	}
}
