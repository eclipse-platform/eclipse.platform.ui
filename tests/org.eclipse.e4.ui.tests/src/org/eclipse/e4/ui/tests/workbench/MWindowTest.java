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

package org.eclipse.e4.ui.tests.workbench;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.ApplicationFactory;
import org.eclipse.e4.ui.model.application.MContributedPart;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MSashForm;
import org.eclipse.e4.ui.model.application.MStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.tests.Activator;
import org.eclipse.e4.workbench.ui.internal.Workbench;
import org.eclipse.e4.workbench.ui.renderers.swt.PartRenderer;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 *
 */
public class MWindowTest extends TestCase {
	private IEclipseContext appContext;
	private IContributionFactory contributionFactory;
	private ServiceTracker bundleTracker;

	class ManageContributions implements IContributionFactory {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.e4.core.services.IContributionFactory#call(java.lang.
		 * Object, java.lang.String, java.lang.String,
		 * org.eclipse.e4.core.services.context.IEclipseContext,
		 * java.lang.Object)
		 */
		public Object call(Object object, String uriString, String methodName,
				IEclipseContext context, Object defaultValue) {
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.e4.core.services.IContributionFactory#create(java.lang
		 * .String, org.eclipse.e4.core.services.context.IEclipseContext)
		 */
		public Object create(String uriString, IEclipseContext context) {
			URI uri = URI.createURI(uriString);
			Bundle bundle = getBundle(uri);
			if (bundle != null) {
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

	}

	private Object createObject(Class<?> targetClass, IEclipseContext context) {

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

	private PackageAdmin getBundleAdmin() {
		if (bundleTracker == null) {
			BundleContext bundleContext = getBundleContext();
			if (bundleContext == null)
				return null;
			bundleTracker = new ServiceTracker(bundleContext,
					PackageAdmin.class.getName(), null);
			bundleTracker.open();
		}
		return (PackageAdmin) bundleTracker.getService();
	}

	private Bundle getBundleForName(String bundleName) {
		Bundle[] bundles = getBundleAdmin().getBundles(bundleName, null);
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

	private BundleContext getBundleContext() {
		return Activator.getDefault().getBundle().getBundleContext();
	}

	private IEclipseContext getAppContext() {
		if (appContext == null) {
			IEclipseContext serviceContext = EclipseContextFactory
					.createServiceContext(getBundleContext());
			appContext = EclipseContextFactory.create(serviceContext, null);
			appContext.set(IContextConstants.DEBUG_STRING, "application"); //$NON-NLS-1$
		}
		return appContext;
	}

	private IContributionFactory getCFactory() {
		if (contributionFactory == null) {
			contributionFactory = new ManageContributions();
		}
		return contributionFactory;
	}

	private Display getDisplay() {
		display = Display.getCurrent();
		if (display == null) {
			display = new Display();
		}
		return display;
	}

	protected void processEventLoop() {
		if (display != null) {
			while (display.readAndDispatch())
				;
		}
	}

	private Widget topWidget;
	private Display display;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		if (topWidget != null) {
			topWidget.dispose();
			topWidget = null;
		}
	}

	public void testCreateWindow() {
		final MWindow<MPart<?>> window = ApplicationFactory.eINSTANCE
				.createMWindow();
		window.setHeight(300);
		window.setWidth(400);
		window.setName("MyWindow");
		Realm.runWithDefault(SWTObservables.getRealm(getDisplay()),
				new Runnable() {

					public void run() {
						IEclipseContext context = getAppContext();
						PartRenderer renderer = new PartRenderer(getCFactory(),
								context);
						Workbench.initializeRenderer(RegistryFactory
								.getRegistry(), renderer, appContext,
								getCFactory());
						Object o = renderer.createGui(window);
						assertNotNull(o);
						topWidget = (Widget) o;
						assertTrue(topWidget instanceof Shell);
						assertEquals("MyWindow", ((Shell) topWidget).getText());
					}
				});
	}

	public void testCreateView() {
		final MWindow<MPart<?>> window = ApplicationFactory.eINSTANCE
				.createMWindow();
		window.setHeight(300);
		window.setWidth(400);
		window.setName("MyWindow");
		MSashForm<MPart<?>> sash = ApplicationFactory.eINSTANCE
				.createMSashForm();
		window.getChildren().add(sash);
		MStack stack = ApplicationFactory.eINSTANCE.createMStack();
		sash.getChildren().add(stack);
		MContributedPart<MPart<?>> contributedPart = ApplicationFactory.eINSTANCE
				.createMContributedPart();
		stack.getChildren().add(contributedPart);
		contributedPart.setName("Sample View");
		contributedPart
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		Realm.runWithDefault(SWTObservables.getRealm(getDisplay()),
				new Runnable() {
					public void run() {
						IEclipseContext context = getAppContext();
						PartRenderer renderer = new PartRenderer(getCFactory(),
								context);
						Workbench.initializeRenderer(RegistryFactory
								.getRegistry(), renderer, appContext,
								getCFactory());
						Object o = renderer.createGui(window);
						assertNotNull(o);
						topWidget = (Widget) o;
						assertTrue(topWidget instanceof Shell);
						Shell shell = (Shell) topWidget;
						assertEquals("MyWindow", shell.getText());
						Control[] controls = shell.getChildren();
						assertEquals(1, controls.length);
						SashForm sash = (SashForm) controls[0];
						Control[] sashChildren = sash.getChildren();
						assertEquals(1, sashChildren.length);
						CTabFolder folder = (CTabFolder) sashChildren[0];
						assertEquals(1, folder.getItemCount());
						Control c = folder.getItem(0).getControl();
						assertTrue(c instanceof Composite);
						Control[] viewPart = ((Composite)c).getChildren();
						assertEquals(1, viewPart.length);
						assertTrue(viewPart[0] instanceof Tree);
					}
				});
	}
}
