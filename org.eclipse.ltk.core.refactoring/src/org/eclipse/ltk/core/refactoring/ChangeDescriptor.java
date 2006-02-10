/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.Assert;

/**
 * Descriptor of a change object. These descriptor object may be used to
 * describe the effect of a {@link Change}. Subclassed may provide more
 * specific information about the represented change.
 * <p>
 * Note: this class is indented to be subclassed by clients to provide
 * specialized descriptors for particular changes.
 * </p>
 * 
 * @since 3.2
 */
public abstract class ChangeDescriptor {

	/** A human-readable description of the particular change instance */
	private final String fDescription;

	/** The globally unique id of the change */
	private final String fID;

	/**
	 * Creates a new change descriptor.
	 * 
	 * @param id
	 *            the unique id of the change
	 * @param description
	 *            a non-empty human-readable description of the particular
	 *            change instance
	 */
	protected ChangeDescriptor(final String id, final String description) {
		Assert.isTrue(id != null && !"".equals(id)); //$NON-NLS-1$
		Assert.isTrue(description != null && !"".equals(description)); //$NON-NLS-1$
		fID= id;
		fDescription= description;
	}

	/**
	 * Returns a human-readable description of this change.
	 * 
	 * @return a description of this change
	 */
	public final String getDescription() {
		return fDescription;
	}

	/**
	 * Returns the unique id of this change.
	 * 
	 * @return the unique id
	 */
	public final String getID() {
		return fID;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		final StringBuffer buffer= new StringBuffer(128);
		buffer.append(getClass().getName());
		buffer.append("[id="); //$NON-NLS-1$
		buffer.append(getID());
		buffer.append(",description="); //$NON-NLS-1$
		buffer.append(getDescription());
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}
}