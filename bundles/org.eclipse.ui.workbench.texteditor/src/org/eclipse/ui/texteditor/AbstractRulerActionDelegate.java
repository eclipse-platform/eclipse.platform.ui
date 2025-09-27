/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/


package org.eclipse.ui.texteditor;


import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.jface.text.source.IVerticalRulerInfo;

import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * This class serves as an adapter for actions contributed to the vertical ruler's
 * context menu. This adapter provides the contributed actions access to their editor
 * and the editor's vertical ruler. These actions gain only limited access to the vertical
 * ruler as defined by <code>IVerticalRulerInfo</code>.  The adapter updates the
 * adapter (inner) action on menu and mouse action on the vertical ruler.<p>
 * Extending classes must implement the factory method
 * <code>createAction(ITextEditor editor, IVerticalRulerInfo)</code>.
 *
 * @since 2.0
 */
public abstract class AbstractRulerActionDelegate extends ActionDelegate implements IEditorActionDelegate, MouseListener, IMenuListener {

	/** The editor. */
	private ITextEditor fEditor;
	/** The action calling the action delegate. */
	private IAction fCallerAction;
	/** The underlying action. */
	private IAction fAction;

	/**
	 * The factory method creating the underlying action.
	 *
	 * @param editor the editor the action to be created will work on
	 * @param rulerInfo the vertical ruler the action to be created will work on
	 * @return the created action
	 */
	protected abstract IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo);


	@Override
	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
		if (fEditor != null) {
			IVerticalRulerInfo rulerInfo= fEditor.getAdapter(IVerticalRulerInfo.class);
			if (rulerInfo != null) {
				Control control= rulerInfo.getControl();
				if (control != null && !control.isDisposed()) {
					control.removeMouseListener(this);
				}
			}

			if (fEditor instanceof ITextEditorExtension) {
				((ITextEditorExtension) fEditor).removeRulerContextMenuListener(this);
			}
		}

		fEditor= targetEditor == null ? null : targetEditor.getAdapter(ITextEditor.class);
		fCallerAction= callerAction;
		fAction= null;

		if (fEditor != null) {
			if (fEditor instanceof ITextEditorExtension) {
				((ITextEditorExtension) fEditor).addRulerContextMenuListener(this);
			}

			IVerticalRulerInfo rulerInfo= fEditor.getAdapter(IVerticalRulerInfo.class);
			if (rulerInfo != null) {
				fAction= createAction(fEditor, rulerInfo);
				update();

				Control control= rulerInfo.getControl();
				if (control != null && !control.isDisposed()) {
					control.addMouseListener(this);
				}
			}
		}
	}

	@Override
	public void run(IAction callerAction) {
		if (fAction != null) {
			fAction.run();
		}
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 * @since 3.2
	 */
	@Override
	public void runWithEvent(IAction action, Event event) {
		if (fAction != null) {
			fAction.runWithEvent(event);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		/*
		 * This is a ruler action - don't update on selection.
		 * The call was introduced to support annotation roll-overs
		 * but this seems not to be needed.
		 */
//		update();
	}

	/**
	 * Updates to the current state.
	 */
	private void update() {
		if (fAction instanceof IUpdate) {
			((IUpdate) fAction).update();
			if (fCallerAction != null) {
				fCallerAction.setText(fAction.getText());
				fCallerAction.setEnabled(fAction.isEnabled());
			}
		}
	}

	@Override
	public void menuAboutToShow(IMenuManager manager) {
		update();
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
	}

	@Override
	public void mouseDown(MouseEvent e) {
		update();
	}

	@Override
	public void mouseUp(MouseEvent e) {
	}
}
