/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The abstract superclass for save actions that depend on the active editor.
 */
public abstract class BaseSaveAction extends ActiveEditorAction {
	private final IPropertyListener propListener = new IPropertyListener() {
		public void propertyChanged(Object source, int propId) {
			if (source == getActiveEditor()) {
				if (propId == IEditorPart.PROP_DIRTY)
					updateState();
			}
		}
	};
	
	/**
	 * Creates a new action with the given text.
	 *
	 * @param text the string used as the text for the action, 
	 *   or <code>null</code> if there is no text
	 * @param window the workbench window this action is
	 *   registered with.
	 */
	protected BaseSaveAction(String text, IWorkbenchWindow window) {
		super(text, window);
	}
	
	/* (non-Javadoc)
	 * Method declared on ActiveEditorAction.
	 */
	protected void editorActivated(IEditorPart part) {
		if (part != null)
			part.addPropertyListener(propListener);
	}
	
	/* (non-Javadoc)
	 * Method declared on ActiveEditorAction.
	 */
	protected void editorDeactivated(IEditorPart part) {
		if (part != null)
			part.removePropertyListener(propListener);
	}
	




/*
 * **********************************************************************************
 * **********************************************************************************
 * **********************************************************************************
 * The code below was added to track the view with focus
 * in order to support save actions from a view. Remove this
 * experimental code if the decision is to not allow views to 
 * participate in save actions (see bug 10234) 
 */
	private IViewPart activeView;
	private final IPropertyListener propListener2 = new IPropertyListener() {
		public void propertyChanged(Object source, int propId) {
			if (source == activeView) {
				if (propId == IEditorPart.PROP_DIRTY)
					updateState();
			}
		}
	};
	
	/* (non-Javadoc)
	 * Method declared on PageEventAction.
	 */
	public void pageActivated(IWorkbenchPage page) {
		super.pageActivated(page);
		updateActiveView();
		updateState();
	}
	/* (non-Javadoc)
	 * Method declared on PageEventAction.
	 */
	public void pageClosed(IWorkbenchPage page) {
		super.pageClosed(page);
		updateActiveView();
		updateState();
	}
	/* (non-Javadoc)
	 * Method declared on PartEventAction.
	 */
	public void partActivated(IWorkbenchPart part) {
		super.partActivated(part);
		if (part instanceof IViewPart) {
			updateActiveView();
			updateState();
		}
	}
	/* (non-Javadoc)
	 * Method declared on PartEventAction.
	 */
	public void partClosed(IWorkbenchPart part) {
		super.partClosed(part);
		if (part instanceof IViewPart) {
			updateActiveView();
			updateState();
		}
	}
	/* (non-Javadoc)
	 * Method declared on PartEventAction.
	 */
	public void partDeactivated(IWorkbenchPart part) {
		super.partDeactivated(part);
		if (part instanceof IViewPart) {
			updateActiveView();
			updateState();
		}
	}
	/**
	 * Update the active view based on the current
	 * active page.
	 */
	private void updateActiveView() {
		if (getActivePage() == null)
			setActiveView(null);
		else
			setActiveView(getActivePage().getActivePart());
	}
	/**
	 * Set the active editor
	 */
	private void setActiveView(IWorkbenchPart part) {
		if (activeView == part)
			return;
		if (activeView != null)
			activeView.removePropertyListener(propListener2);
		if (part instanceof IViewPart)
			activeView = (IViewPart)part;
		else
			activeView = null;
		if (activeView != null)
			activeView.addPropertyListener(propListener2);
	}
	protected final ISaveablePart getSaveableView() {
		if (activeView == null)
			return null;
		if (activeView instanceof ISaveablePart)
			return (ISaveablePart)activeView;
		return (ISaveablePart)activeView.getAdapter(ISaveablePart.class);
	}
}
