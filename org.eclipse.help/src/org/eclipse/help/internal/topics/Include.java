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
class Include extends NavigationElement {
	protected String href;
	/**
	 * Contstructor.  Used when parsing help contributions.
	 */
	protected Include(TopicsFile topicsFile, Attributes attrs)
	{
		if (attrs == null)
			return;
		href = attrs.getValue("href");

		//include element must specify href attribute.
		href = HrefUtil.normalizeHref(topicsFile.getPluginID(), href);

	}

	/**
	 * Implements abstract method.
	 */
	public void build(NavigationBuilder builder) {
		builder.buildInclude(this);
	}

	/**
	 * Obtains href
	 */
	protected String getHref() {
		return href;
	}
}