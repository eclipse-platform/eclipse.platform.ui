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


import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.IAntUIHelpContextIds;
import org.eclipse.ant.internal.ui.IAntUIPreferenceConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

public class AntPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private StringFieldEditor fBuildFileNames;
	
	private List fConsoleColorList;
	private ColorEditor fConsoleColorEditor;
	
	private BooleanFieldEditor toolsWarningEditor= null;
	
	// Array containing the message to display, the preference key, and the 
	// default value (initialized in storeInitialValues()) for each color preference
	private final String[][] fAppearanceColorListModel= new String[][] {
		{AntPreferencesMessages.getString("AntPreferencePage.&Error__2"), IAntUIPreferenceConstants.CONSOLE_ERROR_COLOR, null}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.&Warning__3"), IAntUIPreferenceConstants.CONSOLE_WARNING_COLOR, null}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.I&nformation__4"), IAntUIPreferenceConstants.CONSOLE_INFO_COLOR, null}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.Ve&rbose__5"), IAntUIPreferenceConstants.CONSOLE_VERBOSE_COLOR, null}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.Deb&ug__6"), IAntUIPreferenceConstants.CONSOLE_DEBUG_COLOR, null}, //$NON-NLS-1$
	};

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
		storeAppliedValues();

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
				
		createColorComposite();
		getPreferenceStore().addPropertyChangeListener(this);
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
		Font font= getFieldEditorParent().getFont();
		Label label= new Label(getFieldEditorParent(), SWT.LEFT);
		label.setText(AntPreferencesMessages.getString("AntPreferencePage.Ant_Color_Options__6"));  //$NON-NLS-1$
		label.setFont(font);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);
				
		Composite editorComposite= new Composite(getFieldEditorParent(), SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		editorComposite.setFont(font);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);		

		fConsoleColorList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.heightHint= convertHeightInCharsToPixels(8);
		fConsoleColorList.setLayoutData(gd);
		fConsoleColorList.setFont(font);
				
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		stylesComposite.setFont(font);

		label= new Label(stylesComposite, SWT.LEFT);
		label.setText(AntPreferencesMessages.getString("AntPreferencePage.Color__7"));  //$NON-NLS-1$
		label.setFont(font);
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);

		fConsoleColorEditor= new ColorEditor(stylesComposite);
		Button foregroundColorButton= fConsoleColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);
		foregroundColorButton.setFont(font);

		fConsoleColorList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAppearanceColorListSelection();
			}
		});
		foregroundColorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int i= fConsoleColorList.getSelectionIndex();
				String key= fAppearanceColorListModel[i][1];
		
				PreferenceConverter.setValue(getPreferenceStore(), key, fConsoleColorEditor.getColorValue());
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
		int i= fConsoleColorList.getSelectionIndex();
		String key= fAppearanceColorListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(getPreferenceStore(), key);
		fConsoleColorEditor.setColorValue(rgb);		
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
	
	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
	 */
	protected void initialize() {
		super.initialize();
		for (int i= 0; i < fAppearanceColorListModel.length; i++) {
			fConsoleColorList.add(fAppearanceColorListModel[i][0]);
		}
		fConsoleColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fConsoleColorList != null && !fConsoleColorList.isDisposed()) {
					fConsoleColorList.select(0);
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