/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.preferences;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Preference page to manage launch history & favorites
 */
public class LaunchHistoryPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private IntegerFieldEditor fHistoryMaxEditor;
	/**
	 * History tabs.
	 */
	protected LaunchHistoryPreferenceTab[] fTabs;
		
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fHistoryMaxEditor = new IntegerFieldEditor(IDebugUIConstants.PREF_MAX_HISTORY_SIZE, DebugPreferencesMessages.getString("LaunchHistoryPreferencePage.Maximum_launch_history_size_1"), composite); //$NON-NLS-1$
		int historyMax = IDebugPreferenceConstants.MAX_LAUNCH_HISTORY_SIZE;
		fHistoryMaxEditor.setPreferenceStore(DebugUIPlugin.getDefault().getPreferenceStore());
		fHistoryMaxEditor.setPreferencePage(this);
		fHistoryMaxEditor.setTextLimit(Integer.toString(historyMax).length());
		fHistoryMaxEditor.setErrorMessage(MessageFormat.format(DebugPreferencesMessages.getString("LaunchHistoryPreferencePage.The_size_of_the_launch_history_should_be_between_{0}_and_{1}_1"), new Object[] { new Integer(1), new Integer(historyMax)})); //$NON-NLS-1$
		fHistoryMaxEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		fHistoryMaxEditor.setValidRange(1, historyMax);
		fHistoryMaxEditor.load();
		fHistoryMaxEditor.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) 
					setValid(fHistoryMaxEditor.isValid());
			}
		});
		fHistoryMaxEditor.fillIntoGrid(composite, 2);

		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan= 2;
		tabFolder.setLayoutData(gd);
		
		// create tabs (debug and run first) 
		LaunchConfigurationManager manager = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
		LaunchGroupExtension[] groups = manager.getLaunchGroups();
		List tabList = new ArrayList();
		LaunchHistory history = manager.getLaunchHistory(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
		tabList.add(createTab(history, tabFolder));
		history = manager.getLaunchHistory(IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
		tabList.add(createTab(history, tabFolder));
		
		// create other tabs
		for (int i = 0; i < groups.length; i++) {
			LaunchGroupExtension extension = groups[i];
			String id = extension.getIdentifier();
			if (!(id.equals(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP) || id.equals(IDebugUIConstants.ID_RUN_LAUNCH_GROUP))) {
				history = manager.getLaunchHistory(id);
				if (history != null) {
					tabList.add(createTab(history, tabFolder));
				}
			}
		}				
		fTabs = (LaunchHistoryPreferenceTab[])tabList.toArray(new LaunchHistoryPreferenceTab[tabList.size()]);
		return composite;
	}
	
	protected LaunchHistoryPreferenceTab createTab(LaunchHistory history, TabFolder tabFolder) {
		TabItem tab = new TabItem(tabFolder, SWT.NONE);
		tab.setText(history.getLaunchGroup().getLabel());
		ImageDescriptor descriptor = history.getLaunchGroup().getImageDescriptor();
		Image image = null;
		if (descriptor != null) {
			image = descriptor.createImage();
			tab.setImage(image);
		}
		LaunchHistoryPreferenceTab prefTab = new LaunchHistoryPreferenceTab(history, this);
		prefTab.setImage(image);
		tab.setControl(prefTab.createControl(tabFolder));	
		return prefTab;	
	}
	
	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		setDescription(DebugPreferencesMessages.getString("LaunchHistoryPreferencePage.description")); //$NON-NLS-1$
	}

	/**
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		
		for (int i = 0; i < fTabs.length; i++) {
			LaunchHistoryPreferenceTab tab = fTabs[i];
			tab.performOK();
		}
		
		if (fHistoryMaxEditor.getIntValue() != fHistoryMaxEditor.getPreferenceStore().getInt(IDebugUIConstants.PREF_MAX_HISTORY_SIZE)) {
			fHistoryMaxEditor.store();
			LaunchHistory.launchHistoryChanged();
		}		
		
		DebugUIPlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	/**
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		for (int i = 0; i < fTabs.length; i++) {
			LaunchHistoryPreferenceTab tab = fTabs[i];
			tab.performDefaults();
		}
		fHistoryMaxEditor.loadDefault();
		super.performDefaults();
	}
	
	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), IDebugHelpContextIds.LAUNCH_HISTORY_PREFERENCE_PAGE);
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		super.dispose();
		for (int i = 0; i < fTabs.length; i++) {
			LaunchHistoryPreferenceTab tab = fTabs[i];
			tab.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#setButtonLayoutData(org.eclipse.swt.widgets.Button)
	 */
	protected GridData setButtonLayoutData(Button button) {
		//exists here for package visibility
		return super.setButtonLayoutData(button);
	}
}
