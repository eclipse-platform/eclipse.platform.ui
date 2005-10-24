/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.commands.common;

/**
 * <p>
 * An event fired from a <code>NamedHandleObject</code>. This provides
 * notification of changes to the defined state, the name and the description.
 * </p>
 * 
 * @since 3.1
 */
public abstract class AbstractNamedHandleEvent extends
		AbstractHandleObjectEvent {

	/**
	 * The bit used to represent whether the category has changed its
	 * description.
	 */
	protected static final int CHANGED_DESCRIPTION = 1 << LAST_BIT_USED_ABSTRACT_HANDLE;

	/**
	 * The bit used to represent whether the category has changed its name.
	 */
	protected static final int CHANGED_NAME = 1 << LAST_BIT_USED_ABSTRACT_HANDLE;

	/**
	 * The last used bit so that subclasses can add more properties.
	 */
	protected static final int LAST_USED_BIT = CHANGED_NAME;

	/**
	 * Constructs a new instance of <code>AbstractHandleObjectEvent</code>.
	 * 
	 * @param definedChanged
	 *            <code>true</code>, iff the defined property changed.
	 * @param descriptionChanged
	 *            <code>true</code>, iff the description property changed.
	 * @param nameChanged
	 *            <code>true</code>, iff the name property changed.
	 */
	protected AbstractNamedHandleEvent(final boolean definedChanged,
			final boolean descriptionChanged, final boolean nameChanged) {
		super(definedChanged);

		if (descriptionChanged) {
			changedValues |= CHANGED_DESCRIPTION;
		}
		if (nameChanged) {
			changedValues |= CHANGED_NAME;
		}
	}

	/**
	 * Returns whether or not the description property changed.
	 * 
	 * @return <code>true</code>, iff the description property changed.
	 */
	public final boolean isDescriptionChanged() {
		return ((changedValues & CHANGED_DESCRIPTION) != 0);
	}

	/**
	 * Returns whether or not the name property changed.
	 * 
	 * @return <code>true</code>, iff the name property changed.
	 */
	public final boolean isNameChanged() {
		return ((changedValues & CHANGED_NAME) != 0);
	}

}
