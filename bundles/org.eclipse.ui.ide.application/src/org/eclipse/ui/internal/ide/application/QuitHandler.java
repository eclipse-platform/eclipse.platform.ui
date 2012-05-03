/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brian de Alwis - adapted to prompt user for confirmation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Adaptation of {@link org.eclipse.ui.internal.handlers.QuitHandler} that
 * prompts the user to confirm whether to quit.
 */
public class QuitHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
		IWorkbench workbench = (IWorkbench) context.getVariable(IWorkbench.class.getName());
		Shell shell = HandlerUtil.getActiveShell(event);
		if (IDEWorkbenchWindowAdvisor.promptOnExit(shell)) {
			workbench.close();
		}
		return null;
	}

}
