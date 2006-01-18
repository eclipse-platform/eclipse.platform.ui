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
package org.eclipse.ui.internal.ide.filesystem;

import java.io.File;
import java.net.URI;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.fileSystem.FileSystemContributor;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

/**
 * LocalFileSystemContributor is the local file system
 * implementation of FileSystemContributor.
 * @since 3.2
 *
 */
public class LocalFileSystemContributor extends FileSystemContributor {

	/**
	 * Create an instance of the receiver.
	 */
	public LocalFileSystemContributor() {
		super();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ide.fileSystem.FileSystemContributor#browseFileSystem(java.lang.String, org.eclipse.swt.widgets.Shell)
	 */
	public URI browseFileSystem(String initialPath, Shell shell) {
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog
				.setMessage(IDEWorkbenchMessages.ProjectLocationSelectionDialog_directoryLabel);

		if (!initialPath.equals(IDEResourceInfoUtils.EMPTY_STRING)) {
			IFileInfo info = IDEResourceInfoUtils.getFileInfo(initialPath);
			if (info != null && info.exists())
				dialog.setFilterPath(initialPath);
		}

		String selectedDirectory = dialog.open();
		if (selectedDirectory == null)
			return null;
		return new File(selectedDirectory).toURI();
	}

}
