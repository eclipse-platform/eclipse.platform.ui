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
package org.eclipse.ui.part;


import java.util.ArrayList;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.PopupMenuExtender;

/**
 * Site for a nested editor within a multi-page editor.
 * Selection is handled by forwarding the event to the multi-page editor's 
 * selection listeners; most other methods are forwarded to the multi-page 
 * editor's site.
 * <p>
 * The base implementation of <code>MultiPageEditor.createSite</code> creates 
 * an instance of this class. This class may be instantiated or subclassed.
 * </p>
 */
public class MultiPageEditorSite implements IEditorSite {

	/**
	 * The nested editor.
	 */
	private IEditorPart editor;

	/**
	 * The multi-page editor.
	 */
	private MultiPageEditorPart multiPageEditor;

	/**
	 * The selection provider; <code>null</code> if none.
	 * @see #setSelectionProvider
	 */
	private ISelectionProvider selectionProvider = null;

	/**
	 * The selection change listener, initialized lazily; <code>null</code>
	 * if not yet created.
	 */
	private ISelectionChangedListener selectionChangedListener = null;

	/**
	 * The list of popup menu extenders; <code>null</code> if none registered.
	 */
	private ArrayList menuExtenders;
	
	/**
	 * Creates a site for the given editor nested within the given multi-page editor.
	 *
	 * @param multiPageEditor the multi-page editor
	 * @param editor the nested editor
	 */
	public MultiPageEditorSite(MultiPageEditorPart multiPageEditor, IEditorPart editor) {
		Assert.isNotNull(multiPageEditor);
		Assert.isNotNull(editor);
		this.multiPageEditor = multiPageEditor;
		this.editor = editor;
	}
	
