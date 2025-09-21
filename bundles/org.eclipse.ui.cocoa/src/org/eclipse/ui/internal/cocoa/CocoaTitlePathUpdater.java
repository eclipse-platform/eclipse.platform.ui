/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.cocoa;

import org.eclipse.swt.internal.cocoa.NSString;
import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.tweaklets.TitlePathUpdater;

/**
 *
 * @since 3.7
 *
 */
public class CocoaTitlePathUpdater extends TitlePathUpdater {

	@Override
	public void updateTitlePath(Shell window, String path) {
		if (window == null || window.isDisposed()) {
			return;
		}
		if (path == null) {
			path = "";
		}
		NSWindow nsWindow = window.getShell().view.window();
		NSString filePathString = NSString.stringWith(path);
		nsWindow.setRepresentedFilename(filePathString);
	}

}
