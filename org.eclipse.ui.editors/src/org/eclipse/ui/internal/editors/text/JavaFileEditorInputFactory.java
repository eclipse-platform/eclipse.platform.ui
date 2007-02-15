/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ide.IURIEditorInput;

/**
 * Factory for saving and restoring a <code>JavaFileEditorInput</code>. 
 * The stored representation of a <code>JavaFileEditorInput</code> remembers
 * the path of the editor input.
 * <p>
 * The workbench will automatically create instances of this class as required.
 * It is not intended to be instantiated or subclassed by the client.</p>
 * 
 * @since 3.3
 */
public class JavaFileEditorInputFactory implements IElementFactory {

	/**
	 * This factory's ID.
	 * <p>
	 * The editor plug-in registers a factory by this name with
	 * the <code>"org.eclipse.ui.elementFactories"<code> extension point.
	 */
	static final String ID= "org.eclipse.ui.ide.FileStoreEditorInputFactory"; //$NON-NLS-1$

	/**
	 * Saves the state of the given editor input into the given memento.
	 *
	 * @param memento the storage area for element state
	 * @param input the file editor input
	 */
	static void saveState(IMemento memento, IURIEditorInput input) {
		URI uri= input.getURI();
		memento.putString(TAG_URI, uri.toString());
	}

	/**
	 * Tag for the URI string.
	 */
	private static final String TAG_URI= "uri"; //$NON-NLS-1$

	/*
	 * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
	 */
	public IAdaptable createElement(IMemento memento) {
		// Get the file name.
		String uriString= memento.getString(TAG_URI);
		if (uriString == null)
			return null;
		
		URI uri;
		try {
			uri= new URI(uriString);
		} catch (URISyntaxException e) {
			return null;
		}
		
		IFileStore fileStore= EFS.getLocalFileSystem().getStore(uri);
		return new JavaFileEditorInput(fileStore);
	}

}
