package org.eclipse.help.internal.contributions.xml;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.xml.sax.*;
import org.eclipse.help.internal.util.ContextResources;

/**
 * Default implementation for a topic contribution
 */
public class HelpContextTopic extends HelpTopic {

	public HelpContextTopic(Attributes attrs) {
		super(attrs);
	}
	/**
	 * Returns the translated label
	 */
	public String getLabel() {
		if (translatedLabel == null) {
			translatedLabel = label;
			if (translatedLabel.indexOf('%') == 0) {
				int lastPeriod = id.lastIndexOf('.');
				String pluginID = id.substring(0, lastPeriod);
				translatedLabel =
					ContextResources.getPluginString(pluginID, translatedLabel.substring(1));
			}
		}
		return translatedLabel;
	}
}
