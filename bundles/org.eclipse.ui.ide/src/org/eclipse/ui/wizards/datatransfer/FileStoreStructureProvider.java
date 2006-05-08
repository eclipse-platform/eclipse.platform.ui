/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.wizards.datatransfer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * FileStoreStructureProvider is the structure provider for {@link IFileStore}
 * based file structures.
 * 
 * @since 3.2
 * 
 */
public class FileStoreStructureProvider implements IImportStructureProvider {

	/**
	 * Holds a singleton instance of this class.
	 */
	public final static FileStoreStructureProvider INSTANCE = new FileStoreStructureProvider();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.datatransfer.IImportStructureProvider#getChildren(java.lang.Object)
	 */
	public List getChildren(Object element) {
		try {
			return Arrays.asList(((IFileStore) element).childStores(EFS.NONE,
					new NullProgressMonitor()));
		} catch (CoreException exception) {
			logException(exception);
			return new ArrayList();
		}
	}

	/**
	 * Log the exception.
	 * 
	 * @param exception
	 */
	private void logException(CoreException exception) {
		IDEWorkbenchPlugin.log(exception.getLocalizedMessage(), exception);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.datatransfer.IImportStructureProvider#getContents(java.lang.Object)
	 */
	public InputStream getContents(Object element) {
		try {
			return ((IFileStore) element).openInputStream(EFS.NONE,
					new NullProgressMonitor());
		} catch (CoreException exception) {
			logException(exception);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.datatransfer.IImportStructureProvider#getFullPath(java.lang.Object)
	 */
	public String getFullPath(Object element) {
		return ((IFileStore) element).toURI().getSchemeSpecificPart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.datatransfer.IImportStructureProvider#getLabel(java.lang.Object)
	 */
	public String getLabel(Object element) {
		return ((IFileStore) element).getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.datatransfer.IImportStructureProvider#isFolder(java.lang.Object)
	 */
	public boolean isFolder(Object element) {
		return ((IFileStore) element).fetchInfo().isDirectory();
	}

}
