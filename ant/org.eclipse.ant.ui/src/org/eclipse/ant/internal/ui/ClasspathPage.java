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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

/**
 * A widget group for the jars tab of the ant classpath preference page.
 */
public class ClasspathPage extends CustomizeAntPage {
	//button constants
	protected static final int ADD_JAR_BUTTON = IDialogConstants.CLIENT_ID + 1;
	protected static final int ADD_FOLDER_BUTTON = IDialogConstants.CLIENT_ID + 2;
	protected static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;
	
public ClasspathPage() {
}
/**
 * @see CustomizeAntPage#addButtonsToButtonGroup(Composite)
 */
protected void addButtonsToButtonGroup(Composite parent) {
	createButton(parent, "preferences.customize.addJarButtonTitle", ADD_JAR_BUTTON);
	createButton(parent, "preferences.customize.addFolderButtonTitle", ADD_FOLDER_BUTTON);
	createSeparator(parent);
	createButton(parent, "preferences.customize.removeButtonTitle", REMOVE_BUTTON);
}
protected void addFolderButtonPressed() {
	DirectoryDialog dialog = new DirectoryDialog(tableViewer.getControl().getShell());
	String result = dialog.open();
	if (result != null) {
		try {
			URL url = new URL("file:" + result + "/");
			contentProvider.add(url);
		} catch (MalformedURLException e) {
		}
	}
}
protected void addJarButtonPressed() {
	FileDialog dialog = new FileDialog(tableViewer.getControl().getShell());
	dialog.setFilterExtensions(new String[] {"*.jar"});
	String result = dialog.open();
	if (result != null) {
		try {
			URL url = new URL("file:" + result);
			contentProvider.add(url);
		} catch (MalformedURLException e) {
		}
	}
}
/**
 * @see CustomizeAntPage#buttonPressed(int)
 */
protected void buttonPressed(int buttonId) {
	switch (buttonId) {
		case ADD_JAR_BUTTON:
			addJarButtonPressed();
			break;
		case ADD_FOLDER_BUTTON:
			addFolderButtonPressed();
			break;
		case REMOVE_BUTTON:
			removeButtonPressed();
			break;
	}
}
/**
 * Creates and returns a tab item that contains this widget group.
 */
public TabItem createTabItem(TabFolder folder) {
	TabItem item = new TabItem(folder, SWT.NONE);
	item.setText(Policy.bind("preferences.customize.classpathPageTitle"));
	final Image image = AntUIPlugin.getPlugin().getImageDescriptor(AntUIPlugin.IMG_CLASSPATH).createImage();
	item.setImage(image);
	item.setData(this);
	item.setControl(createControl(folder));
	item.addDisposeListener(new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			if (image != null)
				image.dispose();
		}
	});

	return item;
}

	






}