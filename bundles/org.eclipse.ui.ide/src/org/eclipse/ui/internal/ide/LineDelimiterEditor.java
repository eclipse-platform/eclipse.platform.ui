/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * A class to handle editing of the line delimiter preferences in core.
 * 
 * @since 3.1
 */
public class LineDelimiterEditor {

	private Button defaultButton;

	private Button otherButton;

	private Combo choiceCombo;

	/**
	 * The project whose preferences should be set. In some cases this class
	 * will be used to edit project preferences. If project is null, then we
	 * are editing workspace preferences.
	 */
	private IProject project;

	private Group group;

	/**
	 * Creates a new line delimiter editor for workspace.
	 * 
	 * @param composite
	 *            the parent of the field editor's control
	 */
	public LineDelimiterEditor(Composite composite) {
		this(composite, null);
	}

	/**
	 * Creates a new line delimiter editor for the given project.
	 * 
	 * @param composite
	 *            the parent of the field editor's control
	 * @param project
	 *            the project to set preferences on
	 */
	public LineDelimiterEditor(Composite composite, IProject project) {
		this.project = project;
		createControl(composite);
	}

	/**
	 * Creates this field editor's main control containing all of its basic
	 * controls.
	 * 
	 * @param parent
	 *            the parent control
	 */
	private void createControl(Composite parent) {
		Font font = parent.getFont();
		group = new Group(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setText(IDEWorkbenchMessages.IDEWorkspacePreference_fileLineDelimiter);
		group.setFont(font);

		SelectionAdapter buttonListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget.equals(defaultButton)) {
					updateState(true);
				} else {
					updateState(false);
				}
			}
		};

		defaultButton = new Button(group, SWT.RADIO);
		String key = getKeyForValue(getDefaultValue());
		if (project == null) {
			defaultButton.setText(NLS.bind(IDEWorkbenchMessages.IDEWorkspacePreference_defaultLineDelim, key));
		} else {
			defaultButton.setText(NLS.bind(IDEWorkbenchMessages.IDEWorkspacePreference_defaultLineDelimProj, key));
		}

		data = new GridData();
		data.horizontalSpan = 2;
		defaultButton.setLayoutData(data);
		defaultButton.addSelectionListener(buttonListener);
		defaultButton.setFont(font);

		otherButton = new Button(group, SWT.RADIO);
		otherButton.setText(IDEWorkbenchMessages.IDEWorkspacePreference_otherLineDelim);
		otherButton.addSelectionListener(buttonListener);
		otherButton.setFont(font);

		choiceCombo = new Combo(group, SWT.NONE | SWT.READ_ONLY);
		data = new GridData();
		choiceCombo.setFont(font);
		choiceCombo.setLayoutData(data);
	}

	/**
	 * Load the list items from core and update the state of the buttons to
	 * match what the preference is currently set to.
	 */
	public void doLoad() {
		if (choiceCombo != null) {
			choiceCombo.setItems(getChoices());
			String resourcePreference = getStoredValue(getPreferences(project));
			selectChoice(getKeyForValue(resourcePreference));
			updateState(resourcePreference == null);
		}
	}

	/**
	 * Initializes this field editor with the preference default value.
	 */
	public void loadDefault() {
		if (choiceCombo != null) {
			updateState(true);
		}
	}

	private String getDefaultValue() {
		String value = null;
		if (project != null) {
			value = getStoredValue(getPreferences(null));
		}
		return value != null ? value : System.getProperty(Platform.PREF_LINE_SEPARATOR);
	}

	private String getKeyForValue(String value) {
		Map knownValues = Platform.knownPlatformLineSeparators();
		Set keys = knownValues.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			if (knownValues.get(key).equals(value)) {
				return key;
			}
		}
		return null;
	}

	/**
	 * Returns the value that is currently stored for the line delimiter.
	 * 
	 * @param node
	 *            preferences node from which the value should be read
	 * @return the currently stored line delimiter
	 */
	private String getStoredValue(Preferences node) {
		try {
			// be careful looking up for our node so not to create any nodes as side effect
			if (node.nodeExists(Platform.PI_RUNTIME))
				return node.node(Platform.PI_RUNTIME).get(Platform.PREF_LINE_SEPARATOR, null);
		} catch (BackingStoreException e) {
			// ignore
		}
		return null;
	}

	/**
	 * Answer the <code>Preferences</code> for the receiver, this will be a
	 * project preferences if the receiver is editing project preferences,
	 * otherwise instance preferences.
	 * 
	 * @param project
	 *            the project for which the line editor will be modified
	 * @return the preferences
	 */
	private Preferences getPreferences(IProject project) {
		if (project != null) {
			return Platform.getPreferencesService().getRootNode().node(ProjectScope.SCOPE).node(project.getName());
		}

		return Platform.getPreferencesService().getRootNode().node(InstanceScope.SCOPE);
	}

	/**
	 * Returns the default setting for the object being shown.
	 * 
	 * @return the default setting for the object being shown
	 */
	private String[] getChoices() {
		Set keys = Platform.knownPlatformLineSeparators().keySet();
		String[] keyArray = new String[keys.size()];
		keys.toArray(keyArray);
		return keyArray;
	}

	private void updateState(boolean useDefault) {
		if (useDefault) {
			defaultButton.setSelection(true);
			otherButton.setSelection(false);
			choiceCombo.setEnabled(false);
			selectChoice(getKeyForValue(getDefaultValue()));
		} else {
			defaultButton.setSelection(false);
			otherButton.setSelection(true);
			choiceCombo.setEnabled(true);
		}
	}

	/**
	 * Select the item in the combo that matches the current preferences
	 * setting. NOTE: not handling the case where two platform line separators
	 * are defined with the same value. Assumption is that they are unique and
	 * the key will be modified to represent that. E.g. a key might be Mac OS
	 * 10/Linux if the same value is required for two platforms.
	 */
	private void selectChoice(String selection) {
		String[] items = choiceCombo.getItems();
		for (int i = 0; i < items.length; i++) {
			String item = items[i];
			if (item.equals(selection)) {
				choiceCombo.select(i);
				break;
			}
		}
	}

	/**
	 * Store the currently selected line delimiter value in the preference
	 * store.
	 */
	public void store() {
		String val;
		if (defaultButton.getSelection() || choiceCombo.getText().equals("")) { //$NON-NLS-1$
			val = null;
		} else {
			Map lineSeparators = Platform.knownPlatformLineSeparators();
			val = (String) lineSeparators.get(choiceCombo.getText());
		}

		Preferences node = getPreferences(project).node(Platform.PI_RUNTIME);
		if (val == null) {
			node.remove(Platform.PREF_LINE_SEPARATOR);
		} else {
			node.put(Platform.PREF_LINE_SEPARATOR, val);
		}
		try {
			node.flush();
		} catch (BackingStoreException e) {
			IDEWorkbenchPlugin.log(e.getMessage(), e);
		}
	}

	/**
	 * Set whether or not the controls in the field editor are enabled.
	 * 
	 * @param enabled
	 *            The enabled state.
	 */
	public void setEnabled(boolean enabled) {
		group.setEnabled(enabled);
		Control[] children = group.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].setEnabled(enabled);
		}
	}
}
