/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.browser;

import java.util.Iterator;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.IHelpBaseConstants;
import org.eclipse.help.internal.browser.BrowserManager;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.preference.IPreferenceNode;
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
public class BrowsersPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	private Button alwaysExternal;

	private static final String WBROWSER_PAGE_ID = "org.eclipse.ui.browser.preferencePage";//$NON-NLS-1$
	/*
	 * private Button[] externalBrowsers; private Button customBrowserRadio;
	 * private Label customBrowserPathLabel; private Text customBrowserPath;
	 * private Button customBrowserBrowse;
	 */

	private Button whelpAsViewButton;

	private Button whelpAsInfopopButton;

	private Button dhelpAsWindowButton;

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
				IHelpUIConstants.PREF_PAGE_BROWSERS);
		Composite mainComposite = new Composite(parent, SWT.NULL);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		mainComposite.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComposite.setLayout(layout);
		Label description = new Label(mainComposite, SWT.NULL);
		description.setText(Messages.select_browser);
		// createSpacer(mainComposite);
		if (BrowserManager.getInstance().isEmbeddedBrowserPresent()) {
			alwaysExternal = new Button(mainComposite, SWT.CHECK);
			alwaysExternal
					.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
			alwaysExternal.setText(Messages.use_only_external_browser); 
			alwaysExternal.setSelection(HelpBasePlugin.getDefault()
					.getPluginPreferences().getBoolean(
							IHelpBaseConstants.P_KEY_ALWAYS_EXTERNAL_BROWSER));
			// createSpacer(mainComposite);
		}
		/*
		 * Label tableDescription = new Label(mainComposite, SWT.NULL);
		 * tableDescription.setText(HelpUIResources.getString("current_browser"));
		 * //$NON-NLS-1$ //data = new
		 * GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		 * //description.setLayoutData(data); Color bgColor =
		 * parent.getDisplay().getSystemColor( SWT.COLOR_LIST_BACKGROUND); Color
		 * fgColor = parent.getDisplay().getSystemColor(
		 * SWT.COLOR_LIST_FOREGROUND); final ScrolledComposite
		 * externalBrowsersScrollable = new ScrolledComposite( mainComposite,
		 * SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL); GridData gd = new
		 * GridData(GridData.FILL_BOTH); gd.heightHint =
		 * convertHeightInCharsToPixels(2);
		 * externalBrowsersScrollable.setLayoutData(gd);
		 * externalBrowsersScrollable.setBackground(bgColor);
		 * externalBrowsersScrollable.setForeground(fgColor); Composite
		 * externalBrowsersComposite = new Composite(
		 * externalBrowsersScrollable, SWT.NONE);
		 * externalBrowsersScrollable.setContent(externalBrowsersComposite);
		 * GridLayout layout2 = new GridLayout();
		 * externalBrowsersComposite.setLayout(layout2);
		 * externalBrowsersComposite.setBackground(bgColor);
		 * externalBrowsersComposite.setForeground(fgColor); BrowserDescriptor[]
		 * descriptors = BrowserManager.getInstance() .getBrowserDescriptors();
		 * externalBrowsers = new Button[descriptors.length]; for (int i = 0; i <
		 * descriptors.length; i++) { Button radio = new
		 * Button(externalBrowsersComposite, SWT.RADIO);
		 * org.eclipse.jface.dialogs.Dialog.applyDialogFont(radio);
		 * radio.setBackground(bgColor); radio.setForeground(fgColor);
		 * radio.setText(descriptors[i].getLabel()); if
		 * (BrowserManager.getInstance().getCurrentBrowserID().equals(
		 * descriptors[i].getID())) radio.setSelection(true); else
		 * radio.setSelection(false); radio.setData(descriptors[i]);
		 * externalBrowsers[i] = radio; if
		 * (BrowserManager.BROWSER_ID_CUSTOM.equals(descriptors[i].getID())) {
		 * customBrowserRadio = radio; radio.addSelectionListener(new
		 * SelectionListener() { public void widgetSelected(SelectionEvent
		 * selEvent) { setCustomBrowserPathEnabled(); } public void
		 * widgetDefaultSelected(SelectionEvent selEvent) {
		 * widgetSelected(selEvent); } }); } }
		 * externalBrowsersComposite.setSize(externalBrowsersComposite
		 * .computeSize(SWT.DEFAULT, SWT.DEFAULT));
		 * createCustomBrowserPathPart(mainComposite);
		 */
		createLinkArea(mainComposite);
		createSpacer(mainComposite);
		createDynamicHelpArea(mainComposite);
		org.eclipse.jface.dialogs.Dialog.applyDialogFont(mainComposite);
		return mainComposite;
	}

	private void createLinkArea(Composite parent) {
		// PreferenceManager manager =
		// PlatformUI.getWorkbench().getPreferenceManager();
		IPreferenceNode node = getPreferenceNode(WBROWSER_PAGE_ID);
		if (node != null) {
			PreferenceLinkArea linkArea = new PreferenceLinkArea(parent,
					SWT.WRAP, WBROWSER_PAGE_ID,
					Messages.BrowsersPreferencePage_message,
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
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(Messages.BrowsersPreferencePage_wgroup);
		whelpAsViewButton = new Button(group, SWT.RADIO);
		whelpAsViewButton.setText(Messages.BrowsersPreferencePage_view);
		whelpAsInfopopButton = new Button(group, SWT.RADIO);
		whelpAsInfopopButton.setText(Messages.BrowsersPreferencePage_winfopop);
		boolean winfopop = HelpBasePlugin.getDefault().getPluginPreferences()
				.getBoolean(IHelpBaseConstants.P_KEY_WINDOW_INFOPOP);
		whelpAsViewButton.setSelection(!winfopop);
		whelpAsInfopopButton.setSelection(winfopop);

		createSpacer(parent);
		layout = new GridLayout();
		layout.numColumns = 2;
		group = new Group(parent, SWT.NONE);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setLayout(layout);
		group.setText(Messages.BrowsersPreferencePage_dgroup);
		dhelpAsWindowButton = new Button(group, SWT.RADIO);
		dhelpAsWindowButton.setText(Messages.BrowsersPreferencePage_window);
		dhelpAsInfopopButton = new Button(group, SWT.RADIO);
		dhelpAsInfopopButton.setText(Messages.BrowsersPreferencePage_dinfopop);
		boolean dinfopop = HelpBasePlugin.getDefault().getPluginPreferences()
				.getBoolean(IHelpBaseConstants.P_KEY_DIALOG_INFOPOP);
		dhelpAsWindowButton.setSelection(!dinfopop);
		dhelpAsInfopopButton.setSelection(dinfopop);

		if (PlatformUI.getWorkbench().getBrowserSupport()
				.isInternalWebBrowserAvailable()) {
			createSpacer(parent);
			layout = new GridLayout();
			layout.numColumns = 2;
			group = new Group(parent, SWT.NONE);
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			group.setLayout(layout);
			group.setText(Messages.BrowsersPreferencePage_openModeGroup);
			openInPlaceButton = new Button(group, SWT.RADIO);
			openInPlaceButton
					.setText(Messages.BrowsersPreferencePage_openInPlace);
			openInEditorButton = new Button(group, SWT.RADIO);
			openInEditorButton
					.setText(Messages.BrowsersPreferencePage_openInEditor);
			boolean openInBrowser = HelpBasePlugin.getDefault()
					.getPluginPreferences().getBoolean(
							IHelpBaseConstants.P_KEY_OPEN_IN_EDITOR);
			openInPlaceButton.setSelection(!openInBrowser);
			openInEditorButton.setSelection(openInBrowser);
		}
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

	/*
	 * private void createCustomBrowserPathPart(Composite mainComposite) { Font
	 * font = mainComposite.getFont(); // vertical space new
	 * Label(mainComposite, SWT.NULL); Composite bPathComposite = new
	 * Composite(mainComposite, SWT.NULL);
	 * PlatformUI.getWorkbench().getHelpSystem().setHelp(bPathComposite,
	 * IHelpUIConstants.PREF_PAGE_CUSTOM_BROWSER_PATH); GridLayout layout = new
	 * GridLayout(); layout.marginWidth = 0; layout.marginHeight = 0;
	 * layout.numColumns = 3; bPathComposite.setLayout(layout);
	 * bPathComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	 * customBrowserPathLabel = new Label(bPathComposite, SWT.LEFT);
	 * customBrowserPathLabel.setFont(font);
	 * customBrowserPathLabel.setText(HelpUIResources
	 * .getString("CustomBrowserPreferencePage.Program")); //$NON-NLS-1$
	 * customBrowserPath = new Text(bPathComposite, SWT.BORDER);
	 * customBrowserPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	 * customBrowserPath.setFont(font);
	 * customBrowserPath.setText(HelpBasePlugin.getDefault()
	 * .getPluginPreferences().getString(
	 * CustomBrowser.CUSTOM_BROWSER_PATH_KEY)); GridData data = new
	 * GridData(GridData.FILL_HORIZONTAL); data.horizontalAlignment =
	 * GridData.FILL; data.widthHint = convertWidthInCharsToPixels(10);
	 * customBrowserPath.setLayoutData(data); customBrowserBrowse = new
	 * Button(bPathComposite, SWT.NONE); customBrowserBrowse.setFont(font);
	 * customBrowserBrowse.setText(HelpUIResources
	 * .getString("CustomBrowserPreferencePage.Browse")); //$NON-NLS-1$ data =
	 * new GridData(); data.horizontalAlignment = GridData.FILL; int widthHint =
	 * convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
	 * data.widthHint = Math.max(widthHint, customBrowserBrowse.computeSize(
	 * SWT.DEFAULT, SWT.DEFAULT, true).x);
	 * customBrowserBrowse.setLayoutData(data);
	 * customBrowserBrowse.addSelectionListener(new SelectionListener() { public
	 * void widgetDefaultSelected(SelectionEvent event) { } public void
	 * widgetSelected(SelectionEvent event) { FileDialog d = new
	 * FileDialog(getShell()); d.setText(HelpUIResources
	 * .getString("CustomBrowserPreferencePage.Details")); //$NON-NLS-1$ String
	 * file = d.open(); if (file != null) { customBrowserPath.setText("\"" +
	 * file + "\" %1"); //$NON-NLS-1$ //$NON-NLS-2$ } } });
	 * setCustomBrowserPathEnabled(); }
	 */
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
		/*
		 * String defaultBrowserID = BrowserManager.getInstance()
		 * .getDefaultBrowserID(); for (int i = 0; i < externalBrowsers.length;
		 * i++) { BrowserDescriptor descriptor = (BrowserDescriptor)
		 * externalBrowsers[i] .getData(); externalBrowsers[i]
		 * .setSelection(descriptor.getID() == defaultBrowserID); }
		 * customBrowserPath.setText(HelpBasePlugin.getDefault()
		 * .getPluginPreferences().getDefaultString(
		 * CustomBrowser.CUSTOM_BROWSER_PATH_KEY));
		 * setCustomBrowserPathEnabled();
		 */
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
		dhelpAsWindowButton.setSelection(!dinfopop);
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
		/*
		 * for (int i = 0; i < externalBrowsers.length; i++) { if
		 * (externalBrowsers[i].getSelection()) { // set new current browser
		 * String browserID = ((BrowserDescriptor) externalBrowsers[i]
		 * .getData()).getID();
		 * BrowserManager.getInstance().setCurrentBrowserID(browserID); // save
		 * id in help preferences
		 * pref.setValue(BrowserManager.DEFAULT_BROWSER_ID_KEY, browserID);
		 * break; } } pref.setValue(CustomBrowser.CUSTOM_BROWSER_PATH_KEY,
		 * customBrowserPath .getText());
		 */
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
	/*
	 * private void setCustomBrowserPathEnabled() { boolean enabled =
	 * customBrowserRadio.getSelection();
	 * customBrowserPathLabel.setEnabled(enabled);
	 * customBrowserPath.setEnabled(enabled);
	 * customBrowserBrowse.setEnabled(enabled); }
	 */
}
