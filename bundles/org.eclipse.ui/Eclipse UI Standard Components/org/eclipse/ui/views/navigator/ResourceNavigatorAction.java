package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.*;
import org.eclipse.ui.actions.SelectionProviderAction;

/**
 * Superclass of all actions provided by the resource navigator.
 */
public abstract class ResourceNavigatorAction extends SelectionProviderAction {
	
	private IResourceNavigator navigator;

	/**
	 * Creates a new instance of the class.
	 */
	public ResourceNavigatorAction(
		IResourceNavigator navigator,
		String label) {
		super(navigator.getViewer(), label);
		this.navigator = navigator;
	}

	/**
	 * Returns the resource navigator for which this action was created.
	 */
	public IResourceNavigator getNavigator() {
		return navigator;
	}

	/**
	 * Returns the resource viewer
	 */
	protected Viewer getViewer() {
		return getNavigator().getViewer();
	}

	/**
	 * Returns the shell to use within actions.
	 */
	protected Shell getShell() {
		return getNavigator().getSite().getShell();
	}

	/**
	 * Returns the workbench.
	 */
	protected IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	/**
	 * Returns the workbench window.
	 */
	protected IWorkbenchWindow getWorkbenchWindow() {
		return getNavigator().getSite().getWorkbenchWindow();
	}
}