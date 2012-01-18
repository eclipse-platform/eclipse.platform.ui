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
package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.PlatformUI;

/**
 * @author Prakash G.R.
 * @since 3.7
 * 
 */
public class HelpContentsHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) {

		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp();
			}
		});
		return null;
	}

}
