package org.eclipse.ui.externaltools.internal.view;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolType;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ViewPart;

/**
 * The view to display all the external tools available to
 * the user. From this view, a user can create a new external
 * tool, delete an existing external tool, run an external
 * tool, or edit the properties of an existing external tool.
 */
public class ExternalToolView extends ViewPart {
	private TreeViewer viewer;
	private ExternalToolActionGroup actionGroup;
	
	/**
	 * Creates a new instance of the external tools view
	 */
	public ExternalToolView() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchPart.
	 */
	public void createPartControl(Composite parent) {
		viewer = createViewer(parent);
		initContextMenu();
		makeActions();
		viewer.setInput(getInitialInput());

		// Fill the action bars and update the global action handlers'
		// enabled state to match the current selection.
		actionGroup.fillActionBars(getViewSite().getActionBars());
		updateActionBars((IStructuredSelection) viewer.getSelection());

		getSite().setSelectionProvider(viewer);
		WorkbenchHelp.setHelp(viewer.getControl(), getHelpContextId());
	}

	/**
	 * Creates the viewer control for the view.
	 * 
	 * @param parent the parent composite
	 * @return the tree viewer for the view
	 */
	protected TreeViewer createViewer(Composite parent) {
		TreeViewer treeViewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.setUseHashlookup(true);
		initContentProvider(treeViewer);
		initLabelProvider(treeViewer);
		initFilters(treeViewer);
		initListeners(treeViewer);

		return treeViewer;
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchPart.
	 */
	public void dispose() {
		if (actionGroup != null)
			actionGroup.dispose();
		
		super.dispose();
	}
	
	/**
	 * Called when the context menu is about to open.
	 * Delegates to the action group using the 
	 * viewer's selection as the action context.
	 */
	protected void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection =
			(IStructuredSelection) viewer.getSelection();
		actionGroup.setContext(new ActionContext(selection));
		actionGroup.fillContextMenu(menu);
	}

	/**
	 * Returns the action group.
	 * 
	 * @return the action group
	 */
	protected final ExternalToolActionGroup getActionGroup() {
		return actionGroup;
	}

	/**
	 * Returns the help context id to use for this view.
	 */
	protected String getHelpContextId() {
		return IHelpContextIds.EXTERNAL_TOOLS_VIEW;
	}

	/** 
	 * Returns the initial input for the viewer.
	 */
	protected Object getInitialInput() {
		return ExternalToolsPlugin.getDefault().getTypeRegistry();
	}

	/**
	 * Returns the message to show in the status line.
	 *
	 * @param selection the current selection
	 * @return the status line message
	 */
	protected String getStatusLineMessage(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object element = selection.getFirstElement();
			if (element instanceof ExternalToolType)
				return ((ExternalToolType)element).getDescription();
			if (element instanceof ExternalTool)
				return ((ExternalTool)element).getDescription();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the tree viewer which shows the external tools.
	 */
	public final TreeViewer getTreeViewer() {
		return viewer;
	}

	/**
	 * Handles a double-click event from the viewer.
	 * Expands or collapses a type when double-clicked.
	 * 
	 * @param event the double-click event
	 */
	protected void handleDoubleClick(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();
		if (viewer.isExpandable(element))
			viewer.setExpandedState(element, !viewer.getExpandedState(element));
	}

	/**
	 * Handles a key press event from the viewer.
	 * Delegates to the action group.
	 * 
	 * @param event the key event
	 */
	protected void handleKeyPressed(KeyEvent event) {
		actionGroup.handleKeyPressed(event);
	}

	/**
	 * Handles a key release in the viewer. Does 
	 * nothing by default.
	 * 
	 * @param event the key event
	 */
	protected void handleKeyReleased(KeyEvent event) {
	}

	/**
	 * Handles an open event from the viewer.
	 * Causes the selected external tool to be run.
	 * 
	 * @param event the open event
	 */
	protected void handleOpen(OpenEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		actionGroup.runDefaultAction(selection);
	}

	/**
	 * Handles a selection changed event from the viewer.
	 * Updates the status line and the action bars.
	 * 
	 * @param event the selection event
	 */
	protected void handleSelectionChanged(SelectionChangedEvent event) {
		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		updateStatusLine(sel);
		updateActionBars(sel);
	}

	/**
	 * Initializes and registers the context menu.
	 */
	protected void initContextMenu() {
		MenuManager menuMgr = new MenuManager(); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getTree());
		viewer.getTree().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/**
	 * Sets the content provider for the viewer.
	 * 
	 * @param treeViewer the tree viewer
	 */
	protected void initContentProvider(TreeViewer treeViewer) {
		Shell shell = getSite().getShell();
		treeViewer.setContentProvider(new ExternalToolContentProvider(shell));
	}

	/**
	 * Sets the label provider for the viewer.
	 * 
	 * @param treeViewer the tree viewer
	 */
	protected void initLabelProvider(TreeViewer treeViewer) {
		treeViewer.setLabelProvider(new ExternalToolLabelProvider());
	}

	/**
	 * Adds the filters to the viewer.
	 * 
	 * @param treeViewer the tree viewer
	 */
	protected void initFilters(TreeViewer treeViewer) {
	}

	/**
	 * Adds the listeners to the viewer.
	 * 
	 * @param treeViewer the tree viewer
	 */
	protected void initListeners(TreeViewer treeViewer) {
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
		
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
		
		treeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				handleOpen(event);
			}
		});
		
		treeViewer.getControl().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent event) {
				handleKeyPressed(event);
			}
			public void keyReleased(KeyEvent event) {
				handleKeyReleased(event);
			}
		});
	}

	/**
	 * Creates the action group, which encapsulates all actions
	 * for the view.
	 */
	protected void makeActions() {
		setActionGroup(new ExternalToolActionGroup(this));
	}

	/**
	 * Sets the action group.
	 * 
	 * @param actionGroup the action group
	 */
	protected void setActionGroup(ExternalToolActionGroup actionGroup) {
		this.actionGroup = actionGroup;
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchPart.
	 */
	public void setFocus() {
		viewer.getTree().setFocus();
	}

	/**
	 * Updates the action bar actions.
	 * 
	 * @param selection the current selection
	 */
	protected void updateActionBars(IStructuredSelection selection) {
		actionGroup.setContext(new ActionContext(selection));
		actionGroup.updateActionBars();
	}

	/**
	 * Updates the message shown in the status line.
	 *
	 * @param selection the current selection
	 */
	protected void updateStatusLine(IStructuredSelection selection) {
		String msg = getStatusLineMessage(selection);
		getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
	}
}
