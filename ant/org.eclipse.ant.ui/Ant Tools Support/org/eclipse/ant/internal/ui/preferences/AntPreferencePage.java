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
package org.eclipse.ant.internal.ui.preferences;


import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.model.IAntUIHelpContextIds;
import org.eclipse.ant.internal.ui.model.IAntUIPreferenceConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

public class AntPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private StringFieldEditor fBuildFileNames;
	
	private BooleanFieldEditor toolsWarningEditor= null;

	/**
 	 * Create the Ant page.
     */
	public AntPreferencePage() {
		super(GRID);
		setDescription(AntPreferencesMessages.getString("AntPreferencePage.General")); //$NON-NLS-1$
		setPreferenceStore(AntUIPlugin.getDefault().getPreferenceStore());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {

		Font font= getFieldEditorParent().getFont();
		Label label= new Label(getFieldEditorParent(), SWT.NONE);
		label.setText(AntPreferencesMessages.getString("AntPreferencePage.Enter")); //$NON-NLS-1$
		GridData gd= new GridData();
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);
		label.setFont(font);
		
		fBuildFileNames = new StringFieldEditor(IAntUIPreferenceConstants.ANT_FIND_BUILD_FILE_NAMES, AntPreferencesMessages.getString("AntPreferencePage.&Names__3"), getFieldEditorParent()); //$NON-NLS-1$
		addField(fBuildFileNames);
		
		new Label(getFieldEditorParent(), SWT.NONE);
	
		if (!AntUIPlugin.isMacOS()) {
			//the mac does not have a tools.jar Bug 40778
			toolsWarningEditor= new BooleanFieldEditor(IAntUIPreferenceConstants.ANT_TOOLS_JAR_WARNING, AntPreferencesMessages.getString("AntPreferencePage.10"), getFieldEditorParent());  //$NON-NLS-1$
			addField(toolsWarningEditor);
		}
		
		addField(new BooleanFieldEditor(IAntUIPreferenceConstants.ANT_ERROR_DIALOG, AntPreferencesMessages.getString("AntPreferencePage.12"), getFieldEditorParent())); //$NON-NLS-1$
		new Label(getFieldEditorParent(), SWT.NONE);
				
		getPreferenceStore().addPropertyChangeListener(this);
	}
	
	/**
	 * @see FieldEditorPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(parent, IAntUIHelpContextIds.ANT_PREFERENCE_PAGE);
		return super.createContents(parent);
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		getPreferenceStore().removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IAntUIPreferenceConstants.ANT_TOOLS_JAR_WARNING)) {
			if (toolsWarningEditor != null) {
				toolsWarningEditor.load();
			}
		} else {
			super.propertyChange(event);
		}
	}
}