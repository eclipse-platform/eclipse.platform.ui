/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.misc.Assert;

/**
 * Adapter for making a file resource a suitable input for an editor.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class FileEditorInput implements IFileEditorInput, IPersistableElement {
	private IFile file;
/**
 * Creates an editor input based of the given file resource.
 *
 * @param file the file resource
 */
public FileEditorInput(IFile file) {
	Assert.isNotNull(file);
	this.file = file;
}
/**
 * The <code>FileEditorInput</code> implementation of this <code>Object</code>
 * method bases the equality of two <code>FileEditorInput</code> objects on the
 * equality of their underlying <code>IFile</code> resources.
 */
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (!(obj instanceof FileEditorInput))
		return false;
	FileEditorInput other = (FileEditorInput)obj;
	return file.equals(other.file);
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
	return file.exists();
}
/* (non-Javadoc)
 * Method declared on IAdaptable.
 */
public Object getAdapter(Class adapter) {
	if (adapter == IFile.class)
		return file;
	return file.getAdapter(adapter);
}
/* (non-Javadoc)
 * Method declared on IPersistableElement.
 */
public String getFactoryId() {
	return FileEditorInputFactory.getFactoryId();
}
/* (non-Javadoc)
 * Method declared on IFileEditorInput.
 */
public IFile getFile() {
	return file;
}
/* (non-Javadoc)
 * Returns the image descriptor for this input.
 *
 * @return the image descriptor for this input
 */
public ImageDescriptor getImageDescriptor() {
	return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(file);
}
/* (non-Javadoc)
 * Method declared on IEditorInput.
 */
public String getName() {
	return file.getName();
}
/* (non-Javadoc)
 * Method declared on IEditorInput.
 */
public IPersistableElement getPersistable() {
	return this;
}
/* (non-Javadoc)
 * Method declared on IStorageEditorInput.
 */
public IStorage getStorage() throws CoreException {
	return file;
}
/* (non-Javadoc)
 * Method declared on IEditorInput.
 */
public String getToolTipText() {
	return file.getFullPath().makeRelative().toString();
}
/* (non-Javadoc)
 * Method declared on Object.
 */
public int hashCode() {
	return file.hashCode();
}
/* (non-Javadoc)
 * Method declared on IPersistableElement.
 */
public void saveState(IMemento memento) {
	FileEditorInputFactory.saveState(memento, this);
}
}
