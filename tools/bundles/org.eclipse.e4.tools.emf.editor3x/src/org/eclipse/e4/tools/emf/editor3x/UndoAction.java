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

import java.util.Collection;

import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.DeleteCommand;
import org.eclipse.emf.edit.command.SetCommand;
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
			Command undoCommand = resource.getEditingDomain().getCommandStack()
					.getUndoCommand();
			String label = getCommandLabel(undoCommand);
			setText(Messages.UndoAction_Undo + " " //$NON-NLS-1$
					+ label);
			setEnabled(true);
		} else {
			setText(Messages.UndoAction_Undo);
			setEnabled(false);
		}
	}

	/**
	 * Compute a command label depending on the command for undo/redo label.
	 *
	 * For instance : Add Trimmedwindow or Delete 5 objects
	 *
	 * @return a description string for the command
	 */
	public static String getCommandLabel(Command cmd) {

		if (cmd instanceof SetCommand) {
			SetCommand sc = (SetCommand) cmd;
			return sc.getLabel() + " " + sc.getFeature().getName() + " on " + sc.getOwner().eClass().getName(); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (cmd instanceof AddCommand) {
			AddCommand ac = (AddCommand) cmd;
			return ac.getLabel() + " " + getFirstClassName(ac.getCollection()); //$NON-NLS-1$
		} else if (cmd instanceof DeleteCommand) {
			DeleteCommand dc = (DeleteCommand) cmd;
			Collection<?> deleted = dc.getCollection();
			if (deleted.size() == 1) {
				return dc.getLabel() + " " + getFirstClassName(deleted); //$NON-NLS-1$
			}
			return dc.getLabel() + " " + dc.getCollection().size() + " Objects"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return cmd.getLabel();
	}

	/**
	 * Get the eClassname or empty string of the first element in the collection
	 *
	 * @return name of classname if object is an EObject else an empty string
	 */
	private static String getFirstClassName(Collection<?> c)
	{
		Object o = c.iterator().next();
		String clname = (o instanceof EObject) ? ((EObject) o).eClass().getName() : ""; //$NON-NLS-1$
		String dname = (o instanceof MUILabel) ? ((MUILabel) o).getLabel() : ""; //$NON-NLS-1$
		return clname + " " + (dname == null ? "" : dname); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Clean up
	 */
	public void dispose() {
		resource.removeModelListener(listener);
	}
}