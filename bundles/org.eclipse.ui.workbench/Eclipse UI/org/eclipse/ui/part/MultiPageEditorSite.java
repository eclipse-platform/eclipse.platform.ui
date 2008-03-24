/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.INestableKeyBindingService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PopupMenuExtender;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.part.IMultiPageEditorSiteHolder;
import org.eclipse.ui.internal.services.INestable;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Site for a nested editor within a multi-page editor. Selection is handled by
 * forwarding the event to the multi-page editor's selection listeners; most
 * other methods are forwarded to the multi-page editor's site.
 * <p>
 * The base implementation of <code>MultiPageEditor.createSite</code> creates
 * an instance of this class. This class may be instantiated or subclassed.
 * </p>
 */
public class MultiPageEditorSite implements IEditorSite, INestable {

	/**
	 * The nested editor.
	 */
	private IEditorPart editor;

	/**
	 * The list of popup menu extenders; <code>null</code> if none registered.
	 */
	private ArrayList menuExtenders;

	/**
	 * The multi-page editor.
	 */
	private MultiPageEditorPart multiPageEditor;

	/**
	 * The post selection changed listener.
	 */
	private ISelectionChangedListener postSelectionChangedListener = null;

	/**
	 * The selection change listener, initialized lazily; <code>null</code> if
	 * not yet created.
	 */
	private ISelectionChangedListener selectionChangedListener = null;

	/**
	 * The selection provider; <code>null</code> if none.
	 * 
	 * @see MultiPageEditorSite#setSelectionProvider(ISelectionProvider)
	 */
	private ISelectionProvider selectionProvider = null;

	/**
	 * The cached copy of the key binding service specific to this multi-page
	 * editor site. This value is <code>null</code> if it is not yet
	 * initialized.
	 */
	private IKeyBindingService service = null;

	/**
	 * The local service locator for this multi-page editor site. This value is
	 * never <code>null</code>.
	 */
	private final ServiceLocator serviceLocator;

	/**
	 * Creates a site for the given editor nested within the given multi-page
	 * editor.
	 * 
	 * @param multiPageEditor
	 *            the multi-page editor
	 * @param editor
	 *            the nested editor
	 */
	public MultiPageEditorSite(MultiPageEditorPart multiPageEditor,
			IEditorPart editor) {
		Assert.isNotNull(multiPageEditor);
		Assert.isNotNull(editor);
		this.multiPageEditor = multiPageEditor;
		this.editor = editor;

		final IServiceLocator parentServiceLocator = multiPageEditor.getSite();
		IServiceLocatorCreator slc = (IServiceLocatorCreator) parentServiceLocator
				.getService(IServiceLocatorCreator.class);
		this.serviceLocator = (ServiceLocator) slc.createServiceLocator(
				parentServiceLocator, null);

		initializeDefaultServices();
	}

	/**
	 * Initialize the slave services for this site.
	 */
	private void initializeDefaultServices() {
		serviceLocator.registerService(IMultiPageEditorSiteHolder.class,
				new IMultiPageEditorSiteHolder() {
					public MultiPageEditorSite getSite() {
						return MultiPageEditorSite.this;
					}
				});
	}

	/**
	 * Notifies the multi page editor service that the component within which it 
	 * exists has become active.
	 * 
	 * @since 3.2
	 */
	public final void activate() {
		serviceLocator.activate();
	}

	/**
	 * Notifies the multi page editor service that the component within which it 
	 * exists has been deactived.
	 * 
	 * @since 3.2
	 */
	public final void deactivate() {
		serviceLocator.deactivate();
	}

