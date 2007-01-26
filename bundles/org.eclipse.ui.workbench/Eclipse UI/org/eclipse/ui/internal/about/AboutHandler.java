/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.about;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.dialogs.AboutDialog;

/**
 * Creates an About dialog and opens it.
 * @since 3.3
 */
public class AboutHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object appContextObj = event.getApplicationContext();
		if (appContextObj instanceof IEvaluationContext) {
			IEvaluationContext appContext = (IEvaluationContext) appContextObj;
			IWorkbenchWindow window = (IWorkbenchWindow) appContext
					.getVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
			if (window == null) {
				throw new ExecutionException("No active workbench window"); //$NON-NLS-1$
			}
			new AboutDialog(window.getShell()).open();
		}
		return null;
	}
}
