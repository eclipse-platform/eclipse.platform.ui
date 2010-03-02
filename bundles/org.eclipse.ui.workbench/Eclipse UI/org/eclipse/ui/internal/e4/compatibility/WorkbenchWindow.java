/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.lang.reflect.InvocationTargetException;
import javax.inject.Inject;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.internal.Activator;
import org.eclipse.e4.workbench.ui.internal.Policy;
import org.eclipse.e4.workbench.ui.renderers.swt.TrimmedPartLayout;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.expressions.WorkbenchWindowExpression;
import org.eclipse.ui.internal.handlers.ActionCommandMappingService;
import org.eclipse.ui.internal.handlers.ActionDelegateHandlerProxy;
import org.eclipse.ui.internal.handlers.IActionCommandMappingService;
import org.eclipse.ui.internal.handlers.LegacyHandlerService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.UIExtensionTracker;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.internal.services.WorkbenchLocationService;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceScopes;

/**
 * @since 3.5
 * 
 */
public class WorkbenchWindow implements IWorkbenchWindow {

	public static final String ACTION_SET_CMD_PREFIX = "AS::"; //$NON-NLS-1$

	@Inject
	private IWorkbench workbench;
	@Inject
	private MWindow model;
	@Inject
	private IPresentationEngine engine;
	private WorkbenchPage page;
	private UIExtensionTracker tracker;

	private ISelectionService selectionService;

	private IAdaptable input;
	private IPerspectiveDescriptor perspective;

	private ListenerList pageListeners = new ListenerList();
	private ListenerList perspectiveListeners = new ListenerList();

	private ServiceLocator serviceLocator;

	private StatusLineManager statusLineManager;

	public WorkbenchWindow(IAdaptable input, IPerspectiveDescriptor perspective) {
		this.input = input;
		this.perspective = perspective;
	}

	@PostConstruct
	void setup() throws InvocationTargetException, InstantiationException {
		IEclipseContext windowContext = model.getContext();
		IServiceLocatorCreator slc = (IServiceLocatorCreator) workbench
				.getService(IServiceLocatorCreator.class);
		this.serviceLocator = (ServiceLocator) slc.createServiceLocator(workbench, null,
				new IDisposable() {
					public void dispose() {
						final Shell shell = getShell();
						if (shell != null && !shell.isDisposed()) {
							close();
						}
					}
				});
		serviceLocator.setContext(windowContext);

		page = new WorkbenchPage(this, input);

		windowContext.set(IWorkbenchWindow.class.getName(), this);
		windowContext.set(IWorkbenchPage.class.getName(), page);

		windowContext.set(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, this);
		windowContext.set(ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME, getShell());
		EContextService cs = (EContextService) windowContext.get(EContextService.class.getName());
		cs.activateContext(IContextService.CONTEXT_ID_WINDOW);
		cs.getActiveContextIds();

		final ActionCommandMappingService mappingService = new ActionCommandMappingService();
		serviceLocator.registerService(IActionCommandMappingService.class, mappingService);

		windowContext.set(IWorkbenchLocationService.class.getName(), new WorkbenchLocationService(
				IServiceScopes.WINDOW_SCOPE, getWorkbench(), this, null, null, null, 1));

		ContextInjectionFactory.inject(page, windowContext);
		page.setPerspective(perspective);

		selectionService = (ISelectionService) ContextInjectionFactory.make(SelectionService.class,
				model.getContext());

		LegacyHandlerService hs = new LegacyHandlerService(windowContext);
		windowContext.set(IHandlerService.class.getName(), hs);
		readActionSets();
	}

	public StatusLineManager getStatusLineManager() {
		if (statusLineManager == null) {
			Shell shell = (Shell) model.getWidget();
			if (shell != null) {
				TrimmedPartLayout layout = (TrimmedPartLayout) shell.getLayout();
				Composite trimComposite = layout.getTrimComposite(shell, SWT.BOTTOM);
				trimComposite.setLayout(new FillLayout());

				statusLineManager = new StatusLineManager();
				Control control = statusLineManager.createControl(trimComposite);
				control.setSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		}
		return statusLineManager;
	}

	public static String getId(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);

		// For sub-menu management -all- items must be id'd so enforce this
		// here (we could optimize by checking the 'name' of the config
		// element == "menu"
		if (id == null || id.length() == 0) {
			id = getCommandId(element);
		}
		if (id == null || id.length() == 0) {
			id = element.toString();
		}

		return id;
	}

