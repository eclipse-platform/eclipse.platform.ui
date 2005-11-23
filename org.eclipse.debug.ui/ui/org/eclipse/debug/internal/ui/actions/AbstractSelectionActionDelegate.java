/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.actions;

import java.util.Iterator;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public abstract class AbstractSelectionActionDelegate implements IViewActionDelegate, IActionDelegate2 {

	/**
	 * The underlying action for this delegate
	 */
	private IAction fAction;
	
	/**
	 * This action's view part, or <code>null</code> if not installed in a
	 * view.
	 */
	private IViewPart fViewPart;

	/**
	 * Cache of the most recent seletion
	 */
	private IStructuredSelection fSelection = StructuredSelection.EMPTY;

	/**
	 * Used to schedule jobs, or <code>null</code> if none
	 */
	private IWorkbenchSiteProgressService fProgressService = null;
	
	/**
	 * It's crucial that delegate actions have a zero-arg constructor so that
	 * they can be reflected into existence when referenced in an action set in
	 * the plugin's plugin.xml file.
	 */
	public AbstractSelectionActionDelegate() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		fSelection = null;
	}

	public void selectionChanged(IAction action, ISelection s) {
		if (s instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) s;
			action.setEnabled(getEnableStateForSelection(ss));
			setSelection(ss);
		} else {
			action.setEnabled(false);
			setSelection(StructuredSelection.EMPTY);
		}
	}

	/**
	 * Returns the String to use as an error dialog message for a failed action.
	 * This message appears as the "Message:" in the error dialog for this
	 * action. Default is to return null.
	 */
	protected String getErrorDialogMessage() {
		return null;
	}

	/**
	 * Returns the String to use as a status message for a failed action. This
	 * message appears as the "Reason:" in the error dialog for this action.
	 * Default is to return the empty String.
	 */
	protected String getStatusMessage() {
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		setView(view);
		fProgressService = (IWorkbenchSiteProgressService) view.getAdapter(IWorkbenchSiteProgressService.class);
	}

	/**
	 * Returns this action's view part, or <code>null</code> if not installed
	 * in a view.
	 * 
	 * @return view part or <code>null</code>
	 */
	protected IViewPart getView() {
		return fViewPart;
	}

	/**
	 * Returns the most recent selection
	 * 
	 * @return structured selection
	 */
	protected IStructuredSelection getSelection() {
		return fSelection;
	}

	/**
	 * Sets the most recent selection
	 * 
	 * @parm selection structured selection
	 */
	private void setSelection(IStructuredSelection context) {
		fSelection = context;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}

	protected IAction getAction() {
		return fAction;
	}

	protected void setView(IViewPart viewPart) {
		fViewPart = viewPart;
	}

	/**
	 * Return whether the action should be enabled or not based on the given
	 * selection.
	 */
	protected boolean getEnableStateForSelection(IStructuredSelection selection) {
		if (selection.size() == 0) {
			return false;
		}
		Iterator itr = selection.iterator();
		while (itr.hasNext()) {
			Object element = itr.next();
			if (!isEnabledFor(element)) {
				return false;
			}
		}
		return true;
	}

	protected boolean isEnabledFor(Object element) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		setAction(action);
	}

	/**
	 * Schedules the given job with this action's progress service
	 * 
	 * @param job
	 */
	protected void schedule(Job job) {
		if (fProgressService == null) {
			job.schedule();
		} else {
			fProgressService.schedule(job);
		}
	}
}
