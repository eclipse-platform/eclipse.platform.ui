/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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

import org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action which fully expands the tree in the breakpoints view.
 */
public class BreakpointsExpandAllAction implements IViewActionDelegate {	
	
	private AbstractDebugView fView;

	private boolean fFinishedExpanding;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		fView = (AbstractDebugView) view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
	    Display display = fView.getSite().getShell().getDisplay(); 
	    
	    VirtualTreeModelViewer virtualViewer = new VirtualTreeModelViewer(
	        display, 0, ((ITreeModelViewer)fView.getViewer()).getPresentationContext());
	    
	    fFinishedExpanding = false;
	    virtualViewer.setAutoExpandLevel(-1);
	    virtualViewer.addViewerUpdateListener(new IViewerUpdateListener() {
            public void viewerUpdatesComplete() {
                fFinishedExpanding = true;
            }
            
            public void viewerUpdatesBegin() {}
            public void updateStarted(IViewerUpdate update) {}
            public void updateComplete(IViewerUpdate update) {}
        });
	    
	    virtualViewer.setInput(fView.getViewer().getInput());
	    
	    while (!fFinishedExpanding) {
	       if (!display.readAndDispatch ()) display.sleep ();
	    }

	    ModelDelta stateDelta = new ModelDelta(virtualViewer.getInput(), IModelDelta.NO_CHANGE);
	    virtualViewer.saveElementState(TreePath.EMPTY, stateDelta, IModelDelta.EXPAND);
		((ITreeModelViewer) fView.getViewer()).updateViewer(stateDelta);
		
		virtualViewer.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
