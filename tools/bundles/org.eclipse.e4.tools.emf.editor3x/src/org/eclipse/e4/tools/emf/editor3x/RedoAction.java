/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.emf.common.command.Command;
import org.eclipse.jface.action.Action;

public class RedoAction extends Action {
	private final IModelResource resource;
	private final IModelResource.ModelListener listener;

	public RedoAction(IModelResource resource) {
		this.resource = resource;
		listener = new IModelResource.ModelListener() {

			@Override
			public void commandStackChanged() {
				update();
			}

			@Override
			public void dirtyChanged() {

			}

		};
		resource.addModelListener(listener);
		update();
	}

	@Override
	public void run() {
		if (resource.getEditingDomain().getCommandStack().canRedo()) {
			resource.getEditingDomain().getCommandStack().redo();
		}
	}

	private void update() {
		if (resource.getEditingDomain().getCommandStack().canRedo()) {

			Command redoCommand = resource.getEditingDomain().getCommandStack().getRedoCommand();
			String label = UndoAction.getCommandLabel(redoCommand);

			setText(Messages.RedoAction_Redo + " " //$NON-NLS-1$
					+ label);
			setEnabled(true);
		} else {
			setText(Messages.RedoAction_Redo);
			setEnabled(false);
		}
	}

	/**
	 * Clean up
	 */
	public void dispose() {
		resource.removeModelListener(listener);
	}
}
