package org.eclipse.ui.externaltools.internal.ant.preferences;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.IPreferenceConstants;
import org.eclipse.ui.help.WorkbenchHelp;

public class AntPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private StringFieldEditor fBuildFileNames;
	
	private List fAppearanceColorList;
	private ColorEditor fAppearanceColorEditor;
	
	// Array containing the message to display, the preference key, and the 
	// default value (initialized in storeInitialValues()) for each color preference
	private final String[][] fAppearanceColorListModel= new String[][] {
		{AntPreferencesMessages.getString("AntPreferencePage.&Error__2"), IPreferenceConstants.CONSOLE_ERROR_RGB, null}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.&Warning__3"), IPreferenceConstants.CONSOLE_WARNING_RGB, null}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.I&nformation__4"), IPreferenceConstants.CONSOLE_INFO_RGB, null}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.Ve&rbose__5"), IPreferenceConstants.CONSOLE_VERBOSE_RGB, null}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.Deb&ug__6"), IPreferenceConstants.CONSOLE_DEBUG_RGB, null}, //$NON-NLS-1$
	};

	/**
 	 * Create the Ant page.
     */
		public AntPreferencePage() {
			super(GRID);
			setDescription(AntPreferencesMessages.getString("AntPreferencePage.General")); //$NON-NLS-1$
			setPreferenceStore(ExternalToolsPlugin.getDefault().getPreferenceStore());
		}
	
	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		storeAppliedValues();

		Label label= new Label(getFieldEditorParent(), SWT.NONE);
		label.setText(AntPreferencesMessages.getString("AntPreferencePage.Enter")); //$NON-NLS-1$
		GridData gd= new GridData();
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);
		label.setFont(getFieldEditorParent().getFont());
		
		fBuildFileNames = new StringFieldEditor(IPreferenceConstants.ANT_FIND_BUILD_FILE_NAMES, AntPreferencesMessages.getString("AntPreferencePage.&Names__3"), getFieldEditorParent()); //$NON-NLS-1$
		addField(fBuildFileNames);
		fBuildFileNames.getTextControl(getFieldEditorParent()).addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {

			}
		});
		
		new Label(getFieldEditorParent(), SWT.NONE);
		createColorComposite();
	}
	
	/**
	 * Stores the initial values of the color preferences. The preference values are updated 
	 * on the fly as the user edits them (instead of only when they press "Apply"). We need
	 * to store the old values so that we can reset them when the user chooses "Cancel".
	 */
	private void storeAppliedValues() {
		IPreferenceStore store= getPreferenceStore();
		for (int i = 0; i < fAppearanceColorListModel.length; i++) {
			String preference = fAppearanceColorListModel[i][1];
			fAppearanceColorListModel[i][2]= store.getString(preference);
		}
	}
	
	private void createColorComposite() {
		
		Label l= new Label(getFieldEditorParent(), SWT.LEFT);
		l.setText(AntPreferencesMessages.getString("AntPreferencePage.Ant_Color_Options__6"));  //$NON-NLS-1$
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		l.setLayoutData(gd);
				
		Composite editorComposite= new Composite(getFieldEditorParent(), SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);		

		fAppearanceColorList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint = fAppearanceColorList.getItemHeight();
		fAppearanceColorList.setLayoutData(gd);
				
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		l= new Label(stylesComposite, SWT.LEFT);
		l.setText(AntPreferencesMessages.getString("AntPreferencePage.Color__7"));  //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		l.setLayoutData(gd);

		fAppearanceColorEditor= new ColorEditor(stylesComposite);
		Button foregroundColorButton= fAppearanceColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		fAppearanceColorList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAppearanceColorListSelection();
			}
		});
		foregroundColorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int i= fAppearanceColorList.getSelectionIndex();
				String key= fAppearanceColorListModel[i][1];
		
				PreferenceConverter.setValue(getPreferenceStore(), key, fAppearanceColorEditor.getColorValue());
			}
		});
	}
	
	/**
	 * Restore all color preferences to their values when the page was opened.
	 */
	public boolean performCancel() {
		for (int i = 0; i < fAppearanceColorListModel.length; i++) {
			String preference = fAppearanceColorListModel[i][1];
			PreferenceConverter.setValue(getPreferenceStore(), preference, StringConverter.asRGB(fAppearanceColorListModel[i][2]));
		}
		return super.performCancel();
	}
	
	/**
	 * When the user applies the preferences, update the set of stored
	 * preferences so that we will fall back to the applied values on Cancel.
	 */
	public boolean performOk() {
		storeAppliedValues();
		return super.performOk();
	}
	
	private void handleAppearanceColorListSelection() {	
		int i= fAppearanceColorList.getSelectionIndex();
		String key= fAppearanceColorListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(getPreferenceStore(), key);
		fAppearanceColorEditor.setColorValue(rgb);		
	}
	
	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(parent, IExternalToolsHelpContextIds.ANT_PREFERENCE_PAGE);
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
	 */
	protected void initialize() {
		super.initialize();
		for (int i= 0; i < fAppearanceColorListModel.length; i++) {
			fAppearanceColorList.add(fAppearanceColorListModel[i][0]);
		}
		fAppearanceColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fAppearanceColorList != null && !fAppearanceColorList.isDisposed()) {
					fAppearanceColorList.select(0);
					handleAppearanceColorListSelection();
				}
			}
		});
	}
	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		for (int i = 0; i < fAppearanceColorListModel.length; i++) {
			String key= fAppearanceColorListModel[i][1];
			PreferenceConverter.setValue(getPreferenceStore(), key, PreferenceConverter.getDefaultColor(getPreferenceStore(), key));
		}
		handleAppearanceColorListSelection();
		
		super.performDefaults();
	}

}