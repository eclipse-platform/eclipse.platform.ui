/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cocoa;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @since 3.7
 * 
 */

public class ArrangeWindowsHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		NSApplication app = NSApplication.sharedApplication();
		app.arrangeInFront(app);
		return null;
	}

	public boolean isEnabled() {
		boolean isEnabled = false;
		Shell[] shells = Display.getDefault().getShells();

		// not all windows should be in minimized state
		for (int i = 0; i < shells.length; i++) {
			if (shells[i].view.window().isKeyWindow()) {
				isEnabled = true;
				break;
			}
		}
		return isEnabled;
	}

}
