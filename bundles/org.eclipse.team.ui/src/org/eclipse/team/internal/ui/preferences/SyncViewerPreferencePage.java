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
package org.eclipse.team.internal.ui.preferences;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This area provides the widgets for providing the CVS commit comment
 */
public class SyncViewerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private BooleanFieldEditor bkgRefresh = null;
	private BooleanFieldEditor bkgScheduledRefresh = null;
	private IntegerFieldEditor2 scheduledDelay = null;
	private BooleanFieldEditor compressFolders = null;
	
	class IntegerFieldEditor2 extends IntegerFieldEditor {
			public IntegerFieldEditor2(String name, String labelText, Composite parent, int size) {
				super(name, labelText, parent, size);
			}

			protected boolean checkState() {
				Text control= getTextControl();
				if (!control.isEnabled()) {
					clearErrorMessage();
					return true;
				}
				return super.checkState();
			}
		
			/**
			 * Overrode here to be package visible.
			 */
			protected void refreshValidState() {
				super.refreshValidState();
			}
		
			/**
			 * Only store if the text control is enabled
			 * @see FieldEditor#doStore()
			 */
			protected void doStore() {
				Text text = getTextControl();
				if (text.isEnabled()) {
					super.doStore();
				}
			}
			/**
			 * Clears the error message from the message line if the error
			 * message is the error message from this field editor.
			 */
			protected void clearErrorMessage() {
				if (getPreferencePage() != null) {
					String message= getPreferencePage().getErrorMessage();
					if (message != null) {
						if(getErrorMessage().equals(message)) {
							super.clearErrorMessage();
						}
					
					} else {
						super.clearErrorMessage();
					}
				}
			}
		}
	
	public SyncViewerPreferencePage() {
		super(GRID);
		setTitle("Synchronize view preferences");
		setDescription("Preferences for the Synchronize view");
		setPreferenceStore(TeamUIPlugin.getPlugin().getPreferenceStore());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		//WorkbenchHelp.setHelp(getControl(), IDebugHelpContextIds.CONSOLE_PREFERENCE_PAGE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	public void createFieldEditors() {
		
		bkgRefresh = new BooleanFieldEditor(IPreferenceIds.SYNCVIEW_BACKGROUND_SYNC, "Refresh with the remote resources in the background", SWT.NONE, getFieldEditorParent());
		addField(bkgRefresh);
		
		bkgScheduledRefresh = new BooleanFieldEditor(IPreferenceIds.SYNCVIEW_SCHEDULED_SYNC, "Enable a background task to refresh with remote resources", SWT.NONE, getFieldEditorParent());
		addField(bkgScheduledRefresh);
		
		scheduledDelay = new IntegerFieldEditor2(IPreferenceIds.SYNCVIEW_DELAY, "How often should the background refresh run? (in minutes)", getFieldEditorParent(), 2);
		addField(scheduledDelay);
		
		updateLastRunTime(new Label(getFieldEditorParent(), SWT.NONE));
		
		compressFolders = new BooleanFieldEditor(IPreferenceIds.SYNCVIEW_COMPRESS_FOLDERS, "Compress in-sync folder paths when using the tree view", SWT.NONE, getFieldEditorParent());
		addField(compressFolders);
	}
	
	private void updateLastRunTime(Label label) {
		String text;
		long mills = TeamUIPlugin.getPlugin().getRefreshJob().getLastTimeRun();
		if(mills == 0) {
			String never = Policy.bind("SyncViewPreferencePage.lastRefreshRunNever"); //$NON-NLS-1$
			text = Policy.bind("SyncViewPreferencePage.lastRefreshRun", never); //$NON-NLS-1$
		} else {
			Date lastTimeRun = new Date(TeamUIPlugin.getPlugin().getRefreshJob().getLastTimeRun());
			String sLastTimeRun = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(lastTimeRun);
			text = Policy.bind("SyncViewPreferencePage.lastRefreshRun", sLastTimeRun); //$NON-NLS-1$
		}
		label.setText(text);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if(event.getSource() == bkgScheduledRefresh) {			
			updateEnablements();	
		}
		super.propertyChange(event);
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		TeamUIPlugin.getPlugin().savePluginPreferences();
		return super.performOk();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#checkState()
	 */
	protected void initialize() {
		super.initialize();		
		updateEnablements();
	}

	protected void updateEnablements() {
		boolean enabled = bkgScheduledRefresh.getBooleanValue();
		scheduledDelay.setEnabled(enabled, getFieldEditorParent());
		scheduledDelay.refreshValidState();
	}	
}