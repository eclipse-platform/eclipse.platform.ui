/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import org.eclipse.help.internal.util.Resources;
import org.xml.sax.*;
/**
 *  Include.  Place holder to link to other Topics objects.
 */
class Link extends NavigationElement {
	protected String topics;
	/**
	 * Contstructor.  Used when parsing help contributions.
	 */
	protected Link(TopicsFile topicsFile, Attributes attrs) {
		if (attrs == null)
			return;
		topics = attrs.getValue("topics");
		topics = HrefUtil.normalizeHref(topicsFile.getPluginID(), topics);
	}
	/**
	 * Implements abstract method.
	 */
	public void build(NavigationBuilder builder) {
		builder.buildLink(this);
	}
	/**
	 * Obtains href
	 */
	protected String getTopics() {
		return topics;
	}
}