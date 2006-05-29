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

import java.net.URI;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.swt.widgets.Shell;

/**
 * FileSystemContributor is the abstract superclass
 * of class attributes of <a href="../../../../../../extension-points/org_eclipse_ui_ide_filesystemSupport.html">filesystemSupport</a> extensions. 
 * @since 3.2
 * @see org.eclipse.core.filesystem.IFileSystem
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
	 * Return a URI for the supplied String from the user.
	 * @param string
	 * @return URI or <code>null</code> if the string is 
	 * invalid.
	 */
	public URI getURI(String string){
		return URIUtil.toURI(string);
	}

}