	/**
	 * Dispose the contributions.
	 */
	public void dispose() {
		if (menuExtenders != null) {
			for (int i = 0; i < menuExtenders.size(); i++) {
				((PopupMenuExtender)menuExtenders.get(i)).dispose();
			}
			menuExtenders = null;
		}
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this 
	 * <code>IEditorSite</code> method returns <code>null</code>,
	 * since nested editors do not have their own action bar contributor.
	 */
	public IEditorActionBarContributor getActionBarContributor() {
		return null;
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this 
	 * <code>IEditorSite</code> method forwards to the multi-page editor
	 * to return the action bars.
	 */
	public IActionBars getActionBars() {
		return multiPageEditor.getEditorSite().getActionBars();
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this 
	 * <code>IWorkbenchPartSite</code> method forwards to the multi-page editor to
	 * return the decorator manager.
	 * 
	 * @deprecated use IWorkbench.getDecoratorManager()
	 */
	public ILabelDecorator getDecoratorManager() {
		return getWorkbenchWindow().getWorkbench().getDecoratorManager().getLabelDecorator();
	}

	/**
	 * Returns the nested editor.
	 *
	 * @return the nested editor
	 */
	public IEditorPart getEditor() {
		return editor;
	}
	/**
	 * The <code>MultiPageEditorSite</code> implementation of this 
	 * <code>IWorkbenchPartSite</code> method returns an empty string since the
	 * nested editor is not created from the registry.
	 */
	public String getId() {
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on IEditorSite.
	 */
	public IKeyBindingService getKeyBindingService() {
		return getMultiPageEditor().getEditorSite().getKeyBindingService();
	}

	/**
	 * Returns the multi-page editor.
	 *
	 * @return the multi-page editor
	 */
	public MultiPageEditorPart getMultiPageEditor() {
		return multiPageEditor;
	}
	/**
	 * The <code>MultiPageEditorSite</code> implementation of this 
	 * <code>IWorkbenchPartSite</code> method forwards to the multi-page editor to
	 * return the workbench page.
	 */
	public IWorkbenchPage getPage() {
		return getMultiPageEditor().getSite().getPage();
	}
	/**
	 * The <code>MultiPageEditorSite</code> implementation of this 
	 * <code>IWorkbenchPartSite</code> method returns an empty string since the
	 * nested editor is not created from the registry. 
	 */
	public String getPluginId() {
		return ""; //$NON-NLS-1$
	}
	/**
	 * The <code>MultiPageEditorSite</code> implementation of this 
	 * <code>IWorkbenchPartSite</code> method returns an empty string since the
	 * nested editor is not created from the registry. 
	 */
	public String getRegisteredName() {
		return ""; //$NON-NLS-1$
	}
	/**
	 * Returns the selection changed listener which listens to the nested editor's selection
	 * changes, and calls <code>handleSelectionChanged</code>.
	 *
	 * @return the selection changed listener
	 */
	private ISelectionChangedListener getSelectionChangedListener() {
		if (selectionChangedListener == null) {
			selectionChangedListener = new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					MultiPageEditorSite.this.handleSelectionChanged(event);
				}
			};
		}
		return selectionChangedListener;
	}
	/**
	 * The <code>MultiPageEditorSite</code> implementation of this 
	 * <code>IWorkbenchPartSite</code> method returns the selection provider 
	 * set by <code>setSelectionProvider</code>.
	 */
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}
	/**
	 * The <code>MultiPageEditorSite</code> implementation of this 
	 * <code>IWorkbenchPartSite</code> method forwards to the multi-page editor to
	 * return the shell.
	 */
	public Shell getShell() {
		return getMultiPageEditor().getSite().getShell();
	}
	/**
	 * The <code>MultiPageEditorSite</code> implementation of this 
	 * <code>IWorkbenchPartSite</code> method forwards to the multi-page editor to
	 * return the workbench window.
	 */
	public IWorkbenchWindow getWorkbenchWindow() {
		return getMultiPageEditor().getSite().getWorkbenchWindow();
	}
	/**
	 * Handles a selection changed event from the nested editor.
	 * The default implementation gets the selection provider from the
	 * multi-page editor's site, and calls <code>fireSelectionChanged</code>
	 * on it (only if it is an instance of <code>MultiPageSelectionProvider</code>),
	 * passing a new event object.
	 * <p>
	 * Subclasses may extend or reimplement this method.
	 * </p>
	 *
	 * @param event the event
	 */
	protected void handleSelectionChanged(SelectionChangedEvent event) {
		ISelectionProvider parentProvider = getMultiPageEditor().getSite().getSelectionProvider();
		if (parentProvider instanceof MultiPageSelectionProvider) {
			SelectionChangedEvent newEvent = new SelectionChangedEvent(parentProvider, event.getSelection());
			((MultiPageSelectionProvider) parentProvider).fireSelectionChanged(newEvent);
		}
	}
	/**
	 * The <code>MultiPageEditorSite</code> implementation of this 
	 * <code>IWorkbenchPartSite</code> method forwards to the multi-page editor for
	 * registration.
	 */
	public void registerContextMenu(String menuID, MenuManager menuMgr, ISelectionProvider selProvider) {
		if (menuExtenders == null) {
			menuExtenders = new ArrayList(1);
		}
		menuExtenders.add(new PopupMenuExtender(menuID, menuMgr, selProvider, editor));
	}
	/**
	 * The <code>MultiPageEditorSite</code> implementation of this 
	 * <code>IWorkbenchPartSite</code> method forwards to the multi-page editor for
	 * registration.
	 */
	public void registerContextMenu(MenuManager menuManager, ISelectionProvider selectionProvider) {
		getMultiPageEditor().getSite().registerContextMenu(menuManager, selectionProvider);
	}
	/**
	 * The <code>MultiPageEditorSite</code> implementation of this
	 * <code>IWorkbenchPartSite</code> method remembers the selection provider, 
	 * and also hooks a listener on it, which calls <code>handleSelectionChanged</code> 
	 * when a selection changed event occurs.
	 *
	 * @see #handleSelectionChanged
	 */
	public void setSelectionProvider(ISelectionProvider provider) {
		ISelectionProvider oldSelectionProvider = selectionProvider;
		selectionProvider = provider;
		if (oldSelectionProvider != null) {
			oldSelectionProvider.removeSelectionChangedListener(getSelectionChangedListener());
		}
		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(getSelectionChangedListener());
		}
	}
}
