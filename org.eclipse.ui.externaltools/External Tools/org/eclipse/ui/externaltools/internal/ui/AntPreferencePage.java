package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.ant.core.Task;
import org.eclipse.ant.core.Type;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.externaltools.internal.core.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A page to set the preferences for Ant
 */
public class AntPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private AntClasspathPage fClasspathPage;
	private AntTasksPage fTasksPage;
	private AntTypesPage fTypesPage;
	/**
	 * Create the Ant preference page
	 */
	public AntPreferencePage() {
		setDescription(ToolMessages.getString("AntPreferencePage.description")); //$NON-NLS-1$
		IPreferenceStore store =
			ExternalToolsPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}
	/**
	 * @see IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(parent, IHelpContextIds.ANT_PREFERENCE_PAGE);

		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new GridLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		fClasspathPage = new AntClasspathPage();
		fClasspathPage.createTabItem(folder);
		fTasksPage = new AntTasksPage();
		fTasksPage.createTabItem(folder);
		fTypesPage = new AntTypesPage();
		fTypesPage.createTabItem(folder);

		//set the page inputs
		AntCorePreferences prefs = AntCorePlugin.getPlugin().getPreferences();
		fClasspathPage.setInput(Arrays.asList(prefs.getCustomURLs()));
		fTasksPage.setInput(Arrays.asList(prefs.getCustomTasks()));
		fTypesPage.setInput(Arrays.asList(prefs.getCustomTypes()));

		return folder;
	}
	
	/**
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		AntCorePreferences prefs = AntCorePlugin.getPlugin().getPreferences();
		fClasspathPage.setInput(Arrays.asList(prefs.getDefaultCustomURLs()));
		fTasksPage.setInput(Arrays.asList(prefs.getCustomTasks()));
		fTypesPage.setInput(Arrays.asList(prefs.getCustomTypes()));
	}
	
	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		List contents = fClasspathPage.getContents();
		if (contents != null) {
			URL[] urls = (URL[]) contents.toArray(new URL[contents.size()]);
			AntCorePlugin.getPlugin().getPreferences().setCustomURLs(urls);
		}
		contents = fTasksPage.getContents();
		if (contents != null) {
			Task[] tasks = (Task[]) contents.toArray(new Task[contents.size()]);
			AntCorePlugin.getPlugin().getPreferences().setCustomTasks(tasks);
		}
		contents = fTypesPage.getContents();
		if (contents != null) {
			Type[] types = (Type[]) contents.toArray(new Type[contents.size()]);
			AntCorePlugin.getPlugin().getPreferences().setCustomTypes(types);
		}
		return super.performOk();
	}
}