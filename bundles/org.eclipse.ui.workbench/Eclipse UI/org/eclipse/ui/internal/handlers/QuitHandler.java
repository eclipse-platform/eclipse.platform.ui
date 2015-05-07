/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;

/**
 * Exit the workbench. Normal invocation calls {@link IWorkbench#close()}, which
 * typically doesn't prompt the user before exiting.
 * <p>
 * Invocation with parameter mayPrompt="true" calls {@link Display#close()},
 * which may prompt the user (via a hook installed by
 * <code>org.eclipse.ui.internal.ide.application.IDEWorkbenchAdvisor</code>).
 *
 * @since 3.4
 *
 */
public class QuitHandler extends AbstractHandler {
	private static final String COMMAND_PARAMETER_ID_MAY_PROMPT = "mayPrompt"; //$NON-NLS-1$
	private static final String TRUE = "true"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
		IWorkbench workbench = (IWorkbench) context.getVariable(IWorkbench.class.getName());
		if (TRUE.equals(event.getParameter(COMMAND_PARAMETER_ID_MAY_PROMPT))) {
			workbench.getDisplay().close();
		} else {
			workbench.close();
		}
		return null;
	}
}
