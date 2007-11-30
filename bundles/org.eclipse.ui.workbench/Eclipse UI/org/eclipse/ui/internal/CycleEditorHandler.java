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
import org.eclipse.ui.IEditorReference;
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
public class CycleEditorHandler extends CycleBaseHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#addItems(org.eclipse.swt.widgets.Table, org.eclipse.ui.internal.WorkbenchPage)
	 */
	protected void addItems(Table table, WorkbenchPage page) {
		// TODO Auto-generated method stub
        IEditorReference refs[] = page.getSortedEditors();
        for (int i = refs.length - 1; i >= 0; i--) {
            TableItem item = null;
            item = new TableItem(table, SWT.NONE);
            if (refs[i].isDirty()) {
				item.setText("*" + refs[i].getTitle()); //$NON-NLS-1$
			} else {
				item.setText(refs[i].getTitle());
			}
            item.setImage(refs[i].getTitleImage());
            item.setData(refs[i]);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#getBackwardCommand()
	 */
	protected ParameterizedCommand getBackwardCommand() {
		final ICommandService commandService = (ICommandService) window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand("org.eclipse.ui.window.previousEditor"); //$NON-NLS-1$
		ParameterizedCommand commandBack = new ParameterizedCommand(command, null);
		return commandBack;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#getForwardCommand()
	 */
	protected ParameterizedCommand getForwardCommand() {
		final ICommandService commandService = (ICommandService) window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand("org.eclipse.ui.window.nextEditor"); //$NON-NLS-1$
		ParameterizedCommand commandF = new ParameterizedCommand(command, null);
		return commandF;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#getTableHeader()
	 */
	protected String getTableHeader(IWorkbenchPart activePart) {
		// TODO Auto-generated method stub
		return WorkbenchMessages.CycleEditorAction_header;
	}

}
