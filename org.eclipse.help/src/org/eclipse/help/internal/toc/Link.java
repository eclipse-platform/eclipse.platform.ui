/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.toc;
import org.xml.sax.Attributes;
/**
 *  Include.  Place holder to link to other Toc objects.
 */
class Link extends TocNode {
	protected Toc parentToc;
	protected String toc;
	/**
	 * Contstructor.  Used when parsing help contributions.
	 */
	protected Link(TocFile tocFile, Attributes attrs) {
		if (attrs == null)
			return;
		toc = attrs.getValue("toc");
		toc = HrefUtil.normalizeHref(tocFile.getPluginID(), toc);
		parentToc = tocFile.getToc();
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
	/**
	 * Adds another element as child of this element
	 * Modifies parents of a child as well
	 */
	public void addChild(ITocNode child) {
		super.addChild(child);
		if (child instanceof Toc && parentToc != null) {
			parentToc.getChildrenTocs().add(child);
		}
	}
}