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
package org.eclipse.help.ui.internal.browser;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.browser.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.help.ui.internal.util.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.*;

/**
 * Preference page for selecting default web browser.
 */
public class BrowsersPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {
	private Table browsersTable;
	private Label customBrowserPathLabel;
	private Text customBrowserPath;
	private Button customBrowserBrowse;
	/**
	 * Creates preference page controls on demand.
	 *
	 * @param parent the parent for the preference page
	 */
	protected Control createContents(Composite parent) {
		Font font = parent.getFont();

		WorkbenchHelp.setHelp(parent, IHelpUIConstants.PREF_PAGE_BROWSERS);
		Composite mainComposite = new Composite(parent, SWT.NULL);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		//data.grabExcessHorizontalSpace = true;
		mainComposite.setLayoutData(data);
		mainComposite.setFont(font);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComposite.setLayout(layout);

		Label description = new Label(mainComposite, SWT.NULL);
		description.setFont(font);
		description.setText(WorkbenchResources.getString("select_browser"));
		createSpacer(mainComposite);

		Label tableDescription = new Label(mainComposite, SWT.NULL);
		tableDescription.setFont(font);
		tableDescription.setText(
			WorkbenchResources.getString("current_browser"));
		//data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		//description.setLayoutData(data);
		browsersTable = new Table(mainComposite, SWT.CHECK | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(6);
		browsersTable.setLayoutData(gd);
		browsersTable.setFont(font);
		browsersTable.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent selEvent) {
				if (selEvent.detail == SWT.CHECK) {
					TableItem item = (TableItem) selEvent.item;
					if (item.getChecked()) {
						// Deselect others
						TableItem[] items = browsersTable.getItems();
						for (int i = 0; i < items.length; i++) {
							if (items[i] == item)
								continue;
							else
								items[i].setChecked(false);
						}
					} else {
						// Do not allow deselection
						item.setChecked(true);
					}
					setEnabledCustomBrowserPath();
				}
			}
			public void widgetDefaultSelected(SelectionEvent selEvent) {
			}
		});
		// populate table with browsers
		BrowserDescriptor[] aDescs =
			BrowserManager.getInstance().getBrowserDescriptors();
		for (int i = 0; i < aDescs.length; i++) {
			TableItem item = new TableItem(browsersTable, SWT.NONE);
			item.setText(aDescs[i].getLabel());
			if (BrowserManager
				.getInstance()
				.getCurrentBrowserID()
				.equals(aDescs[i].getID()))
				item.setChecked(true);
			else
				item.setChecked(false);
			item.setGrayed(aDescs.length == 1);
		}

		createCustomBrowserPathPart(mainComposite);

		return mainComposite;
	}
	protected void createCustomBrowserPathPart(Composite mainComposite) {
		Font font = mainComposite.getFont();

		// vertical space
		new Label(mainComposite, SWT.NULL);

		Composite bPathComposite = new Composite(mainComposite, SWT.NULL);
		WorkbenchHelp.setHelp(
			bPathComposite,
			IHelpUIConstants.PREF_PAGE_CUSTOM_BROWSER_PATH);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 3;
		bPathComposite.setLayout(layout);
		bPathComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		customBrowserPathLabel = new Label(bPathComposite, SWT.LEFT);
		customBrowserPathLabel.setFont(font);
		customBrowserPathLabel.setText(WorkbenchResources.getString("CustomBrowserPreferencePage.Program")); //$NON-NLS-1$

		customBrowserPath = new Text(bPathComposite, SWT.BORDER);
		customBrowserPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		customBrowserPath.setFont(font);
		customBrowserPath.setText(
			HelpPlugin.getDefault().getPluginPreferences().getString(
				CustomBrowser.CUSTOM_BROWSER_PATH_KEY));

		customBrowserBrowse = new Button(bPathComposite, SWT.NONE);
		customBrowserBrowse.setFont(font);
		customBrowserBrowse.setText(WorkbenchResources.getString("CustomBrowserPreferencePage.Browse")); //$NON-NLS-1$
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint =
			convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint =
			convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint =
			Math.max(
				widthHint,
				customBrowserBrowse.computeSize(
					SWT.DEFAULT,
					SWT.DEFAULT,
					true).x);
		customBrowserBrowse.setLayoutData(data);
		customBrowserBrowse.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent event) {
			}
			public void widgetSelected(SelectionEvent event) {
				FileDialog d = new FileDialog(getShell());
				d.setText(WorkbenchResources.getString("CustomBrowserPreferencePage.Details")); //$NON-NLS-1$
				String file = d.open();
				if (file != null) {
					customBrowserPath.setText("\"" + file + "\" %1");
				}
			}
		});
		setEnabledCustomBrowserPath();
	}
	/**
	 * @see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench workbench) {
	}
	/**
	 * Performs special processing when this page's Defaults button has been pressed.
	 * <p>
	 * This is a framework hook method for sublcasses to do special things when
	 * the Defaults button has been pressed.
	 * Subclasses may override, but should call <code>super.performDefaults</code>.
	 * </p>
	 */
	protected void performDefaults() {
		TableItem[] items = browsersTable.getItems();
		String defaultBrowserID =
			BrowserManager.getInstance().getDefaultBrowserID();
		for (int i = 0; i < items.length; i++) {
			String browserID =
				BrowserManager.getInstance().getBrowserDescriptors()[i].getID();
			items[i].setChecked(browserID == defaultBrowserID);
		}
		customBrowserPath.setText(
			HelpPlugin.getDefault().getPluginPreferences().getDefaultString(
				CustomBrowser.CUSTOM_BROWSER_PATH_KEY));
		super.performDefaults();
	}
	/**
	 * @see IPreferencePage
	 */
	public boolean performOk() {
		Preferences pref = HelpPlugin.getDefault().getPluginPreferences();
		TableItem[] items = browsersTable.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				// set new current browser
				String browserID =
					BrowserManager
						.getInstance()
						.getBrowserDescriptors()[i]
						.getID();
				BrowserManager.getInstance().setCurrentBrowserID(browserID);
				// save id in help preferences
				pref.setValue(BrowserManager.DEFAULT_BROWSER_ID_KEY, browserID);
				break;
			}
		}
		pref.setValue(
			CustomBrowser.CUSTOM_BROWSER_PATH_KEY,
			customBrowserPath.getText());
		HelpPlugin.getDefault().savePluginPreferences();
		return true;
	}
	/**
	* Creates a horizontal spacer line that fills the width of its container.
	*
	* @param parent the parent control
	*/
	private void createSpacer(Composite parent) {
		Label spacer = new Label(parent, SWT.NONE);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		spacer.setLayoutData(data);
	}
	private void setEnabledCustomBrowserPath() {
		TableItem[] items = browsersTable.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				boolean enabled =
					(HelpPlugin.PLUGIN_ID + ".custombrowser").equals(
						BrowserManager
							.getInstance()
							.getBrowserDescriptors()[i]
							.getID());
				customBrowserPathLabel.setEnabled(enabled);
				customBrowserPath.setEnabled(enabled);
				customBrowserBrowse.setEnabled(enabled);
				break;
			}
		}

	}

}
