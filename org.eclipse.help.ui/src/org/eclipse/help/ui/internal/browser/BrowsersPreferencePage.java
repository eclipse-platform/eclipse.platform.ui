package org.eclipse.help.ui.internal.browser;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.ui.internal.*;
import org.eclipse.help.ui.internal.util.WorkbenchResources;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Preference page for selecting default web browser.
 */
public class BrowsersPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {
	private String defaultBrowserID;
	private Table browsersTable;
	/**
	 * Creates preference page controls on demand.
	 *
	 * @param parent the parent for the preference page
	 */
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		WorkbenchHelp.setHelp(parent, IHelpUIConstants.PREF_PAGE_BROWSERS);
		Composite mainComposite = new Composite(parent, SWT.NULL);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		//data.grabExcessHorizontalSpace = true;
		mainComposite.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComposite.setLayout(layout);
		Label description = new Label(mainComposite, SWT.NULL);
		description.setText(WorkbenchResources.getString("select_browser"));
		createSpacer(mainComposite);
		Label tableDescription = new Label(mainComposite, SWT.NULL);
		tableDescription.setText(WorkbenchResources.getString("current_browser"));
		//data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		//description.setLayoutData(data);
		browsersTable = new Table(mainComposite, SWT.CHECK | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(6);
		browsersTable.setLayoutData(gd);
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
				.getDefaultBrowserID()
				.equals(aDescs[i].getID()))
				item.setChecked(true);
			else
				item.setChecked(false);
			item.setGrayed(aDescs.length == 1);
		}
		return mainComposite;
	}
	/**
	 * @see IWorkbenchPreferencePage
	 */
	public void init(IWorkbench workbench) {
	}
	/**
	 * @see IPreferencePage
	 */
	public boolean performOk() {
		TableItem[] items = browsersTable.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				// set new default browser
				String browserID =
					BrowserManager.getInstance().getBrowserDescriptors()[i].getID();
				BrowserManager.getInstance().setDefaultBrowserID(browserID);
				// save id in help ui preferences
				Preferences pref = WorkbenchHelpPlugin.getDefault().getPluginPreferences();
				pref.setValue(BrowserManager.DEFAULT_BROWSER_ID_KEY, browserID);
				WorkbenchHelpPlugin.getDefault().savePluginPreferences();
				return true;
			}
		}
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

}