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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPreferencePage;
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
	 * change the tab style of the workbench
	 */
	private Button showTraditionalStyleTabs;

	/*
	 * No longer supported - removed when confirmed!
	 * private Button openFloatButton;
	 */
	int editorAlignment;
	int viewAlignment;

	/*
	 * No longer supported - remove when confirmed!
	 * private static final String OVM_FLOAT = WorkbenchMessages.getString("OpenViewMode.float"); //$NON-NLS-1$
	 */

    private Combo themeCombo;

    private Button colorIcons;

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
		
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		data.horizontalSpan = 2;
				
		themeCombo = new Combo(composite, SWT.READ_ONLY);
        themeCombo.setLayoutData(data);
		refreshThemeCombo();
		
		createShowTextOnPerspectiveBarPref(composite);

		createShowTraditionalStyleTabsPref(composite);
		
		createNoteComposite(font, composite, WorkbenchMessages.getString("Preference.note"), WorkbenchMessages.getString("ViewsPreference.applyMessage")); //$NON-NLS-1$ //$NON-NLS-2$

		createColorIconsPref(composite);
		
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
		showTraditionalStyleTabs.setText(WorkbenchMessages.getString("ViewsPreference.traditionalTabs")); //$NON-NLS-1$
		showTraditionalStyleTabs.setFont(composite.getFont());
		showTraditionalStyleTabs.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
		setButtonLayoutData(showTraditionalStyleTabs);
	}
	
	/**
     * @param composite
     */
    private void createColorIconsPref(Composite composite) {
		colorIcons = new Button(composite, SWT.CHECK);
		colorIcons.setText(WorkbenchMessages.getString("ViewsPreference.colorIcons")); //$NON-NLS-1$
		colorIcons.setFont(composite.getFont());
		colorIcons.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.COLOR_ICONS));
		setButtonLayoutData(colorIcons);
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
		showTraditionalStyleTabs.setSelection(store.getDefaultBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
		colorIcons.setSelection(store.getDefaultBoolean(IPreferenceConstants.COLOR_ICONS));

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
		store.setValue(IPreferenceConstants.COLOR_ICONS, colorIcons.getSelection());		
				
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
