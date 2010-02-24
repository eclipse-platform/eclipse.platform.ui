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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.commands.internal.CommandServiceImpl;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.e4.workbench.ui.internal.E4CommandProcessor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.ILocalWorkingSetManager;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISaveableFilter;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.JFaceUtil;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkingSetManager;
import org.eclipse.ui.internal.activities.ws.WorkbenchActivitySupport;
import org.eclipse.ui.internal.commands.CommandImageManager;
import org.eclipse.ui.internal.commands.CommandImageService;
import org.eclipse.ui.internal.commands.CommandService;
import org.eclipse.ui.internal.handlers.LegacyHandlerService;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;
import org.eclipse.ui.internal.registry.UIExtensionTracker;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.internal.services.ServiceLocatorCreator;
import org.eclipse.ui.internal.services.WorkbenchLocationService;
import org.eclipse.ui.internal.themes.ColorDefinition;
import org.eclipse.ui.internal.themes.ThemeElementHelper;
import org.eclipse.ui.internal.themes.WorkbenchThemeManager;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceScopes;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipse.ui.views.IViewRegistry;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class Workbench implements IWorkbench {

	private static Workbench instance;

	@Inject
	private MApplication application;

	@Inject
	private IEventBroker eventBroker;

	private ServiceLocator serviceLocator;

	private UIExtensionTracker tracker;
	private IPerspectiveRegistry perspectiveRegistry;
	private IViewRegistry viewRegistry;
	private WorkingSetManager workingSetManager;
	private IThemeManager themeManager;

	private ListenerList workbenchListeners = new ListenerList();
	private ListenerList windowListeners = new ListenerList();

	private WorkbenchAdvisor wbAdvisor;

	private WorkbenchActivitySupport activitySupport;

	Workbench() {
		// prevent external initialization
	}

	public MApplication getApplication() {
		return application;
	}

	@PostConstruct
	void postConstruct() throws InstantiationException, InvocationTargetException {
		ServiceLocatorCreator slc = new ServiceLocatorCreator();
		serviceLocator = (ServiceLocator) slc.createServiceLocator(null, null, new IDisposable() {
			public void dispose() {
				final Display display = getDisplay();
				if (display != null && !display.isDisposed()) {
					MessageDialog.openInformation(null,
							WorkbenchMessages.Workbench_NeedsClose_Title,
							WorkbenchMessages.Workbench_NeedsClose_Message);
					close();
				}
			}
		});
		serviceLocator.setContext(application.getContext());
		serviceLocator.registerService(IServiceLocatorCreator.class, slc);


		viewRegistry = (IViewRegistry) ContextInjectionFactory.make(ViewRegistry.class, application
				.getContext());
		perspectiveRegistry = (IPerspectiveRegistry) ContextInjectionFactory.make(
				PerspectiveRegistry.class, application.getContext());

		IBindingService bindingService = (IBindingService) ContextInjectionFactory.make(
				BindingService.class,
				application.getContext());
		application.getContext().set(IBindingService.class.getName(), bindingService);

		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.CHILDREN), new EventHandler() {
			public void handleEvent(Event event) {
				if (application == event.getProperty(UIEvents.EventTags.ELEMENT)) {
					if (UIEvents.EventTypes.REMOVE.equals(event
							.getProperty(UIEvents.EventTags.TYPE))) {
						MWindow window = (MWindow) event.getProperty(UIEvents.EventTags.OLD_VALUE);
						IWorkbenchWindow wwindow = (IWorkbenchWindow) window.getContext().get(
								IWorkbenchWindow.class.getName());
						if (wwindow != null) {
							fireWindowClosed(wwindow);
						}
					}
				}
			}
		});

		application.getContext().set(
				IWorkbenchLocationService.class.getName(),
				new WorkbenchLocationService(IServiceScopes.WORKBENCH_SCOPE, this, null, null,
						null,
						null, 0));
		JFaceUtil.initializeJFacePreferences();
		initializeApplicationColors();
	}

	private void initializeApplicationColors() {
		ColorDefinition[] colorDefinitions = WorkbenchPlugin.getDefault().getThemeRegistry()
				.getColors();
		ThemeElementHelper.populateRegistry(
				getThemeManager().getTheme(IThemeManager.DEFAULT_THEME), colorDefinitions, PrefUtil
						.getInternalPreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getDisplay()
	 */
	public Display getDisplay() {
		if (application != null && application.getChildren().size() > 0) {
			MWindow window = application.getChildren().get(0);
			Widget widget = (Widget) window.getWidget();
			return widget == null ? Display.getCurrent() : widget.getDisplay();
		}
		return Display.getCurrent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getProgressService()
	 */
	public IProgressService getProgressService() {
		// FIXME compat getProgressService
		E4Util.unsupported("getProgressService"); //$NON-NLS-1$
		return new IProgressService() {

			public void showInDialog(Shell shell, Job job) {
				E4Util.unsupported("showInDialog"); //$NON-NLS-1$
			}

			public void runInUI(IRunnableContext context, IRunnableWithProgress runnable,
					ISchedulingRule rule) throws InvocationTargetException, InterruptedException {
				E4Util.unsupported("runInUI"); //$NON-NLS-1$
				runnable.run(new NullProgressMonitor());
			}

			public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
					throws InvocationTargetException, InterruptedException {
				E4Util.unsupported("run"); //$NON-NLS-1$
				runnable.run(new NullProgressMonitor());
			}

			public void registerIconForFamily(ImageDescriptor icon, Object family) {
				E4Util.unsupported("registerIconForFamily"); //$NON-NLS-1$
			}

			public int getLongOperationTime() {
				E4Util.unsupported("getLongOperationTime"); //$NON-NLS-1$
				return 0;
			}

			public Image getIconFor(Job job) {
				E4Util.unsupported("getIconFor"); //$NON-NLS-1$
				return null;
			}

			public void busyCursorWhile(IRunnableWithProgress runnable)
					throws InvocationTargetException, InterruptedException {
				E4Util.unsupported("busyCursorWhile"); //$NON-NLS-1$
				runnable.run(new NullProgressMonitor());
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.IWorkbench#addWorkbenchListener(org.eclipse.ui.
	 * IWorkbenchListener)
	 */
	public void addWorkbenchListener(IWorkbenchListener listener) {
		workbenchListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.IWorkbench#removeWorkbenchListener(org.eclipse.ui.
	 * IWorkbenchListener)
	 */
	public void removeWorkbenchListener(IWorkbenchListener listener) {
		workbenchListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbench#addWindowListener(org.eclipse.ui.IWindowListener
	 * )
	 */
	public void addWindowListener(IWindowListener listener) {
		windowListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbench#removeWindowListener(org.eclipse.ui.IWindowListener
	 * )
	 */
	public void removeWindowListener(IWindowListener listener) {
		windowListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#close()
	 */
	public boolean close() {
		// FIXME compat close
		E4Util.unsupported("close"); //$NON-NLS-1$
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getActiveWorkbenchWindow()
	 */
	public IWorkbenchWindow getActiveWorkbenchWindow() {
		// Return null if called from a non-UI thread.
		// This is not spec'ed behaviour and is misleading, however this is how
		// it
		// worked in 2.1 and we cannot change it now.
		// For more details, see [Bug 57384] [RCP] Main window not active on
		// startup
		if (Display.getCurrent() == null) {
			return null;
		}

		MWindow activeWindow = application.getSelectedElement();
		if (activeWindow == null && !application.getChildren().isEmpty()) {
			activeWindow = application.getChildren().get(0);
		}

		return createWorkbenchWindow(activeWindow);
	}

	IWorkbenchWindow createWorkbenchWindow(MWindow window) {
		IEclipseContext windowContext = window.getContext();
		IWorkbenchWindow result = (IWorkbenchWindow) windowContext.get(IWorkbenchWindow.class
				.getName());
		if (result == null) {
			result = new WorkbenchWindow(ResourcesPlugin.getWorkspace().getRoot(),
					getPerspectiveRegistry().findPerspectiveWithId(
							getPerspectiveRegistry().getDefaultPerspective()));
			ContextInjectionFactory.inject(result, windowContext);
			windowContext.set(IWorkbenchWindow.class.getName(), result);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getEditorRegistry()
	 */
	public IEditorRegistry getEditorRegistry() {
		return WorkbenchPlugin.getDefault().getEditorRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getOperationSupport()
	 */
	public IWorkbenchOperationSupport getOperationSupport() {
		return WorkbenchPlugin.getDefault().getOperationSupport();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getPerspectiveRegistry()
	 */
	public IPerspectiveRegistry getPerspectiveRegistry() {
		return perspectiveRegistry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getPreferenceManager()
	 */
	public PreferenceManager getPreferenceManager() {
		return WorkbenchPlugin.getDefault().getPreferenceManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getPreferenceStore()
	 */
	public IPreferenceStore getPreferenceStore() {
		// FIXME compat getPreferenceStore
		E4Util.unsupported("getPreferenceStore"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getSharedImages()
	 */
	public ISharedImages getSharedImages() {
		return WorkbenchPlugin.getDefault().getSharedImages();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getWorkbenchWindowCount()
	 */
	public int getWorkbenchWindowCount() {
		return getWorkbenchWindows().length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getWorkbenchWindows()
	 */
	public IWorkbenchWindow[] getWorkbenchWindows() {
		List<IWorkbenchWindow> windows = new ArrayList<IWorkbenchWindow>();
		for (MWindow window : application.getChildren()) {
			IEclipseContext context = window.getContext();
			IWorkbenchWindow wwindow = (IWorkbenchWindow) context.get(IWorkbenchWindow.class
					.getName());
			if (wwindow != null) {
				windows.add(wwindow);
			}
		}
		return windows.toArray(new IWorkbenchWindow[windows.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getWorkingSetManager()
	 */
	public IWorkingSetManager getWorkingSetManager() {
		if (workingSetManager == null) {
			workingSetManager = new WorkingSetManager(WorkbenchPlugin.getDefault()
					.getBundleContext());
			workingSetManager.restoreState();
		}
		return workingSetManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#createLocalWorkingSetManager()
	 */
	public ILocalWorkingSetManager createLocalWorkingSetManager() {
		// FIXME compat createLocalWorkingSetManager
		E4Util.unsupported("createLocalWorkingSetManager"); //$NON-NLS-1$
		return null;
	}

	private void fireWindowOpened(IWorkbenchWindow window) {
		for (Object listener : windowListeners.getListeners()) {
			((IWindowListener) listener).windowOpened(window);
		}
	}

	private void fireWindowClosed(IWorkbenchWindow window) {
		for (Object listener : windowListeners.getListeners()) {
			((IWindowListener) listener).windowClosed(window);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#openWorkbenchWindow(java.lang.String,
	 * org.eclipse.core.runtime.IAdaptable)
	 */
	public IWorkbenchWindow openWorkbenchWindow(String perspectiveId,
			IAdaptable input) throws WorkbenchException {
		IPerspectiveDescriptor descriptor = getPerspectiveRegistry().findPerspectiveWithId(
				perspectiveId);
		if (descriptor == null) {
			throw new WorkbenchException(NLS.bind(
					WorkbenchMessages.WorkbenchPage_ErrorCreatingPerspective, perspectiveId));
		}

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		return openWorkbenchWindow(input, descriptor, window);
	}

	public WorkbenchWindow openWorkbenchWindow(IAdaptable input, IPerspectiveDescriptor descriptor,
			MWindow window) {
		WorkbenchWindow result = new WorkbenchWindow(input, descriptor);
		ContextInjectionFactory.inject(result, window.getContext());
		fireWindowOpened(result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbench#openWorkbenchWindow(org.eclipse.core.runtime
	 * .IAdaptable)
	 */
	public IWorkbenchWindow openWorkbenchWindow(IAdaptable input)
			throws WorkbenchException {
		return openWorkbenchWindow(getPerspectiveRegistry().getDefaultPerspective(), input);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#restart()
	 */
	public boolean restart() {
		// FIXME compat restart
		E4Util.unsupported("restart"); //$NON-NLS-1$
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#showPerspective(java.lang.String,
	 * org.eclipse.ui.IWorkbenchWindow)
	 */
	public IWorkbenchPage showPerspective(String perspectiveId,
			IWorkbenchWindow window) throws WorkbenchException {
		return showPerspective(perspectiveId, window, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#showPerspective(java.lang.String,
	 * org.eclipse.ui.IWorkbenchWindow, org.eclipse.core.runtime.IAdaptable)
	 */
	public IWorkbenchPage showPerspective(String perspectiveId,
			IWorkbenchWindow targetWindow, IAdaptable input)
			throws WorkbenchException {
		Assert.isNotNull(perspectiveId);
		IPerspectiveDescriptor targetPerspective = getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
		if (targetPerspective == null) {
			throw new WorkbenchException(NLS.bind(
					WorkbenchMessages.WorkbenchPage_ErrorCreatingPerspective, perspectiveId));
		}
		
		if (targetWindow != null) {
			IWorkbenchPage page = targetWindow.getActivePage();
			if (activate(perspectiveId, page, input, true)) {
				return page;
			}
		}

		for (IWorkbenchWindow window : getWorkbenchWindows()) {
			IWorkbenchPage page = window.getActivePage();
			if (activate(perspectiveId, page, input, true)) {
				return page;
			}
		}
		
		if (targetWindow != null) {
			IWorkbenchPage page = targetWindow.getActivePage();
			if (activate(perspectiveId, page, input, false)) {
				return page;
			}
			IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
			int mode = store.getInt(IPreferenceConstants.OPEN_PERSP_MODE);

			if (IPreferenceConstants.OPM_NEW_WINDOW != mode) {
				targetWindow.getShell().open();
				if (page == null) {
					page = targetWindow.openPage(perspectiveId, input);
				} else {
					page.setPerspective(targetPerspective);
				}
				return page;
			}
		}

		return openWorkbenchWindow(perspectiveId, input).getActivePage();
	}

	private boolean activate(String perspectiveId, IWorkbenchPage page, IAdaptable input,
			boolean checkPerspective) {
		if (page != null) {
			for (IPerspectiveDescriptor openedPerspective : page.getOpenPerspectives()) {
				if (!checkPerspective || openedPerspective.getId().equals(perspectiveId)) {
					if (page.getInput() == input) {
						WorkbenchWindow wwindow = (WorkbenchWindow) page.getWorkbenchWindow();
						MWindow model = wwindow.getModel();
						application.setSelectedElement(model);
						return true;
					}
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getDecoratorManager()
	 */
	public IDecoratorManager getDecoratorManager() {
		return WorkbenchPlugin.getDefault().getDecoratorManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#saveAllEditors(boolean)
	 */
	public boolean saveAllEditors(boolean confirm) {
		boolean success = true;
		for (IWorkbenchWindow window : getWorkbenchWindows()) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				if (page.saveAllEditors(confirm)) {
					success = false;
				}
			}
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getElementFactory(java.lang.String)
	 */
	public IElementFactory getElementFactory(String factoryId) {
		return WorkbenchPlugin.getDefault().getElementFactory(factoryId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getActivitySupport()
	 */
	public IWorkbenchActivitySupport getActivitySupport() {
		if (activitySupport == null) {
			activitySupport = new WorkbenchActivitySupport();
		}
		return activitySupport;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getCommandSupport()
	 */
	public IWorkbenchCommandSupport getCommandSupport() {
		// FIXME compat getCommandSupport
		E4Util.unsupported("getCommandSupport"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getContextSupport()
	 */
	public IWorkbenchContextSupport getContextSupport() {
		// FIXME compat getContextSupport
		E4Util.unsupported("getContextSupport"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getThemeManager()
	 */
	public IThemeManager getThemeManager() {
		if (themeManager == null) {
			themeManager = WorkbenchThemeManager.getInstance();
		}
		return themeManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getIntroManager()
	 */
	public IIntroManager getIntroManager() {
		// FIXME compat getIntroManager
		E4Util.unsupported("getIntroManager"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getHelpSystem()
	 */
	public IWorkbenchHelpSystem getHelpSystem() {
		return WorkbenchHelpSystem.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getBrowserSupport()
	 */
	public IWorkbenchBrowserSupport getBrowserSupport() {
		// FIXME compat getBrowserSupport
		E4Util.unsupported("getBrowserSupport"); //$NON-NLS-1$
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#isStarting()
	 */
	public boolean isStarting() {
		// FIXME compat isStarting
		E4Util.unsupported("isStarting"); //$NON-NLS-1$
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#isClosing()
	 */
	public boolean isClosing() {
		// FIXME compat isClosing
		E4Util.unsupported("isClosing"); //$NON-NLS-1$
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getExtensionTracker()
	 */
	public IExtensionTracker getExtensionTracker() {
		if (tracker == null) {
			tracker = new UIExtensionTracker(getDisplay());
		}
		return tracker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getViewRegistry()
	 */
	public IViewRegistry getViewRegistry() {
		return viewRegistry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getNewWizardRegistry()
	 */
	public IWizardRegistry getNewWizardRegistry() {
		return WorkbenchPlugin.getDefault().getNewWizardRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getImportWizardRegistry()
	 */
	public IWizardRegistry getImportWizardRegistry() {
		return WorkbenchPlugin.getDefault().getImportWizardRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getExportWizardRegistry()
	 */
	public IWizardRegistry getExportWizardRegistry() {
		return WorkbenchPlugin.getDefault().getExportWizardRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbench#saveAll(org.eclipse.jface.window.IShellProvider
	 * , org.eclipse.jface.operation.IRunnableContext,
	 * org.eclipse.ui.ISaveableFilter, boolean)
	 */
	public boolean saveAll(IShellProvider shellProvider,
			IRunnableContext runnableContext, ISaveableFilter filter,
			boolean confirm) {
		// FIXME compat saveAll
		E4Util.unsupported("saveAll"); //$NON-NLS-1$
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return serviceLocator.getService(adapter);
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

	/**
	 * @return
	 */
	public synchronized static IWorkbench getInstance() {
		if (instance == null) {
			IEclipseContext serviceContext = org.eclipse.e4.workbench.ui.internal.E4Workbench
					.getServiceContext();
			if (serviceContext.getLocal(MApplication.class.getName()) == null) {
				return null;
			}

			MApplication application = (MApplication) serviceContext.get(MApplication.class
					.getName());

			IEclipseContext appContext = application.getContext();
			instance = new Workbench();
			ContextInjectionFactory.inject(instance, appContext);
			appContext.set(IWorkbench.class.getName(), instance);
			initializeLegacyServices(appContext);
			running = true;
		}
		return instance;
	}

	/**
	 * @param appContext
	 */
	private static void initializeLegacyServices(IEclipseContext appContext) {
		initializeCommandService(appContext);
		initializeCommandImageService(appContext);
		initializeHandlerService(appContext);
		appContext.set(IMenuService.class.getName(), new FakeMenuService());
	}

	/**
	 * @param appContext
	 */
	private static void initializeHandlerService(IEclipseContext appContext) {
		final LegacyHandlerService service = new LegacyHandlerService(appContext);
		appContext.set(IHandlerService.class.getName(), service);
		service.readRegistry();
	}

	/**
	 * @param appContext
	 */
	private static void initializeCommandImageService(IEclipseContext appContext) {
		final CommandImageManager commandImageManager = new CommandImageManager();
		final CommandImageService commandImageService = new CommandImageService(
				commandImageManager, (ICommandService) appContext.get(ICommandService.class
						.getName()));
		commandImageService.readRegistry();
		appContext.set(ICommandImageService.class.getName(), commandImageService);
	}

	static class MakeHandlersGo extends AbstractHandler {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands
		 * .ExecutionEvent)
		 */
		public Object execute(ExecutionEvent event) throws ExecutionException {
			org.eclipse.e4.workbench.ui.internal.Activator.trace(
					org.eclipse.e4.workbench.ui.internal.Policy.DEBUG_CMDS,
					"AllHandlerGo: not for executing", null); //$NON-NLS-1$
			return null;
		}

	}

	private static void initializeCommandService(IEclipseContext appContext) {
		MApplication app = (MApplication) appContext.get(MApplication.class.getName());
		CommandManager manager = (CommandManager) appContext.get(CommandManager.class.getName());

		// save the e4 commands, just in case
		ArrayList<MCommand> existing = new ArrayList<MCommand>(app.getCommands());
		HashSet<String> existingIds = new HashSet<String>();
		for (MCommand mCommand : existing) {
			existingIds.add(mCommand.getId());
		}

		CommandService service = new CommandService(manager);
		service.readRegistry();
		appContext.set(ICommandService.class.getName(), service);

		// put back the e4 commands
		E4CommandProcessor.processCommands(appContext, existing);

		MakeHandlersGo allHandlers = new MakeHandlersGo();


		Category[] definedCategories = manager.getDefinedCategories();
		for (int i = 0; i < definedCategories.length; i++) {
			// must match definition in CommandServiceImpl
			appContext.set(CommandServiceImpl.CAT_ID + definedCategories[i].getId(),
					definedCategories[i]);
		}

		Command[] cmds = manager.getAllCommands();
		for (int i = 0; i < cmds.length; i++) {
			Command cmd = cmds[i];
			final String cmdId = cmd.getId();
			if (cmdId.contains("(")) { //$NON-NLS-1$
				org.eclipse.e4.workbench.ui.internal.Activator.trace(
						org.eclipse.e4.workbench.ui.internal.Policy.DEBUG_CMDS,
						"Invalid command: " + cmd, null); //$NON-NLS-1$
				continue;
			}
			if (existingIds.contains(cmdId)) {
				// these were added back above.
				continue;
			}
			cmd.setHandler(allHandlers);

			// must match definition in CommandServiceImpl
			appContext.set(CommandServiceImpl.CMD_ID + cmdId, cmd);
			MCommand mcmd = MApplicationFactory.eINSTANCE.createCommand();
			mcmd.setId(cmdId);
			try {
				mcmd.setCommandName(cmd.getName());
			} catch (NotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			app.getCommands().add(mcmd);
		}

	}

	public WorkbenchAdvisor getAdvisor() {
		if (wbAdvisor == null) {
			wbAdvisor = new WorkbenchAdvisor() {
				@Override
				public String getInitialWindowPerspectiveId() {
					return PrefUtil.getAPIPreferenceStore().getString(
							IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID);
				}
			};
		}
		return wbAdvisor;
	}

	static boolean running = true;

	public static boolean isWorkbenchRunning() {
		return running;
	}
}
