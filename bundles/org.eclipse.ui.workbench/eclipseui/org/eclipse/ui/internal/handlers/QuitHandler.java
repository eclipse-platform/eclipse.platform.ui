/*******************************************************************************
 * Copyright (c) 2007, 2026 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;

import jakarta.inject.Named;

/**
 * Exit the workbench. Normal invocation calls {@link IWorkbench#close()}, which
 * typically doesn't prompt the user before exiting.
 * <p>
 * Invocation with parameter mayPrompt="true" calls {@link Display#close()},
 * which may prompt the user (via a hook installed by
 * <code>org.eclipse.ui.internal.ide.application.IDEWorkbenchAdvisor</code>).
 *
 * @since 3.4
 */
public class QuitHandler {
	private static final String COMMAND_PARAMETER_ID_MAY_PROMPT = "mayPrompt"; //$NON-NLS-1$
	private static final String TRUE = "true"; //$NON-NLS-1$

	@Execute
	public void execute(IWorkbench workbench, @Optional @Named(COMMAND_PARAMETER_ID_MAY_PROMPT) String mayPrompt) {
		if (TRUE.equals(mayPrompt)) {
			workbench.getDisplay().close();
		} else {
			workbench.close();
		}
	}
}
