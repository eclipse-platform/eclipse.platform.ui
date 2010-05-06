/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * A class to handle editing of the Line delimiter preferences in core.
 * 
 * @since 3.1
 */
public class ResourceDragAndDropEditor {

	class PreferenceGroup
	{
		Group group;
		String preferenceKey;
		String title;
		String[] labels;
		String[] values;
		Button[] buttons;
		
		public PreferenceGroup(String title, String pref, String[] labels, String[] values) {
			this.preferenceKey = pref;
			this.title = title;
			this.labels = labels;
			this.values = values;
		}
		
		public void createControl(Composite parent) {
			Font font = parent.getFont();
			group = new Group(parent, SWT.NONE);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			group.setLayoutData(data);
			GridLayout layout = new GridLayout();
			layout.numColumns = labels.length;
			group.setLayout(layout);
			group.setText(title);
			group.setFont(font);

			buttons = new Button[labels.length];
			for (int i = 0; i < labels.length; i++) {
				buttons[i] = new Button(group, SWT.RADIO);
				buttons[i].setText(labels[i]);
				buttons[i].setData(values[i]);
				buttons[i].setFont(font);
			}
		}

		/**
		 * 
		 */
		public void doLoad() {
			String resourcePreference = getStoredValue(false);
			updateState(resourcePreference);
		}
		/**
		 * @param value
		 */
		private void updateState(String value) {
			for (int i = 0; i < labels.length; i++) {
				if (value.equals(buttons[i].getData()))
					buttons[i].setSelection(true);
			}
		}

		/**
		 * 
		 */
		public void loadDefault() {
			String value = getStoredValue(true);
			updateState(value);
		}
		/**
		 * Returns the value that is currently stored for the encoding.
		 * @param useDefault 
		 * 
		 * @return the currently stored encoding
		 */
		public String getStoredValue(boolean useDefault) {
			IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
			if (useDefault)
				return store.getDefaultString(preferenceKey);
			return store.getString(preferenceKey);
		}

		 /**
		 * @return the current selection
		 */
		private String getSelection() {
			for (int i = 0; i < labels.length; i++) {
				if (buttons[i].getSelection())
					return (String) buttons[i].getData();
			}
			return values[0];
		}

		/**
		 * 
		 */
		public void store() {
			IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
			store.putValue(preferenceKey, getSelection());
		}

		/**
		 * @param enableLinking
		 */
		public void setEnabled(boolean enableLinking) {
			group.setEnabled(enableLinking);
			for (int i = 0; i < labels.length; i++) {
				if (buttons[i] != null && !buttons[i].isDisposed())
					buttons[i].setEnabled(enableLinking);
			}
			
		}
	}
	
	PreferenceGroup folderPref;
	PreferenceGroup virtualFolderPref;
	/**
	 * Creates a new drag and drop resource editor with the given preference name, label
	 * and parent.
	 * 
	 * @param composite
	 *            the parent of the field editor's control
	 */
	public ResourceDragAndDropEditor(Composite composite) {
		folderPref = new PreferenceGroup(IDEWorkbenchMessages.LinkedResourcesPreference_dragAndDropHandlingMessage,
				IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE,
				new String[] {
				IDEWorkbenchMessages.Prompt,
				IDEWorkbenchMessages.linkedResourcesPreference_copy,
				IDEWorkbenchMessages.LinkedResourcesPreference_link, 
				IDEWorkbenchMessages.LinkedResourcesPreference_linkAndVirtualFolder },
				new String[] {IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_PROMPT,
				IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_MOVE_COPY,
				IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_LINK,
				IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_LINK_AND_VIRTUAL_FOLDER});
		virtualFolderPref = new PreferenceGroup(IDEWorkbenchMessages.LinkedResourcesPreference_dragAndDropVirtualFolderHandlingMessage,
				IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_VIRTUAL_FOLDER_MODE,
				new String[] {
				IDEWorkbenchMessages.LinkedResourcesPreference_promptVirtual,
				IDEWorkbenchMessages.LinkedResourcesPreference_linkVirtual, 
				IDEWorkbenchMessages.LinkedResourcesPreference_linkAndVirtualFolderVirtual },
				new String[] {IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_PROMPT,
				IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_LINK,
				IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_LINK_AND_VIRTUAL_FOLDER});
		createControl(composite);
	}

	/**
	 * Creates this field editor's main control containing all of its basic
	 * controls.
	 * 
	 * @param parent
	 *            the parent control
	 */
	protected void createControl(Composite parent) {
		folderPref.createControl(parent);
		virtualFolderPref.createControl(parent);
	}

	/**
	 * Load the list items from core and update the state of the buttons to
	 * match what the preference is currently set to.
	 */
	public void doLoad() {
		folderPref.doLoad();
		virtualFolderPref.doLoad();
	}

	/**
	 * Initializes this field editor with the preference value from the
	 * preference store.
	 */
	public void loadDefault() {
		folderPref.loadDefault();
		virtualFolderPref.loadDefault();
	}

	/**
	 * Store the currently selected line delimiter value in the preference
	 * store.
	 */
	public void store() {
		folderPref.store();
		virtualFolderPref.store();
	}

	/**
     * Sets the enabled state of the group's widgets.
     * Does nothing if called prior to calling <code>createContents</code>.

	 * @param enableLinking
	 */
	public void setEnabled(boolean enableLinking) {
		folderPref.setEnabled(enableLinking);
		virtualFolderPref.setEnabled(enableLinking);
	}
}
