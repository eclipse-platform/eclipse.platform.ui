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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
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
	 * indicate whether the perspective bar should be docked in the main
	 * toolbar
	 */
	private Button dockPerspectiveBar;
	
	/*
	 * change the tab style of the workbench
	 */
	private Button showTraditionalStyleTabs;

	private Button editorTopButton;
	private Button editorBottomButton;
	private Button viewTopButton;
	private Button viewBottomButton;

	/*
	 * No longer supported - removed when confirmed!
	 * private Button openFloatButton;
	 */
	int editorAlignment;
	int viewAlignment;

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
		
		createEditorTabButtonGroup(composite);
		createViewTabButtonGroup(composite);
		
		GridData data =
			new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(WorkbenchMessages.getString("ViewsPreference.currentTheme")); //$NON-NLS-1$
		label.setFont(parent.getFont());
		label.setLayoutData(data);
		
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		data.horizontalSpan = 2;
				
		themeCombo = new Combo(composite, SWT.READ_ONLY);
        themeCombo.setLayoutData(data);
        themeCombo.setFont(parent.getFont());
		refreshThemeCombo();
		
		createShowTextOnPerspectiveBarPref(composite);
		createDockPerspectiveBarPref(composite);
		createShowTraditionalStyleTabsPref(composite);
		
		return composite;
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
	
		
    /**
     * 
     */
    private void refreshThemeCombo() {
        themeCombo.removeAll();
        ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
		
		IThemeDescriptor [] descs = WorkbenchPlugin.getDefault().getThemeRegistry().getThemes();
		int selection = 0;
		String themeString = PlatformUI.getWorkbench().getThemeManager().getTheme(IThemeManager.DEFAULT_THEME).getLabel();
		if (currentTheme.getId().equals(IThemeManager.DEFAULT_THEME)) {
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
	protected void createDockPerspectiveBarPref(Composite composite) {
		dockPerspectiveBar = new Button(composite, SWT.CHECK);
		dockPerspectiveBar.setText(WorkbenchMessages.getString("WorkbenchPreference.dockPerspectiveBar"));  //$NON-NLS-1$
		dockPerspectiveBar.setFont(composite.getFont());
		dockPerspectiveBar.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.DOCK_PERSPECTIVE_BAR));
		setButtonLayoutData(dockPerspectiveBar);
	}

	/**
	 * Create the button and text that support setting the preference for showing
	 * text labels on the perspective switching bar
	 */
	protected void createShowTraditionalStyleTabsPref(Composite composite) {
		showTraditionalStyleTabs = new Button(composite, SWT.CHECK);
		showTraditionalStyleTabs.setText(WorkbenchMessages.getString("ViewsPreference.traditionalTabs")); //$NON-NLS-1$
		showTraditionalStyleTabs.setFont(composite.getFont());
		showTraditionalStyleTabs.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
		setButtonLayoutData(showTraditionalStyleTabs);
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
	    //no-op
	}
	/**
	 * The default button has been pressed. 
	 */
	protected void performDefaults() {
		IPreferenceStore store =
			WorkbenchPlugin.getDefault().getPreferenceStore();

		showTextOnPerspectiveBar.setSelection(store.getDefaultBoolean(IPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR));
		dockPerspectiveBar.setSelection(store.getDefaultBoolean(IPreferenceConstants.DOCK_PERSPECTIVE_BAR));
		showTraditionalStyleTabs.setSelection(store.getDefaultBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));

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
		store.setValue(IPreferenceConstants.DOCK_PERSPECTIVE_BAR, dockPerspectiveBar.getSelection());
		store.setValue(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, showTraditionalStyleTabs.getSelection());
		
		// store the editor tab value to setting
		store.setValue(
			IPreferenceConstants.EDITOR_TAB_POSITION,
			editorAlignment);

		// store the view tab value to setting
		store.setValue(IPreferenceConstants.VIEW_TAB_POSITION, viewAlignment);
		
				
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
