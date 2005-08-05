/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup.browsers;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * The dialog for selecting the external folder for which a source container will be created.
 * 
 * @since 3.0
 */
public class DirectorySourceContainerDialog {
	
	private String fRes;
	private static final String LAST_PATH_SETTING = "EXT_FOLDER_LAST_PATH_SETTING"; //$NON-NLS-1$
	
	public DirectorySourceContainerDialog(Shell shell) {
		
		String lastUsedPath= DebugUIPlugin.getDefault().getDialogSettings().get(LAST_PATH_SETTING);
		if (lastUsedPath == null) {
			lastUsedPath= "";  //$NON-NLS-1$
		}
		//TODO not supposed to subclass DirectoryDialog, but need a checkbox added for subfolder searching
		DirectoryDialog dialog = new DirectoryDialog(shell, SWT.MULTI);
		dialog.setText(SourceLookupUIMessages.DirectorySourceContainerDialog_0); 
		dialog.setFilterPath(lastUsedPath);
		dialog.setMessage(SourceLookupUIMessages.DirectorySourceContainerDialog_1); 
		fRes = dialog.open();
		
		if (fRes == null) {
			return;
		}
		
		IPath filterPath= new Path(dialog.getFilterPath());		
		DebugUIPlugin.getDefault().getDialogSettings().put(LAST_PATH_SETTING, filterPath.toOSString());		
	}
	
	/**
	 * Returns the result of the dialog.open() operation
	 * @return the dialog.open() result
	 */
	public String getResult() {
		return fRes;
	}
	
}
