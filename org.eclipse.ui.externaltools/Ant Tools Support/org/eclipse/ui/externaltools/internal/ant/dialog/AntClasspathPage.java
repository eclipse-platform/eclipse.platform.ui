package org.eclipse.ui.externaltools.internal.ant.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;

/**
 * Sub-page that allows the user to enter custom classpaths
 * to be used when running Ant build files.
 */
public class AntClasspathPage extends AntPage {
	private static final int ADD_JAR_BUTTON = IDialogConstants.CLIENT_ID + 1;
	private static final int ADD_FOLDER_BUTTON = IDialogConstants.CLIENT_ID + 2;
	private static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;

	private final AntClasspathLabelProvider labelProvider = new AntClasspathLabelProvider();

	/**
	 * Creates an instance.
	 */
	public AntClasspathPage(AntPreferencePage preferencePage) {
		super(preferencePage);
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void addButtonsToButtonGroup(Composite parent) {
		createButton(parent, "AntClasspathPage.addJarButtonTitle", ADD_JAR_BUTTON); //$NON-NLS-1$;
		createButton(parent, "AntClasspathPage.addFolderButtonTitle", ADD_FOLDER_BUTTON); //$NON-NLS-1$;
		createSeparator(parent);
		createButton(parent, "AntClasspathPage.removeButtonTitle", REMOVE_BUTTON); //$NON-NLS-1$;
	}
	
	/**
	 * Allows the user to enter a folder as a classpath.
	 */
	private void addFolderButtonPressed() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		String result = dialog.open();
		if (result != null) {
			try {
				URL url = new URL("file:" + result + "/"); //$NON-NLS-2$;//$NON-NLS-1$;
				addContent(url);
			} catch (MalformedURLException e) {
			}
		}
	}
	
	/**
	 * Allows the user to enter a JAR as a classpath.
	 */
	private void addJarButtonPressed() {
		FileDialog dialog = new FileDialog(getShell());
		dialog.setFilterExtensions(new String[] { "*.jar" }); //$NON-NLS-1$;
		String result = dialog.open();
		if (result != null) {
			try {
				URL url = new URL("file:" + result); //$NON-NLS-1$;
				addContent(url);
			} catch (MalformedURLException e) {
			}
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
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
	 * Creates the tab item that contains this sub-page.
	 */
	public TabItem createTabItem(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(ToolMessages.getString("AntClasspathPage.title")); //$NON-NLS-1$;
		item.setImage(labelProvider.getClasspathImage());
		item.setData(this);
		item.setControl(createControl(folder));
		return item;
	}

	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected ITableLabelProvider getLabelProvider() {
		return labelProvider;
	}


	/**
	 * Label provider for classpath elements
	 */
	private static final class AntClasspathLabelProvider extends LabelProvider implements ITableLabelProvider {
		private static final String IMG_JAR_FILE = "icons/full/obj16/jar_l_obj.gif"; //$NON-NLS-1$;
		private static final String IMG_CLASSPATH = "icons/full/obj16/classpath.gif"; //$NON-NLS-1$;

		private Image classpathImage;
		private Image folderImage;
		private Image jarImage;
	
		/**
		 * Creates an instance.
		 */
		public AntClasspathLabelProvider() {
		}
		
		/* (non-Javadoc)
		 * Method declared on IBaseLabelProvider.
		 */
		public void dispose() {
			// Folder image is shared, do not dispose.
			folderImage = null;
			if (jarImage != null) {
				jarImage.dispose();
				jarImage = null;
			}
			if (classpathImage != null) {
				classpathImage.dispose();
				classpathImage = null;
			}
		}
		
		/* (non-Javadoc)
		 * Method declared on ITableLabelProvider.
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			URL url = (URL) element;
			if (url.getFile().endsWith("/")) //$NON-NLS-1$
				return getFolderImage();
			else
				return getJarImage();
		}
		
		/* (non-Javadoc)
		 * Method declared on ITableLabelProvider.
		 */
		public String getColumnText(Object element, int columnIndex) {
			return ((URL) element).getFile();
		}

		private Image getFolderImage() {
			if (folderImage == null)
				folderImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			return folderImage;
		}
		
		private Image getJarImage() {
			if (jarImage == null) {
				ImageDescriptor desc = ExternalToolsPlugin.getDefault().getImageDescriptor(IMG_JAR_FILE);
				jarImage = desc.createImage();
			}
			return jarImage;
		}
		
		private Image getClasspathImage() {
			if (classpathImage == null) {
				ImageDescriptor desc = ExternalToolsPlugin.getDefault().getImageDescriptor(IMG_CLASSPATH);
				classpathImage = desc.createImage();
			}
			return classpathImage;
		}
	}
}