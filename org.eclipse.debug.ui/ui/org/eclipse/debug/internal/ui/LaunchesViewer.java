package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

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
	
	protected void autoExpand(Object element, boolean refreshNeeded, boolean selectNeeded) {
		fView.autoExpand(element, refreshNeeded, selectNeeded);
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
		
	/**
	 * Update the icons for all stack frame children of the given thread.
	 */	
	public void updateStackFrameIcons(IThread parentThread) {
		Widget parentItem= doFindItem(parentThread);
		Item[] items= getItems((Item)parentItem);
		for (int i = 0; i < items.length; i++) {
			TreeItem treeItem = (TreeItem)items[i];
			updateOneStackFrameIcon(treeItem, (IStackFrame)treeItem.getData());
		}
	}
	
	/**
	 * For the given stack frame and associated TreeItem, update the icon on the
	 * TreeItem.
	 */
	protected void updateOneStackFrameIcon(TreeItem treeItem, IStackFrame stackFrame) {
		ILabelProvider provider = (ILabelProvider) getLabelProvider();
		Image image = provider.getImage(stackFrame);
		if (image != null) {
			treeItem.setImage(image);
		}			
	}
	
}

