/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import org.eclipse.help.internal.util.Resources;
import org.xml.sax.*;
/**
 * Anchor.  Place holder that Topics objects can atatch to.
 */
class Anchor extends NavigationElement {
	protected String id;
	protected TopicsFile topicsFile;
	/**
	 * Constructor. 
	 */
	protected Anchor(TopicsFile topicsFile, Attributes attrs) {
		this.topicsFile = topicsFile;
		if (attrs == null)
			return;
		id = attrs.getValue("id");
		id =
			HrefUtil.normalizeHref(
				topicsFile.getPluginID(),
				topicsFile.getHref() + "#" + id);
	}
	/**
	 * Implements abstract method.
	 */
	public void build(NavigationBuilder builder) {
		builder.buildAnchor(this);
	}
	/**
	 * Obtains the ID
	 */
	protected String getID() {
		return id;
	}
	/**
	 * Returns the topics file
	 */
	public TopicsFile getTopicsFile() {
		return topicsFile;
	}
}