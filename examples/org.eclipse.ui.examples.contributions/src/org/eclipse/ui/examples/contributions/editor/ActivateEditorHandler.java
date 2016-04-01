/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.examples.contributions.editor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.examples.contributions.model.PersonInput;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Activate an already open editor (although technically this would open a new
 * one as well)
 * 
 * @since 3.4
 */
public class ActivateEditorHandler extends AbstractHandler {
	public static final String ID = "org.eclipse.ui.examples.contributions.editor.activate"; //$NON-NLS-1$
	public static final String PARM_EDITOR = "org.eclipse.ui.examples.contributions.editor.activate.index"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		Object index = event.getObjectParameterForExecution(PARM_EDITOR);
		if (!(index instanceof Integer)) {
			throw new ExecutionException("Invalid index: " + index); //$NON-NLS-1$
		}
		PersonInput input = new PersonInput(((Integer) index).intValue());
		try {
			window.getActivePage().openEditor(input, InfoEditor.ID, true);
		} catch (PartInitException e) {
			throw new ExecutionException("Failed to activate editor", e); //$NON-NLS-1$
		}
		return null;
	}

}
