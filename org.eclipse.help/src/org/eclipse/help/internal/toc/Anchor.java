/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.toc;
import org.eclipse.help.internal.util.Resources;
import org.xml.sax.*;
/**
 * Anchor.  Place holder that Toc objects can atatch to.
 */
class Anchor extends TocNode {
	protected String id;
	protected TocFile tocFile;
	/**
	 * Constructor. 
	 */
	protected Anchor(TocFile tocFile, Attributes attrs) {
		this.tocFile = tocFile;
		if (attrs == null)
			return;
		id = attrs.getValue("id");
		id =
			HrefUtil.normalizeHref(
				tocFile.getPluginID(),
				tocFile.getHref() + "#" + id);
	}
	/**
	 * Implements abstract method.
	 */
	public void build(TocBuilder builder) {
		builder.buildAnchor(this);
	}
	/**
	 * Obtains the ID
	 */
	protected String getID() {
		return id;
	}
	/**
	 * Returns the toc file
	 */
	public TocFile getTocFile() {
		return tocFile;
	}
}