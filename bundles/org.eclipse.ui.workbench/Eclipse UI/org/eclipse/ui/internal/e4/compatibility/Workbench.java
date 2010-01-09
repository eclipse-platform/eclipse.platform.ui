/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.ILocalWorkingSetManager;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISaveableFilter;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;
import org.eclipse.ui.internal.registry.UIExtensionTracker;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipse.ui.views.IViewRegistry;
import org.eclipse.ui.wizards.IWizardRegistry;

/**
 * @since 3.5
 *
 */
public class Workbench implements IWorkbench {

	private static Workbench instance;

	@Inject
	private MApplication application;

	@Inject
	private IPresentationEngine engine;

	private UIExtensionTracker tracker;
	private IPerspectiveRegistry perspectiveRegistry = new PerspectiveRegistry();
	private IViewRegistry viewRegistry;

	Workbench() {
		// prevent external initialization
	}

	@PostConstruct
	void postConstruct() {
		try {
			viewRegistry = (IViewRegistry) ContextInjectionFactory.make(ViewRegistry.class,
					application.getContext());
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getDisplay()
	 */
	public Display getDisplay() {
		if (application.getChildren().size() > 0) {
			MWindow window = application.getChildren().get(0);
			Widget widget = (Widget) window.getWidget();
			return widget == null ? null : widget.getDisplay();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getProgressService()
	 */
	public IProgressService getProgressService() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.IWorkbench#addWorkbenchListener(org.eclipse.ui.
	 * IWorkbenchListener)
	 */
	public void addWorkbenchListener(IWorkbenchListener listener) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.IWorkbench#removeWorkbenchListener(org.eclipse.ui.
	 * IWorkbenchListener)
	 */
	public void removeWorkbenchListener(IWorkbenchListener listener) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbench#addWindowListener(org.eclipse.ui.IWindowListener
	 * )
	 */
	public void addWindowListener(IWindowListener listener) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbench#removeWindowListener(org.eclipse.ui.IWindowListener
	 * )
	 */
	public void removeWindowListener(IWindowListener listener) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#close()
	 */
	public boolean close() {
		// TODO Auto-generated method stub
		return false;
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

		Object activeChild = application.getActiveChild();
		MWindow activeWindow = (MWindow) activeChild;
		if (activeWindow == null && !application.getChildren().isEmpty()) {
			activeWindow = application.getChildren().get(0);
		}
		IEclipseContext windowContext = activeWindow
				.getContext();
		IWorkbenchWindow result = (IWorkbenchWindow) windowContext
				.get(
				IWorkbenchWindow.class.getName());
		if (result == null) {
			result = new WorkbenchWindow(null);
			ContextInjectionFactory.inject(result, windowContext);
			windowContext.set(IWorkbenchWindow.class.getName(), result);
			//			throw new RuntimeException("No workbench window found"); //$NON-NLS-1$
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
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getPreferenceStore()
	 */
	public IPreferenceStore getPreferenceStore() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getWorkbenchWindows()
	 */
	public IWorkbenchWindow[] getWorkbenchWindows() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getWorkingSetManager()
	 */
	public IWorkingSetManager getWorkingSetManager() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#createLocalWorkingSetManager()
	 */
	public ILocalWorkingSetManager createLocalWorkingSetManager() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#openWorkbenchWindow(java.lang.String,
	 * org.eclipse.core.runtime.IAdaptable)
	 */
	public IWorkbenchWindow openWorkbenchWindow(String perspectiveId,
			IAdaptable input) throws WorkbenchException {
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);
		engine.createGui(window);
		WorkbenchWindow result = new WorkbenchWindow(input);
		ContextInjectionFactory.inject(result, window.getContext());
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
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#showPerspective(java.lang.String,
	 * org.eclipse.ui.IWorkbenchWindow)
	 */
	public IWorkbenchPage showPerspective(String perspectiveId,
			IWorkbenchWindow window) throws WorkbenchException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#showPerspective(java.lang.String,
	 * org.eclipse.ui.IWorkbenchWindow, org.eclipse.core.runtime.IAdaptable)
	 */
	public IWorkbenchPage showPerspective(String perspectiveId,
			IWorkbenchWindow window, IAdaptable input)
			throws WorkbenchException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getElementFactory(java.lang.String)
	 */
	public IElementFactory getElementFactory(String factoryId) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getActivitySupport()
	 */
	public IWorkbenchActivitySupport getActivitySupport() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getCommandSupport()
	 */
	public IWorkbenchCommandSupport getCommandSupport() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getContextSupport()
	 */
	public IWorkbenchContextSupport getContextSupport() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getThemeManager()
	 */
	public IThemeManager getThemeManager() {
		return new IThemeManager() {

			public void setCurrentTheme(String id) {
				// TODO Auto-generated method stub

			}

			public void removePropertyChangeListener(IPropertyChangeListener listener) {
				// TODO Auto-generated method stub

			}

			public ITheme getTheme(String id) {
				return getCurrentTheme();
			}

			public ITheme getCurrentTheme() {
				return new ITheme() {

					public void removePropertyChangeListener(IPropertyChangeListener listener) {
						// TODO Auto-generated method stub

					}

					public Set keySet() {
						// TODO Auto-generated method stub
						return null;
					}

					public String getString(String key) {
						// TODO Auto-generated method stub
						return null;
					}

					public String getLabel() {
						// TODO Auto-generated method stub
						return null;
					}

					public int getInt(String key) {
						// TODO Auto-generated method stub
						return 0;
					}

					public String getId() {
						// TODO Auto-generated method stub
						return null;
					}

					public FontRegistry getFontRegistry() {
						// TODO Auto-generated method stub
						return null;
					}

					public ColorRegistry getColorRegistry() {
						// TODO Auto-generated method stub
						return null;
					}

					public boolean getBoolean(String key) {
						// TODO Auto-generated method stub
						return false;
					}

					public void dispose() {
						// TODO Auto-generated method stub

					}

					public void addPropertyChangeListener(IPropertyChangeListener listener) {
						// TODO Auto-generated method stub

					}
				};
			}

			public void addPropertyChangeListener(IPropertyChangeListener listener) {
				// TODO Auto-generated method stub

			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getIntroManager()
	 */
	public IIntroManager getIntroManager() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#isStarting()
	 */
	public boolean isStarting() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#isClosing()
	 */
	public boolean isClosing() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getImportWizardRegistry()
	 */
	public IWizardRegistry getImportWizardRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getExportWizardRegistry()
	 */
	public IWizardRegistry getExportWizardRegistry() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceLocator#getService(java.lang.Class)
	 */
	public Object getService(Class api) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceLocator#hasService(java.lang.Class)
	 */
	public boolean hasService(Class api) {
		// TODO Auto-generated method stub
		return false;
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

			try {
				IEclipseContext appContext = application
						.getContext();
				instance = (Workbench) ContextInjectionFactory.make(Workbench.class, appContext);
				appContext.set(IWorkbench.class.getName(), instance);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return instance;
	}

}
