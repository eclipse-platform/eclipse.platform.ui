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
package org.eclipse.team.internal.ccvs.ui;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.team.internal.ui.preferences.ComboFieldEditor;
import org.eclipse.ui.*;

public class WorkInProgressPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    
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
    
	public WorkInProgressPreferencePage() {
		super(GRID);
		setTitle(Policy.bind("WorkInProgressPreferencePage.0")); //$NON-NLS-1$
		setDescription(Policy.bind("WorkInProgressPreferencePage.1")); //$NON-NLS-1$
		setPreferenceStore(CVSUIPlugin.getPlugin().getPreferenceStore());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	public void createFieldEditors() {	
	    
	    addField(new RadioGroupFieldEditor(
		        ICVSUIConstants.PREF_CHANGE_PERSPECTIVE_ON_SHOW_ANNOTATIONS, 
		        Policy.bind("WorkInProgressPreferencePage.7"), //$NON-NLS-1$
		        3,
				new String[][] {
		            {Policy.bind("WorkInProgressPreferencePage.8"), MessageDialogWithToggle.ALWAYS},  //$NON-NLS-1$
		            {Policy.bind("WorkInProgressPreferencePage.2"), MessageDialogWithToggle.NEVER},  //$NON-NLS-1$
		            {Policy.bind("WorkInProgressPreferencePage.3"), MessageDialogWithToggle.PROMPT} //$NON-NLS-1$
		        },
		        getFieldEditorParent(), true));
		
		

		
		final Group perspectiveGroup = createGroup(
		        getFieldEditorParent(), 
		        Policy.bind("WorkInProgressPreferencePage.4"));  //$NON-NLS-1$

		handleDeletedPerspectives();
		final String[][] perspectiveNamesAndIds = getPerspectiveNamesAndIds();
		
		ComboFieldEditor comboEditor= new ComboFieldEditor(
			ICVSUIConstants.PREF_DEFAULT_PERSPECTIVE_FOR_SHOW_ANNOTATIONS,
			Policy.bind("WorkInProgressPreferencePage.5"), //$NON-NLS-1$
			perspectiveNamesAndIds,
			perspectiveGroup);
		addField(comboEditor);

		addField(new RadioGroupFieldEditor(
		        ICVSUIConstants.PREF_ALLOW_EMPTY_COMMIT_COMMENTS,
		        "&Allow empty commit comments:", //$NON-NLS-1$
		        3,
				new String[][] {
		            { "Yes", MessageDialogWithToggle.ALWAYS},  //$NON-NLS-1$
		            { "No", MessageDialogWithToggle.NEVER},  //$NON-NLS-1$
		            { "Prompt", MessageDialogWithToggle.PROMPT} //$NON-NLS-1$
		        },
		        getFieldEditorParent(), true));

		updateLayout(perspectiveGroup);
		getFieldEditorParent().layout(true);	
		
		Dialog.applyDialogFont(getFieldEditorParent());
	    
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
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

	private static void handleDeletedPerspectives() {
		final IPreferenceStore store= CVSUIPlugin.getPlugin().getPreferenceStore();
		final String id= store.getString(ICVSUIConstants.PREF_DEFAULT_PERSPECTIVE_FOR_SHOW_ANNOTATIONS);
		if (PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(id) == null) {
			store.putValue(ICVSUIConstants.PREF_DEFAULT_PERSPECTIVE_FOR_SHOW_ANNOTATIONS, ICVSUIConstants.OPTION_NO_PERSPECTIVE);
		}
	}	
	
	private String[][] getPerspectiveNamesAndIds() {
	    
	    final IPerspectiveRegistry registry= PlatformUI.getWorkbench().getPerspectiveRegistry();
	    final IPerspectiveDescriptor[] perspectiveDescriptors= registry.getPerspectives();
	    
	    Arrays.sort(perspectiveDescriptors, new PerspectiveDescriptorComparator());
	    
	    final String[][] table = new String[perspectiveDescriptors.length + 1][2];
	    table[0][0] = Policy.bind("WorkInProgressPreferencePage.6"); //$NON-NLS-1$
	    table[0][1] = ICVSUIConstants.OPTION_NO_PERSPECTIVE;
	    for (int i = 0; i < perspectiveDescriptors.length; i++) {
	        table[i + 1][0] = perspectiveDescriptors[i].getLabel();
	        table[i + 1][1] = perspectiveDescriptors[i].getId();
	    }
	    return table;
	}
}