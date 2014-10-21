/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.jface.action.Action;

public class UndoAction extends Action {
	private final IModelResource resource;
	private final IModelResource.ModelListener listener;

	public UndoAction(IModelResource resource) {
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
		if (resource.getEditingDomain().getCommandStack().canUndo()) {
			resource.getEditingDomain().getCommandStack().undo();
		}
	}

	private void update() {
		if (resource.getEditingDomain().getCommandStack().canUndo()) {
			setText(Messages.UndoAction_Undo + " " //$NON-NLS-1$
				+ resource.getEditingDomain().getCommandStack()
					.getUndoCommand().getLabel());
			setEnabled(true);
		} else {
			setText(Messages.UndoAction_Undo);
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