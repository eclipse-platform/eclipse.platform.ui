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

import org.eclipse.e4.tools.compat.parts.DIEditorPart;
import org.eclipse.e4.tools.emf.ui.common.IModelResource.ModelListener;
import org.eclipse.e4.tools.emf.ui.internal.wbm.ApplicationModelEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.actions.ActionFactory;

@SuppressWarnings("restriction")
public class E4WorkbenchModelEditor extends
	DIEditorPart<ApplicationModelEditor> {
	private UndoAction undoAction;
	private RedoAction redoAction;

	private final ModelListener listener = new ModelListener() {

		@Override
		public void dirtyChanged() {
			firePropertyChange(PROP_DIRTY);
		}

		@Override
		public void commandStackChanged() {

		}
	};

	public E4WorkbenchModelEditor() {
		super(ApplicationModelEditor.class, COPY | CUT | PASTE);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		setPartName(getEditorInput().getName());
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		undoAction = new UndoAction(getComponent().getModelProvider());
		undoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_UNDO);

		redoAction = new RedoAction(getComponent().getModelProvider());
		redoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_REDO);

		getEditorSite().getActionBars().setGlobalActionHandler(
			ActionFactory.UNDO.getId(), undoAction);
		getEditorSite().getActionBars().setGlobalActionHandler(
			ActionFactory.REDO.getId(), redoAction);
	}

	@Override
	public void dispose() {
		if (undoAction != null) {
			undoAction.dispose();
		}

		if (redoAction != null) {
			redoAction.dispose();
		}

		if (listener != null && getComponent() != null && getComponent().getModelProvider() != null) {
			getComponent().getModelProvider().removeModelListener(listener);
		}

		super.dispose();
	}
}
