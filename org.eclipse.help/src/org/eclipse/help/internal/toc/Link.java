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
import org.eclipse.help.internal.model.*;
import org.xml.sax.*;
/**
 * Include. Place holder to link to other Toc objects.
 */
class Link extends TocNode implements ILinkElement {
	protected Toc parentToc;
	protected String toc;
	/**
	 * Contstructor. Used when parsing help contributions.
	 */
	protected Link(TocFile tocFile, Attributes attrs) {
		if (attrs == null)
			return;
		toc = attrs.getValue("toc"); //$NON-NLS-1$
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
	 * Adds another element as child of this element Modifies parents of a child
	 * as well
	 */
	public void addChild(ITocNode child) {
		super.addChild(child);
		if (child instanceof Toc && parentToc != null) {
			parentToc.getChildrenTocs().add(child);
		}
	}
}
