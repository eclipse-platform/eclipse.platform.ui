/*******************************************************************************
 * Copyright (c) 2008 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Boris Bokowski, IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.workbench.ui.internal;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Map;
import org.eclipse.core.internal.runtime.PlatformURLPluginConnection;
import org.eclipse.core.runtime.*;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.*;
import org.eclipse.e4.ui.model.workbench.MWorkbenchWindow;
import org.eclipse.e4.ui.model.workbench.WorkbenchPackage;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.*;
import org.eclipse.e4.workbench.ui.renderers.PartFactory;
import org.eclipse.e4.workbench.ui.renderers.PartRenderer;
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
	public static final String ID = "org.eclipse.e4.workbench.fakedWBWindow"; //$NON-NLS-1$
	private MApplication<? extends MWindow> workbench;
	private static final boolean saveAndRestore = true;
	private File workbenchData;
	private IWorkbenchWindowHandler windowHandler;
	private Object appWindow;
	private final IExtensionRegistry registry;
	private ResourceSetImpl resourceSet;

	public IEclipseContext getContext() {
		return globalContext;
	}

	// UI Construction...
	private PartRenderer renderer;
	private int rv;

	private ExceptionHandler exceptionHandler;
	private IEclipseContext globalContext;
	private ReflectionContributionFactory contributionFactory;

	public Workbench(Location instanceLocation, IExtensionRegistry registry,
			PackageAdmin packageAdmin, URI workbenchXmiURI,
			IEclipseContext applicationContext,
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

		globalContext = createContext(applicationContext);
		if (workbenchData != null && workbenchData.exists() && saveAndRestore) {
			createWorkbenchModel(URI.createFileURI(workbenchData
					.getAbsolutePath()), workbenchXmiURI);
		} else {
			createWorkbenchModel(null, workbenchXmiURI);
		}
	}

	private IEclipseContext createContext(IEclipseContext applicationContext) {
		final IEclipseContext mainContext = EclipseContextFactory.create(
				applicationContext, UISchedulerStrategy.getInstance());
		mainContext.set(Logger.class.getName(), new WorkbenchLogger());
		mainContext.set(IContextConstants.DEBUG_STRING, "globalContext"); //$NON-NLS-1$

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
		mainContext.set(IWorkbench.class.getName(), this);
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
					Object adapted = Platform.getAdapterManager().loadAdapter(
							newValue, adapterType.getName());
					if (adapted != null) {
						newInput = adapted;
					}
				}
				return newInput;
			}
		});

		return mainContext;
	}

	private MApplication<? extends MWindow> createWorkbenchModel(
			URI restoreLocation, URI applicationDefinitionInstance) {
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
			resource.setURI(URI.createFileURI(workbenchData.getAbsolutePath()));
		}

		init(workbench);

		return workbench;
	}

	private MApplication<? extends MWindow> loadDefaultModel(
			URI defaultModelPath) {
		Resource resource = new ResourceSetImpl().getResource(defaultModelPath,
				true);
		MApplication<MWindow> app = (MApplication<MWindow>) resource
				.getContents().get(0);

		processPartContributions(resource, app.getWindows().get(0));

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

	private void init(MApplication<? extends MWindow> workbench2) {
		// Capture the MApplication into the context
		globalContext.set(MApplication.class.getName(), workbench);

		// Initialize the workbench for legacy support if required
		globalContext.get(ILegacyHook.class.getName());
	}

	public int run() {
		MWindow wbw = workbench.getWindows().get(0);
		createGUI(wbw);

		rv = 0;
		Platform.endSplash();

		// A position of 0 is not possible on OS-X because then the title-bar is
		// hidden
		// below the MMenu-Bar
		windowHandler.setBounds(appWindow, wbw.getX(), wbw.getY(), wbw
				.getWidth(), wbw.getHeight());
		windowHandler.layout(appWindow);

		windowHandler.open(appWindow);

		windowHandler.runEvenLoop(appWindow);

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

	private void createGUI(MWindow wbw) {
		if (renderer == null) {
			renderer = new PartRenderer(contributionFactory, globalContext);
			initializeRenderer(registry, renderer, globalContext,
					contributionFactory);

		}

		renderer.createGui(wbw);
		appWindow = wbw.getWidget();
	}

	public void close() {
		if (appWindow != null)
			windowHandler.dispose(appWindow);
	}

	// public Display getDisplay() {
	// return appWindow.getDisplay();
	// }

	public void closeWindow(MWorkbenchWindow workbenchWindow) {
		windowHandler.close(workbenchWindow.getWidget());
	}

	public Object getWindow() {
		return appWindow;
	}
}
