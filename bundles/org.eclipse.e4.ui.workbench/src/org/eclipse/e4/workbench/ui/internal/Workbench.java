/*******************************************************************************
 * Copyright (c) 2008, 2009 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.workbench.ui.internal;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.internal.runtime.PlatformURLPluginConnection;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.internal.services.ActiveContextsFunction;
import org.eclipse.e4.ui.internal.services.ContextCommandService;
import org.eclipse.e4.ui.model.application.ApplicationFactory;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MContributedPart;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.model.workbench.MWorkbenchWindow;
import org.eclipse.e4.ui.model.workbench.WorkbenchPackage;
import org.eclipse.e4.ui.services.ECommandService;
import org.eclipse.e4.ui.services.EHandlerService;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.IExceptionHandler;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.e4.workbench.ui.IWorkbenchWindowHandler;
import org.eclipse.e4.workbench.ui.renderers.PartFactory;
import org.eclipse.e4.workbench.ui.renderers.PartRenderer;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

public class Workbench implements IWorkbench {
	public static final String LOCAL_ACTIVE_SHELL = "localActiveShell"; //$NON-NLS-1$
	public static final String ID = "org.eclipse.e4.workbench.fakedWBWindow"; //$NON-NLS-1$
	private MApplication<? extends MWindow> workbench;
	private static final boolean saveAndRestore = true;
	private File workbenchData;
	private IWorkbenchWindowHandler windowHandler;
	private final IExtensionRegistry registry;
	private ResourceSetImpl resourceSet;

	public IEclipseContext getContext() {
		return workbenchContext;
	}

	// UI Construction...
	private PartRenderer renderer;
	private int rv;

	private ExceptionHandler exceptionHandler;
	private IEclipseContext workbenchContext;
	private ReflectionContributionFactory contributionFactory;

	public Workbench(Location instanceLocation, IExtensionRegistry registry,
			PackageAdmin packageAdmin, IEclipseContext applicationContext,
			IWorkbenchWindowHandler windowHandler) {
		this.windowHandler = windowHandler;
		exceptionHandler = new ExceptionHandler();
		this.registry = registry;
		try {
			workbenchData = new File(instanceLocation.getURL().toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		workbenchData = new File(workbenchData, ".metadata"); //$NON-NLS-1$
		workbenchData = new File(workbenchData, ".plugins"); //$NON-NLS-1$
		workbenchData = new File(workbenchData, "org.eclipse.e4.workbench"); //$NON-NLS-1$
		workbenchData = new File(workbenchData, "workbench.xmi"); //$NON-NLS-1$

		contributionFactory = new ReflectionContributionFactory(registry);
		resourceSet = new ResourceSetImpl();

		// Register the appropriate resource factory to handle all file
		// extensions.
		//
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
						new XMIResourceFactoryImpl());

		// Register the package to ensure it is available during loading.
		//
		resourceSet.getPackageRegistry().put(WorkbenchPackage.eNS_URI,
				WorkbenchPackage.eINSTANCE);

		workbenchContext = createWorkbenchContext(applicationContext, registry,
				exceptionHandler, contributionFactory);
		workbenchContext.set(IWorkbench.class.getName(), this);
	}

	public void setWorkbenchModel(MApplication<MWindow<?>> model) {
		workbench = model;
		init();
	}

	public void setWorkbenchModelURI(URI workbenchXmiURI) {
		createWorkbenchModel(workbenchXmiURI);
	}

	public static IEclipseContext createWorkbenchContext(
			final IEclipseContext applicationContext,
			IExtensionRegistry registry, IExceptionHandler exceptionHandler,
			IContributionFactory contributionFactory) {
		final IEclipseContext mainContext = EclipseContextFactory.create(
				applicationContext, UISchedulerStrategy.getInstance());
		mainContext.set(Logger.class.getName(), ContextInjectionFactory.inject(
				new WorkbenchLogger(), mainContext));
		mainContext.set(IContextConstants.DEBUG_STRING, "globalContext"); //$NON-NLS-1$

		// setup for commands and handlers
		if (contributionFactory != null) {
			mainContext.set(IContributionFactory.class.getName(),
					contributionFactory);
		}
		mainContext.set(CommandManager.class.getName(), new CommandManager());
		mainContext.set(ContextManager.class.getName(), new ContextManager());
		mainContext.set(ECommandService.class.getName(),
				new ContextCommandService(mainContext));
		mainContext.set(IServiceConstants.ACTIVE_CONTEXTS,
				new ActiveContextsFunction());
		// EHandlerService comes from a ContextFunction
		// EContextService comes from a ContextFunction

		IConfigurationElement[] contributions = registry
				.getConfigurationElementsFor("org.eclipse.e4.services"); //$NON-NLS-1$
		for (IConfigurationElement contribution : contributions) {
			try {
				for (IConfigurationElement serviceElement : contribution
						.getChildren("service")) { //$NON-NLS-1$
					Object factory = contribution
							.createExecutableExtension("class"); //$NON-NLS-1$
					String apiClassname = serviceElement.getAttribute("api"); //$NON-NLS-1$
					mainContext.set(apiClassname, factory);
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		mainContext.set(IExceptionHandler.class.getName(), exceptionHandler);
		mainContext.set(IExtensionRegistry.class.getName(), registry);
		mainContext.set(IServiceConstants.SELECTION,
				new ActiveChildOutputFunction(IServiceConstants.SELECTION));
		mainContext.set(IServiceConstants.INPUT, new ContextFunction() {
			public Object compute(IEclipseContext context, Object[] arguments) {
				Class adapterType = null;
				if (arguments.length > 0 && arguments[0] instanceof Class) {
					adapterType = (Class) arguments[0];
				}
				Object newInput = null;
				Object newValue = mainContext.get(IServiceConstants.SELECTION);
				if (adapterType == null || adapterType.isInstance(newValue)) {
					newInput = newValue;
				} else if (newValue != null && adapterType != null) {
					IAdapterManager adapters = (IAdapterManager) applicationContext
							.get(IAdapterManager.class.getName());
					if (adapters != null) {
						Object adapted = adapters.loadAdapter(newValue,
								adapterType.getName());
						if (adapted != null) {
							newInput = adapted;
						}
					}
				}
				return newInput;
			}
		});
		mainContext.set(IServiceConstants.ACTIVE_SHELL,
				new ActiveChildLookupFunction(IServiceConstants.ACTIVE_SHELL,
						LOCAL_ACTIVE_SHELL));

		return mainContext;
	}

	private MApplication<? extends MWindow> createWorkbenchModel(
			URI applicationDefinitionInstance) {
		URI restoreLocation = null;
		if (workbenchData != null && workbenchData.exists() && saveAndRestore) {
			restoreLocation = URI
					.createFileURI(workbenchData.getAbsolutePath());
		}
		long restoreLastModified = restoreLocation == null ? 0L : new File(
				restoreLocation.toFileString()).lastModified();

		long appLastModified = 0L;

		ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
		Map<String, ?> attributes = resourceSetImpl
				.getURIConverter()
				.getAttributes(
						applicationDefinitionInstance,
						Collections
								.singletonMap(
										URIConverter.OPTION_REQUESTED_ATTRIBUTES,
										Collections
												.singleton(URIConverter.ATTRIBUTE_TIME_STAMP)));
		Object timestamp = attributes.get(URIConverter.ATTRIBUTE_TIME_STAMP);
		if (timestamp instanceof Long) {
			appLastModified = ((Long) timestamp).longValue();
		} else if (applicationDefinitionInstance.isPlatformPlugin()) {
			try {
				java.net.URL url = new java.net.URL(
						applicationDefinitionInstance.toString());
				Object[] obj = PlatformURLPluginConnection.parse(url.getFile()
						.trim(), url);
				Bundle b = (Bundle) obj[0];
				URLConnection openConnection = b.getResource((String) obj[1])
						.openConnection();
				appLastModified = openConnection.getLastModified();
			} catch (Exception e) {
				// ignore
			}
		}

		// new java.util.Date(appLastModified)
		boolean restore = restoreLastModified > appLastModified;

		if (restore) {
			System.err.println("Restoring workbench: " + restoreLocation); //$NON-NLS-1$
			workbench = (MApplication<MWindow>) resourceSetImpl.getResource(
					restoreLocation, true).getContents().get(0);
		} else {
			System.err
					.println("Initializing workbench: " + applicationDefinitionInstance); //$NON-NLS-1$
			Resource resource = new XMIResourceImpl();
			workbench = loadDefaultModel(applicationDefinitionInstance);
			resource.getContents().add((EObject) workbench);
			resource.setURI(restoreLocation);
		}

		init();

		return workbench;
	}

	private MApplication<? extends MWindow> loadDefaultModel(
			URI defaultModelPath) {
		Resource resource = new ResourceSetImpl().getResource(defaultModelPath,
				true);
		MApplication<MWindow> app = (MApplication<MWindow>) resource
				.getContents().get(0);

		final EList<MWindow> windows = app.getWindows();
		for (MWindow window : windows) {
			processPartContributions(resource, window);
		}

		return app;
	}

	private void processPartContributions(Resource resource, MWindow mWindow) {
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		String extId = "org.eclipse.e4.workbench.parts"; //$NON-NLS-1$
		IConfigurationElement[] parts = registry
				.getConfigurationElementsFor(extId);

		for (int i = 0; i < parts.length; i++) {
			MContributedPart<?> part = ApplicationFactory.eINSTANCE
					.createMContributedPart();
			part.setName(parts[i].getAttribute("label")); //$NON-NLS-1$
			part.setIconURI("platform:/plugin/" //$NON-NLS-1$
					+ parts[i].getContributor().getName() + "/" //$NON-NLS-1$
					+ parts[i].getAttribute("icon")); //$NON-NLS-1$
			part.setURI("platform:/plugin/" //$NON-NLS-1$
					+ parts[i].getContributor().getName() + "/" //$NON-NLS-1$
					+ parts[i].getAttribute("class")); //$NON-NLS-1$
			String parentId = parts[i].getAttribute("parentId"); //$NON-NLS-1$

			MPart parent = (MPart) findObject(resource.getAllContents(),
					parentId);
			if (parent != null) {
				parent.getChildren().add(part);
			}
		}

	}

	private EObject findObject(TreeIterator<EObject> it, String id) {
		while (it.hasNext()) {
			EObject el = it.next();
			if (el instanceof MApplicationElement) {
				if (el.eResource().getURIFragment(el).equals(id)) {
					return el;
				}
			}
		}

		return null;
	}

	private void init() {
		// Capture the MApplication into the context
		workbenchContext.set(MApplication.class.getName(), workbench);
		workbench.setContext(workbenchContext);

		// fill in commands
		System.err.println("workbench init commands"); //$NON-NLS-1$
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		Category cat = cs.getCategory(MApplication.class.getName());
		cat.define("Application Category", null); //$NON-NLS-1$
		EList<MCommand> commands = workbench.getCommand();
		for (MCommand cmd : commands) {
			String id = cmd.getId();
			String name = cmd.getName();
			Command command = cs.getCommand(id);
			command.define(name, null, cat);
		}

		// take care of generating the contexts.
		for (MWindow window : (EList<? extends MWindow>) workbench.getWindows()) {
			initializeContext(workbenchContext, window);
		}
		workbench.eAdapters().add(new AdapterImpl() {
			@Override
			public void notifyChanged(Notification msg) {
				if (ApplicationPackage.Literals.MAPPLICATION__WINDOWS
						.equals(msg.getFeature())
						&& msg.getEventType() == Notification.ADD) {
					MPart<?> added = (MPart<?>) msg.getNewValue();
					initializeContext(workbenchContext, added);
				}
			}
		});
	}

	/**
	 * Create the context chain. It both creates the chain for the current
	 * model, and adds eAdapters so it can add new contexts when new model items
	 * are added.
	 * 
	 * @param parentContext
	 *            The parent context
	 * @param part
	 *            needs a context created
	 */
	public static void initializeContext(IEclipseContext parentContext,
			MPart<?> part) {
		final IEclipseContext context;
		if (part.getContext() != null) {
			context = part.getContext();
		} else {
			context = EclipseContextFactory.create(parentContext,
					UISchedulerStrategy.getInstance());
		}

		// fill in the interfaces, so MContributedPart.class.getName() will
		// return the model element, for example.
		final Class[] interfaces = part.getClass().getInterfaces();
		for (Class intf : interfaces) {
			System.err.println("add intf: " + intf.getName()); //$NON-NLS-1$
			context.set(intf.getName(), part);
		}

		part.setContext(context);

		// take care of generating the contexts.
		for (MPart<?> child : (EList<MPart<?>>) part.getChildren()) {
			initializeContext(context, child);
		}
		part.eAdapters().add(new AdapterImpl() {
			@Override
			public void notifyChanged(Notification msg) {
				if (ApplicationPackage.Literals.MPART__CHILDREN.equals(msg
						.getFeature())
						&& msg.getEventType() == Notification.ADD) {
					MPart<?> added = (MPart<?>) msg.getNewValue();
					initializeContext(context, added);
				}
			}
		});
	}

	/**
	 * Should be called prior to running the e4 workench.
	 */
	public void createUIFromModel() {
		final EList<? extends MWindow> windows = workbench.getWindows();
		for (MWindow wbw : windows) {
			createGUI(wbw);
		}
	}

	public int run() {
		windowHandler.runEvenLoop(workbench.getWindows().get(0).getWidget());

		if (workbenchData != null && saveAndRestore && workbench != null) {
			try {
				System.err.println("Saving workbench: " //$NON-NLS-1$
						+ ((EObject) workbench).eResource().getURI());
				// workbenchData.getParentFile().mkdirs();
				// workbenchData.createNewFile();
				// FileOutputStream fos = new FileOutputStream(workbenchData);
				// ((EObject)workbench).eResource().save(fos, null);
				// fos.close();
				((EObject) workbench).eResource().save(null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return rv;
	}

	private void processHandlers(MPart<MPart<?>> part) {
		IEclipseContext context = part.getContext();
		if (context != null) {
			EHandlerService hs = (EHandlerService) context
					.get(EHandlerService.class.getName());
			EList<MHandler> handlers = part.getHandlers();
			for (MHandler handler : handlers) {
				String commandId = handler.getCommand().getId();
				if (handler.getObject() == null) {
					handler.setObject(contributionFactory.create(handler
							.getURI(), context));
				}
				hs.activateHandler(commandId, handler.getObject());
			}
		}
		EList<MPart<?>> children = part.getChildren();
		for (MPart<?> child : children) {
			processHandlers((MPart<MPart<?>>) child);
		}
	}

	/**
	 * Initialize a part renderer from the extension point.
	 * 
	 * @param registry
	 *            the registry for the EP
	 * @param r
	 *            the created renderer
	 * @param context
	 *            the context for the part factories
	 * @param f
	 *            the IContributionFactory already provided to <code>r</code>
	 */
	public static void initializeRenderer(IExtensionRegistry registry,
			PartRenderer r, IEclipseContext context, IContributionFactory f) {
		// add the factories from the extension point, sort by dependency
		// * Need to make the EP more declarative to avoid aggressive
		// loading
		IConfigurationElement[] factories = registry
				.getConfigurationElementsFor("org.eclipse.e4.workbench.partfactory"); //$NON-NLS-1$

		// Sort the factories based on their dependence
		// This is a hack, should be based on plug-in dependencies
		int offset = 0;
		for (int i = 0; i < factories.length; i++) {
			String clsSpec = factories[i].getAttribute("class"); //$NON-NLS-1$
			if (clsSpec.indexOf("Legacy") >= 0 //$NON-NLS-1$
					|| clsSpec.indexOf("PartSash") >= 0) { //$NON-NLS-1$
				IConfigurationElement tmp = factories[offset];
				factories[offset++] = factories[i];
				factories[i] = tmp;
			}
		}

		for (int i = 0; i < factories.length; i++) {
			PartFactory factory = null;
			try {
				factory = (PartFactory) factories[i]
						.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				e.printStackTrace();
			}
			if (factory != null) {
				factory.init(r, context, f);
				r.addPartFactory(factory);
			}
		}
	}

	public void createGUI(MWindow wbw) {
		if (renderer == null) {
			renderer = new PartRenderer(contributionFactory, workbenchContext);
			initializeRenderer(registry, renderer, workbenchContext,
					contributionFactory);

		}

		renderer.createGui(wbw);
		Object appWindow = wbw.getWidget();

		rv = 0;
		// TODO get access to IApplicationContext to call
		// applicationRunning()
		// Platform.endSplash();

		// A position of 0 is not possible on OS-X because then the
		// title-bar is
		// hidden
		// below the MMenu-Bar
		windowHandler.setBounds(appWindow, wbw.getX(), wbw.getY(), wbw
				.getWidth(), wbw.getHeight());
		windowHandler.layout(appWindow);

		windowHandler.open(appWindow);
		processHandlers(wbw);
	}

	public void close() {
		final EList<? extends MWindow> windows = workbench.getWindows();
		for (MWindow window : windows) {
			windowHandler.dispose(window.getWidget());
		}
	}

	// public Display getDisplay() {
	// return appWindow.getDisplay();
	// }

	public void closeWindow(MWorkbenchWindow workbenchWindow) {
		windowHandler.close(workbenchWindow.getWidget());
	}

	public Object getWindow() {
		return workbench.getWindows().get(0).getWidget();
	}
}
