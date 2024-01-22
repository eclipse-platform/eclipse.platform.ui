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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *     Friederike Schertel <friederike@schertel.org> - Bug 478336
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 504088
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.ICommandService;

/**
 * This is the handler for NextEditor and PrevEditor commands.
 * <p>
 * Replacement for CycleEditorAction
 * </p>
 *
 * @since 3.3
 */
public class CycleEditorHandler extends FilteredTableBaseHandler {

	@Override
	protected Object getInput(WorkbenchPage page) {
		return page.getSortedEditorReferences();
	}

	@Override
	protected ParameterizedCommand getBackwardCommand() {
		final ICommandService commandService = window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand(IWorkbenchCommandConstants.WINDOW_PREVIOUS_EDITOR);
		return new ParameterizedCommand(command, null);
	}

	@Override
	protected ParameterizedCommand getForwardCommand() {
		final ICommandService commandService = window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand(IWorkbenchCommandConstants.WINDOW_NEXT_EDITOR);
		return new ParameterizedCommand(command, null);
	}

	@Override
	protected String getTableHeader(IWorkbenchPart activePart) {
		return WorkbenchMessages.CycleEditorAction_header;
	}

}
