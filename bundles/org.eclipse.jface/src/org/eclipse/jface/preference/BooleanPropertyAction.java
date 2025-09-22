/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.preference;

import org.eclipse.jface.action.Action;

/**
 * The BooleanPropertyAction is an action that set the values of a
 * boolean property in the preference store.
 */

public class BooleanPropertyAction extends Action {

	private final IPreferenceStore preferenceStore;

	private final String property;

	/**
	 * Create a new instance of the receiver.
	 * @param title The displayable name of the action.
	 * @param preferenceStore The preference store to propogate changes to
	 * @param property The property that is being updated
	 * @throws IllegalArgumentException Thrown if preferenceStore or
	 * property are <code>null</code>.
	 */
	public BooleanPropertyAction(String title,
			IPreferenceStore preferenceStore, String property)
			throws IllegalArgumentException {
		super(title, AS_CHECK_BOX);

		if (preferenceStore == null || property == null) {
			throw new IllegalArgumentException();
		}

		this.preferenceStore = preferenceStore;
		this.property = property;
		final String finalProprety = property;

		preferenceStore
				.addPropertyChangeListener(event -> {
					if (finalProprety.equals(event.getProperty())) {
						setChecked(Boolean.TRUE.equals(event.getNewValue()));
					}
				});

		setChecked(preferenceStore.getBoolean(property));
	}

	@Override
	public void run() {
		preferenceStore.setValue(property, isChecked());
	}
}
