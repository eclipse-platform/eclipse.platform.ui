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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.*;

/**
 * Factory for saving and restoring a <code>FileEditorInput</code>. 
 * The stored representation of a <code>FileEditorInput</code> remembers
 * the full path of the file (that is, <code>IFile.getFullPath</code>).
 * <p>
 * The workbench will automatically create instances of this class as required.
 * It is not intended to be instantiated or subclassed by the client.
 * </p>
 */
public class FileEditorInputFactory implements IElementFactory {
	/**
	 * Factory id. The workbench plug-in registers a factory by this name
	 * with the "org.eclipse.ui.elementFactories" extension point.
	 */
	private static final String ID_FACTORY = 
		"org.eclipse.ui.part.FileEditorInputFactory";//$NON-NLS-1$

	/**
	 * Tag for the IFile.fullPath of the file resource.
	 */
	private static final String TAG_PATH = "path";//$NON-NLS-1$
/**
 * Creates a new factory.
 */
public FileEditorInputFactory() {
}
/* (non-Javadoc)
 * Method declared on IElementFactory.
 */
public IAdaptable createElement(IMemento memento) {
	// Get the file name.
	String fileName = memento.getString(TAG_PATH);
	if (fileName == null)
		return null;

	// Create an IResource.
	IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(fileName));
	if (res instanceof IFile)
		return new FileEditorInput((IFile)res);
	else
		return null;
}
/**
 * Returns the element factory id for this class.
 * 
 * @return the element factory id
 */
public static String getFactoryId() {
	return ID_FACTORY;
}
/**
 * Saves the state of the given file editor input into the given memento.
 *
 * @param memento the storage area for element state
 * @param input the file editor input
 */
public static void saveState(IMemento memento, FileEditorInput input) {
	IFile file = input.getFile();
	memento.putString(TAG_PATH, file.getFullPath().toString());
}
}
