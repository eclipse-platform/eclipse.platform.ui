/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.ant.internal.ui;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.internal.core.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
/**
 * A page to set the preferences for the classpath
 */
public class CustomizeAntPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
		
	protected ClasspathPage jarsPage;
	protected TasksPage tasksPage;
	protected TypesPage typesPage;
/**
 * Create the console page.
 */
public CustomizeAntPreferencePage() {
	setDescription(Policy.bind("preferences.customize.description"));
	IPreferenceStore store = AntUIPlugin.getPlugin().getPreferenceStore();
	setPreferenceStore(store);
}
/**
 * @see IWorkbenchPreferencePage#init
 */
public void init(IWorkbench workbench) {
}
protected Control createContents(Composite parent) {
	TabFolder folder= new TabFolder(parent, SWT.NONE);
	folder.setLayout(new GridLayout());	
	folder.setLayoutData(new GridData(GridData.FILL_BOTH));

	jarsPage = new ClasspathPage();
	jarsPage.createTabItem(folder);
	tasksPage = new TasksPage();
	tasksPage.createTabItem(folder);
	typesPage = new TypesPage();
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
	jarsPage.setInput(Arrays.asList(prefs.getCustomURLs()));
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
		Task[] tasks= (Task[]) contents.toArray(new Task[contents.size()]);
		AntCorePlugin.getPlugin().getPreferences().setCustomTasks(tasks);
	}
	contents = typesPage.getContents();
	if (contents != null) {
		Type[] types= (Type[]) contents.toArray(new Type[contents.size()]);
		AntCorePlugin.getPlugin().getPreferences().setCustomTypes(types);
	}
	return super.performOk();
}
}
