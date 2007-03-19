/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.IStickyViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * @since 3.3
 *
 */
class StickyViewManager32 implements IStickyViewManager {

	// a mapping of perspectives to a set of stickyviews that have been activated in that perspective.
	// this map is persisted across sessions
	private Map stickyPerspectives = new HashMap(7);
	
	private IWorkbenchPage page;
	
	public StickyViewManager32(IWorkbenchPage page) {
		this.page = page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IStickyViewManager#remove(java.lang.String)
	 */
	public void remove(String pespectiveId) {
		stickyPerspectives.remove(pespectiveId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IStickyViewManager#add(java.lang.String, java.util.Set)
	 */
	public void add(String pespectiveId, Set stickyViewList) {
		stickyPerspectives.put(pespectiveId, stickyViewList);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IStickyViewManager#clear()
	 */
	public void clear() {
		stickyPerspectives.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IStickyViewManager#update(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.internal.Perspective, org.eclipse.ui.internal.Perspective)
	 */
	public void update(Perspective oldPersp, Perspective newPersp) {
		if (newPersp != null && oldPersp != null) {
			Set activatedStickyViewsInThisPerspective = (Set) stickyPerspectives
					.get(newPersp.getDesc().getId());
			if (activatedStickyViewsInThisPerspective == null) {
				activatedStickyViewsInThisPerspective = new HashSet(7);
				stickyPerspectives.put(newPersp.getDesc().getId(),
						activatedStickyViewsInThisPerspective);
			}
			IViewRegistry viewReg = WorkbenchPlugin.getDefault()
					.getViewRegistry();
			IStickyViewDescriptor[] stickyDescs = viewReg.getStickyViews();
			for (int i = 0; i < stickyDescs.length; i++) {
				final String viewId = stickyDescs[i].getId();
				try {
					// show a sticky view if it was in the last perspective and hasn't already been activated in this one
					if (oldPersp.findView(viewId) != null
							&& !activatedStickyViewsInThisPerspective
									.contains(viewId)) {
						page.showView(viewId, null, IWorkbenchPage.VIEW_CREATE);
						activatedStickyViewsInThisPerspective.add(viewId);
					}
				} catch (PartInitException e) {
					WorkbenchPlugin
							.log(
									"Could not open view :" + viewId, new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, "Could not open view :" + viewId, e)); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IStickyViewManager#save(org.eclipse.ui.IMemento)
	 */
	public void save(IMemento memento) {
		IMemento stickyState = memento.createChild(IWorkbenchConstants.TAG_STICKY_STATE);
		Iterator itr = stickyPerspectives.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry entry = (Map.Entry) itr.next();
			String perspectiveId = (String) entry.getKey();
			Set activatedViewIds = (Set) entry.getValue();
			IMemento perspectiveState = stickyState.createChild(
					IWorkbenchConstants.TAG_PERSPECTIVE, perspectiveId);
			for (Iterator i = activatedViewIds.iterator(); i.hasNext();) {
				String viewId = (String) i.next();
				perspectiveState.createChild(IWorkbenchConstants.TAG_VIEW,
						viewId);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IStickyViewManager#restore(org.eclipse.ui.IMemento)
	 */
	public void restore(IMemento memento) {
		// restore the sticky activation state
		IMemento stickyState = memento
				.getChild(IWorkbenchConstants.TAG_STICKY_STATE);

		if (stickyState != null) {
			IMemento[] stickyPerspMems = stickyState
					.getChildren(IWorkbenchConstants.TAG_PERSPECTIVE);
			for (int i = 0; i < stickyPerspMems.length; i++) {
				String perspectiveId = stickyPerspMems[i].getID();
				Set viewState = new HashSet(7);
				stickyPerspectives.put(perspectiveId, viewState);
				IMemento[] viewStateMementos = stickyPerspMems[i]
						.getChildren(IWorkbenchConstants.TAG_VIEW);
				for (int j = 0; j < viewStateMementos.length; j++) {
					viewState.add(viewStateMementos[j].getID());
				}
			}
		}
	}
}
