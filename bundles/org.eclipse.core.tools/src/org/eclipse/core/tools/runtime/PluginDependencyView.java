/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.runtime;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tools.*;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.*;

public class PluginDependencyView extends SpyView implements ISelectionListener {

	private TextViewer viewer;
	private Map dependencyGraph = null;

	/** The id by which this view is known in the plug-in registry */
	public static final String VIEW_ID = PluginDependencyView.class.getName();

	/**
	 * @see IWorkbenchPart#createPartControl
	 */
	public void createPartControl(Composite parent) {
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

		// build up the dependency graph
		buildDependencyGraph();
		getViewSite().getPage().addSelectionListener(this);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		dependencyGraph = null;
	}

	/**
	 * Build the table of plug-in dependencies. Iterate over all the plug-ins in the
	 * plug-in registry and the cycle through the list of pre-requisites and create the
	 * parent/child relationships in the nodes.
	 */
	private void buildDependencyGraph() {
		// Build up the dependency graph (see PluginDependencyGraphNode) so
		// we have the information readily available for any plug-in.
		IPluginDescriptor[] plugins = Platform.getPluginRegistry().getPluginDescriptors();
		dependencyGraph = new HashMap();
		for (int i = 0; i < plugins.length; i++) {
			IPluginDescriptor descriptor = plugins[i];
			PluginDependencyGraphNode node = (PluginDependencyGraphNode) dependencyGraph.get(descriptor);
			if (node == null) {
				node = new PluginDependencyGraphNode(descriptor);
				dependencyGraph.put(descriptor, node);
			}

			// Cycle through the prerequisites
			IPluginPrerequisite[] requires = descriptor.getPluginPrerequisites();
			for (int j = 0; j < requires.length; j++) {
				String childId = requires[j].getUniqueIdentifier();
				IPluginDescriptor childDesc = Platform.getPluginRegistry().getPluginDescriptor(childId);
				// if the child doesn't exist in the plug-in registry then move to the next child
				if (childDesc == null)
					continue;

				// if the child entry is not in the table yet then add it
				PluginDependencyGraphNode childNode = (PluginDependencyGraphNode) dependencyGraph.get(childDesc);
				if (childNode == null) {
					childNode = new PluginDependencyGraphNode(childDesc);
					dependencyGraph.put(childDesc, childNode);
				}

				// Add the child to this node's children and set this node as an ancestor
				// of the child node
				node.addChild(childNode);
				childNode.addAncestor(node);
			}
		}
	}

	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(selection instanceof IStructuredSelection))
			return;
		Object element = ((IStructuredSelection) selection).getFirstElement();
		if (!(element instanceof IPluginDescriptor))
			return;
		IPluginDescriptor descriptor = (IPluginDescriptor) element;
		PluginDependencyGraphNode node = (PluginDependencyGraphNode) dependencyGraph.get(descriptor);
		String text = node == null ? Policy.bind("depend.noInformation", descriptor.getUniqueIdentifier()) : node.toDeepString(); //$NON-NLS-1$
		viewer.getDocument().set(text);
		viewer.refresh();
	}
}