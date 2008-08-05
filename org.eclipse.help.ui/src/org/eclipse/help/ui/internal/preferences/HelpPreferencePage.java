/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.preferences;

import java.util.Iterator;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.IHelpBaseConstants;
import org.eclipse.help.internal.browser.BrowserManager;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * Preference page for selecting default web browser.
 */
public class HelpPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	private Button alwaysExternal;

	private static final String WBROWSER_PAGE_ID = "org.eclipse.ui.browser.preferencePage";//$NON-NLS-1$

	private Button dhelpAsTrayButton;

	private Button dhelpAsInfopopButton;

	private Combo openModeCombo;
	
	private Combo dialogHelpCombo;
	
	private Combo windowHelpCombo;

	/**
	 * Creates preference page controls on demand.
	 * 
	 * @param parent
	 *            the parent for the preference page
	 */
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IHelpUIConstants.PREF_PAGE_HELP);
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		mainComposite.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComposite.setLayout(layout);
		Label description = new Label(mainComposite, SWT.NONE);
		description.setText(Messages.select_browser);
		
		if (BrowserManager.getInstance().isEmbeddedBrowserPresent()) {
			alwaysExternal = new Button(mainComposite, SWT.CHECK);
			alwaysExternal
					.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
			alwaysExternal.setText(Messages.use_only_external_browser); 
			alwaysExternal.setSelection(HelpBasePlugin.getDefault()
					.getPluginPreferences().getBoolean(
							IHelpBaseConstants.P_KEY_ALWAYS_EXTERNAL_BROWSER));
		}

		
		createLinkArea(mainComposite);
		createSpacer(mainComposite);
		createDynamicHelpArea(mainComposite);
		createSpacer(mainComposite);
		Dialog.applyDialogFont(mainComposite);
		return mainComposite;
	}

	private void createLinkArea(Composite parent) {
		IPreferenceNode node = getPreferenceNode(WBROWSER_PAGE_ID);
		if (node != null) {
			PreferenceLinkArea linkArea = new PreferenceLinkArea(parent,
					SWT.WRAP, WBROWSER_PAGE_ID,
					Messages.HelpPreferencePage_message,
					(IWorkbenchPreferenceContainer) getContainer(), null);
			GridData data = new GridData(GridData.FILL_HORIZONTAL
					| GridData.GRAB_HORIZONTAL);
			linkArea.getControl().setLayoutData(data);
		}
	}

	private void createDynamicHelpArea(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		group.setText(Messages.HelpPreferencePage_openModeGroup);
		
		Label whelpDescription = new Label(group, SWT.NONE);
		whelpDescription.setText(Messages.HelpPreferencePage_wlabel);
		whelpDescription.setLayoutData(createLabelData());
		
		windowHelpCombo = new Combo(group, SWT.READ_ONLY);
		windowHelpCombo.add(Messages.HelpPreferencePage_view);
		windowHelpCombo.add(Messages.HelpPreferencePage_infopop);
		windowHelpCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		boolean winfopop = HelpBasePlugin.getDefault().getPluginPreferences()
				.getBoolean(IHelpBaseConstants.P_KEY_WINDOW_INFOPOP);
		windowHelpCombo.setText(winfopop ? Messages.HelpPreferencePage_infopop : Messages.HelpPreferencePage_view);
		
		Label dhelpDescription = new Label(group, SWT.NONE);
		dhelpDescription.setText(Messages.HelpPreferencePage_dlabel);
		dhelpDescription.setLayoutData(createLabelData());
		dialogHelpCombo = new Combo(group, SWT.READ_ONLY);
		dialogHelpCombo.add(Messages.HelpPreferencePage_tray);
		dialogHelpCombo.add(Messages.HelpPreferencePage_infopop);
		dialogHelpCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		boolean dinfopop = HelpBasePlugin.getDefault().getPluginPreferences()
				.getBoolean(IHelpBaseConstants.P_KEY_DIALOG_INFOPOP);
		dialogHelpCombo.setText(dinfopop ? Messages.HelpPreferencePage_infopop : Messages.HelpPreferencePage_tray);

		if (PlatformUI.getWorkbench().getBrowserSupport()
				.isInternalWebBrowserAvailable()) {
			Label ohelpDescription = new Label(group, SWT.NONE);
			ohelpDescription.setText(Messages.HelpPreferencePage_olabel);
			ohelpDescription.setLayoutData(createLabelData());
			openModeCombo = new Combo(group, SWT.READ_ONLY);
			openModeCombo.add(Messages.HelpPreferencePage_openInPlace);
			openModeCombo.add(Messages.HelpPreferencePage_openInEditor);
			openModeCombo.add(Messages.HelpPreferencePage_openInBrowser);
			openModeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			String openMode = HelpBasePlugin.getDefault()
			    .getPluginPreferences().getString(IHelpBaseConstants.P_KEY_HELP_VIEW_OPEN_MODE);
			openModeCombo.setText(openModeToString(openMode));		
		}
	}
	
	private GridData createLabelData () {
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.verticalIndent = 5;
		data.horizontalSpan = 2;
		return data;
	}
	
	private IPreferenceNode getPreferenceNode(String pageId) {
		Iterator iterator = PlatformUI.getWorkbench().getPreferenceManager()
				.getElements(PreferenceManager.PRE_ORDER).iterator();
		while (iterator.hasNext()) {
			IPreferenceNode next = (IPreferenceNode) iterator.next();
			if (next.getId().equals(pageId))
				return next;
		}
		return null;
	}

	/**
	 * @see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * Performs special processing when this page's Defaults button has been
	 * pressed.
	 * <p>
	 * This is a framework hook method for sublcasses to do special things when
	 * the Defaults button has been pressed. Subclasses may override, but should
	 * call <code>super.performDefaults</code>.
	 * </p>
	 */
	protected void performDefaults() {
		if (alwaysExternal != null) {
			alwaysExternal.setSelection(HelpBasePlugin.getDefault()
					.getPluginPreferences().getDefaultBoolean(
							IHelpBaseConstants.P_KEY_ALWAYS_EXTERNAL_BROWSER));
		}

		boolean winfopop = HelpBasePlugin.getDefault().getPluginPreferences()
				.getDefaultBoolean(IHelpBaseConstants.P_KEY_WINDOW_INFOPOP);
		windowHelpCombo.setText(winfopop ? Messages.HelpPreferencePage_infopop : Messages.HelpPreferencePage_view);

		boolean dinfopop = HelpBasePlugin.getDefault().getPluginPreferences()
				.getDefaultBoolean(IHelpBaseConstants.P_KEY_DIALOG_INFOPOP);
		dhelpAsTrayButton.setSelection(!dinfopop);
		dhelpAsInfopopButton.setSelection(dinfopop);
	
		
		if (openModeCombo !=null) {
		   String openMode = HelpBasePlugin.getDefault()
				.getPluginPreferences().getDefaultString(
						IHelpBaseConstants.P_KEY_HELP_VIEW_OPEN_MODE);
		   openModeCombo.setText(openModeToString(openMode));
		}

		super.performDefaults();
	}

	/**
	 * @see IPreferencePage
	 */
	public boolean performOk() {
		Preferences pref = HelpBasePlugin.getDefault().getPluginPreferences();
		if (alwaysExternal != null) {
			pref.setValue(IHelpBaseConstants.P_KEY_ALWAYS_EXTERNAL_BROWSER,
					alwaysExternal.getSelection());
			BrowserManager.getInstance().setAlwaysUseExternal(
					alwaysExternal.getSelection());
		}
		pref.setValue(IHelpBaseConstants.P_KEY_WINDOW_INFOPOP,
				windowHelpCombo.getText().equals(Messages.HelpPreferencePage_infopop));

		pref.setValue(IHelpBaseConstants.P_KEY_DIALOG_INFOPOP,
				dialogHelpCombo.getText().equals(Messages.HelpPreferencePage_infopop));
		if (openModeCombo!=null) {
			pref.setValue(IHelpBaseConstants.P_KEY_HELP_VIEW_OPEN_MODE, openModeFromString(openModeCombo.getText()));
		}

		HelpBasePlugin.getDefault().savePluginPreferences();
		return true;
	}

	private String openModeToString(String openMode) {
	    if (IHelpBaseConstants.P_IN_BROWSER.equals(openMode)) {
		    return Messages.HelpPreferencePage_openInBrowser;
	   } else if (IHelpBaseConstants.P_IN_EDITOR.equals(openMode)) {
		    return Messages.HelpPreferencePage_openInEditor;
		} else {
			return Messages.HelpPreferencePage_openInPlace;
		}
	}
	
	private String openModeFromString(String openMode) {
	    if (Messages.HelpPreferencePage_openInBrowser.equals(openMode)) {
		    return IHelpBaseConstants.P_IN_BROWSER;
	   } else if (Messages.HelpPreferencePage_openInEditor.equals(openMode)) {
		    return IHelpBaseConstants.P_IN_EDITOR;
		} else {
			return IHelpBaseConstants.P_IN_PLACE;
		}
	}

	/**
	 * Creates a horizontal spacer line that fills the width of its container.
	 * 
	 * @param parent
	 *            the parent control
	 */
	private void createSpacer(Composite parent) {
		Label spacer = new Label(parent, SWT.NONE);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		spacer.setLayoutData(data);
	}
}
