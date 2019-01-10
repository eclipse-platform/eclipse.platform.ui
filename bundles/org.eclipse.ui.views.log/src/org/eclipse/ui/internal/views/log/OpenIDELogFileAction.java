/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

/*
 * This action is used to Open the Log File from the LogView if both org.eclipse.ui.ide and
 * org.eclipse.core.filesystem are available.  If both plugins are resolved, we will open
 * the log file through the IDE's file association preferences.  Otherwise,
 * LogView.getOpenLogJob() is called to open the file.
 */
public class OpenIDELogFileAction extends Action {

	private LogView fView;

	public OpenIDELogFileAction(LogView logView) {
		fView = logView;
	}

	@Override
	public void run() {
		IPath logPath = new Path(fView.getLogFile().getAbsolutePath());
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(logPath);
		if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
			IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = ww.getActivePage();
			try {
				IDE.openEditorOnFileStore(page, fileStore);
			} catch (PartInitException e) { // do nothing
			}
		}
	}

}
