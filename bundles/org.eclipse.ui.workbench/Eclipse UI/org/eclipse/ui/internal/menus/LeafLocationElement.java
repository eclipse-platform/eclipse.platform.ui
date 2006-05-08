/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.jface.util.Util;
import org.eclipse.ui.internal.misc.Policy;

/**
 * <p>
 * A leaf element within a location. This provides the most specific piece of
 * information as to where the menu element should appear. The <code>path</code>
 * specifies where within the popup menu the menu element should be placed.
 * </p>
 * <p>
 * Clients must not implement or extend.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class will eventually exist in <code>org.eclipse.jface.menus</code>.
 * </p>
 * 
 * @since 3.2
 * @see org.eclipse.ui.internal.menus.SBar
 * @see org.eclipse.ui.internal.menus.SPopup
 */
public abstract class LeafLocationElement implements LocationElement {

	/**
	 * 
	 */
	public static final String BREAKPOINT_PATH = "org.eclipse.search.menu"; //$NON-NLS-1$

	/**
	 * The path separator used to separate menu containers.
	 */
	public static final char PATH_SEPARATOR = '/';

	/**
	 * The path within this location element to the final location. The path is
	 * a slash-delimited list of menu elements.
	 */
	private final String path;

	/**
	 * Constructs a new instance of <code>LeafLocationElement</code>.
	 * 
	 * @param path
	 *            The path to the final location. If this value is
	 *            <code>null</code>, it means that it should be inserted at
	 *            the top-level of the location.
	 */
	public LeafLocationElement(final String path) {
		if (Policy.EXPERIMENTAL_MENU && path != null 
				&& path.indexOf(BREAKPOINT_PATH) > -1) {
			System.err.println("LeafLocationElement: " + path); //$NON-NLS-1$
		}
		this.path = path;
	}

	/**
	 * Returns the full path for this location. The path is a slash-delimited
	 * list of menu elements.
	 * 
	 * @return The full path. If this value is <code>null</code>, it means
	 *         that it should be inserted at the top-level of the location.
	 */
	public final String getPath() {
		return path;
	}

	/**
	 * Returns a tokenizer for this location element. A location tokenizer is
	 * capable of returning each element along the path as its own instance of
	 * <code>LeafLocationElement</code>. This can be used for creating
	 * implicit locations when parsing paths.
	 * 
	 * @return A tokenizer for this location element; never <code>null</code>.
	 */
	public abstract ILocationElementTokenizer getTokenizer();

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this==obj) {
			return true;
		}
		if (obj instanceof LeafLocationElement) {
			LeafLocationElement leaf = (LeafLocationElement) obj;
			return Util.equals(path, leaf.path);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return Util.hashCode(path);
	}
}
