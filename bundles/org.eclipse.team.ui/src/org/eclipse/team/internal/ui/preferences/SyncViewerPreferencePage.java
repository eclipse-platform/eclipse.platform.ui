/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.preferences;

import java.text.Collator;
import java.text.DateFormat;
import java.util.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.ui.*;

/**
 * This area provides the widgets for providing the CVS commit comment
 */
public class SyncViewerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IPreferenceIds {
	
	private BooleanFieldEditor compressFolders = null;
	private BooleanFieldEditor showSyncInLabels = null;
	private BooleanFieldEditor useDefaultPerspective = null;
	private RadioGroupFieldEditor synchronizePerspectiveSwitch = null;
	
	private static class PerspectiveDescriptorComparator implements Comparator {
		/*
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			if (o1 instanceof IPerspectiveDescriptor && o2 instanceof IPerspectiveDescriptor) {
				String id1= ((IPerspectiveDescriptor)o1).getLabel();
				String id2= ((IPerspectiveDescriptor)o2).getLabel();
				return Collator.getInstance().compare(id1, id2);
			}
			return 0;
		}
	}
	
	public SyncViewerPreferencePage() {
		super(GRID);
		setTitle(Policy.bind("SyncViewerPreferencePage.6")); //$NON-NLS-1$
		setDescription(Policy.bind("SyncViewerPreferencePage.7")); //$NON-NLS-1$
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
		compressFolders = new BooleanFieldEditor(SYNCVIEW_COMPRESS_FOLDERS, Policy.bind("SyncViewerPreferencePage.9"), SWT.NONE, getFieldEditorParent()); //$NON-NLS-1$
		addField(compressFolders);
		showSyncInLabels = new BooleanFieldEditor(SYNCVIEW_VIEW_SYNCINFO_IN_LABEL, Policy.bind("SyncViewerPreferencePage.19"), SWT.NONE, getFieldEditorParent()); //$NON-NLS-1$
		addField(showSyncInLabels);	

		synchronizePerspectiveSwitch= new RadioGroupFieldEditor(SYNCHRONIZING_COMPLETE_PERSPECTIVE, Policy.bind("SyncViewerPreferencePage.13"), 3,  //$NON-NLS-1$
				new String[][] {
								{Policy.bind("SyncViewerPreferencePage.14"), MessageDialogWithToggle.ALWAYS}, //$NON-NLS-1$
								{Policy.bind("SyncViewerPreferencePage.42"), MessageDialogWithToggle.NEVER}, //$NON-NLS-1$
								{Policy.bind("SyncViewerPreferencePage.16"), MessageDialogWithToggle.PROMPT} //$NON-NLS-1$
							},
							getFieldEditorParent(), true);
		addField(synchronizePerspectiveSwitch);
		
		Group perspectiveGroup = createGroup(getFieldEditorParent(), Policy.bind("SyncViewerPreferencePage.15")); //$NON-NLS-1$
		
		createLabel(perspectiveGroup, Policy.bind("SynchronizationViewPreference.defaultPerspectiveDescription"), 1); //$NON-NLS-1$
		
		handleDeletedPerspectives();
		String[][] perspectiveNamesAndIds = getPerspectiveNamesAndIds();
		ComboFieldEditor comboEditor= new ComboFieldEditor(
			SYNCVIEW_DEFAULT_PERSPECTIVE,
			Policy.bind("SynchronizationViewPreference.defaultPerspectiveLabel"), //$NON-NLS-1$
			perspectiveNamesAndIds,
			perspectiveGroup);
		addField(comboEditor);

		Dialog.applyDialogFont(getFieldEditorParent());
		updateLayout(perspectiveGroup);
		getFieldEditorParent().layout(true);	
	}
	
	private Label createLabel(Composite parent, String title, int spacer) {
		GridData data;
		Label l = new Label(parent, SWT.WRAP);
		l.setFont(parent.getFont());
		data = new GridData();
		data.horizontalSpan = 2;
		if(spacer != 0) {
			data.verticalSpan = spacer;
		}
		data.horizontalAlignment = GridData.FILL;		
		l.setLayoutData(data);
		if(title != null) {
			l.setText(title); //$NON-NLS-1$
		}
		return l;
	}

	private Group createGroup(Composite parent, String title) {
		Group display = new Group(parent, SWT.NONE);
		updateLayout(display);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		display.setLayoutData(data);						
		display.setText(title);
		return display;
	}
	
	private void updateLayout(Composite composite) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 5;
		layout.marginHeight =5;
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 5;
		composite.setLayout(layout);
	}

	private void updateLastRunTime(Label label) {
		String text;
		long mills = 0;
		if(mills == 0) {
			String never = Policy.bind("SyncViewPreferencePage.lastRefreshRunNever"); //$NON-NLS-1$
			text = Policy.bind("SyncViewPreferencePage.lastRefreshRun", never); //$NON-NLS-1$
		} else {
			Date lastTimeRun = new Date(mills);
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
		//if(event.getSource() == bkgScheduledRefresh || event.getSource() == scheduledDelay) {			
	//		updateEnablements();	
	//	}
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
		//boolean enabled = bkgScheduledRefresh.getBooleanValue();
		//scheduledDelay.setEnabled(enabled, refreshGroup);
		//scheduledDelay.refreshValidState();
	}
	
	/**
	 * Return a 2-dimensional array of perspective names and ids.
	 */
	private String[][] getPerspectiveNamesAndIds() {
	
		IPerspectiveRegistry registry= PlatformUI.getWorkbench().getPerspectiveRegistry();
		IPerspectiveDescriptor[] perspectiveDescriptors= registry.getPerspectives();
	
		Arrays.sort(perspectiveDescriptors, new PerspectiveDescriptorComparator());
	
		String[][] table = new String[perspectiveDescriptors.length + 1][2];
		table[0][0] = Policy.bind("SynchronizationViewPreference.defaultPerspectiveNone"); //$NON-NLS-1$;
		table[0][1] = SYNCVIEW_DEFAULT_PERSPECTIVE_NONE;
		for (int i = 0; i < perspectiveDescriptors.length; i++) {
			table[i + 1][0] = perspectiveDescriptors[i].getLabel();
			table[i + 1][1] = perspectiveDescriptors[i].getId();
		}
		return table;
	}

	private static void handleDeletedPerspectives() {
		IPreferenceStore store= TeamUIPlugin.getPlugin().getPreferenceStore();
		String id= store.getString(SYNCVIEW_DEFAULT_PERSPECTIVE);
		if (PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(id) == null) {
			store.putValue(SYNCVIEW_DEFAULT_PERSPECTIVE, SYNCVIEW_DEFAULT_PERSPECTIVE_NONE);
		}
	}	
}
