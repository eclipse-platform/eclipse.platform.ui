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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.IWorkbenchPreferences;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ColorSchemeService;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The ViewsPreferencePage is the page used to set preferences for the look of the
 * views in the workbench.
 */
public class ViewsPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	private Button editorTopButton;
	private Button editorBottomButton;
	private Button viewTopButton;
	private Button viewBottomButton;
	private Button showTextOnPerspectiveBar;

	/*
	 * Editors for working with colors in the Views/Appearance preference page 
	 */
	private BooleanFieldEditor colorIconsEditor;
	private ColorThemeDemo colorThemeDemo;
	private ColorFieldEditor errorColorEditor;
	private ColorFieldEditor hyperlinkColorEditor;
	private ColorFieldEditor activeHyperlinkColorEditor;
	BooleanFieldEditor useDefault;	
	private ColorFieldEditor colorSchemeTabBGColorEditor;
	private ColorFieldEditor colorSchemeTabFGColorEditor;
	private ColorFieldEditor colorSchemeSelectedTabBGColorEditor;
	private ColorFieldEditor colorSchemeSelectedTabFGColorEditor;
	
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
		editorAlignment =
			store.getInt(IPreferenceConstants.EDITOR_TAB_POSITION);
		viewAlignment = store.getInt(IPreferenceConstants.VIEW_TAB_POSITION);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(font);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		//layout.verticalSpacing = 10;
		composite.setLayout(layout);

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

		createEditorTabButtonGroup(composite);
		createViewTabButtonGroup(composite);
		createShowTextOnPerspectiveBarPref(composite);

		createNoteComposite(font, composite, NOTE_LABEL, APPLY_MESSAGE);

		Group colorComposite = new Group(composite, SWT.NONE);
		colorComposite.setLayout(new GridLayout());
		colorComposite.setText(WorkbenchMessages.getString("ViewsPreference.ColorsTitle")); //$NON-NLS-1$
		colorComposite.setFont(font);

		GridData data =
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

		Group colorSchemeComposite = new Group(composite, SWT.NONE);
		colorSchemeComposite.setLayout(new GridLayout());
		
		colorSchemeComposite.setText("Workbench Color Theme"); 
		colorSchemeComposite.setFont(font);
		GridData data2 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		colorSchemeComposite.setLayoutData(data2); 
		//Add in an intermediate composite to allow for spacing
		final Composite spacingComposite2 = new Composite(colorSchemeComposite, SWT.NONE);
		GridLayout spacingLayout2 = new GridLayout();
		spacingLayout2.numColumns = 2;
		spacingComposite2.setLayout(spacingLayout2);
		spacingComposite2.setFont(font);
		spacingComposite2.setLayoutData(new GridData());
		
		//Add in the demo viewing area for color settings
		colorThemeDemo = new ColorThemeDemo(spacingComposite2);
		
		useDefault =
		new BooleanFieldEditor(
				JFacePreferences.USE_DEFAULT_THEME,
				"Use System Colors",
				spacingComposite2);
		useDefault.setPreferencePage(this);
		useDefault.setPreferenceStore(doGetPreferenceStore());
		useDefault.load();		
		
		// 	dummy label to fill the space
		Label spacer = new Label(spacingComposite2, SWT.NONE);
		
		colorSchemeSelectedTabBGColorEditor = new ColorFieldEditor(JFacePreferences.SCHEME_TAB_SELECTION_BACKGROUND, "Selected Tab Background", spacingComposite2);
		colorSchemeSelectedTabBGColorEditor.setPreferenceStore(doGetPreferenceStore());
		// If the value is still the default, this means the use has been using system colors
		// Or has not set this particular color from the default.
		if (store.isDefault(JFacePreferences.SCHEME_TAB_SELECTION_BACKGROUND)) {
			PreferenceConverter.setValue(store, JFacePreferences.SCHEME_TAB_SELECTION_BACKGROUND, JFaceColors.getTabFolderSelectionBackground(composite.getDisplay()).getRGB());
		}
		colorSchemeSelectedTabBGColorEditor.load();
		
		colorSchemeSelectedTabFGColorEditor = new ColorFieldEditor(JFacePreferences.SCHEME_TAB_SELECTION_FOREGROUND, "Selected Tab Foreground", spacingComposite2);
		colorSchemeSelectedTabFGColorEditor.setPreferenceStore(doGetPreferenceStore());
		// If the value is still the default, this means the use has been using system colors
		// Or has not set this particular color from the default.
		if (store.isDefault(JFacePreferences.SCHEME_TAB_SELECTION_FOREGROUND)) {
			PreferenceConverter.setValue(store, JFacePreferences.SCHEME_TAB_SELECTION_FOREGROUND, JFaceColors.getTabFolderSelectionForeground(composite.getDisplay()).getRGB());
		}	
		colorSchemeSelectedTabFGColorEditor.load();
		
		colorSchemeTabBGColorEditor = new ColorFieldEditor(JFacePreferences.SCHEME_TAB_BACKGROUND, "Tab Background", spacingComposite2);
		colorSchemeTabBGColorEditor.setPreferenceStore(doGetPreferenceStore());
		// If the value is still the default, this means the use has been using system colors
		// Or has not set this particular color from the default.
		if (store.isDefault(JFacePreferences.SCHEME_TAB_BACKGROUND)) {
			PreferenceConverter.setValue(store, JFacePreferences.SCHEME_TAB_BACKGROUND, JFaceColors.getTabFolderBackground(composite.getDisplay()).getRGB());
		}
		colorSchemeTabBGColorEditor.load();
		
		colorSchemeTabFGColorEditor = new ColorFieldEditor(JFacePreferences.SCHEME_TAB_FOREGROUND, "Tab Foreground", spacingComposite2);
		colorSchemeTabFGColorEditor.setPreferenceStore(doGetPreferenceStore());
		// If the value is still the default, this means the use has been using system colors
		// Or has not set this particular color from the default.
		if (store.isDefault(JFacePreferences.SCHEME_TAB_FOREGROUND)) {
			PreferenceConverter.setValue(store, JFacePreferences.SCHEME_TAB_FOREGROUND, JFaceColors.getTabFolderForeground(composite.getDisplay()).getRGB());
		}
		colorSchemeTabFGColorEditor.load();
		
		
		IPropertyChangeListener colorSettingsListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				updateColorThemeDemo(spacingComposite2);				
			}
		};

		useDefault.setPropertyChangeListener(colorSettingsListener);
		colorSchemeSelectedTabBGColorEditor.setPropertyChangeListener(colorSettingsListener);
		colorSchemeSelectedTabFGColorEditor.setPropertyChangeListener(colorSettingsListener);
		colorSchemeTabBGColorEditor.setPropertyChangeListener(colorSettingsListener);
		colorSchemeTabFGColorEditor.setPropertyChangeListener(colorSettingsListener);
		//initialize
		updateColorThemeDemo(spacingComposite2);
		
		return composite;
	}

	void updateColorThemeDemo(Composite parentComposite) {
		boolean defaultColor = useDefault.getBooleanValue();
		colorSchemeTabBGColorEditor.setEnabled(!defaultColor, parentComposite);
		colorSchemeTabFGColorEditor.setEnabled(!defaultColor, parentComposite);
		colorSchemeSelectedTabBGColorEditor.setEnabled(!defaultColor, parentComposite);
		colorSchemeSelectedTabFGColorEditor.setEnabled(!defaultColor, parentComposite);
		
		if (!defaultColor) {
			colorThemeDemo.setTabBGColor(new Color(parentComposite.getDisplay(), colorSchemeTabBGColorEditor.getColorSelector().getColorValue()));
			colorThemeDemo.setTabFGColor(new Color(parentComposite.getDisplay(), colorSchemeTabFGColorEditor.getColorSelector().getColorValue()));
			colorThemeDemo.setTabSelectionBGColor(new Color(parentComposite.getDisplay(), colorSchemeSelectedTabBGColorEditor.getColorSelector().getColorValue()));
			colorThemeDemo.setTabSelectionFGColor(new Color(parentComposite.getDisplay(), colorSchemeSelectedTabFGColorEditor.getColorSelector().getColorValue()));
			colorThemeDemo.redraw();
		} else {
			colorThemeDemo.setTabBGColor(JFaceColors.getDefaultColor(JFacePreferences.SCHEME_TAB_BACKGROUND));
			colorThemeDemo.setTabFGColor(JFaceColors.getDefaultColor(JFacePreferences.SCHEME_TAB_FOREGROUND));
			colorThemeDemo.setTabSelectionBGColor(JFaceColors.getDefaultColor(JFacePreferences.SCHEME_TAB_SELECTION_BACKGROUND));
			colorThemeDemo.setTabSelectionFGColor(JFaceColors.getDefaultColor(JFacePreferences.SCHEME_TAB_SELECTION_FOREGROUND));
			colorThemeDemo.redraw();
		}
	}
	
	/**
	 * Create a composite that contains buttons for selecting tab position for the edit selection. 
	 * @param composite Composite
	 * @param store IPreferenceStore
	 */
	private void createEditorTabButtonGroup(Composite composite) {

		Font font = composite.getFont();

		Group buttonComposite = createButtonGroup(composite, EDITORS_TITLE);

		this.editorTopButton = new Button(buttonComposite, SWT.RADIO);
		this.editorTopButton.setText(EDITORS_TOP_TITLE);
		this.editorTopButton.setSelection(this.editorAlignment == SWT.TOP);
		this.editorTopButton.setFont(font);

		this.editorTopButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editorAlignment = SWT.TOP;
			}
		});

		this
			.editorTopButton
			.getAccessible()
			.addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result = EDITORS_TITLE;
			}
		});

		this.editorBottomButton = new Button(buttonComposite, SWT.RADIO);
		this.editorBottomButton.setText(EDITORS_BOTTOM_TITLE);
		this.editorBottomButton.setSelection(
			this.editorAlignment == SWT.BOTTOM);
		this.editorBottomButton.setFont(font);

		this.editorBottomButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editorAlignment = SWT.BOTTOM;
			}
		});

		attachControls(this.editorTopButton, this.editorBottomButton);

	}

	/**
	 * Create a composite that contains buttons for selecting tab position for the view selection. 
	 * @param composite Composite
	 * @param store IPreferenceStore
	 */
	private void createViewTabButtonGroup(Composite composite) {

		Font font = composite.getFont();

		Group buttonComposite = createButtonGroup(composite, VIEWS_TITLE);
		buttonComposite.setFont(font);

		this.viewTopButton = new Button(buttonComposite, SWT.RADIO);
		this.viewTopButton.setText(VIEWS_TOP_TITLE);
		this.viewTopButton.setSelection(this.viewAlignment == SWT.TOP);
		this.viewTopButton.setFont(font);

		this.viewTopButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				viewAlignment = SWT.TOP;
			}
		});

		this.viewBottomButton = new Button(buttonComposite, SWT.RADIO);
		this.viewBottomButton.setText(VIEWS_BOTTOM_TITLE);
		this.viewBottomButton.setSelection(this.viewAlignment == SWT.BOTTOM);
		this.viewBottomButton.setFont(font);

		this.viewBottomButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				viewAlignment = SWT.BOTTOM;
			}
		});

		attachControls(this.viewTopButton, this.viewBottomButton);

	}

	protected void createShowTextOnPerspectiveBarPref(Composite composite) {
		showTextOnPerspectiveBar = new Button(composite, SWT.CHECK);
		showTextOnPerspectiveBar.setText(WorkbenchMessages.getString("WorkbenchPreference.showTextOnPerspectiveBar")); //$NON-NLS-1$
		showTextOnPerspectiveBar.setFont(composite.getFont());
		showTextOnPerspectiveBar.setSelection(getPreferenceStore().getBoolean(IWorkbenchPreferences.SHOW_TEXT_ON_PERSPECTIVE_BAR));
		setButtonLayoutData(showTextOnPerspectiveBar);
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

		int editorTopValue =
			store.getDefaultInt(IPreferenceConstants.EDITOR_TAB_POSITION);
		editorTopButton.setSelection(editorTopValue == SWT.TOP);
		editorBottomButton.setSelection(editorTopValue == SWT.BOTTOM);
		editorAlignment = editorTopValue;

		int viewTopValue =
			store.getDefaultInt(IPreferenceConstants.VIEW_TAB_POSITION);
		viewTopButton.setSelection(viewTopValue == SWT.TOP);
		viewBottomButton.setSelection(viewTopValue == SWT.BOTTOM);
		viewAlignment = viewTopValue;

		showTextOnPerspectiveBar.setSelection(store.getDefaultBoolean(IWorkbenchPreferences.SHOW_TEXT_ON_PERSPECTIVE_BAR));
		
		colorIconsEditor.loadDefault();
		errorColorEditor.loadDefault();
		hyperlinkColorEditor.loadDefault();
		useDefault.loadDefault();
		activeHyperlinkColorEditor.loadDefault();
		colorSchemeTabBGColorEditor.loadDefault();
		colorSchemeTabFGColorEditor.loadDefault();
		colorSchemeSelectedTabBGColorEditor.loadDefault();
		colorSchemeSelectedTabFGColorEditor.loadDefault();
		
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

		// store the editor tab value to setting
		store.setValue(
			IPreferenceConstants.EDITOR_TAB_POSITION,
			editorAlignment);

		// store the view tab value to setting
		store.setValue(IPreferenceConstants.VIEW_TAB_POSITION, viewAlignment);

		store.setValue(IWorkbenchPreferences.SHOW_TEXT_ON_PERSPECTIVE_BAR, showTextOnPerspectiveBar.getSelection());
		
		colorIconsEditor.store();
		errorColorEditor.store();
		hyperlinkColorEditor.store();
		activeHyperlinkColorEditor.store();
		useDefault.store();
		colorSchemeTabBGColorEditor.store();
		colorSchemeTabFGColorEditor.store();
		colorSchemeSelectedTabBGColorEditor.store();
		colorSchemeSelectedTabFGColorEditor.store();
		
		if (Workbench.getInstance() != null) {
			IWorkbenchWindow[] windows = Workbench.getInstance().getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++) {
				ColorSchemeService.setSchemeColors(windows[i].getShell());
			}
		}
		return true;
	}
}
