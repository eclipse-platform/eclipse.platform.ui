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
package org.eclipse.team.internal.ui.synchronize.actions;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ICompareNavigator;
import org.eclipse.compare.internal.INavigatable;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Action to navigate the changes shown in the Synchronize View. This
 * will coordinate change browsing between the view and the compare 
 * editors.
 *
 * @since 3.0
 */
public class NavigateAction extends Action {
	private final boolean next;
	private INavigatable navigator;
	private ISynchronizePageSite site;
	private String title;
	
	/**
	 * Direction to navigate
	 */
	final public static int NEXT = 1;
	final public static int PREVIOUS = 2;
	
	public NavigateAction(ISynchronizePageSite site, String title, INavigatable navigator, boolean next) {
		this.site = site;
		this.title = title;
		this.navigator = navigator;
		this.next = next;
		IWorkbenchSite workbenchSite = site.getWorkbenchSite();
		IViewSite viewSite = null;
		if (workbenchSite instanceof IViewSite) {
			viewSite = (IViewSite)workbenchSite;
		}
		if (next) {
			Utils.initAction(this, "action.navigateNext."); //$NON-NLS-1$
			if (viewSite != null)
				viewSite.getActionBars().setGlobalActionHandler(ActionFactory.NEXT.getId(), this);
		} else {
			Utils.initAction(this, "action.navigatePrevious."); //$NON-NLS-1$
			if (viewSite != null)
				viewSite.getActionBars().setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), this);
		}
	}
	
	public void run() {
		IWorkbenchSite ws = site.getWorkbenchSite();
		if (ws instanceof IWorkbenchPartSite) {
			navigate();
		} else {
			navigator.gotoDifference(next);
		}
	}
	
	private void navigate() {
		SyncInfo info = getSyncInfoFromSelection();
		if(info == null) {
			if(navigator.gotoDifference(next)) {
				return;
			} else {
				info = getSyncInfoFromSelection();
				if(info == null) return;
			}
		}
		
		if(info.getLocal().getType() != IResource.FILE) {
			if(! navigator.gotoDifference(next)) {
				info = getSyncInfoFromSelection();
				OpenInCompareAction.openCompareEditor(site, getTitle(), info, true /* keep focus */);
			}
			return;
		}
		
		IWorkbenchSite ws = site.getWorkbenchSite();
		if (ws instanceof IWorkbenchPartSite) {
			IEditorPart editor = OpenInCompareAction.findOpenCompareEditor((IWorkbenchPartSite)ws, info.getLocal());
			CompareEditorInput input;
			ICompareNavigator navigator;
			
			if(editor != null) {
				// if an existing editor is open on the current selection, use it			 
				input = (CompareEditorInput)editor.getEditorInput();
				navigator = (ICompareNavigator)input.getAdapter(ICompareNavigator.class);
				if(navigator != null) {
					if(navigator.selectChange(next)) {
						if(! this.navigator.gotoDifference(next)) {
							info = getSyncInfoFromSelection();
							OpenInCompareAction.openCompareEditor(site, getTitle(), info, true /* keep focus */);
						}
					}				
				}
			} else {
				// otherwise, select the next change and open a compare editor which will automatically
				// show the first change.
				OpenInCompareAction.openCompareEditor(site, getTitle(), info, true /* keep focus */);
			}
		}
	}

	private SyncInfo getSyncInfoFromSelection() {
		IStructuredSelection selection = (IStructuredSelection)site.getSelectionProvider().getSelection();
		if(selection == null) return null;
		Object obj = selection.getFirstElement();
		if (obj instanceof SyncInfoModelElement) {
			return ((SyncInfoModelElement) obj).getSyncInfo();
		} else {
			return null;
		}
	}
	
	private String getTitle() {
		return title;
	}
}