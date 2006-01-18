/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.ide.fileSystem;

import java.io.File;
import java.net.URI;

import org.eclipse.swt.widgets.Shell;

/**
 * FileSystemContributor is the abstract superclass
 * of class attributes of filesystemSupport extensions. 
 * @since 3.2
 * @see org.eclipse.ui.ide.filesystemSupport
 * @see org.eclipse.core.filesystem.filesystem
 *
 */
public abstract class FileSystemContributor {
	
	/**
	 * Browse the file system for a URI to display to the user.
	 * @param initialPath The path to initialize the selection with.
	 * @param shell The shell to parent any required dialogs from
	 * @return URI if the file system is browsed successfully or
	 * <code>null</code> if a URI could not be determined.
	 */
	public abstract URI browseFileSystem(String initialPath, Shell shell);
	
	/**
	 * Return the string to display to the user for the
	 * supplied uri.
	 * @param uri
	 * @return String
	 */
	public String getDisplayString(URI uri){
		return uri.toString();		
	}
	
	/**
	 * Return a URI for the supplied String from the user.
	 * @param string
	 * @return URI or <code>null</code> if the string is 
	 * invalid.
	 */
	public URI getURI(String string){
		return (new File(string)).toURI();
	}

}
