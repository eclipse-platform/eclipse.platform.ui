/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Selection dialog to select files and/or folders on the file system.
 * Use setInput to set input to a java.io.File that points to a folder.
 * 
 * @since 2.1
 */
public class FileFolderSelectionDialog extends ElementTreeSelectionDialog {

	/**
	 * Label provider for java.io.File objects.
	 */
	private static class FileLabelProvider extends LabelProvider {
		private static final Image IMG_FOLDER = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		private static final Image IMG_FILE =  PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
	
		public Image getImage(Object element) {
			if (element instanceof File) {
				File curr= (File) element;
				if (curr.isDirectory()) {
					return IMG_FOLDER;
				} else {
					return IMG_FILE;
				}
			}
			return null;
		}	
		public String getText(Object element) {
			if (element instanceof File) {
				return ((File) element).getName();
			}
			return super.getText(element);
		}
	}
	
	/**
	 * Content provider for java.io.File objects.
	 */
	private static class FileContentProvider implements ITreeContentProvider {
		private static final Object[] EMPTY= new Object[0];
		private FileFilter fileFilter;

		/**
		 * Creates a new instance of the receiver.
		 * 
		 * @param showFiles <code>true</code> files and folders are returned
		 * 	by the receiver. <code>false</code> only folders are returned.
		 */		
		public FileContentProvider(final boolean showFiles) {
			fileFilter = new FileFilter() {
				public boolean accept(File file) {
					if (file.isFile() && showFiles == false)
						return false;
					return true;
				}
			};			
		}
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof File) {
				File[] children= ((File) parentElement).listFiles(fileFilter);
				if (children != null) {
					return children;
				}
			}
			return EMPTY;
		}	
		public Object getParent(Object element) {
			if (element instanceof File) {
				return ((File) element).getParentFile();
			}
			return null;
		}	
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}	
		public Object[] getElements(Object element) {
			return getChildren(element);
		}	
		public void dispose() {
		}	
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	/**
	 * Viewer sorter that places folders first, then files.  
	 */
	private static class FileViewerSorter extends ViewerSorter {
		public int category(Object element) {
			if (element instanceof File) {
				if (((File) element).isFile()) {
					return 1;
				}
			}
			return 0;
		}
	}
	
	/**
	 * Validates the selection based on the multi select and folder setting.
	 */
	private static class FileSelectionValidator implements ISelectionStatusValidator {
		private boolean multiSelect;
		private boolean acceptFolders;
	
		/**
		 * Creates a new instance of the receiver.
		 * 
		 * @param multiSelect <code>true</code> if multi selection is allowed.
		 * 	<code>false</code> if only single selection is allowed. 
		 * @param acceptFolders <code>true</code> if folders can be selected
		 * 	in the dialog. <code>false</code> only files and be selected.  
		 */
		public FileSelectionValidator(boolean multiSelect, boolean acceptFolders) {
			this.multiSelect = multiSelect;
			this.acceptFolders = acceptFolders;
		}	
		public IStatus validate(Object[] selection) {
			int nSelected= selection.length;
			String pluginId = WorkbenchPlugin.getDefault().getDescriptor().getUniqueIdentifier();
			
			if (nSelected == 0 || (nSelected > 1 && multiSelect == false)) {
				return new Status(IStatus.ERROR, pluginId, IStatus.ERROR, "", null);  //$NON-NLS-1$
			}
			for (int i= 0; i < selection.length; i++) {
				Object curr= selection[i];
				if (curr instanceof File) {
					File file= (File) curr;
					if (acceptFolders == false && file.isFile() == false) {
						return new Status(IStatus.ERROR, pluginId, IStatus.ERROR, "", null);  //$NON-NLS-1$
					}
				}
			}
			return new Status(IStatus.OK, pluginId, IStatus.OK, "", null);  //$NON-NLS-1$
		}
	}	

/**
 * Creates a new instance of the receiver.
 * 
 * @param multiSelect <code>true</code> if multi selection is allowed.
 * 	<code>false</code> if only single selection is allowed. 
 * @param type one or both of <code>IResource.FILE</code> and 
 * 	<code>IResource.FOLDER</code>, ORed together.
 * 	If <code>IResource.FILE</code> is specified files and folders are 
 * 	displayed in the dialog. Otherwise only folders are displayed.
 * 	If <code>IResource.FOLDER</code> is specified folders can be selected
 * 	in addition to files. 
 */
public FileFolderSelectionDialog(Shell parent, boolean multiSelect, int type) {
	super(parent, new FileLabelProvider(), new FileContentProvider((type & IResource.FILE) != 0));
	setSorter(new FileViewerSorter());
	setValidator(new FileSelectionValidator(multiSelect, (type & IResource.FOLDER) != 0));
}
}