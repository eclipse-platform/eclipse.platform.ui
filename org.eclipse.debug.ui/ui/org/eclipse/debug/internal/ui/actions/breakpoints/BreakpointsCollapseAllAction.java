/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
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
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.handlers.CollapseAllHandler;

/**
 * 
 */
public class BreakpointsCollapseAllAction implements IViewActionDelegate, IActionDelegate2, IViewerUpdateListener, IModelChangedListener  {
	
	private AbstractDebugView fView;
	
	private IAction fAction;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		fView = (AbstractDebugView) view;
        IInternalTreeModelViewer viewer = (IInternalTreeModelViewer)fView.getViewer();
        if (viewer != null) {
            viewer.addViewerUpdateListener(this);
            viewer.addModelChangedListener(this);
        }		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		((TreeViewer) fView.getViewer()).collapseAll();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
        ITreeModelViewer viewer = (ITreeModelViewer)fView.getViewer();
        if (viewer != null) {
            viewer.removeViewerUpdateListener(this);
            viewer.removeModelChangedListener(this);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		fAction = action;
		action.setActionDefinitionId(CollapseAllHandler.COMMAND_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

    public void viewerUpdatesBegin() {
    }

    public void viewerUpdatesComplete() {
    }

    public void updateStarted(IViewerUpdate update) {
    }
    
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
    
    public void modelChanged(IModelDelta delta, IModelProxy proxy) {
        update();
    }	
}
