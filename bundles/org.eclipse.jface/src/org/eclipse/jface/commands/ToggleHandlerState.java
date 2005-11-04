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

package org.eclipse.jface.commands;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * <p>
 * A piece of handler state storing whether a command is in one of two states.
 * The value stored by this state is a <code>Boolean</code>. The
 * <code>commandId</code> is ignored, except on
 * {@link ToggleHandlerState#load(IPreferenceStore, String)} and
 * {@link ToggleHandlerState#save(IPreferenceStore, String)}.
 * </p>
 * <p>
 * Clients may instantiate this class, but must not extend.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class ToggleHandlerState extends AbstractPersistentHandlerState {

	/**
	 * Constructs a new <code>ToggleHandlerState</code>. By default, the
	 * toggle is off (e.g., <code>false</code>).
	 */
	public ToggleHandlerState() {
		setValue(Boolean.FALSE);
	}

	public final void load(final IPreferenceStore store,
			final String preferenceKey) {
		if (isPersisted()) {
			final boolean value = store.getBoolean(preferenceKey);
			setValue(value ? Boolean.TRUE : Boolean.FALSE);
		}
	}

	public final void save(final IPreferenceStore store,
			final String preferenceKey) {
		if (isPersisted()) {
			final Object value = getValue();
			if (value instanceof Boolean) {
				store.setValue(preferenceKey, ((Boolean) value).booleanValue());
			}
		}
	}

	public void setValue(final Object value) {
		if (!(value instanceof Boolean)) {
			throw new IllegalArgumentException(
					"ToggleHandlerState takes a Boolean as a value"); //$NON-NLS-1$
		}

		super.setValue(value);
	}
}
