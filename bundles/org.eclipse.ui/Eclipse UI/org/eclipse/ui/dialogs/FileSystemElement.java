package org.eclipse.ui.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.internal.model.WorkbenchAdapter;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.DisposeEvent;
import java.util.*;

/**
 *  Instances of this class represent files or file-like entities (eg.- zip
 *  file entries) on the local file system.  They do not represent resources
 *  within the workbench.  This distinction is made because the representation of
 *  a file system resource is significantly different from that of a workbench
 *  resource.
 *
 *  If self represents a collection (eg.- file system directory, zip directory)
 *  then its icon will be the folderIcon static field.  Otherwise (ie.- self
 *  represents a file system file) self's icon is stored in field "icon", and is
 *  determined by the extension of the file that self represents.
 *
 * This class is adaptable, and implements one adapter itself, namely the 
 * IWorkbenchAdapter adapter used for navigation and display in the workbench.
 */
public class FileSystemElement implements IAdaptable {
	private String name;
	private Object fileSystemObject;
	private AdaptableList folders = new AdaptableList();
	private AdaptableList files = new AdaptableList();
	private boolean isDirectory = false;
	private FileSystemElement parent;

	private WorkbenchAdapter workbenchAdapter = new WorkbenchAdapter() {
		/**
		 *	Answer the children property of this element
		 */
		public Object[] getChildren(Object o) {
			return folders.getChildren(o);
			
		}
		/**
		 * Returns the parent of this element
		 */
		public Object getParent(Object o) {
			return parent;
		}
		/**
		 * Returns an appropriate label for this file system element.
		 */
		public String getLabel(Object o) {
			return name;
		}
	/**
	 * Returns an image descriptor for this file system element
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		if (isDirectory()) {
			return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
		} else {
			return WorkbenchPlugin.getDefault().getEditorRegistry().getImageDescriptor(name);
		}
	}
};
/**
 * Creates a new <code>FileSystemElement</code> and initializes it
 * and its parent if applicable.
 *
 * @param name java.lang.String
 */
public FileSystemElement(String name,FileSystemElement parent,boolean isDirectory) {
	this.name = name;
	this.parent = parent;
	this.isDirectory = isDirectory;
	if (parent != null)
		parent.addChild(this);
}
/**
 * Adds the passed child to this object's collection of children.
 *
 * @param child FileSystemElement
 */
public void addChild(FileSystemElement child) {
	if (child.isDirectory()) {
		folders.add(child);
	} else {
		files.add(child);
	}
}
/**
 * Returns the adapter
 */
public Object getAdapter(Class adapter) {
	if (adapter == IWorkbenchAdapter.class) {
		return workbenchAdapter;
	}
	//defer to the platform
	return Platform.getAdapterManager().getAdapter(this, adapter);
}
/**
 * Returns the extension of this element's filename.  Returns
 * The empty string if there is no extension.
 */
public String getFileNameExtension() {
	int lastDot = name.lastIndexOf('.');
	return lastDot < 0 ? "" : name.substring(lastDot+1);	
}
/**
 *	Answer the files property of this element
 */
public AdaptableList getFiles() {
	return files;
}
/**
 *	Returns the file system object property of this element
 *
 *	@return the file system object
 */
public Object getFileSystemObject() {
	return fileSystemObject;
}
/**
 * Returns a list of the folders that are immediate children
 * of this folder.
 */
public AdaptableList getFolders() {
	return folders;
}
/**
 * Return the parent of this element.
 *
 * @return the parent file system element, or <code>null</code> if this is the root
 */
public FileSystemElement getParent() {
	return this.parent;
}
/**
 * Returns true if this element represents a directory, and false
 * otherwise.
 */
public boolean isDirectory() {
	return isDirectory;
}
/**
 * Removes a sub-folder from this file system element.
 */
public void removeFolder(FileSystemElement child) {
	folders.remove(child);
	child.setParent(null);
}
/**
 *	Set the file system object property of this element
 *
 *	@param value the file system object
 */
public void setFileSystemObject(Object value) {
	fileSystemObject = value;
}
/**
 * Sets the parent of this file system element.
 */
public void setParent(FileSystemElement element) {
	parent = element;
}
/**
 * For debugging purposes only.
 */
public String toString() {
	StringBuffer buf = new StringBuffer();
	if (isDirectory()) {
		buf.append("Folder(");
	} else {
		buf.append("File(");
	}
	buf.append(name).append(")");

	if (!isDirectory()) {
		return buf.toString();
	}

	buf.append(" folders: ");
	buf.append(folders);
	buf.append(" files: ");
	buf.append(files);
	return buf.toString();
}
}
