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
package org.eclipse.update.internal.ui.views;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.update.internal.ui.UpdateUIImages;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public abstract class BaseTreeView extends BaseView {
	private DrillDownAdapter drillDownAdapter;
	protected Action collapseAllAction;
	/**
	 * The constructor.
	 */
	public BaseTreeView() {
	}

	protected StructuredViewer createViewer(Composite parent, int styles) {
		TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | styles);
		return viewer;
	}
	
	protected void initDrillDown() {
		drillDownAdapter = new DrillDownAdapter(getTreeViewer());
	}

	protected void addDrillDownAdapter(IActionBars bars) {
		drillDownAdapter.addNavigationActions(bars.getToolBarManager());
	}
	
	protected void addDrillDownAdapter(IMenuManager menu) {
		drillDownAdapter.addNavigationActions(menu); 
	}
	
	public TreeViewer getTreeViewer() {
		return (TreeViewer)getViewer();
	}
	
	protected void makeActions() {
		super.makeActions();
		collapseAllAction = new Action() {
			public void run() {
				TreeViewer treeViewer = getTreeViewer();
				treeViewer.getControl().setRedraw(false);		
				treeViewer.collapseToLevel(treeViewer.getInput(), TreeViewer.ALL_LEVELS);
				treeViewer.getControl().setRedraw(true);
			}
		};
		collapseAllAction.setText("Collapse All");
		collapseAllAction.setToolTipText("Collapse All");
		collapseAllAction.setImageDescriptor(UpdateUIImages.DESC_COLLAPSE_ALL);
	}
	
	protected abstract Object getRootObject();
	
	void updateTitle(Object newInput) {
		IConfigurationElement config = getConfigurationElement();
		if (config == null)
			return;
		String viewName = config.getAttribute("name"); //$NON-NLS-1$
		if (newInput == null
			|| newInput.equals(getRootObject())) {
			// restore old
			setTitle(viewName);
			setTitleToolTip(getTitle());
		} else {
			String name =
				((LabelProvider) getViewer().getLabelProvider()).getText(
					newInput);
			setTitle(viewName + ": " + name); //$NON-NLS-1$
			setTitleToolTip(getTitle());
		}
	}	
}
