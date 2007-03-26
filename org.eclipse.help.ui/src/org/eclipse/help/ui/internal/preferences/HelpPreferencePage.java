/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

	private Button whelpAsViewButton;

	private Button whelpAsInfopopButton;

	private Button dhelpAsTrayButton;

	private Button dhelpAsInfopopButton;

	private Button openInPlaceButton;

	private Button openInEditorButton;

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
		Composite composite;
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		group.setText(Messages.HelpPreferencePage_openModeGroup);
		
		Label whelpDescription = new Label(group, SWT.NONE);
		whelpDescription.setText(Messages.HelpPreferencePage_wlabel);
		whelpDescription.setLayoutData(createLabelData());
		composite = createRadioComposite(group);
		whelpAsViewButton = new Button(composite, SWT.RADIO);
		whelpAsViewButton.setText(Messages.HelpPreferencePage_view);
		whelpAsViewButton.setLayoutData(createIndentData());
		whelpAsInfopopButton = new Button(composite, SWT.RADIO);
		whelpAsInfopopButton.setText(Messages.HelpPreferencePage_winfopop);
		whelpAsInfopopButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		boolean winfopop = HelpBasePlugin.getDefault().getPluginPreferences()
				.getBoolean(IHelpBaseConstants.P_KEY_WINDOW_INFOPOP);
		whelpAsViewButton.setSelection(!winfopop);
		whelpAsInfopopButton.setSelection(winfopop);

		Label dhelpDescription = new Label(group, SWT.NONE);
		dhelpDescription.setText(Messages.HelpPreferencePage_dlabel);
		dhelpDescription.setLayoutData(createLabelData());
		composite = createRadioComposite(group);
		dhelpAsTrayButton = new Button(composite, SWT.RADIO);
		dhelpAsTrayButton.setText(Messages.HelpPreferencePage_tray);
		dhelpAsTrayButton.setLayoutData(createIndentData());
		dhelpAsInfopopButton = new Button(composite, SWT.RADIO);
		dhelpAsInfopopButton.setText(Messages.HelpPreferencePage_dinfopop);
		dhelpAsInfopopButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		boolean dinfopop = HelpBasePlugin.getDefault().getPluginPreferences()
				.getBoolean(IHelpBaseConstants.P_KEY_DIALOG_INFOPOP);
		dhelpAsTrayButton.setSelection(!dinfopop);
		dhelpAsInfopopButton.setSelection(dinfopop);

		if (PlatformUI.getWorkbench().getBrowserSupport()
				.isInternalWebBrowserAvailable()) {
			Label ohelpDescription = new Label(group, SWT.NONE);
			ohelpDescription.setText(Messages.HelpPreferencePage_olabel);
			ohelpDescription.setLayoutData(createLabelData());
			composite = createRadioComposite(group);
			openInPlaceButton = new Button(composite, SWT.RADIO);
			openInPlaceButton
					.setText(Messages.HelpPreferencePage_openInPlace);
			openInPlaceButton.setLayoutData(createIndentData());
			openInEditorButton = new Button(composite, SWT.RADIO);
			openInEditorButton
					.setText(Messages.HelpPreferencePage_openInEditor);
			openInEditorButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			boolean openInBrowser = HelpBasePlugin.getDefault()
					.getPluginPreferences().getBoolean(
							IHelpBaseConstants.P_KEY_OPEN_IN_EDITOR);
			openInPlaceButton.setSelection(!openInBrowser);
			openInEditorButton.setSelection(openInBrowser);
		}
	}
	
	private Composite createRadioComposite (Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, true);
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return composite;
	}
	
	private GridData createIndentData () {
		GridData data = new GridData();
		data.horizontalIndent = 20;
		return data;
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
		whelpAsViewButton.setSelection(!winfopop);
		whelpAsInfopopButton.setSelection(winfopop);

		boolean dinfopop = HelpBasePlugin.getDefault().getPluginPreferences()
				.getDefaultBoolean(IHelpBaseConstants.P_KEY_DIALOG_INFOPOP);
		dhelpAsTrayButton.setSelection(!dinfopop);
		dhelpAsInfopopButton.setSelection(dinfopop);
		
		if (openInPlaceButton!=null) {
		boolean openInEditor = HelpBasePlugin.getDefault()
				.getPluginPreferences().getDefaultBoolean(
						IHelpBaseConstants.P_KEY_OPEN_IN_EDITOR);
		openInPlaceButton.setSelection(!openInEditor);
		openInEditorButton.setSelection(openInEditor);
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
				whelpAsInfopopButton.getSelection());
		pref.setValue(IHelpBaseConstants.P_KEY_DIALOG_INFOPOP,
				dhelpAsInfopopButton.getSelection());
		if (openInEditorButton!=null)
			pref.setValue(IHelpBaseConstants.P_KEY_OPEN_IN_EDITOR,
				openInEditorButton.getSelection());

		HelpBasePlugin.getDefault().savePluginPreferences();
		return true;
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
