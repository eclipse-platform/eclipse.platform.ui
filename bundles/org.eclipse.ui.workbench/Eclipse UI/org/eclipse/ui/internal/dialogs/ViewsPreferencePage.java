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

package org.eclipse.ui.internal.dialogs;

import java.text.MessageFormat;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ColorSchemeService;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.themes.IThemeDescriptor;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * The ViewsPreferencePage is the page used to set preferences for the look of the
 * views in the workbench.
 */
public class ViewsPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	private Button showTextOnPerspectiveBar;
	
	/*
	 * change the tab style of the workbench
	 */
	private Button showTraditionalStyleTabs;
	
	/*
	 * Editors for working with colors in the Views/Appearance preference page 
	 */
	private BooleanFieldEditor colorIconsEditor;
	private ColorFieldEditor errorColorEditor;
	private ColorFieldEditor hyperlinkColorEditor;
	private ColorFieldEditor activeHyperlinkColorEditor;

	/*
	 * No longer supported - removed when confirmed!
	 * private Button openFloatButton;
	 */
	int editorAlignment;
	int viewAlignment;

	private static final String COLOUR_ICONS_TITLE = WorkbenchMessages.getString("ViewsPreference.colorIcons"); //$NON-NLS-1$
	static final String EDITORS_TITLE = WorkbenchMessages.getString("ViewsPreference.editors"); //$NON-NLS-1$
	private static final String EDITORS_TOP_TITLE = WorkbenchMessages.getString("ViewsPreference.editors.top"); //$NON-NLS-1$
	private static final String EDITORS_BOTTOM_TITLE = WorkbenchMessages.getString("ViewsPreference.editors.bottom"); //$NON-NLS-1$
	private static final String VIEWS_TITLE = WorkbenchMessages.getString("ViewsPreference.views"); //$NON-NLS-1$
	private static final String VIEWS_TOP_TITLE = WorkbenchMessages.getString("ViewsPreference.views.top"); //$NON-NLS-1$
	private static final String VIEWS_BOTTOM_TITLE = WorkbenchMessages.getString("ViewsPreference.views.bottom"); //$NON-NLS-1$
	/*
	 * No longer supported - remove when confirmed!
	 * private static final String OVM_FLOAT = WorkbenchMessages.getString("OpenViewMode.float"); //$NON-NLS-1$
	 */
	/**
	 * The label used for the note text.
	 */
	private static final String NOTE_LABEL = WorkbenchMessages.getString("Preference.note"); //$NON-NLS-1$
	private static final String APPLY_MESSAGE = WorkbenchMessages.getString("ViewsPreference.applyMessage"); //$NON-NLS-1$

    private Combo themeCombo;

	/**
	 * Create a composite that for creating the tab toggle buttons.
	 * @param composite Composite
	 * @param title String
	 */
	private Group createButtonGroup(Composite composite, String title) {

		Group buttonComposite = new Group(composite, SWT.NONE);
		buttonComposite.setText(title);
		buttonComposite.setFont(composite.getFont());
		FormLayout layout = new FormLayout();
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		buttonComposite.setLayout(layout);
		GridData data =
			new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		buttonComposite.setLayoutData(data);

		return buttonComposite;

	}
	/**
	 * Creates and returns the SWT control for the customized body 
	 * of this preference page under the given parent composite.
	 * <p>
	 * This framework method must be implemented by concrete
	 * subclasses.
	 * </p>
	 *
	 * @param parent the parent composite
	 * @return the new control
	 */
	protected Control createContents(Composite parent) {

 		Font font = parent.getFont();

		WorkbenchHelp.setHelp(parent, IHelpContextIds.VIEWS_PREFERENCE_PAGE);

		IPreferenceStore store =
			WorkbenchPlugin.getDefault().getPreferenceStore();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(font);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		//layout.verticalSpacing = 10;
		composite.setLayout(layout);
		
		GridData data =
			new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(WorkbenchMessages.getString("ViewsPreference.currentTheme")); //$NON-NLS-1$
		label.setLayoutData(data);
		
		data =
			new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		
		themeCombo = new Combo(composite, SWT.READ_ONLY);
        themeCombo.setLayoutData(data);
		refreshThemeCombo();

		Composite colorIconsComposite = new Composite(composite, SWT.NONE);
		colorIconsComposite.setFont(font);
		colorIconsEditor =
			new BooleanFieldEditor(
				IPreferenceConstants.COLOR_ICONS,
				COLOUR_ICONS_TITLE,
				colorIconsComposite);
		colorIconsEditor.setPreferencePage(this);
		colorIconsEditor.setPreferenceStore(doGetPreferenceStore());
		colorIconsEditor.load();

		createShowTextOnPerspectiveBarPref(composite);

		createShowTraditionalStyleTabsPref(composite);
		
		createNoteComposite(font, composite, NOTE_LABEL, APPLY_MESSAGE);

		Group colorComposite = new Group(composite, SWT.NONE);
		colorComposite.setLayout(new GridLayout());
		colorComposite.setText(WorkbenchMessages.getString("ViewsPreference.ColorsTitle")); //$NON-NLS-1$
		colorComposite.setFont(font);

		data =
			new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		colorComposite.setLayoutData(data);

		//Add in an intermediate composite to allow for spacing
		Composite spacingComposite = new Composite(colorComposite, SWT.NONE);
		GridLayout spacingLayout = new GridLayout();
		spacingLayout.numColumns = 4;
		spacingComposite.setLayout(spacingLayout);
		spacingComposite.setFont(font);

		errorColorEditor = new ColorFieldEditor(JFacePreferences.ERROR_COLOR, WorkbenchMessages.getString("ViewsPreference.ErrorText"), spacingComposite); //$NON-NLS-1$,

		errorColorEditor.setPreferenceStore(doGetPreferenceStore());
		errorColorEditor.load();

		hyperlinkColorEditor = new ColorFieldEditor(JFacePreferences.HYPERLINK_COLOR, WorkbenchMessages.getString("ViewsPreference.HyperlinkText"), spacingComposite); //$NON-NLS-1$

		hyperlinkColorEditor.setPreferenceStore(doGetPreferenceStore());
		hyperlinkColorEditor.load();

		activeHyperlinkColorEditor = new ColorFieldEditor(JFacePreferences.ACTIVE_HYPERLINK_COLOR, WorkbenchMessages.getString("ViewsPreference.ActiveHyperlinkText"),spacingComposite); //$NON-NLS-1$

		activeHyperlinkColorEditor.setPreferenceStore(doGetPreferenceStore());
		activeHyperlinkColorEditor.load();

		return composite;
	}
	
	/**
     * 
     */
    private void refreshThemeCombo() {
        themeCombo.removeAll();
        ITheme currentTheme = WorkbenchPlugin.getDefault().getWorkbench().getThemeManager().getCurrentTheme();
		
		IThemeDescriptor [] descs = WorkbenchPlugin.getDefault().getThemeRegistry().getThemes();
		int selection = 0;
		String themeString = WorkbenchMessages.getString("ViewsPreference.defaultTheme"); //$NON-NLS-1$
		if (currentTheme.getId() == null) {
		    themeString = MessageFormat.format(WorkbenchMessages.getString("ViewsPreference.currentThemeFormat"), new Object [] {themeString}); //$NON-NLS-1$
		}
		themeCombo.add(themeString);
		
		for (int i = 0; i < descs.length; i++) {
		    themeString = descs[i].getLabel();
			if (descs[i].getId().equals(currentTheme.getId())) {
			    themeString = MessageFormat.format(WorkbenchMessages.getString("ViewsPreference.currentThemeFormat"), new Object [] {themeString}); //$NON-NLS-1$
			    selection = i + 1;
			}
            themeCombo.add(themeString);
        }
		
		themeCombo.select(selection);
    }
    /**
	 * Create the button and text that support setting the preference for showing
	 * text labels on the perspective switching bar
	 */
	protected void createShowTextOnPerspectiveBarPref(Composite composite) {
		showTextOnPerspectiveBar = new Button(composite, SWT.CHECK);
		showTextOnPerspectiveBar.setText(WorkbenchMessages.getString("WorkbenchPreference.showTextOnPerspectiveBar")); //$NON-NLS-1$
		showTextOnPerspectiveBar.setFont(composite.getFont());
		showTextOnPerspectiveBar.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR));
		setButtonLayoutData(showTextOnPerspectiveBar);
	}
	
	/**
	 * Create the button and text that support setting the preference for showing
	 * text labels on the perspective switching bar
	 */
	protected void createShowTraditionalStyleTabsPref(Composite composite) {
		showTraditionalStyleTabs = new Button(composite, SWT.CHECK);
		showTraditionalStyleTabs.setText("Show &traditional style tabs");
		showTraditionalStyleTabs.setFont(composite.getFont());
		showTraditionalStyleTabs.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
		setButtonLayoutData(showTraditionalStyleTabs);
	}
	
	/**
	 * Set the two supplied controls to be beside each other.
	 */
	private void attachControls(Control leftControl, Control rightControl) {

		FormData leftData = new FormData();
		leftData.left = new FormAttachment(0, 0);

		FormData rightData = new FormData();
		rightData.left = new FormAttachment(leftControl, 5);

		leftControl.setLayoutData(leftData);
		rightControl.setLayoutData(rightData);
	}

	/**
	 * Returns preference store that belongs to the our plugin.
	 *
	 * @return the preference store for this plugin
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}
	/**
	 * Initializes this preference page for the given workbench.
	 * <p>
	 * This method is called automatically as the preference page is being created
	 * and initialized. Clients must not call this method.
	 * </p>
	 *
	 * @param workbench the workbench
	 */
	public void init(org.eclipse.ui.IWorkbench workbench) {
	}
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {
		IPreferenceStore store =
			WorkbenchPlugin.getDefault().getPreferenceStore();

		showTextOnPerspectiveBar.setSelection(store.getDefaultBoolean(IPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR));
		showTraditionalStyleTabs.setSelection(store.getDefaultBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
		
		colorIconsEditor.loadDefault();
		errorColorEditor.loadDefault();
		hyperlinkColorEditor.loadDefault();
		activeHyperlinkColorEditor.loadDefault();
		/*
		 * No longer supported - remove when confirmed!
		 * if (openFloatButton != null) 
		 * 	openFloatButton.setSelection(value == IPreferenceConstants.OVM_FLOAT);
		 */

		WorkbenchPlugin.getDefault().savePluginPreferences();
		super.performDefaults();
	}
	/**
	 *	The user has pressed Ok.  Store/apply this page's values appropriately.
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();

		store.setValue(IPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR, showTextOnPerspectiveBar.getSelection());
		store.setValue(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, showTraditionalStyleTabs.getSelection());
		
		colorIconsEditor.store();
		errorColorEditor.store();
		hyperlinkColorEditor.store();
		activeHyperlinkColorEditor.store();
	
		if (Workbench.getInstance() != null) {
			IWorkbenchWindow[] windows = Workbench.getInstance().getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++) {
				ColorSchemeService.setSchemeColors(windows[i].getShell());
			}
		}
			
		int idx = themeCombo.getSelectionIndex();
		if (idx == 0) {		    
		    Workbench.getInstance().getThemeManager().setCurrentTheme(IThemeManager.DEFAULT_THEME);
		}
		else {
		    Workbench.getInstance().getThemeManager().setCurrentTheme(WorkbenchPlugin.getDefault().getThemeRegistry().getThemes()[idx - 1].getId());
		}
		
		refreshThemeCombo();
		
		return true;
	}
}
