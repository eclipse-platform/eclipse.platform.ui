/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.toc;
import org.eclipse.help.internal.util.Resources;
import org.xml.sax.*;
/**
 *  Include.  Place holder to link to other Toc objects.
 */
class Link extends TocNode {
	protected String toc;
	/**
	 * Contstructor.  Used when parsing help contributions.
	 */
	protected Link(TocFile tocFile, Attributes attrs) {
		if (attrs == null)
			return;
		toc = attrs.getValue("toc");
		toc = HrefUtil.normalizeHref(tocFile.getPluginID(), toc);
	}
	/**
	 * Implements abstract method.
	 */
	public void build(TocBuilder builder) {
		builder.buildLink(this);
	}
	/**
	 * Obtains URL of linked TOC
	 */
	protected String getToc() {
		return toc;
	}
}