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
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.IContributionFactorySpi;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ComputedValue;
import org.eclipse.e4.ui.model.application.*;
import org.eclipse.e4.ui.model.workbench.*;
import org.eclipse.e4.ui.model.workbench.WorkbenchFactory;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.*;
import org.eclipse.e4.workbench.ui.renderers.swt.*;
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
import org.eclipse.swt.widgets.*;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

public class Workbench implements IWorkbench, IContributionFactory {
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

	private ILegacyHook legacyHook;

	// UI Construction...
	private PartRenderer renderer;
	private int rv;
	private Map<String, Object> languages;
	private ExceptionHandler exceptionHandler;
	private IEclipseContext globalContext;

	public Workbench(Location instanceLocation, IExtensionRegistry registry,
			PackageAdmin packageAdmin, URI workbenchXmiURI, IEclipseContext applicationContext) {

		exceptionHandler = new ExceptionHandler();
		this.registry = registry;
		this.packageAdmin = packageAdmin;
		workbenchData = null;
		try {
			workbenchData = new File(
					new File(instanceLocation.getURL().toURI()),
					".metadata/.plugins/org.eclipse.e4.workbench/workbench.xmi"); //$NON-NLS-1$
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

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

		processLanguages();
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
				"globalContext", applicationContext, UIContextScheduler.instance); //$NON-NLS-1$

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
		mainContext.set(IServiceConstants.SELECTION, new ActiveChildOutputValue(IServiceConstants.SELECTION));
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
								Object adapted = Platform.getAdapterManager().loadAdapter(newValue,
										adapterType.getName());
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

		// Install any registered legacy hook
		installLegacyHook();

		MWorkbenchWindow wbw;

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

			MPerspective<?> persp = WorkbenchFactory.eINSTANCE.createMPerspective();
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

	private void processLanguages() {
		languages = new HashMap<String, Object>();
		IExtensionRegistry registry = InternalPlatform.getDefault()
				.getRegistry();
		String extId = "org.eclipse.e4.languages"; //$NON-NLS-1$
		IConfigurationElement[] languageElements = registry
				.getConfigurationElementsFor(extId);
		for (int i = 0; i < languageElements.length; i++) {
			IConfigurationElement languageElement = languageElements[i];
			try {
				languages
						.put(
								languageElement.getAttribute("name"), //$NON-NLS-1$
								languageElement
										.createExecutableExtension("contributionFactory")); //$NON-NLS-1$
			} catch (InvalidRegistryObjectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void processPartContributions(Resource resource, MWorkbenchWindow wbw) {
		IExtensionRegistry registry = InternalPlatform.getDefault()
				.getRegistry();
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

			MPart parent = (MPart) findObject(resource.getAllContents(), parentId);
			if (parent != null) {
				parent.getChildren().add(part);
			}
		}

	}

	private void installLegacyHook() {
		IExtensionRegistry registry = InternalPlatform.getDefault()
				.getRegistry();
		String extId = "org.eclipse.e4.workbench.legacy"; //$NON-NLS-1$
		IConfigurationElement[] hooks = registry
				.getConfigurationElementsFor(extId);

		ILegacyHook impl = null;
		if (hooks.length > 0) {
			try {
				impl = (ILegacyHook) hooks[0]
						.createExecutableExtension("class"); //$NON-NLS-1$
				legacyHook = impl;
				legacyHook.init(this, workbench);
			} catch (CoreException e) {
				e.printStackTrace();
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

	private void createGUI(MWorkbenchWindow workbenchWindow) {
		if (renderer == null) {
			renderer = new PartRenderer((IContributionFactory) this,
					globalContext);

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
					factory.init(renderer, globalContext,
							(IContributionFactory) this);
					renderer.addPartFactory(factory);

					// Hack!! initialize the ContributedPartFactory
					if (factory instanceof ContributedPartFactory) {
						ContributedPartFactory cpf = (ContributedPartFactory) factory;
						cpf
								.setContributionFactory(((IContributionFactory) this));
					}
				}
			}
		}

		renderer.createGui(workbenchWindow);
		appWindow = (Shell) workbenchWindow.getWidget();
	}

	public void close() {
		appWindow.dispose();
	}

	private Bundle getBundleForName(String bundleName) {
		Bundle[] bundles = packageAdmin.getBundles(bundleName, null);
		if (bundles == null)
			return null;
		// Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				return bundles[i];
			}
		}
		return null;
	}

	private Bundle getBundle(URI platformURI) {
		return getBundleForName(platformURI.segment(1));
	}

	public Object createObject(Class<?> targetClass, IEclipseContext context) {

		Constructor<?> targetConstructor = null;

		Constructor<?>[] constructors = targetClass.getConstructors();

		// Optimization: if there's only one constructor, use it.
		if (constructors.length == 1) {
			targetConstructor = constructors[0];
		} else {
			ArrayList<Constructor<?>> toSort = new ArrayList<Constructor<?>>();

			for (int i = 0; i < constructors.length; i++) {
				Constructor<?> constructor = constructors[i];

				// Filter out non-public constructors
				if ((constructor.getModifiers() & Modifier.PUBLIC) != 0) {
					toSort.add(constructor);
				}
			}

			// Sort the constructors by descending number of constructor
			// arguments
			Collections.sort(toSort, new Comparator<Constructor<?>>() {
				public int compare(Constructor<?> c1, Constructor<?> c2) {

					int l1 = c1.getParameterTypes().length;
					int l2 = c2.getParameterTypes().length;

					return l1 - l2;
				}
			});

			// Find the first satisfiable constructor
			for (Constructor<?> next : toSort) {
				boolean satisfiable = true;

				Class<?>[] params = next.getParameterTypes();
				for (int i = 0; i < params.length && satisfiable; i++) {
					Class<?> clazz = params[i];

					if (!context.containsKey(clazz.getName())) {
						satisfiable = false;
					}
				}

				if (satisfiable) {
					targetConstructor = next;
				}
			}
		}

		if (targetConstructor == null) {
			throw new RuntimeException(
					"could not find satisfiable constructor in class " + targetClass); //$NON-NLS-1$
		}

		Class<?>[] paramKeys = targetConstructor.getParameterTypes();

		try {
			Object[] params = new Object[paramKeys.length];
			for (int i = 0; i < params.length; i++) {
				params[i] = context.get(paramKeys[i].getName());
			}

			return targetConstructor.newInstance(params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object create(String uriString, IEclipseContext context) {
		URI uri = URI.createURI(uriString);
		Bundle bundle = getBundle(uri);
		if (bundle != null) {
			if (uri.segmentCount() > 3) {
				String prefix = uri.segment(2);
				IContributionFactorySpi factory = (IContributionFactorySpi) languages
						.get(prefix);
				StringBuffer resource = new StringBuffer(uri.segment(3));
				for (int i = 4; i < uri.segmentCount(); i++) {
					resource.append('/');
					resource.append(uri.segment(i));
				}
				return factory.create(bundle, resource.toString(), context);
			}
			try {
				Class targetClass = bundle.loadClass(uri.segment(2));
				return createObject(targetClass, context);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public Object call(Object object, String uriString, String methodName,
			IEclipseContext context, Object defaultValue) {
		URI uri = URI.createURI(uriString);
		if (uri.segmentCount() > 3) {
			String prefix = uri.segment(2);
			IContributionFactorySpi factory = (IContributionFactorySpi) languages
					.get(prefix);
			return factory.call(object, methodName, context, defaultValue);
		}

		Method targetMethod = null;

		Method[] methods = object.getClass().getMethods();

		// Optimization: if there's only one method, use it.
		if (methods.length == 1) {
			targetMethod = methods[0];
		} else {
			ArrayList toSort = new ArrayList();

			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];

				// Filter out non-public constructors
				if ((method.getModifiers() & Modifier.PUBLIC) != 0
						&& method.getName().equals(methodName)) {
					toSort.add(method);
				}
			}

			// Sort the methods by descending number of method
			// arguments
			Collections.sort(toSort, new Comparator() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Comparator#compare(java.lang.Object,
				 * java.lang.Object)
				 */
				public int compare(Object arg0, Object arg1) {
					Constructor c1 = (Constructor) arg0;
					Constructor c2 = (Constructor) arg1;

					int l1 = c1.getParameterTypes().length;
					int l2 = c2.getParameterTypes().length;

					return l1 - l2;
				}
			});

			// Find the first satisfiable method
			for (Iterator iter = toSort.iterator(); iter.hasNext()
					&& targetMethod == null;) {
				Method next = (Method) iter.next();

				boolean satisfiable = true;

				Class[] params = next.getParameterTypes();
				for (int i = 0; i < params.length && satisfiable; i++) {
					Class clazz = params[i];

					if (!context.containsKey(clazz.getName())) {
						satisfiable = false;
					}
				}

				if (satisfiable) {
					targetMethod = next;
				}
			}
		}

		if (targetMethod == null) {
			if (defaultValue != null) {
				return defaultValue;
			}
			throw new RuntimeException(
					"could not find satisfiable method " + methodName + " in class " + object.getClass()); //$NON-NLS-1$//$NON-NLS-2$
		}

		Class[] paramKeys = targetMethod.getParameterTypes();

		try {
			Object[] params = new Object[paramKeys.length];
			for (int i = 0; i < params.length; i++) {
				params[i] = context.get(paramKeys[i].getName());
			}

			return targetMethod.invoke(object, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

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
