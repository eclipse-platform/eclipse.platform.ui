/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.List;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
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
public class CycleEditorHandler extends CycleBaseHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#addItems(org.eclipse.swt.widgets.Table, org.eclipse.ui.internal.WorkbenchPage)
	 */
	@Override
	protected void addItems(Table table, WorkbenchPage page) {
		List<EditorReference> refs = page.getSortedEditorReferences();
		for (EditorReference ref : refs) {
            TableItem item = null;
            item = new TableItem(table, SWT.NONE);
			if (ref.isDirty()) {
				item.setText("*" + ref.getTitle()); //$NON-NLS-1$
			} else {
				item.setText(ref.getTitle());
			}
			item.setImage(ref.getTitleImage());
			item.setData(ref);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#getBackwardCommand()
	 */
	@Override
	protected ParameterizedCommand getBackwardCommand() {
		final ICommandService commandService = (ICommandService) window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand(IWorkbenchCommandConstants.WINDOW_PREVIOUS_EDITOR);
		ParameterizedCommand commandBack = new ParameterizedCommand(command, null);
		return commandBack;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#getForwardCommand()
	 */
	@Override
	protected ParameterizedCommand getForwardCommand() {
		final ICommandService commandService = (ICommandService) window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand(IWorkbenchCommandConstants.WINDOW_NEXT_EDITOR);
		ParameterizedCommand commandF = new ParameterizedCommand(command, null);
		return commandF;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.CycleBaseHandler#getTableHeader()
	 */
	@Override
	protected String getTableHeader(IWorkbenchPart activePart) {
		// TODO Auto-generated method stub
		return WorkbenchMessages.CycleEditorAction_header;
	}

}
