package org.eclipse.help.internal.contributions.xml;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.xml.sax.*;
import org.eclipse.help.internal.contributors.TopicContributor;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.util.*;

/**
 * Default implementation for a topic contribution
 */
public class HelpTopicRef extends HelpTopic implements TopicRef {
	protected Topic topic;
	protected String label;
	protected String translatedLabel;
	public HelpTopicRef(Topic topic) {
		super(null);
		this.topic = topic;
		id = topic.getID();
	}
	public String getHref() {
		return topic.getHref();
	}
	public String getID() {
		return id;
	}
	public String getLabel() {
		if (translatedLabel == null) {
			if(label==null)
				return topic.getLabel();
			translatedLabel = label;
			if (translatedLabel.indexOf('%') == 0) {
				int lastPeriod = id.lastIndexOf('.');
				String pluginID = id.substring(0, lastPeriod);
				translatedLabel =
					DocResources.getPluginString(pluginID, translatedLabel.substring(1));
			}
		}
		return translatedLabel;
	}
	public void setRawLabel(String label) {
		this.label=label;
	}
	public String getRawLabel() {
		if(label!=null)
			return label;
		return ((HelpTopic) topic).getRawLabel();
	}
	public Topic getTopic() {
		return topic;
	}
}
