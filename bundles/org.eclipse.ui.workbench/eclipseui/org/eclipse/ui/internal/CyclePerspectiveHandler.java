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
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 504090
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.model.PerspectiveLabelProvider;

/**
 * This handler is used to switch between perspectives using the keyboard.
 * <p>
 * Replacement for CyclePerspectiveAction
 * </p>
 *
 * @since 3.3
 */
public class CyclePerspectiveHandler extends FilteredTableBaseHandler {
	private PerspectiveLabelProvider labelProvider = new PerspectiveLabelProvider(false);

	@Override
	protected Object getInput(WorkbenchPage page) {
		List<IPerspectiveDescriptor> refs = Arrays.asList(page.getSortedPerspectives());
		Collections.reverse(refs);
		return refs;
	}

	@Override
	protected ParameterizedCommand getBackwardCommand() {
		final ICommandService commandService = window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand(IWorkbenchCommandConstants.WINDOW_PREVIOUS_PERSPECTIVE);
		return new ParameterizedCommand(command, null);
	}

	@Override
	protected ParameterizedCommand getForwardCommand() {
		final ICommandService commandService = window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand(IWorkbenchCommandConstants.WINDOW_NEXT_PERSPECTIVE);
		return new ParameterizedCommand(command, null);
	}

	@Override
	protected String getTableHeader(IWorkbenchPart activePart) {
		return WorkbenchMessages.CyclePerspectiveAction_header;
	}

	@Override
	public void dispose() {
		if (labelProvider != null) {
			labelProvider.dispose();
			labelProvider = null;
		}
		super.dispose();
	}
}
