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
import java.util.Date;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.text.source.ILineRange;


/**
 * Describes a revision of a document. A revision consists of one ore more {@link ILineRange}s.
 * <p>
 * Clients may subclass.
 * </p>
 * <p>
 * XXX This API is provisional and may change any time during the development of eclipse 3.2.
 * </p>
 * 
 * @since 3.2
 */
public abstract class Revision {
	final List fChangeRegions= new ArrayList();
	
	/**
	 * Creates a new revision.
	 */
	protected Revision() {
	}
	
	/**
	 * Adds a line range to this revision.
	 * 
	 * @param range the line range that was changed with this revision
	 */
	public void addRange(ILineRange range) {
		fChangeRegions.add(new ChangeRegion(this, range));
	}

	/**
	 * Returns the hover information that will be shown when the user hovers over the a change
	 * region of this revision.
	 * 
	 * @return the hover information for this revision or <code>null</code> for no hover
	 */
	public abstract Object getHoverInfo();

	/**
	 * Returns the color definition to be used for this revision.
	 * 
	 * @return the RGB color description for this revision
	 */
	public abstract RGB getColor();

	/**
	 * Returns the unique (within the document id of this revision.
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
