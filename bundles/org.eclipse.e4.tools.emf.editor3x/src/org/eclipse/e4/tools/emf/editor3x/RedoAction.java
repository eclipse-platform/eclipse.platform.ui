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
			setText(Messages.RedoAction_Redo + " " //$NON-NLS-1$
				+ resource.getEditingDomain().getCommandStack()
					.getRedoCommand().getLabel());
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
