package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

/**
 * The launches viewer displays a tree of launches. It can be
 * configured to display run launches or debug launches. When
 * displaying run launches, it filters debug targets from the viewer.
 * When displaying debug launches, debug targets are displayed,
 * and the debug target's corresponding system process is filtered
 * from the view.
 */
public class LaunchesViewer extends TreeViewer {
	/**
	 * The view this viewer is contained in
	 */
	protected LaunchesView fView;

	public LaunchesViewer(Composite parent, boolean showDebugTargets, LaunchesView view) {
		super(new Tree(parent, SWT.MULTI));
		fView= view;
		LaunchesViewerFilter filter= new LaunchesViewerFilter(showDebugTargets);
		addFilter(filter);
		setUseHashlookup(true);
	}
	
	/**
	 * Update the buttons in my view
	 */
	protected void updateButtons() {
		fView.updateButtons();			
	}
	
	protected void updateMarkerForSelection() {
		// update the instruction pointer
		if (fView instanceof DebugView) {
			((DebugView) fView).showMarkerForCurrentSelection();
		}		
	}
	
	/** 
	 * Only sets selection if it is different from the current selection
	 */
	public void setSelection(ISelection selection, boolean reveal) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection)selection;
			Object element= ss.getFirstElement();
			ISelection oldSelection= getSelection();
			if (oldSelection instanceof IStructuredSelection) {
				if (element != null) {
					IStructuredSelection oldss= (IStructuredSelection)oldSelection;
					Object oldElement= oldss.getFirstElement();
					if (element.equals(oldElement)) {
						if (element instanceof IStackFrame) {
							//update source selection only...line number change
							((DebugView)fView).showMarkerForCurrentSelection();
						} else if (element instanceof IThread) {
							((DebugView)fView).updateButtons();
						}	
						return;
					}
				}
			}
		}
		super.setSelection(selection, reveal);
	}
	
	protected void clearSourceSelection() {
		if (fView instanceof DebugView) {
			((DebugView)fView).clearSourceSelection();
		}
	}
}

