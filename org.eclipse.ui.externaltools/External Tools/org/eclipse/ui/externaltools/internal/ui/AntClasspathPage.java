package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.core.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;

/**
 * Sub-page that allows the user to enter custom classpaths
 * to be used when running Ant build files.
 */
public class AntClasspathPage extends AntPage {
	private static final int ADD_JARS_BUTTON = IDialogConstants.CLIENT_ID + 1;
	private static final int ADD_FOLDER_BUTTON = IDialogConstants.CLIENT_ID + 2;
	private static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;
	private static final int UP_BUTTON = IDialogConstants.CLIENT_ID + 4;
	private static final int DOWN_BUTTON = IDialogConstants.CLIENT_ID + 5;

	private Button upButton;
	private Button downButton;
	
	private IDialogSettings fDialogSettings;
	
	private final AntClasspathLabelProvider labelProvider = new AntClasspathLabelProvider();

	/**
	 * Creates an instance.
	 */
	public AntClasspathPage(AntPreferencePage preferencePage) {
		super(preferencePage);
		fDialogSettings= ExternalToolsPlugin.getDefault().getDialogSettings();
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void addButtonsToButtonGroup(Composite parent) {
		createButton(parent, "AntClasspathPage.addJarButtonTitle", ADD_JARS_BUTTON); //$NON-NLS-1$;
		createButton(parent, "AntClasspathPage.addFolderButtonTitle", ADD_FOLDER_BUTTON); //$NON-NLS-1$;
		createSeparator(parent);
		upButton= createButton(parent, "AntClasspathPage.upButtonTitle", UP_BUTTON); //$NON-NLS-1$;
		downButton= createButton(parent, "AntClasspathPage.downButtonTitle", DOWN_BUTTON); //$NON-NLS-1$;
		removeButton= createButton(parent, "AntClasspathPage.removeButtonTitle", REMOVE_BUTTON); //$NON-NLS-1$;
	}
	
	/**
	 * Allows the user to enter a folder as a classpath.
	 */
	private void addFolderButtonPressed() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage(AntDialogMessages.getString("AntClasspathPage.&Choose_a_folder_to_add_to_the_classpath__1")); //$NON-NLS-1$
		
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
	 * Allows the user to enter add JARs to the classpath.
	 */
	private void addJarsButtonPressed() {
		String lastUsedPath;
		lastUsedPath= fDialogSettings.get(IUIConstants.DIALOGSTORE_LASTEXTJAR);
		if (lastUsedPath == null) {
			lastUsedPath= ""; //$NON-NLS-1$
		}
		FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
		dialog.setFilterExtensions(new String[] { "*.jar" }); //$NON-NLS-1$;
		dialog.setFilterPath(lastUsedPath);
		String result = dialog.open();
		if (result == null) {
			return;
		}
		IPath filterPath= new Path(dialog.getFilterPath());
		String[] results= dialog.getFileNames();
		for (int i = 0; i < results.length; i++) {
			String jarName = results[i];
			try {
				IPath path= filterPath.append(jarName).makeAbsolute();	
				URL url = new URL("file:" + path.toOSString()); //$NON-NLS-1$;
				addContent(url);
			} catch (MalformedURLException e) {
			}
		}
		
		fDialogSettings.put(IUIConstants.DIALOGSTORE_LASTEXTJAR, filterPath.toOSString());
	}
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
			case ADD_JARS_BUTTON :
				addJarsButtonPressed();
				break;
			case ADD_FOLDER_BUTTON :
				addFolderButtonPressed();
				break;
			case UP_BUTTON :
				handleMove(-1);
				break;
			case DOWN_BUTTON :
				handleMove(1);
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
	
	/* (non-Javadoc)
	 * Method declared on AntPage.
	 */
	protected void tableSelectionChanged(IStructuredSelection newSelection) {
	
		IStructuredSelection selection = (IStructuredSelection)getTableViewer().getSelection();
		List urls = getContents();
		boolean notEmpty = !selection.isEmpty();
		Iterator elements= selection.iterator();
		boolean first= false;
		boolean last= false;
		int lastUrl= urls.size() - 1;
		while (elements.hasNext()) {
			Object element = (Object) elements.next();
			if(!first && urls.indexOf(element) == 0) {
				first= true;
			}
			if (!last && urls.indexOf(element) == lastUrl) {
				last= true;
			}
		}
		
		removeButton.setEnabled(notEmpty);
		upButton.setEnabled(notEmpty && !first);
		downButton.setEnabled(notEmpty && !last);
	}
	
	protected void handleMove(int direction) {
		IStructuredSelection sel = (IStructuredSelection)getTableViewer().getSelection();
		List selList= sel.toList();
		List contents= getContents();
		Object[] movedURL= new Object[contents.size()];
		int i;
		for (Iterator urls = selList.iterator(); urls.hasNext();) {
			Object config = urls.next();
			i= contents.indexOf(config);
			movedURL[i + direction]= config;
		}
		
		contents.removeAll(selList);
			
		for (int j = 0; j < movedURL.length; j++) {
			Object config = movedURL[j];
			if (config != null) {
				contents.add(j, config);		
			}
		}
		setInput(contents);
		getTableViewer().refresh();	
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