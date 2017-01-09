/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Improve usability of the breakpoint view (Bug 238956)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpoints;

import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action which fully expands the tree in the breakpoints view.
 */
public class BreakpointsExpandAllAction implements IViewActionDelegate, IActionDelegate2, IViewerUpdateListener, IModelChangedListener {

    private IAction fAction;
	private BreakpointsView fView;

    @Override
	public void init(IAction action) {
        fAction = action;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		fView = (BreakpointsView) view;
		IInternalTreeModelViewer viewer = (IInternalTreeModelViewer)fView.getViewer();
		if (viewer != null) {
		    viewer.addViewerUpdateListener(this);
		    viewer.addModelChangedListener(this);
		}
	}

    @Override
	public void runWithEvent(IAction action, Event event) {
        run(action);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
	    fView.expandAllElementsInViewer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}


    @Override
	public void dispose() {
        ITreeModelViewer viewer = (ITreeModelViewer)fView.getViewer();
        if (viewer != null) {
            viewer.removeViewerUpdateListener(this);
            viewer.removeModelChangedListener(this);
        }
    }

    @Override
	public void viewerUpdatesBegin() {
    }

    @Override
	public void viewerUpdatesComplete() {
    }

    @Override
	public void updateStarted(IViewerUpdate update) {
    }

    @Override
	public void updateComplete(IViewerUpdate update) {
          if (!update.isCanceled()) {
              if (TreePath.EMPTY.equals(update.getElementPath())) {
                  update();
              }
          }
    }

    private void update() {
        IInternalTreeModelViewer viewer = (IInternalTreeModelViewer)fView.getViewer();
        if (viewer != null && fAction != null) {
            fAction.setEnabled(viewer.getInput() != null && viewer.getChildCount(TreePath.EMPTY) > 0);
        }
    }

    @Override
	public void modelChanged(IModelDelta delta, IModelProxy proxy) {
        update();
    }
}