	public static String getCommandId(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_COMMAND_ID);
	}

	public static String getActionSetCommandId(IConfigurationElement element) {
		String id = getDefinitionId(element);
		if (id != null) {
			return id;
		}
		id = getId(element);
		String actionSetId = null;
		Object obj = element.getParent();
		while (obj instanceof IConfigurationElement && actionSetId == null) {
			IConfigurationElement parent = (IConfigurationElement) obj;
			if (parent.getName().equals(IWorkbenchRegistryConstants.TAG_ACTION_SET)) {
				actionSetId = getId(parent);
			}
			obj = parent.getParent();
		}
		return ACTION_SET_CMD_PREFIX + actionSetId + '/' + id;
	}

	public static String getDefinitionId(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_DEFINITION_ID);
	}

	public static boolean getRetarget(IConfigurationElement element) {
		String r = element.getAttribute(IWorkbenchRegistryConstants.ATT_RETARGET);
		return Boolean.valueOf(r);
	}

	private void readActionSets() {
		IEclipseContext windowContext = model.getContext();
		WorkbenchWindowExpression windowExpression = new WorkbenchWindowExpression(this);
		ICommandService cs = (ICommandService) windowContext.get(ICommandService.class.getName());
		IExtensionRegistry registry = (IExtensionRegistry) windowContext
				.get(IExtensionRegistry.class.getName());
		IExtensionPoint extPoint = registry
				.getExtensionPoint(IWorkbenchRegistryConstants.EXTENSION_ACTION_SETS);
		IConfigurationElement[] actionSetElements = extPoint.getConfigurationElements();
		for (IConfigurationElement ase : actionSetElements) {
			IConfigurationElement[] elements = ase
					.getChildren(IWorkbenchRegistryConstants.TAG_ACTION);
			for (IConfigurationElement configElement : elements) {
				String id = getId(configElement);
				String cmdId = getActionSetCommandId(configElement);
				if (id == null || id.length() == 0 || getRetarget(configElement)) {
					continue;
				}
				Command cmd = cs.getCommand(cmdId);
				if (!cmd.isDefined()) {
					Activator.trace(Policy.DEBUG_CMDS, "Still no command for " //$NON-NLS-1$
							+ cmdId, null);
					continue;
				}
				LegacyHandlerService.registerLegacyHandler(windowContext, id, cmdId,
						new ActionDelegateHandlerProxy(configElement,
								IWorkbenchRegistryConstants.ATT_CLASS, id,
								new ParameterizedCommand(cmd, null), this, null, null, null),
						windowExpression);
			}
		}
	}

	public MWindow getModel() {
		return model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindow#close()
	 */
	public boolean close() {
		// FIXME: the rendering engine should destroy in response to a remove,
		// right?
		MElementContainer<MUIElement> parent = model.getParent();
		model.getParent().getChildren().remove(model);
		if (parent.getSelectedElement() == model) {
			if (!parent.getChildren().isEmpty()) {
				parent.setSelectedElement(parent.getChildren().get(0));
			}
		}
		engine.removeGui(model);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindow#getActivePage()
	 */
	public IWorkbenchPage getActivePage() {
		return page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindow#getPages()
	 */
	public IWorkbenchPage[] getPages() {
		return new IWorkbenchPage[] { page };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindow#getPartService()
	 */
	public IPartService getPartService() {
		return page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindow#getSelectionService()
	 */
	public ISelectionService getSelectionService() {
		return selectionService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindow#getShell()
	 */
	public Shell getShell() {
		return (Shell) model.getWidget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindow#getWorkbench()
	 */
	public IWorkbench getWorkbench() {
		return workbench;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindow#isApplicationMenu(java.lang.String)
	 */
	public boolean isApplicationMenu(String menuId) {
		// FIXME compat isApplicationMenu
		E4Util.unsupported("isApplicationMenu"); //$NON-NLS-1$
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindow#openPage(java.lang.String,
	 * org.eclipse.core.runtime.IAdaptable)
	 */
	public IWorkbenchPage openPage(String perspectiveId, IAdaptable input)
			throws WorkbenchException {
		return workbench.openWorkbenchWindow(perspectiveId, input).getActivePage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchWindow#openPage(org.eclipse.core.runtime.IAdaptable
	 * )
	 */
	public IWorkbenchPage openPage(IAdaptable input) throws WorkbenchException {
		return openPage(workbench.getPerspectiveRegistry().getDefaultPerspective(), input);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindow#run(boolean, boolean,
	 * org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(final boolean fork, boolean cancelable, final IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		final StatusLineManager manager = getStatusLineManager();
		if (manager == null) {
			runnable.run(new NullProgressMonitor());
			return;
		}

		boolean enabled = manager.isCancelEnabled();
		try {
			manager.setCancelEnabled(cancelable);

			final Exception[] holder = new Exception[1];
			BusyIndicator.showWhile(getWorkbench().getDisplay(), new Runnable() {
				public void run() {
					try {
						ModalContext.run(runnable, fork, manager.getProgressMonitor(),
								getWorkbench().getDisplay());
					} catch (InvocationTargetException ite) {
						holder[0] = ite;
					} catch (InterruptedException ie) {
						holder[0] = ie;
					}
				}
			});

			if (holder[0] != null) {
				if (holder[0] instanceof InvocationTargetException) {
					throw (InvocationTargetException) holder[0];
				} else if (holder[0] instanceof InterruptedException) {
					throw (InterruptedException) holder[0];
				}
			}
		} finally {
			manager.setCancelEnabled(enabled);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchWindow#setActivePage(org.eclipse.ui.IWorkbenchPage
	 * )
	 */
	public void setActivePage(IWorkbenchPage page) {
		// TODO Auto-generated method stub
		this.page = (WorkbenchPage) page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindow#getExtensionTracker()
	 */
	public IExtensionTracker getExtensionTracker() {
		if (tracker == null) {
			tracker = new UIExtensionTracker(getWorkbench().getDisplay());
		}
		return tracker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPageService#addPageListener(org.eclipse.ui.IPageListener)
	 */
	public void addPageListener(IPageListener listener) {
		pageListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPageService#addPerspectiveListener(org.eclipse.ui.
	 * IPerspectiveListener)
	 */
	public void addPerspectiveListener(IPerspectiveListener listener) {
		perspectiveListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPageService#removePageListener(org.eclipse.ui.IPageListener
	 * )
	 */
	public void removePageListener(IPageListener listener) {
		pageListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPageService#removePerspectiveListener(org.eclipse.ui.
	 * IPerspectiveListener)
	 */
	public void removePerspectiveListener(IPerspectiveListener listener) {
		perspectiveListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceLocator#getService(java.lang.Class)
	 */
	public Object getService(Class api) {
		return serviceLocator.getService(api);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceLocator#hasService(java.lang.Class)
	 */
	public boolean hasService(Class api) {
		return serviceLocator.hasService(api);
	}

}
