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
package org.eclipse.team.ui.synchronize;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.ui.*;

/**
 * This action provides utilities for performing operations on selections that
 * are obtained from a view populated by a 
 * {@link org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider}.
 * The {@link org.eclipse.team.internal.ui.synchronize.SubscriberParticipantPage} is an example of such a view.
 * Subclasses can use this support to filter the selection in order to 
 * determine action enablement and generate the input for a {@link SynchronizeModelOperation}.
 * @see SyncInfo
 * @see SyncInfoSet
 * @see org.eclipse.team.ui.synchronize.viewers.SynchronizeModelProvider
 * @see org.eclipse.team.internal.ui.synchronize.SubscriberParticipantPage
 * @see org.eclipse.team.ui.synchronize.SynchronizeModelOperation
 * @since 3.0
 */
public abstract class SynchronizeModelAction implements IObjectActionDelegate, IViewActionDelegate, IEditorActionDelegate {
	
	private IStructuredSelection selection;
	private IWorkbenchPart part;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public final void run(IAction action) {
		// TODO: We used to prompt for unsaved changes in any editor. We don't anymore. Would
		// it be better to prompt for unsaved changes to editors affected by this action?
		try {
			getSubscriberOperation(part, getFilteredDiffElements()).run();
		} catch (InvocationTargetException e) {
			handle(e);
		} catch (InterruptedException e) {
			handle(e);
		}
	}

	/**
	 * Return the subscriber operation associated with this action. This operation
	 * will be run when the action is run. Subclass may implement this method and provide 
	 * an operation subclass or may override the <code>run(IAction)</code> method directly
	 * if they choose not to implement a <code>SynchronizeModelOperation</code>.
	 * @param elements the selected diff element for which this action is enabled.
	 * @return the subscriber operation to be run by this action.
	 */
	protected abstract SynchronizeModelOperation getSubscriberOperation(IWorkbenchPart part, IDiffElement[] elements);
	
	/** 
	 * Generic error handling code that uses an error dialog to show the error to the 
	 * user. Subclasses can use this method and/or override it.
	 * @param e the exception that occurred.
	 */
	protected void handle(Exception e) {
		Utils.handle(e);
	}
	
	/**
	 * This method returns all instances of IDiffElement that are in the current
	 * selection.
	 * 
	 * @return the selected elements
	 */
	protected final IDiffElement[] getSelectedDiffElements() {
		return Utils.getDiffNodes(selection.toArray());
	}

	/**
	 * The default enablement behavior for subscriber actions is to enable
	 * the action if there is at least one SyncInfo in the selection
	 * for which the action's filter passes.
	 */
	protected boolean isEnabled() {
		return (getFilteredDiffElements().length > 0);
	}

	/**
	 * Filter uses to filter the user selection to contain only those
	 * elements for which this action is enabled.
	 * Default filter includes all out-of-sync elements in the current
	 * selection. Subsclasses may override.
	 * @return a sync info filter which selects all out-of-sync resources.
	 */
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter();
	}

	/**
	 * Return the selected diff element for which this action is enabled.
	 * @return the list of selected diff elements for which this action is enabled.
	 */
	protected final IDiffElement[] getFilteredDiffElements() {
		IDiffElement[] elements = getSelectedDiffElements();
		List filtered = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			IDiffElement e = elements[i];
			if (e instanceof SyncInfoModelElement) {
				SyncInfo info = ((SyncInfoModelElement) e).getSyncInfo();
				if (info != null && getSyncInfoFilter().select(info)) {
					filtered.add(e);
				}
			}
		}
		return (IDiffElement[]) filtered.toArray(new IDiffElement[filtered.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.part = targetPart;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		this.part = view;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
			if (action != null) {
				setActionEnablement(action);
			}
		}
	}
	
	/**
	 * Method invoked from <code>selectionChanged(IAction, ISelection)</code> 
	 * to set the enablement status of the action. The instance variable 
	 * <code>selection</code> will contain the latest selection so the methods
	 * <code>getSelectedResources()</code> and <code>getSelectedProjects()</code>
	 * will provide the proper objects.
	 * 
	 * This method can be overridden by subclasses but should not be invoked by them.
	 */
	protected void setActionEnablement(IAction action) {
		action.setEnabled(isEnabled());
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// Ignore since these actions aren't meant for editors.
		// This seems to be required because of a bug in the UI 
		// plug-in that will disable viewer actions if they aren't
		// editor actions? Go figure...
	}
	
	/**
	 * Returns the workbench part assigned to this action or <code>null</code>.
	 * @return Returns the part.
	 */
	public IWorkbenchPart getPart() {
		return part;
	}
}