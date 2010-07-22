/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt.cocoa;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Prakash G.R. (grprakash@gmail.com)
 * @since 3.6
 * 
 */
public class CloseDialogHandler extends AbstractHandler implements IHandler {

	@Execute
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Shell activeShell = Display.getDefault().getActiveShell();
		// perform only if close is enabled
		if ((activeShell.getStyle() & SWT.CLOSE) != 0) {
			activeShell.close();
		}

		activeShell.close();
		return null;
	}

}
