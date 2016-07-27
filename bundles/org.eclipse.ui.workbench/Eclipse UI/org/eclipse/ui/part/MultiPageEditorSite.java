/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
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
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.KeyBindingService;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PopupMenuExtender;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.contexts.NestableContextService;
import org.eclipse.ui.internal.expressions.ActivePartExpression;
import org.eclipse.ui.internal.handlers.LegacyHandlerService;
import org.eclipse.ui.internal.part.IMultiPageEditorSiteHolder;
import org.eclipse.ui.internal.services.INestable;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.internal.services.WorkbenchLocationService;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceScopes;

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

	private NestableContextService contextService;

	private IEclipseContext context;

	private boolean active = false;

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

		PartSite site = (PartSite) multiPageEditor.getSite();

		IServiceLocatorCreator slc = site
				.getService(IServiceLocatorCreator.class);
		context = site.getModel().getContext().createChild("MultiPageEditorSite"); //$NON-NLS-1$
		serviceLocator = (ServiceLocator) slc.createServiceLocator(
				multiPageEditor.getSite(), null, new IDisposable(){
					@Override
					public void dispose() {
						getMultiPageEditor().close();
					}
				}, context);

		initializeDefaultServices();
	}

	/**
	 * Initialize the slave services for this site.
	 */
	private void initializeDefaultServices() {
		serviceLocator.registerService(IWorkbenchLocationService.class,
				new WorkbenchLocationService(IServiceScopes.MPESITE_SCOPE,
						getWorkbenchWindow().getWorkbench(),
						getWorkbenchWindow(), getMultiPageEditor().getSite(),
						this, null, 3));
		serviceLocator.registerService(IMultiPageEditorSiteHolder.class,
				new IMultiPageEditorSiteHolder() {
					@Override
					public MultiPageEditorSite getSite() {
						return MultiPageEditorSite.this;
					}
				});

		context.set(IContextService.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext ctxt, String contextKey) {
				if (contextService == null) {
					contextService = new NestableContextService(ctxt.getParent().get(
							IContextService.class), new ActivePartExpression(multiPageEditor));
				}
				return contextService;
			}
		});

		// create a local handler service so that when this page
		// activates/deactivates, its handlers will also be taken into/out of
		// consideration during handler lookups
		IHandlerService handlerService = new LegacyHandlerService(context);
		context.set(IHandlerService.class, handlerService);
	}

	/**
	 * Notifies the multi page editor service that the component within which it
	 * exists has become active.
	 *
	 * @since 3.2
	 */
	@Override
	public final void activate() {
		active = true;
		context.activate();
		serviceLocator.activate();

		if (contextService != null) {
			contextService.activate();
		}
	}

	/**
	 * Notifies the multi page editor service that the component within which it
	 * exists has been deactived.
	 *
	 * @since 3.2
	 */
	@Override
	public final void deactivate() {
		active = false;
		if (contextService != null) {
			contextService.deactivate();
		}

		serviceLocator.deactivate();
		context.deactivate();
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
			IKeyBindingService parentService = getMultiPageEditor().getEditorSite()
					.getKeyBindingService();
			if (parentService instanceof INestableKeyBindingService) {
				INestableKeyBindingService nestableParent = (INestableKeyBindingService) parentService;
				nestableParent.removeKeyBindingService(this);
			}
			if (service instanceof KeyBindingService) {
				((KeyBindingService) service).dispose();
			}
			service = null;
		}

		if (contextService != null) {
			contextService.dispose();
		}

		if (serviceLocator != null) {
			serviceLocator.dispose();
		}
		context.dispose();
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this
	 * <code>IEditorSite</code> method returns <code>null</code>, since
	 * nested editors do not have their own action bar contributor.
	 *
	 * @return <code>null</code>
	 */
	@Override
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
	@Override
	public IActionBars getActionBars() {
		return multiPageEditor.getEditorSite().getActionBars();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
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
	@Deprecated
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
	@Override
	public String getId() {
		return ""; //$NON-NLS-1$
	}

	@Override
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
	@Override
	public IWorkbenchPage getPage() {
		return getMultiPageEditor().getSite().getPage();
	}

	@Override
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
	@Override
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
				@Override
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
	@Override
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
				@Override
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
	@Override
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	@Override
	public final <T> T getService(final Class<T> key) {
		T service = serviceLocator.getService(key);
		if (active && service instanceof INestable) {
			// services need to know that it is currently in an active state
			((INestable) service).activate();
		}
		return service;
	}

	/**
	 * The <code>MultiPageEditorSite</code> implementation of this
	 * <code>IWorkbenchPartSite</code> method forwards to the multi-page
	 * editor to return the shell.
	 *
	 * @return The shell in which this editor site resides.
	 */
	@Override
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
	@Override
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

	@Override
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
	@Override
	public void registerContextMenu(MenuManager menuManager,
			ISelectionProvider selProvider) {
		getMultiPageEditor().getSite().registerContextMenu(menuManager,
				selProvider);
	}

	@Override
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
	@Override
	public void registerContextMenu(String menuID, MenuManager menuMgr,
			ISelectionProvider selProvider) {
		if (menuExtenders == null) {
			menuExtenders = new ArrayList(1);
		}
		PartSite.registerContextMenu(menuID, menuMgr, selProvider, true, editor, context,
				menuExtenders);
	}

	@Override
	public final void registerContextMenu(final String menuId,
			final MenuManager menuManager,
			final ISelectionProvider selectionProvider,
			final boolean includeEditorInput) {
		if (menuExtenders == null) {
			menuExtenders = new ArrayList(1);
		}
		PartSite.registerContextMenu(menuId, menuManager, selectionProvider, includeEditorInput,
				editor, context, menuExtenders);
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
	@Override
	public void setSelectionProvider(ISelectionProvider provider) {
		ISelectionProvider oldSelectionProvider = selectionProvider;
		selectionProvider = provider;
		if (oldSelectionProvider != null) {
			oldSelectionProvider
					.removeSelectionChangedListener(getSelectionChangedListener());
			if (oldSelectionProvider instanceof IPostSelectionProvider) {
				((IPostSelectionProvider) oldSelectionProvider)
						.removePostSelectionChangedListener(getPostSelectionChangedListener());
			} else {
				oldSelectionProvider
						.removeSelectionChangedListener(getPostSelectionChangedListener());
			}
		}
		if (selectionProvider != null) {
			selectionProvider
					.addSelectionChangedListener(getSelectionChangedListener());
			if (selectionProvider instanceof IPostSelectionProvider) {
				((IPostSelectionProvider) selectionProvider)
						.addPostSelectionChangedListener(getPostSelectionChangedListener());
			} else {
				selectionProvider.addSelectionChangedListener(getPostSelectionChangedListener());
			}
		}
	}
}
