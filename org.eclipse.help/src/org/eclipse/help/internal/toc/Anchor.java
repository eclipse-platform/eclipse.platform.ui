/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;
import java.util.*;

import org.eclipse.help.internal.model.*;
import org.xml.sax.*;
/**
 * Anchor. Place holder that Toc objects can atatch to.
 */
class Anchor extends TocNode implements IAnchorElement {
	protected Toc parentToc;
	protected String id;
	protected TocFile tocFile;
	/**
	 * Constructor.
	 */
	protected Anchor(TocFile tocFile, Attributes attrs) {
		this.tocFile = tocFile;
		if (attrs == null)
			return;
		id = attrs.getValue("id"); //$NON-NLS-1$
		id = HrefUtil.normalizeHref(tocFile.getPluginID(), tocFile.getHref()
				+ "#" + id); //$NON-NLS-1$
		parentToc = tocFile.getToc();
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
	/**
	 * Adds another element as child of this element Modifies parents of a child
	 * as well
	 */
	public void addChild(ITocNode child) {
		super.addChild(child);
		if (child instanceof Toc && parentToc != null) {
			parentToc.getChildrenTocs().add(child);
		}
	}

	/**
	 * @return ITopic list
	 */
	public List getChildTopics() {
		// after build, release TocFile
		tocFile = null;
		return super.getChildTopics();
	}

}
