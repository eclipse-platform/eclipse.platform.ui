/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial API and implementation
**********************************************************************/
package org.eclipse.ui.texteditor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * This class serves as an adapter for ruler actions.
 * Extending classes must implement the factory method
 * <code>createAction(ITextEditor editor, IVerticalRulerInfo)</code>.
 */
public abstract class AbstractRulerActionDelegate implements IEditorActionDelegate {

	private Map fMap= new HashMap();
	private IEditorPart fEditor;

	private static class Updater implements MouseListener {
		private IAction fCallerAction;
		private IAction fAction;
		
		public Updater(IAction callerAction, IAction action) {
			fCallerAction= callerAction;
			fAction= action;
		}
		public IAction getAction() {
			return fAction;	
		}
		public void mouseDoubleClick(MouseEvent e) {
		}
		public void mouseDown(MouseEvent e) {
			update();
		}
		public void mouseUp(MouseEvent e) {
		}
		public void update() {
			if (fAction != null && fAction instanceof IUpdate) {
				((IUpdate) fAction).update();
				if (fCallerAction != null) {
					fCallerAction.setText(fAction.getText());
					fCallerAction.setEnabled(fAction.isEnabled());
				}
			}
		}
	};

	/*
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {

		if (fEditor != null) {
			IVerticalRulerInfo rulerInfo= (IVerticalRulerInfo) fEditor.getAdapter(IVerticalRulerInfo.class);
			if (rulerInfo != null) {
				Updater updater= (Updater) fMap.get(fEditor);
				if (updater != null) {
					Control control= rulerInfo.getControl();
					if (!control.isDisposed()) {
						control.removeMouseListener(updater);	
					}
				}
			}
		}

		fEditor= targetEditor;

		if (fEditor != null && fEditor instanceof ITextEditor) {
			IVerticalRulerInfo rulerInfo= (IVerticalRulerInfo) fEditor.getAdapter(IVerticalRulerInfo.class);
			if (rulerInfo != null) {
				Updater updater= (Updater) fMap.get(fEditor);			
				if (updater == null) {			

					IAction action= createAction((ITextEditor) fEditor, rulerInfo);
					updater= new Updater(callerAction, action);
					updater.update();
	
					fMap.put(fEditor, updater);
				}

				Control control= rulerInfo.getControl();
				if (!control.isDisposed()) {
					control.addMouseListener(updater);
				}
			}			
		}
	}

	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction callerAction) {
		if (fEditor != null) {
			Updater updater= (Updater) fMap.get(fEditor);
			if (updater != null) {
				IAction action= updater.getAction();	
				action.run();
			}
		}
	}

	/*
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * The factory method creating the underlying action.
	 */
	protected abstract IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo);

}
