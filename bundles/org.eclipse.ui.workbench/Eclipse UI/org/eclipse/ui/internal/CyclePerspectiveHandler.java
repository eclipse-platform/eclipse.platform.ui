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

package org.eclipse.ui.internal;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IPerspectiveDescriptor;
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
public class CyclePerspectiveHandler extends CycleBaseHandler {
	private PerspectiveLabelProvider labelProvider = new PerspectiveLabelProvider(
            false);
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#addItems(org.eclipse.swt.widgets.Table, org.eclipse.ui.internal.WorkbenchPage)
	 */
	protected void addItems(Table table, WorkbenchPage page) {
		IPerspectiveDescriptor perspectives[] = page.getSortedPerspectives();
        for (int i = perspectives.length - 1; i >= 0; i--) {
            TableItem item = new TableItem(table, SWT.NONE);
            IPerspectiveDescriptor desc = perspectives[i];
            String text = labelProvider.getText(desc);
            if(text == null) {
				text = "";//$NON-NLS-1$
			}
            item.setText(text);
            item.setImage(labelProvider.getImage(desc));
            item.setData(desc);
        }

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#getBackwardCommand()
	 */
	protected ParameterizedCommand getBackwardCommand() {
		final ICommandService commandService = (ICommandService) window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand("org.eclipse.ui.window.previousPerspective"); //$NON-NLS-1$
		ParameterizedCommand commandBack = new ParameterizedCommand(command, null);
		return commandBack;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#getForwardCommand()
	 */
	protected ParameterizedCommand getForwardCommand() {
		final ICommandService commandService = (ICommandService) window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand("org.eclipse.ui.window.nextPerspective"); //$NON-NLS-1$
		ParameterizedCommand commandF = new ParameterizedCommand(command, null);
		return commandF;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#getTableHeader()
	 */
	protected String getTableHeader(IWorkbenchPart activePart) {
		return WorkbenchMessages.CyclePerspectiveAction_header;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#dispose()
	 */
	public void dispose() {
		if (labelProvider!=null) {
			labelProvider.dispose();
			labelProvider = null;
		}
		super.dispose();
	}
}
