/**********************************************************************
 * Copyright (c) 2000,2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.resources;

import org.eclipse.core.internal.events.ResourceDelta;
import org.eclipse.core.resources.*;
import org.eclipse.core.tools.*;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Implements the Delta Spy view. This view uses a <code>TextViewer</code> to 
 * show resource deltas resulting of workspace changes (it is a <code>
 * IResourceChangeListener</code>).
 * 
 * @see org.eclipse.core.resources.IResourceChangeListener  
 */

public class DeltaView extends SpyView implements IResourceChangeListener {

	/** The JFace widget used for showing resource deltas. */
	protected TextViewer viewer;

	/** The maximum number of characters to be shown on this view. */
	private final static int MAX_SIZE = 1024 * 20;

	/** A reference to the parent control. */
	protected Composite parent;

	/**
	 * Constructs a <code>DeltaView</code> object, adding it as a workspace's 
	 * resource change listener. 
	 */
	public DeltaView() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Creates this view's text widget and actions.
	 * 
	 * @param parent the parent control
	 * @see IWorkbenchPart#createPartControl
	 */
	public void createPartControl(Composite parent) {

		this.parent = parent;

		viewer = new TextViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP | SWT.READ_ONLY);
		viewer.setDocument(new Document());

		IActionBars bars = getViewSite().getActionBars();

		final GlobalAction clearOutputAction = new ClearTextAction(viewer.getDocument());
		clearOutputAction.registerAsGlobalAction(bars);

		final GlobalAction selectAllAction = new SelectAllAction(viewer);
		selectAllAction.registerAsGlobalAction(bars);

		// Delete action shortcuts are not captured by the workbench
		// so we need our key binding service to handle Delete keystrokes for us

		this.viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.DEL)
					clearOutputAction.run();
			}
		});

		GlobalAction copyAction = new CopyTextSelectionAction(viewer);
		copyAction.registerAsGlobalAction(bars);

		bars.getToolBarManager().add(clearOutputAction);

		bars.updateActionBars();

		// creates a context menu with actions and adds it to the viewer control
		MenuManager menuMgr = new MenuManager();
		menuMgr.add(copyAction);
		menuMgr.add(clearOutputAction);
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

	}

	/**
	 * Unregister this resource change listener.
	 * 
	 * @see IWorkbenchPart#dispose
	 */
	public void dispose() {
		super.dispose();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	/**
	 * Updates view contents, appending new delta information. This method relies
	 * on <code>ResourceDelta.toDeepDebugString()</code> to produce a string 
	 * representation for a resource delta.
	 *   
	 * @param event the resource change event
	 * @see ResourceDelta#toDeepDebugString()
	 * @see IResourceChangeListener#resourceChanged  
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() != IResourceChangeEvent.POST_CHANGE)
			return;

		if (parent == null || parent.isDisposed())
			return;

		// if we have no changes, there is nothing to do
		final ResourceDelta delta = (ResourceDelta) event.getDelta();
		if (delta == null)
			return;

		// we need to access UI widgets from a SWT thread 
		Runnable update = new Runnable() {
			public void run() {
				// the view might have been disposed at the moment this code runs
				if (parent.isDisposed())
					return;

				// updates viewer document, appending new delta information     
				IDocument doc = viewer.getDocument();
				StringBuffer contents = new StringBuffer(doc.get());
				contents.append('\n');

				// asks for a string representation for the delta
				contents.append(delta.toDeepDebugString());

				// save current number of lines
				int previousNOL = doc.getNumberOfLines();

				// sets the viewer document's new contents
				// ensuring there will be no more than MAX_SIZE chars in it 						
				int length = contents.length();
				doc.set(contents.substring(length - (Math.min(MAX_SIZE, length))));

				// ensures the added content will be immediately visible
				viewer.setTopIndex(previousNOL + 1);
			}
		};
		// run our code in the SWT thread
		parent.getDisplay().syncExec(update);
	}

}