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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 
 * @since 3.7 
 *
 */
public class ZoomWindowHandler extends AbstractWindowHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		Shell activeShell = HandlerUtil.getActiveShell(event);
		NSWindow window = activeShell.view.window();
		if(window!=null)
			window.zoom(window);
		return null;
	}
	
}
