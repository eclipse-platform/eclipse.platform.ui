/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.views.IStickyViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * @since 3.3
 * 
 */
public class StickyViewManager implements IStickyViewManager {
	
	private IWorkbenchPage page;
	
	public StickyViewManager(IWorkbenchPage page) {
		this.page = page;
	}

	
	public static IStickyViewManager getInstance(IWorkbenchPage page) {
		IStickyViewManager stickyViewMan;
		IPreferenceStore preferenceStore = PrefUtil.getAPIPreferenceStore();
		boolean enable32Behavior = preferenceStore
				.getBoolean(IWorkbenchPreferenceConstants.ENABLE_32_STICKY_CLOSE_BEHAVIOR);
		if (enable32Behavior)
			stickyViewMan = new StickyViewManager32(page);
		else 
			stickyViewMan = new StickyViewManager(page);
		
		return stickyViewMan;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.IStickyViewManager#add(java.lang.String,
	 *      java.util.Set)
	 */
	public void add(String perspectiveId, Set stickyViewSet) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.IStickyViewManager#clear()
	 */
	public void clear() {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.IStickyViewManager#remove(java.lang.String)
	 */
	public void remove(String perspectiveId) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.IStickyViewManager#restore(org.eclipse.ui.IMemento)
	 */
	public void restore(IMemento memento) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.IStickyViewManager#save(org.eclipse.ui.IMemento)
	 */
	public void save(IMemento memento) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.IStickyViewManager#update(org.eclipse.ui.internal.Perspective,
	 *      org.eclipse.ui.internal.Perspective)
	 */
	public void update(Perspective oldPersp, Perspective newPersp) {
		if (oldPersp == null || newPersp == null) {
			return;
		}
		IViewRegistry viewReg = WorkbenchPlugin.getDefault().getViewRegistry();
		IStickyViewDescriptor[] stickyDescs = viewReg.getStickyViews();
		IViewReference[] oldPerspectiveViewReferences = oldPersp
				.getViewReferences();
		IViewReference[] newPerspectiveViewReferences = newPersp
				.getViewReferences();
		for (int i = 0; i < stickyDescs.length; i++) {
			final String viewId = stickyDescs[i].getId();
			try {
				for (int j = 0; j < oldPerspectiveViewReferences.length; j++) {
					String oldViewId = oldPerspectiveViewReferences[j].getId();
					// check to see if a sticky view was shown in the previous
					// perspective
					if (viewId.equals(oldViewId)) {
						String oldViewSecondaryId = oldPerspectiveViewReferences[j]
								.getSecondaryId();
						// materialize the same one in the new perspective
						page.showView(viewId, oldViewSecondaryId,
								IWorkbenchPage.VIEW_CREATE);
					}
				}

				for (int j = 0; j < newPerspectiveViewReferences.length; j++) {
					String newViewId = newPerspectiveViewReferences[j].getId();
					// check if a sticky view is being shown in the new
					// perspective
					if (viewId.equals(newViewId)) {
						String newViewSecondaryId = newPerspectiveViewReferences[j]
								.getSecondaryId();
						// if the original perspective didn't have it, that
						// means this sticky view has been hidden, hide it in
						// the new perspective also
						if (oldPersp.findView(newViewId, newViewSecondaryId) == null) {
							page.hideView(newPerspectiveViewReferences[j]);
						}
					}
				}
			} catch (PartInitException e) {
				WorkbenchPlugin
						.log(
								"Could not open view :" + viewId, new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, "Could not open view :" + viewId, e)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

}
