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
 * @since 3.2
 */
public abstract class AbstractHandleObjectEvent extends AbstractBitSetEvent {

	/**
	 * The bit used to represent whether the category has changed its defined
	 * state.
	 */
	protected static final int CHANGED_DEFINED = 1;

	/**
	 * The last used bit so that subclasses can add more properties.
	 */
	protected static final int LAST_BIT_USED_ABSTRACT_HANDLE = CHANGED_DEFINED;

	/**
	 * Constructs a new instance of <code>AbstractHandleObjectEvent</code>.
	 * 
	 * @param definedChanged
	 *            <code>true</code>, iff the defined property changed.
	 */
	protected AbstractHandleObjectEvent(final boolean definedChanged) {
		if (definedChanged) {
			changedValues |= CHANGED_DEFINED;
		}
	}

	/**
	 * Returns whether or not the defined property changed.
	 * 
	 * @return <code>true</code>, iff the defined property changed.
	 */
	public final boolean isDefinedChanged() {
		return ((changedValues & CHANGED_DEFINED) != 0);
	}
}
