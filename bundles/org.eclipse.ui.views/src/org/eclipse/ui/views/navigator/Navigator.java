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

package org.eclipse.ui.views.navigator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.ViewsPlugin;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Implements the Resource Navigator view.
 */
public class Navigator extends ViewPart {
	private TreeViewer viewer;

	/**
	 * Constructs a new resource navigator view.
	 */
	public Navigator() {
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchPart.
	 */
	public void createPartControl(Composite parent) {
		TreeViewer viewer = createViewer(parent);
		this.viewer = viewer;

//		updateTitle();

//		initContextMenu();

		// make sure input is set after sorters and filters,
		// to avoid unnecessary refreshes
		viewer.setInput(getInitialInput());

		// make actions after setting input, because some actions
		// look at the viewer for enablement (e.g. the Up action)
//		makeActions();

		// Fill the action bars and update the global action handlers'
		// enabled state to match the current selection.
//		getActionGroup().fillActionBars(getViewSite().getActionBars());
//		updateActionBars((IStructuredSelection) viewer.getSelection());

		getSite().setSelectionProvider(viewer);

		// Set help for the view 
//		WorkbenchHelp.setHelp(viewer.getControl(), getHelpContextId());
	}

	/**
	 * Creates the viewer.
	 * 
	 * @param parent the parent composite
	 * @since 2.0
	 */
	protected TreeViewer createViewer(Composite parent) {
		TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setUseHashlookup(true);
		initContentProvider(viewer);
		initLabelProvider(viewer);
//		initFilters(viewer);
		initListeners(viewer);

		return viewer;
	}

	/**
	 * Sets the content provider for the viewer.
	 * 
	 * @param viewer the viewer
	 * @since 2.0
	 */
	protected void initContentProvider(TreeViewer viewer) {
		NavigatorContentHandler contentProvider = new NavigatorContentHandler(this);
		viewer.setContentProvider(contentProvider);
	}

	/**
	 * Sets the label provider for the viewer.
	 * 
	 * @param viewer the viewer
	 * @since 2.0
	 */
	protected void initLabelProvider(TreeViewer viewer) {
		viewer.setLabelProvider(
			new DecoratingLabelProvider(
				new WorkbenchLabelProvider(),
				getPlugin().getWorkbench().getDecoratorManager().getLabelDecorator()));
	}

	/**
	 * Adds the listeners to the viewer.
	 * 
	 * @param viewer the viewer
	 * @since 2.0
	 */
	protected void initListeners(TreeViewer viewer) {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				handleOpen(event);
			}
		});
		viewer.getControl().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent event) {
				handleKeyPressed(event);
			}
			public void keyReleased(KeyEvent event) {
				handleKeyReleased(event);
			}
		});
	}
	/** 
	 * Returns the initial input for the viewer.
	 * Tries to convert the page input to a resource, either directly or via IAdaptable.
	 * If the resource is a container, it uses that.
	 * If the resource is a file, it uses its parent folder.
	 * If a resource could not be obtained, it uses the workspace root.
	 * 
	 * @since 2.0
	 */
	protected IAdaptable getInitialInput() {
		return getSite().getPage().getInput();
	}

	/**
	 * Returns the navigator's plugin.
	 */
	public AbstractUIPlugin getPlugin() {
		return (AbstractUIPlugin) Platform.getPlugin(ViewsPlugin.PLUGIN_ID);
	}

	/**
	 * Returns the resource viewer which shows the resource hierarchy.
	 * @since 2.0
	 */
	public TreeViewer getViewer() {
		return viewer;
	}

	/**
	 * Handles an open event from the viewer.
	 * Opens an editor on the selected file.
	 * 
	 * @param event the open event
	 * @since 2.0
	 */
	protected void handleOpen(OpenEvent event) {
	}

	/**
	 * Handles a double-click event from the viewer.
	 * Expands or collapses a folder when double-clicked.
	 * 
	 * @param event the double-click event
	 * @since 2.0
	 */
	protected void handleDoubleClick(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();
		TreeViewer viewer = getViewer();
		
		if (viewer.isExpandable(element)) {
			viewer.setExpandedState(element, !viewer.getExpandedState(element));
		}

	}

	/**
	 * Handles a selection changed event from the viewer.
	 * 
	 * @param event the selection event
	 */
	protected void handleSelectionChanged(SelectionChangedEvent event) {
	}

	/**
	 * Handles a key press event from the viewer.
	 * 
	 * @param event the key event
	 */
	protected void handleKeyPressed(KeyEvent event) {
	}

	/**
	 * Handles a key release in the viewer.  Does nothing by default.
	 * 
	 * @param event the key event
	 */
	protected void handleKeyReleased(KeyEvent event) {
	}

	/* (non-Javadoc)
	 * Method declared on IViewPart.
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
	}

	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		getViewer().getTree().setFocus();
	}
}
