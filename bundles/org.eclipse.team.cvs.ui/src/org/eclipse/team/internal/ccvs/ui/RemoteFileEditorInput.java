package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * An editor input for a file in a repository.
 */
public class RemoteFileEditorInput implements IWorkbenchAdapter, IStorageEditorInput {
	private ICVSRemoteFile file;
	private IStorage storage;

	/**
	 * Creates FileEditionEditorInput on the given file.
	 */
	public RemoteFileEditorInput(ICVSRemoteFile file) {
		this.file = file;
	}
	/**
	 * Returns whether the editor input exists.  
	 * <p>
	 * This method is primarily used to determine if an editor input should 
	 * appear in the "File Most Recently Used" menu.  An editor input will appear 
	 * in the list until the return value of <code>exists</code> becomes 
	 * <code>false</code> or it drops off the bottom of the list.
	 *
	 * @return <code>true</code> if the editor input exists; <code>false</code>
	 *		otherwise
	 */
	public boolean exists() {
		return true;
	}
	public boolean equals(Object o) {
		if (!(o instanceof RemoteFileEditorInput)) return false;
		RemoteFileEditorInput input = (RemoteFileEditorInput)o;
		return file.equals(input.file);
	}
	/**
	 * Returns an object which is an instance of the given class
	 * associated with this object. Returns <code>null</code> if
	 * no such object can be found.
	 *
	 * @param adapter the adapter class to look up
	 * @return a object castable to the given class, 
	 *    or <code>null</code> if this object does not
	 *    have an adapter for the given class
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return this;
		}
		return null;
	}
	/**
	 * Returns the children of this object.  When this object
	 * is displayed in a tree, the returned objects will be this
	 * element's children.  Returns an empty array if this
	 * object has no children.
	 *
	 * @param object The object to get the children for.
	 */
	public Object[] getChildren(Object o) {
		return new Object[0];
	}
	/**
	 * Returns an open input stream on the contents of this file.
	 * The client is responsible for closing the stream when finished.
	 *
	 * @return an input stream containing the contents of the file
	 * @exception CoreException if this method fails. 
	 */
	public InputStream getContents() throws CoreException {
		try {
			return file.getContents(new NullProgressMonitor());
		} catch (TeamException e) {
			throw new CoreException(e.getStatus());
		}
	}
	/**
	 * Returns the content type of the input.  For instance, if the input
	 * wraps an <code>IFile</code> the content type would be derived from 
	 * the extension or mime type.  If the input wraps another object it
	 * may just be the object type.  The content type is used for
	 * editor mapping.
	 */
	public String getContentType() {
		String name = file.getName();
		return name.substring(name.lastIndexOf('.')+1);
	}
	/**
	 * Returns the fully qualified path name of the input.
	 */
	public String getFullPath() {
		//use path to make sure slashes are correct
		// to do. For now, just return the name
		//return new Path(file.getProjectName()).append(file.getProjectRelativePath()).toString();
		return getName();
	}
	/**
	 * Returns the image descriptor for this input.
	 *
	 * @return the image descriptor for this input
	 */
	public ImageDescriptor getImageDescriptor() {
		IWorkbenchAdapter fileAdapter = (IWorkbenchAdapter)file.getAdapter(IWorkbenchAdapter.class);
		return fileAdapter == null ? null : fileAdapter.getImageDescriptor(file);
	}
	/**
	 * @see IWorkbenchAdapter#getImageDescriptor
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		IWorkbenchAdapter fileAdapter = (IWorkbenchAdapter)file.getAdapter(IWorkbenchAdapter.class);
		return fileAdapter == null ? null : fileAdapter.getImageDescriptor(file);
	}
	/**
	 * @see IWorkbenchAdapter#getLabel
	 */
	public String getLabel(Object o) {
		return file.getName();
	}
	/**
	 * Returns the input name for display purposes.  For instance, if
	 * the fully qualified input name is "a\b\MyFile.gif" the return value for
	 * <code>getName</code> is "MyFile.gif".
	 */
	public String getName() {
		String name = file.getName();
		try {
			return Policy.bind("nameAndRevision", name, file.getRevision()); //$NON-NLS-1$
		} catch (TeamException e) {
			return name;
		}
	}
	/**
	 * Returns the logical parent of the given object in its tree.
	 * Returns <code>null</code> if there is no parent, or if this object doesn't
	 * belong to a tree.
	 *
	 * @param object The object to get the parent for.
	 */
	public Object getParent(Object o) {
		return null;
	}
	/*
	 * Returns an interface used to persist the object.  If the editor input
	 * cannot be persisted this method returns <code>null</code>.
	 */
	public IPersistableElement getPersistable() {
		//not persistable
		return null;
	}
	/**
	 * Returns the underlying IStorage object.
	 *
	 * @return an IStorage object.
	 * @exception CoreException if this method fails
	 */
	public IStorage getStorage() throws CoreException {
		if (storage == null) {
			storage = new RemoteFileStorage(file);
		}
		return storage;
	}
	/**
	 * Returns the tool tip text for this editor input.  This text
	 * is used to differentiate between two input with the same name.
	 * For instance, MyClass.java in folder X and MyClass.java in folder Y.
	 * <p> 
	 * The format of the path will vary with each input type.  For instance,
	 * if the editor input is of type <code>IFileEditorInput</code> this method
	 * should return the fully qualified resource path.  For editor input of
	 * other types it may be different. 
	 * </p>
	 * @return the tool tip text
	 */
	public String getToolTipText() {
		//use path to make sure slashes are correct
		return getName();
		//return new Path(file.getProjectName()).append(file.getProjectRelativePath()).toString();
	}
}