	/**
	 * Dispose the contributions.
	 */
	public void dispose() {
		if (menuExtenders != null) {
			for (int i = 0; i < menuExtenders.size(); i++) {
				((PopupMenuExtender) menuExtenders.get(i)).dispose();
			}
			menuExtenders = null;
		}

		// Remove myself from the list of nested key binding services.
		if (service != null) {
			IKeyBindingService parentService = getEditor().getSite()
					.getKeyBindingService();
			if (parentService instanceof INestableKeyBindingService) {
				INestableKeyBindingService nestableParent = (INestableKeyBindingService) parentService;
				nestableParent.removeKeyBindingService(this);
			}
			service = null;
		}

		if (serviceLocator != null) {
			serviceLocator.dispose();
		}
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this
	 * <code>IEditorSite</code> method returns <code>null</code>, since
	 * nested editors do not have their own action bar contributor.
	 * 
	 * @return <code>null</code>
	 */
	public IEditorActionBarContributor getActionBarContributor() {
		return null;
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this
	 * <code>IEditorSite</code> method forwards to the multi-page editor to
	 * return the action bars.
	 * 
	 * @return The action bars from the parent multi-page editor.
	 */
	public IActionBars getActionBars() {
		return multiPageEditor.getEditorSite().getActionBars();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this
	 * <code>IWorkbenchPartSite</code> method forwards to the multi-page
	 * editor to return the decorator manager.
	 * 
	 * @return The decorator from the workbench window.
	 * @deprecated use IWorkbench.getDecoratorManager()
	 */
	public ILabelDecorator getDecoratorManager() {
		return getWorkbenchWindow().getWorkbench().getDecoratorManager()
				.getLabelDecorator();
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
	 * <code>IWorkbenchPartSite</code> method returns an empty string since
	 * the nested editor is not created from the registry.
	 * 
	 * @return An empty string.
	 */
	public String getId() {
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc) Method declared on IEditorSite.
	 */
	public IKeyBindingService getKeyBindingService() {
		if (service == null) {
			service = getMultiPageEditor().getEditorSite()
					.getKeyBindingService();
			if (service instanceof INestableKeyBindingService) {
				INestableKeyBindingService nestableService = (INestableKeyBindingService) service;
				service = nestableService.getKeyBindingService(this);

			} else {
				/*
				 * This is an internal reference, and should not be copied by
				 * client code. If you are thinking of copying this, DON'T DO
				 * IT.
				 */
				WorkbenchPlugin
						.log("MultiPageEditorSite.getKeyBindingService()   Parent key binding service was not an instance of INestableKeyBindingService.  It was an instance of " + service.getClass().getName() + " instead."); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return service;
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
	 * <code>IWorkbenchPartSite</code> method forwards to the multi-page
	 * editor to return the workbench page.
	 * 
	 * @return The workbench page in which this editor site resides.
	 */
	public IWorkbenchPage getPage() {
		return getMultiPageEditor().getSite().getPage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPartSite#getPart()
	 */
	public IWorkbenchPart getPart() {
		return editor;
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this
	 * <code>IWorkbenchPartSite</code> method returns an empty string since
	 * the nested editor is not created from the registry.
	 * 
	 * @return An empty string.
	 */
	public String getPluginId() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the post selection change listener which listens to the nested
	 * editor's selection changes.
	 * 
	 * @return the post selection change listener.
	 */
	private ISelectionChangedListener getPostSelectionChangedListener() {
		if (postSelectionChangedListener == null) {
			postSelectionChangedListener = new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					MultiPageEditorSite.this.handlePostSelectionChanged(event);
				}
			};
		}
		return postSelectionChangedListener;
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this
	 * <code>IWorkbenchPartSite</code> method returns an empty string since
	 * the nested editor is not created from the registry.
	 * 
	 * @return An empty string.
	 */
	public String getRegisteredName() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the selection changed listener which listens to the nested
	 * editor's selection changes, and calls <code>handleSelectionChanged</code>.
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
	 * 
	 * @return The current selection provider.
	 */
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	public final Object getService(final Class key) {
		return serviceLocator.getService(key);
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this
	 * <code>IWorkbenchPartSite</code> method forwards to the multi-page
	 * editor to return the shell.
	 * 
	 * @return The shell in which this editor site resides.
	 */
	public Shell getShell() {
		return getMultiPageEditor().getSite().getShell();
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this
	 * <code>IWorkbenchPartSite</code> method forwards to the multi-page
	 * editor to return the workbench window.
	 * 
	 * @return The workbench window in which this editor site resides.
	 */
	public IWorkbenchWindow getWorkbenchWindow() {
		return getMultiPageEditor().getSite().getWorkbenchWindow();
	}

	/**
	 * Handles a post selection changed even from the nexted editor.
	 * <p>
	 * Subclasses may extend or reimplement this method
	 * 
	 * @param event  the event
	 * 
	 * @since 3.2
	 */
	protected void handlePostSelectionChanged(SelectionChangedEvent event) {
		ISelectionProvider parentProvider = getMultiPageEditor().getSite()
				.getSelectionProvider();
		if (parentProvider instanceof MultiPageSelectionProvider) {
			SelectionChangedEvent newEvent = new SelectionChangedEvent(
					parentProvider, event.getSelection());
			MultiPageSelectionProvider prov = (MultiPageSelectionProvider) parentProvider;
			prov.firePostSelectionChanged(newEvent);
		}
	}

	/**
	 * Handles a selection changed event from the nested editor. The default
	 * implementation gets the selection provider from the multi-page editor's
	 * site, and calls <code>fireSelectionChanged</code> on it (only if it is
	 * an instance of <code>MultiPageSelectionProvider</code>), passing a new
	 * event object.
	 * <p>
	 * Subclasses may extend or reimplement this method.
	 * </p>
	 * 
	 * @param event
	 *            the event
	 */
	protected void handleSelectionChanged(SelectionChangedEvent event) {
		ISelectionProvider parentProvider = getMultiPageEditor().getSite()
				.getSelectionProvider();
		if (parentProvider instanceof MultiPageSelectionProvider) {
			SelectionChangedEvent newEvent = new SelectionChangedEvent(
					parentProvider, event.getSelection());
			MultiPageSelectionProvider prov = (MultiPageSelectionProvider) parentProvider;
			prov.fireSelectionChanged(newEvent);
		}
	}

	public final boolean hasService(final Class key) {
		return serviceLocator.hasService(key);
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this
	 * <code>IWorkbenchPartSite</code> method forwards to the multi-page
	 * editor for registration.
	 * 
	 * @param menuManager
	 *            The menu manager
	 * @param selProvider
	 *            The selection provider.
	 */
	public void registerContextMenu(MenuManager menuManager,
			ISelectionProvider selProvider) {
		getMultiPageEditor().getSite().registerContextMenu(menuManager,
				selProvider);
	}

	public final void registerContextMenu(final MenuManager menuManager,
			final ISelectionProvider selectionProvider,
			final boolean includeEditorInput) {
		registerContextMenu(getId(), menuManager, selectionProvider,
				includeEditorInput);
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this
	 * <code>IWorkbenchPartSite</code> method forwards to the multi-page
	 * editor for registration.
	 * 
	 * @param menuID
	 *            The identifier for the menu.
	 * @param menuMgr
	 *            The menu manager
	 * @param selProvider
	 *            The selection provider.
	 */
	public void registerContextMenu(String menuID, MenuManager menuMgr,
			ISelectionProvider selProvider) {
		if (menuExtenders == null) {
			menuExtenders = new ArrayList(1);
		}
		PartSite.registerContextMenu(menuID, menuMgr, selProvider, true,
				editor, menuExtenders);
	}

	public final void registerContextMenu(final String menuId,
			final MenuManager menuManager,
			final ISelectionProvider selectionProvider,
			final boolean includeEditorInput) {
		if (menuExtenders == null) {
			menuExtenders = new ArrayList(1);
		}
		PartSite.registerContextMenu(menuId, menuManager, selectionProvider,
				includeEditorInput, editor, menuExtenders);
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this
	 * <code>IWorkbenchPartSite</code> method remembers the selection
	 * provider, and also hooks a listener on it, which calls
	 * <code>handleSelectionChanged</code> when a selection changed event
	 * occurs.
	 * 
	 * @param provider
	 *            The selection provider.
	 * @see MultiPageEditorSite#handleSelectionChanged(SelectionChangedEvent)
	 */
	public void setSelectionProvider(ISelectionProvider provider) {
		ISelectionProvider oldSelectionProvider = selectionProvider;
		selectionProvider = provider;
		if (oldSelectionProvider != null) {
			oldSelectionProvider
					.removeSelectionChangedListener(getSelectionChangedListener());
			if (oldSelectionProvider instanceof IPostSelectionProvider) {
				((IPostSelectionProvider) oldSelectionProvider)
						.removePostSelectionChangedListener(getPostSelectionChangedListener());
			}
		}
		if (selectionProvider != null) {
			selectionProvider
					.addSelectionChangedListener(getSelectionChangedListener());
			if (selectionProvider instanceof IPostSelectionProvider) {
				((IPostSelectionProvider) selectionProvider)
						.addPostSelectionChangedListener(getPostSelectionChangedListener());
			}
		}
	}
}
