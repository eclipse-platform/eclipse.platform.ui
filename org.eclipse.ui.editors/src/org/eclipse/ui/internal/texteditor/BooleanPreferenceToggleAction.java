/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import java.util.ResourceBundle;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.ResourceAction;

/**
 *
 * @since 3.3
 */
public class BooleanPreferenceToggleAction extends ResourceAction implements IUpdate {

	private final String fKey;
	private final IPreferenceStore fPreferences;

	public BooleanPreferenceToggleAction(ResourceBundle bundle, String prefix, int style, IPreferenceStore preferences, String key) {
		super(bundle, prefix, style);
		Assert.isLegal(preferences != null);
		Assert.isLegal(key != null);
		fPreferences= preferences;
		fKey= key;

		update();
	}

	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setChecked(fPreferences.getBoolean(fKey));
	}

	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		boolean state= fPreferences.getBoolean(fKey);
		fPreferences.setValue(fKey, !state);
	}
}
