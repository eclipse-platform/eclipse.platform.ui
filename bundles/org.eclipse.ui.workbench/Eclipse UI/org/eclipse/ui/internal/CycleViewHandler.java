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
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.commands.ICommandService;

/**
 * This handler is used to switch between parts using the keyboard.
 * <p>
 * Replacement for CyclePartAction
 * </p>
 * 
 * @since 3.3
 *
 */
public class CycleViewHandler extends CycleBaseHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#addItems(org.eclipse.swt.widgets.Table, org.eclipse.ui.internal.WorkbenchPage)
	 */
	protected void addItems(Table table, WorkbenchPage page) {
		// TODO Auto-generated method stub
		IWorkbenchPartReference refs[] = page.getSortedParts();
		boolean includeEditor = true;

		for (int i = refs.length - 1; i >= 0; i--) {
			if (refs[i] instanceof IEditorReference) {
				if (includeEditor) {
					IEditorReference activeEditor = (IEditorReference) refs[i];
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(WorkbenchMessages.CyclePartAction_editor);
					item.setImage(activeEditor.getTitleImage());
					item.setData(activeEditor);
					includeEditor = false;
				}
			} else {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(refs[i].getTitle());
				item.setImage(refs[i].getTitleImage());
				item.setData(refs[i]);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#getBackwardCommand()
	 */
	protected ParameterizedCommand getBackwardCommand() {
		// TODO Auto-generated method stub
		final ICommandService commandService = (ICommandService) window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand("org.eclipse.ui.window.previousView"); //$NON-NLS-1$
		ParameterizedCommand commandBack = new ParameterizedCommand(command, null);
		return commandBack;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#getForwardCommand()
	 */
	protected ParameterizedCommand getForwardCommand() {
		// TODO Auto-generated method stub
		final ICommandService commandService = (ICommandService) window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand("org.eclipse.ui.window.nextView"); //$NON-NLS-1$
		ParameterizedCommand commandF = new ParameterizedCommand(command, null);
		return commandF;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#getTableHeader()
	 */
	protected String getTableHeader() {
		// TODO Auto-generated method stub
		return WorkbenchMessages.CyclePartAction_header;
	}

}
