/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.revisions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.text.source.ILineRange;

import org.eclipse.jface.internal.text.revisions.ChangeRegion;


/**
 * Describes a revision of a document. A revision consists of one ore more {@link ILineRange}s.
 * <p>
 * Clients may subclass.
 * </p>
 * 
 * @since 3.2
 */
public abstract class Revision {
	private final List fChangeRegions= new ArrayList();
	private final List fROChangeRegions= Collections.unmodifiableList(fChangeRegions);
	
	/**
	 * Creates a new revision.
	 */
	protected Revision() {
	}
	
	/**
	 * Adds a line range to this revision. The range must be non-empty and have a legal start line
	 * (not -1).
	 * 
	 * @param range a line range that was changed with this revision
	 */
	public final void addRange(ILineRange range) {
		fChangeRegions.add(new ChangeRegion(this, range));
	}

	/**
	 * Returns the contained change regions.
	 * 
	 * @return an unmodifiable view of the contained change regions (element type: {@link Object})
	 */
	public final List getRegions() {
		return fROChangeRegions;
	}

	/**
	 * Returns the hover information that will be shown when the user hovers over the a change
	 * region of this revision.
	 * 
	 * @return the hover information for this revision or <code>null</code> for no hover
	 */
	public abstract Object getHoverInfo();

	/**
	 * Returns the color definition to be used for this revision. The color may be used to visually
	 * distinguish one revision from another, for example as background color. The colors of any two
	 * revisions should be as distinct as possible.
	 * 
	 * @return the RGB color description for this revision
	 */
	public abstract RGB getColor();

	/**
	 * Returns the unique (within the document) id of this revision. This may be the version string
	 * or a different identifier.
	 * 
	 * @return the id of this revision
	 */
	public abstract String getId();
	
	/**
	 * Returns the modification date of this revision.
	 * 
	 * @return the modification date of this revision
	 */
	public abstract Date getDate();
	
	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Revision " + getId(); //$NON-NLS-1$
	}
}
