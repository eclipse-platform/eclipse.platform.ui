package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.net.URL;
import java.util.*;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.internal.core.*;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.externaltools.internal.core.*;
import org.eclipse.ui.help.WorkbenchHelp;

import java.util.List;
/**
 * A page to set the preferences for the classpath
 */
public class AntPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	private ClasspathPage jarsPage;
	private AntTasksPage tasksPage;
	private AntTypesPage typesPage;
	/**
	 * Create the console page.
	 */
	public AntPreferencePage() {
		setDescription(ToolMessages.getString("AntPreferencePage.description")); //$NON-NLS-1$
		IPreferenceStore store =
			ExternalToolsPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}
	/**
	 * @see IWorkbenchPreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(parent, IHelpContextIds.ANT_PREFERENCE_PAGE);

		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new GridLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		jarsPage = new ClasspathPage();
		jarsPage.createTabItem(folder);
		tasksPage = new AntTasksPage();
		tasksPage.createTabItem(folder);
		typesPage = new AntTypesPage();
		typesPage.createTabItem(folder);

		//set the page inputs
		AntCorePreferences prefs = AntCorePlugin.getPlugin().getPreferences();
		jarsPage.setInput(Arrays.asList(prefs.getCustomURLs()));
		tasksPage.setInput(Arrays.asList(prefs.getCustomTasks()));
		typesPage.setInput(Arrays.asList(prefs.getCustomTypes()));

		return folder;
	}
	/**
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		AntCorePreferences prefs = AntCorePlugin.getPlugin().getPreferences();
		jarsPage.setInput(Arrays.asList(prefs.getDefaultCustomURLs()));
		tasksPage.setInput(Arrays.asList(prefs.getCustomTasks()));
		typesPage.setInput(Arrays.asList(prefs.getCustomTypes()));
	}
	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		List contents = jarsPage.getContents();
		if (contents != null) {
			URL[] urls = (URL[]) contents.toArray(new URL[contents.size()]);
			AntCorePlugin.getPlugin().getPreferences().setCustomURLs(urls);
		}
		contents = tasksPage.getContents();
		if (contents != null) {
			Task[] tasks = (Task[]) contents.toArray(new Task[contents.size()]);
			AntCorePlugin.getPlugin().getPreferences().setCustomTasks(tasks);
		}
		contents = typesPage.getContents();
		if (contents != null) {
			Type[] types = (Type[]) contents.toArray(new Type[contents.size()]);
			AntCorePlugin.getPlugin().getPreferences().setCustomTypes(types);
		}
		return super.performOk();
	}
}