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

package org.eclipse.jface.text;


import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * This font field editor implements chaining between a source preference
 * store and a target preference store. Any time the source preference
 * store changes, the change is propagated to the target store. Propagation
 * means that the actual value stored in the source store is set as default
 * value in the target store. If the target store does not contain a value
 * other than the default value, the new default value is immediately
 * effective.
 *
 * @see FontFieldEditor
 * @since 2.0
 * @deprecated since 3.0 not longer in use, no longer supported
 */
public class PropagatingFontFieldEditor extends FontFieldEditor {

	/** The editor's parent widget */
	private Composite fParent;
	/** The representation of the default font choice */
	private String fDefaultFontLabel;

	/**
	 * Creates a new font field editor with the given parameters.
	 *
	 * @param name the editor's name
	 * @param labelText the text shown as editor description
	 * @param parent the editor's parent widget
	 * @param defaultFontLabel the label shown in the editor value field when the default value should be taken
	 */
	public PropagatingFontFieldEditor(String name, String labelText, Composite parent, String defaultFontLabel) {
		super(name, labelText, parent);
		fParent= parent;
		fDefaultFontLabel= defaultFontLabel == null ? "" : defaultFontLabel; //$NON-NLS-1$
	}

	/*
	 * @see FontFieldEditor#doLoad()
	 */
	protected void doLoad() {
		if (getPreferenceStore().isDefault(getPreferenceName()))
			loadDefault();
		super.doLoad();
		checkForDefault();
	}

	/*
	 * @see FontFieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() {
		super.doLoadDefault();
		checkForDefault();
	}

	/**
	 * Checks whether this editor presents the default value "inherited"
	 * from the workbench rather than its own font.
	 */
	private void checkForDefault() {
		if (presentsDefaultValue()) {
			Control c= getValueControl(fParent);
			if (c instanceof Label)
				((Label) c).setText(fDefaultFontLabel);
		}
	}

	/**
	 * Propagates the font set in the source store to the
	 * target store using the given keys.
	 *
	 * @param source the store from which to read the text font
	 * @param sourceKey the key under which the font can be found
	 * @param target the store to which to propagate the font
	 * @param targetKey the key under which to store the font
	 */
	private static void propagateFont(IPreferenceStore source, String sourceKey, IPreferenceStore target, String targetKey) {
		FontData fd= PreferenceConverter.getFontData(source, sourceKey);
		if (fd != null) {
			boolean isDefault= target.isDefault(targetKey);	// save old state!
			PreferenceConverter.setDefault(target, targetKey, fd);
			if (isDefault) {
				// restore old state
				target.setToDefault(targetKey);
			}
		}
	}

	/**
	 * Starts the propagation of the font preference stored in the source preference
	 * store under the source key to the target preference store using the target
	 * preference key.
	 *
	 * @param source the source preference store
	 * @param sourceKey the key to be used in the source preference store
	 * @param target the target preference store
	 * @param targetKey the key to be used in the target preference store
	 */
	public static void startPropagate(final IPreferenceStore source, final String sourceKey, final IPreferenceStore target, final String targetKey) {
		source.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (sourceKey.equals(event.getProperty()))
					propagateFont(source, sourceKey, target, targetKey);
			}
		});

		propagateFont(source, sourceKey, target, targetKey);
	}
}

