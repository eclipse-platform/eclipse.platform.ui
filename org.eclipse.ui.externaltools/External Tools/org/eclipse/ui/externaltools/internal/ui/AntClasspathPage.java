package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.externaltools.internal.core.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;

/**
 * A widget group for the classpath tab of the ant preference page.
 */
public class AntClasspathPage extends AntPage {
	//button constants
	private static final int ADD_JAR_BUTTON = IDialogConstants.CLIENT_ID + 1;
	private static final int ADD_FOLDER_BUTTON =IDialogConstants.CLIENT_ID + 2;
	private static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;
	
	public AntClasspathPage() {
	}
	
	/**
	 * @see CustomizeAntPage#addButtonsToButtonGroup(Composite)
	 */
	protected void addButtonsToButtonGroup(Composite parent) {
		createButton(parent, "ClasspathPage.addJarButtonTitle", ADD_JAR_BUTTON); //$NON-NLS-1$;
		createButton(parent, "ClasspathPage.addFolderButtonTitle", ADD_FOLDER_BUTTON); //$NON-NLS-1$;
		createSeparator(parent);
		createButton(parent, "ClasspathPage.removeButtonTitle", REMOVE_BUTTON); //$NON-NLS-1$;
	}
	
	private void addFolderButtonPressed() {
		DirectoryDialog dialog =
			new DirectoryDialog(tableViewer.getControl().getShell());
		String result = dialog.open();
		if (result != null) {
			try {
				URL url = new URL("file:" + result + "/"); //$NON-NLS-2$;//$NON-NLS-1$;
				contentProvider.add(url);
			} catch (MalformedURLException e) {
			}
		}
	}
	
	private void addJarButtonPressed() {
		FileDialog dialog = new FileDialog(tableViewer.getControl().getShell());
		dialog.setFilterExtensions(new String[] { "*.jar" }); //$NON-NLS-1$;
		String result = dialog.open();
		if (result != null) {
			try {
				URL url = new URL("file:" + result); //$NON-NLS-1$;
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
			case ADD_JAR_BUTTON :
				addJarButtonPressed();
				break;
			case ADD_FOLDER_BUTTON :
				addFolderButtonPressed();
				break;
			case REMOVE_BUTTON :
				removeButtonPressed();
				break;
		}
	}
	
	/**
	 * Creates and returns a tab item that contains this widget group.
	 */
	public TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(ToolMessages.getString("ClasspathPage.title")); //$NON-NLS-1$;
		final Image image =
			ExternalToolsPlugin
				.getDefault()
				.getImageDescriptor(ExternalToolsPlugin.IMG_CLASSPATH)
				.createImage();
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