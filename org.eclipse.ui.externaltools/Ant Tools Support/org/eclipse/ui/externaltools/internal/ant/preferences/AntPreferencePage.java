package org.eclipse.ui.externaltools.internal.ant.preferences;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IPreferenceConstants;

public class AntPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	
	private StringFieldEditor fBuildFileNames;
	
	/**
 	 * Create the Ant page.
     */
		public AntPreferencePage() {
			super(GRID);
			setDescription("General Ant Settings:");
			setPreferenceStore(ExternalToolsPlugin.getDefault().getPreferenceStore());
		}
	/**
	* Create all field editors for this page
	*/
	public void createFieldEditors() {

		Label label= new Label(getFieldEditorParent(), SWT.NONE);
		label.setText("Enter the names of the build files to find if using Run As -> Ant Build\nand the currently selected resource is not an Ant build file.\nList is comma separated (e.g export.xml, build.xml, clean.xml).");
		GridData gd= new GridData();
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);
		label.setFont(getFieldEditorParent().getFont());
		
		fBuildFileNames = new StringFieldEditor(IPreferenceConstants.ANT_FIND_BUILD_FILE_NAMES, "&Names:", getFieldEditorParent());
		addField(fBuildFileNames);
		fBuildFileNames.getTextControl(getFieldEditorParent()).addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {

			}
		});
	}
	
	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		//WorkbenchHelp.setHelp(parent, IAntHelpContextIds.ANT_PREFERENCE_PAGE); to do
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}