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

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.tests.Activator;
import org.eclipse.e4.ui.workbench.swt.internal.ResourceUtility;
import org.eclipse.e4.ui.workbench.swt.internal.WorkbenchWindowHandler;
import org.eclipse.e4.workbench.ui.IResourceUtiltities;
import org.eclipse.e4.workbench.ui.internal.Workbench;
import org.eclipse.emf.common.util.URI;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This should be run as a headless Junit Plug-in Test. It creates the
 * application in a separate thread, and then tries to post the test to the
 * application using syncExec(*). It doesn't have the correct "wait for the
 * workbench to come up" code.
 */
public class RunApplicationTest extends TestCase {
	private ServiceTracker instanceLocation;
	private BundleContext bundleContext;
	private ServiceTracker instanceAppContext;
	private ServiceTracker bundleTracker;
	private Workbench workbench;
	private IEclipseContext applicationContext;
	private Display display;

	public RunApplicationTest(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		bundleContext = Activator.getDefault().getBundle().getBundleContext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		if (workbench != null && display != null) {
			display.syncExec(new Runnable() {

				public void run() {
					workbench.close();
				}
			});
		}
		if (instanceLocation != null) {
			instanceLocation.close();
			instanceLocation = null;
		}
		if (instanceAppContext != null) {
			instanceAppContext.close();
			instanceAppContext = null;
		}
		if (bundleTracker != null) {
			bundleTracker.close();
			bundleTracker = null;
		}
	}

	protected Location getInstanceLocation() {
		if (instanceLocation == null) {
			Filter filter = null;
			try {
				filter = bundleContext.createFilter(Location.INSTANCE_FILTER);
			} catch (InvalidSyntaxException e) {
				// ignore this. It should never happen as we have tested the
				// above format.
			}
			instanceLocation = new ServiceTracker(bundleContext, filter, null);
			instanceLocation.open();
		}
		return (Location) instanceLocation.getService();
	}

	protected IApplicationContext getApplicationContext() {
		if (instanceAppContext == null) {
			instanceAppContext = new ServiceTracker(bundleContext,
					IApplicationContext.class.getName(), null);
			instanceAppContext.open();
		}
		return (IApplicationContext) instanceAppContext.getService();
	}

	protected PackageAdmin getBundleAdmin() {
		if (bundleTracker == null) {
			if (bundleContext == null)
				return null;
			bundleTracker = new ServiceTracker(bundleContext,
					PackageAdmin.class.getName(), null);
			bundleTracker.open();
		}
		return (PackageAdmin) bundleTracker.getService();
	}

	protected void launchApplication(final String uri) {
		Runnable app = new Runnable() {

			public void run() {
				display = new Display();
				final URI initialWorkbenchDefinitionInstance = URI
						.createPlatformPluginURI(uri, true);
				Realm.runWithDefault(SWTObservables.getRealm(display),
						new Runnable() {
							public void run() {
								try {
									// parent of the global workbench context is
									// an OSGi service
									// context that can provide OSGi services
									IEclipseContext serviceContext = EclipseContextFactory
											.createServiceContext(bundleContext);
									applicationContext = EclipseContextFactory
											.create(serviceContext, null);
									applicationContext.set(
											IContextConstants.DEBUG_STRING,
											"application"); //$NON-NLS-1$

									applicationContext
											.set(IResourceUtiltities.class
													.getName(),
													new ResourceUtility(
															getBundleAdmin()));

									workbench = new Workbench(
											getInstanceLocation(),
											RegistryFactory.getRegistry(),
											getBundleAdmin(),
											applicationContext,
											new WorkbenchWindowHandler());
									workbench
											.setWorkbenchModelURI(initialWorkbenchDefinitionInstance);
									workbench.createUIFromModel();
									workbench.run();
								} catch (ThreadDeath th) {
									throw th;
								} catch (Exception ex) {
									ex.printStackTrace();
								} catch (Error err) {
									err.printStackTrace();
								}
							}
						});
			}
		};
		new Thread(app).start();
	}

	public void testSampleLookup() throws Exception {
		launchApplication("org.eclipse.e4.ui.tests/xmi/CommandLookup.xmi");
		Thread.sleep(1000);
		display.syncExec(new Runnable() {
			public void run() {
				System.err.println(applicationContext
						.get(IContextConstants.DEBUG_STRING));
			}
		});
	}
}
