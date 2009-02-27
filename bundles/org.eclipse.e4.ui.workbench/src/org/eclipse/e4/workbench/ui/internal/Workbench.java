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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ComputedValue;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.ApplicationFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContributedPart;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.model.workbench.MPerspective;
import org.eclipse.e4.ui.model.workbench.MWorkbenchWindow;
import org.eclipse.e4.ui.model.workbench.WorkbenchFactory;
import org.eclipse.e4.ui.model.workbench.WorkbenchPackage;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.IExceptionHandler;
import org.eclipse.e4.workbench.ui.ILegacyHook;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.e4.workbench.ui.renderers.swt.PartFactory;
import org.eclipse.e4.workbench.ui.renderers.swt.PartRenderer;
import org.eclipse.e4.workbench.ui.utils.ResourceUtility;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.packageadmin.PackageAdmin;

public class Workbench implements IWorkbench {
	public static final String ID = "org.eclipse.e4.workbench.fakedWBWindow"; //$NON-NLS-1$
	private MApplication<MWorkbenchWindow> workbench;
	private ResourceUtility resourceUtility;
	private static final boolean saveAndRestore = true;
	private File workbenchData;
	private Shell appWindow;
	private final IExtensionRegistry registry;
	private final PackageAdmin packageAdmin;
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
			IEclipseContext applicationContext) {

		exceptionHandler = new ExceptionHandler();
		this.registry = registry;
		this.packageAdmin = packageAdmin;
		workbenchData = new File(new File(instanceLocation.getURL()
				.toExternalForm()),
				".metadata/.plugins/org.eclipse.e4.workbench/workbench.xmi"); //$NON-NLS-1$

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
			createWorkbenchModel(workbenchData.getAbsolutePath(),
					workbenchXmiURI);
		} else {
			createWorkbenchModel(null, workbenchXmiURI);
		}
	}

	private IEclipseContext createContext(IEclipseContext applicationContext) {
		// Initialize Services
		resourceUtility = new ResourceUtility(packageAdmin);

		final IEclipseContext mainContext = EclipseContextFactory.create(
				applicationContext, UIContextScheduler.instance);
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
		mainContext.set(ResourceUtility.class.getName(), resourceUtility);
		mainContext.set(IExtensionRegistry.class.getName(), registry);
		mainContext.set(IServiceConstants.SELECTION,
				new ActiveChildOutputValue(IServiceConstants.SELECTION));
		mainContext.set(IServiceConstants.INPUT, new ComputedValue() {
			public Object compute(IEclipseContext context, Object[] arguments) {
				Class adapterType = null;
				if (arguments.length > 0 && arguments[0] instanceof Class) {
					adapterType = (Class) arguments[0];
				}
				Object newInput = null;
				Object newValue = mainContext.get(IServiceConstants.SELECTION);
				if (newValue instanceof IStructuredSelection) {
					newValue = ((IStructuredSelection) newValue)
							.getFirstElement();
				}
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

	private MApplication<MWorkbenchWindow> createWorkbenchModel(
			String restoreFile, URI workbenchDefinitionInstance) {
		boolean restore = false;// restoreFile != null;

		URI uri = null;
		Resource resource = null;
		if (!restore) {
			resource = new XMIResourceImpl();
			workbench = ApplicationFactory.eINSTANCE.createMApplication();
			resource.getContents().add((EObject) workbench);

			// Capture the MApplication into the context
			globalContext.set(MApplication.class.getName(), workbench);

			// Should set up such things as initial perspective id here...
			String initialPerspectiveId = "org.eclipse.e4.ui.workbench.fragment.testPerspective"; //$NON-NLS-1$
			populateWBModel(workbench, workbenchDefinitionInstance,
					initialPerspectiveId);
		} else {
			uri = URI.createFileURI(restoreFile);
			try {
				resource = new ResourceSetImpl().getResource(uri, true);
				workbench = (MApplication<MWorkbenchWindow>) resource
						.getContents().get(0);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!restore) {
			resource.setURI(URI.createFileURI(workbenchData.getAbsolutePath()));
		}

		init(workbench);

		return workbench;
	}

	private void populateWBModel(MApplication<MWorkbenchWindow> wb,
			URI initialWorkbenchDefinitionInstance, String initialPerspectiveId) {

		MWorkbenchWindow wbw;
		ILegacyHook legacyHook = (ILegacyHook) globalContext
				.get(ILegacyHook.class.getName());
		if (legacyHook != null) {
			wbw = WorkbenchFactory.eINSTANCE.createMWorkbenchWindow();
			wbw.setWidth(1280);
			wbw.setHeight(1024);
			wbw.setX(100);
			wbw.setY(100);
			wbw.setName("E4 Workbench MWindow [Java, Debug]"); //$NON-NLS-1$
			wbw.setTrim(ApplicationFactory.eINSTANCE.createMTrim());
			MPart<MPart<?>> cp = ApplicationFactory.eINSTANCE.createMPart();
			wbw.getTrim().setTopTrim(cp);
			cp = ApplicationFactory.eINSTANCE.createMPart();
			wbw.getTrim().setBottomTrim(cp);
			cp = ApplicationFactory.eINSTANCE.createMPart();
			wbw.getTrim().setLeftTrim(cp);
			cp = ApplicationFactory.eINSTANCE.createMPart();
			wbw.getTrim().setRightTrim(cp);

			// MMenu mainMenu = MApplicationFactory.eINSTANCE.createMenu();
			// legacyHook.loadMenu(mainMenu);
			// wbw.setMenu(mainMenu);

			MPerspective<?> persp = WorkbenchFactory.eINSTANCE
					.createMPerspective();
			persp.setId(initialPerspectiveId);
			persp.setName("Java MPerspective"); //$NON-NLS-1$
			legacyHook.loadPerspective(persp);
			wbw.getChildren().add(persp);
			wbw.setActiveChild(persp);
		} else {
			Resource resource = new ResourceSetImpl().getResource(
					initialWorkbenchDefinitionInstance, true);
			MApplication<MWindow<MPart<?>>> app = (MApplication<MWindow<MPart<?>>>) resource
					.getContents().get(0);

			// temporary code - we are reading a new model but the code still
			// assumes
			// a MWorkbenchWindow with a MPerspective, so we need to copy the
			// parts of the
			// window into a perspective.
			wbw = WorkbenchFactory.eINSTANCE.createMWorkbenchWindow();
			wbw.setWidth(app.getWindows().get(0).getWidth());
			wbw.setHeight(app.getWindows().get(0).getHeight());
			wbw.setX(app.getWindows().get(0).getX());
			wbw.setY(app.getWindows().get(0).getY());
			wbw.setMenu(app.getWindows().get(0).getMenu());
			wbw.setToolBar(app.getWindows().get(0).getToolBar());
			wbw.setTrim(ApplicationFactory.eINSTANCE.createMTrim());
			wbw.getHandlers().addAll(app.getWindows().get(0).getHandlers());
			MPerspective<MPart<?>> perspective = WorkbenchFactory.eINSTANCE
					.createMPerspective();
			wbw.getChildren().add(perspective);
			perspective.getChildren().addAll(
					app.getWindows().get(0).getChildren());

			processPartContributions(resource, wbw);
		}

		wb.getWindows().add(wbw);
	}

	private void processPartContributions(Resource resource,
			MWorkbenchWindow wbw) {
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

	private void init(MApplication<MWorkbenchWindow> workbench) {
	}

	public int run() {
		MWorkbenchWindow wbw = workbench.getWindows().get(0);
		createGUI(wbw);

		rv = 0;
		Platform.endSplash();
		appWindow.open();
		// A position of 0 is not possible on OS-X because then the title-bar is
		// hidden
		// below the MMenu-Bar
		// TODO is there a better method to find out the height of the title bar
		int y = wbw.getY();
		if (y == 0 && SWT.getPlatform().equals("carbon")) { //$NON-NLS-1$
			y = 20;
		}
		appWindow.getShell().setBounds(wbw.getX(), y, wbw.getWidth(),
				wbw.getHeight());

		((Composite) wbw.getWidget()).layout(true);

		Display display = appWindow.getDisplay();
		while (appWindow != null && !appWindow.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		display.update();

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

	private void createGUI(MWorkbenchWindow workbenchWindow) {
		if (renderer == null) {
			renderer = new PartRenderer(contributionFactory, globalContext);
			initializeRenderer(registry, renderer, globalContext,
					contributionFactory);

		}

		renderer.createGui(workbenchWindow);
		appWindow = (Shell) workbenchWindow.getWidget();
	}

	public void close() {
		appWindow.dispose();
	}

	public Display getDisplay() {
		return appWindow.getDisplay();
	}

	public void closeWindow(MWorkbenchWindow workbenchWindow) {
		// needs proper closing protocol
		((Shell) workbenchWindow.getWidget()).close();
	}

	public Shell getShell() {
		return appWindow;
	}
}
