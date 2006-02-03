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

package org.eclipse.jface.menus;

import org.eclipse.core.commands.INamedHandleStateIds;
import org.eclipse.jface.commands.PersistentState;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * <p>
 * A piece of state carrying a single {@link String}.
 * </p>
 * <p>
 * If this state is registered using {@link INamedHandleStateIds#NAME} or
 * {@link INamedHandleStateIds#DESCRIPTION}, then this allows the handler to
 * communicate a textual change for a given command. This is typically used by
 * graphical applications to allow more specific text to be displayed in the
 * menus. For example, "Undo" might become "Undo Typing" through the use of a
 * {@link TextState}.
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
 * @see INamedHandleStateIds
 */
public class TextState extends PersistentState {

	public final void load(final IPreferenceStore store,
			final String preferenceKey) {
		final String value = store.getString(preferenceKey);
		setValue(value);
	}

	public final void save(final IPreferenceStore store,
			final String preferenceKey) {
		final Object value = getValue();
		if (value instanceof String) {
			store.setValue(preferenceKey, ((String) value));
		}
	}

	public void setValue(final Object value) {
		if (!(value instanceof String)) {
			throw new IllegalArgumentException(
					"TextState takes a String as a value"); //$NON-NLS-1$
		}

		super.setValue(value);
	}

}
