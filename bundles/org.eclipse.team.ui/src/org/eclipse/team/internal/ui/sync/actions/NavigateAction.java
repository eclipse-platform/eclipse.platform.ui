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
package org.eclipse.team.internal.ui.sync.actions;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ICompareNavigator;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.sync.views.INavigableControl;
import org.eclipse.team.internal.ui.sync.views.SynchronizeView;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionContext;

/**
 * Action to navigate the changes shown in the Synchronize View. This
 * will coordinate change browsing between the view and the compare 
 * editors.
 *
 * @since 3.0
 */
class NavigateAction extends Action {
	private final SyncViewerActions actions;
	private final SynchronizeView synchronizeView;
	private final int direction;
	
	public NavigateAction(SyncViewerActions actions, int direction) {
		this.actions = actions;
		this.synchronizeView = actions.getSyncView();
		this.direction = direction;

		IKeyBindingService kbs = synchronizeView.getSite().getKeyBindingService();		
		if(direction == INavigableControl.NEXT) {
			Utils.initAction(this, "action.navigateNext."); //$NON-NLS-1$
			Utils.registerAction(kbs, this, "org.eclipse.team.ui.syncview.selectNextChange");	//$NON-NLS-1$
			synchronizeView.getViewSite().getActionBars().setGlobalActionHandler(IWorkbenchActionConstants.NEXT, this);			
		} else {
			Utils.initAction(this, "action.navigatePrevious."); //$NON-NLS-1$
			Utils.registerAction(kbs, this, "org.eclipse.team.ui.syncview.selectPreviousChange");	//$NON-NLS-1$
			synchronizeView.getViewSite().getActionBars().setGlobalActionHandler(IWorkbenchActionConstants.PREVIOUS, this);			
		}
	}
	
	public void run() {
		navigate();
	}
	
	private ISelection getSelection() {
		ActionContext context = actions.getContext();
		if (context == null) return null;
		return actions.getContext().getSelection();
	}
	
	private void navigate() {
		Viewer viewer = synchronizeView.getViewer();	
	
		if(viewer instanceof INavigableControl) {			
			SyncInfo info = getSyncInfoFromSelection();
		
			if(info == null) {
				if(((INavigableControl)viewer).gotoDifference(direction)) {
					return;
				} else {
					info = getSyncInfoFromSelection();
					if(info == null) return;
				}
			}
			
			if(info.getLocal().getType() != IResource.FILE) {
				if(! ((INavigableControl)viewer).gotoDifference(direction)) {
					info = getSyncInfoFromSelection();
					OpenInCompareAction.openCompareEditor(synchronizeView, info, true /* keep focus */);
				}
				return;
			}
		
			IEditorPart editor = OpenInCompareAction.findOpenCompareEditor(synchronizeView.getSite(), info.getLocal());			
			boolean atEnd = false;
			CompareEditorInput input;
			ICompareNavigator navigator;
		
			if(editor != null) {
				// if an existing editor is open on the current selection, use it			 
				input = (CompareEditorInput)editor.getEditorInput();
				navigator = (ICompareNavigator)input.getAdapter(ICompareNavigator.class);
				if(navigator != null) {
					if(navigator.selectChange(direction == INavigableControl.NEXT)) {
						if(! ((INavigableControl)viewer).gotoDifference(direction)) {
							info = getSyncInfoFromSelection();
							OpenInCompareAction.openCompareEditor(synchronizeView, info, true /* keep focus */);
						}
					}				
				}
			} else {
				// otherwise, select the next change and open a compare editor which will automatically
				// show the first change.
				OpenInCompareAction.openCompareEditor(synchronizeView, info, true /* keep focus */);
			}
		}
		return;
	}

	private SyncInfo getSyncInfoFromSelection() {
		IStructuredSelection selection = ((IStructuredSelection)getSelection());
		if(selection == null) return null;
		Object obj = selection.getFirstElement();
		SyncInfo info = OpenInCompareAction.getSyncInfo(obj);
		return info;
	}
}