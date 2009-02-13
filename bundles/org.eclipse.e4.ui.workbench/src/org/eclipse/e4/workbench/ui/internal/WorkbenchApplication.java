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

package org.eclipse.e4.workbench.ui.internal;


import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.e4.workbench.ui.WorkbenchFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.equinox.app.IApplication;
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
 *
 */
public class WorkbenchApplication implements IApplication {
	
	// TODO this is a hack until we can review testing
	public static Workbench workbench;

	private ServiceTracker instanceLocation;
	private BundleContext context;
	private ServiceTracker bundleTracker;

	public Object start(IApplicationContext applicationContext)
			throws Exception {
		this.context = Activator.getContext();

		final Display display = new Display();
		final WorkbenchFactory workbenchFactory = new WorkbenchFactory(
				getInstanceLocation(), getBundleAdmin(), RegistryFactory
						.getRegistry());
		String appURI = null;
		String[] args = (String[]) applicationContext.getArguments().get(
				"application.args"); //$NON-NLS-1$
		IProduct product = Platform.getProduct();
		if (args.length > 0 && args[0].equals("-applicationXMI")) { //$NON-NLS-1$
			appURI = args[1];
		} else if (product != null) {
			String path = product.getProperty("applicationXMI"); //$NON-NLS-1$
			if (path != null) {
				appURI = path;
			}
		}
		final String cssURI = product == null? null:product.getProperty("applicationCSS"); //$NON-NLS-1$;
		Assert.isNotNull(appURI, "-applicationXMI argument missing"); //$NON-NLS-1$
		final URI initialWorkbenchDefinitionInstance = URI
				.createPlatformPluginURI(appURI, true);

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				try {
					//parent of the global workbench context is an OSGi service context that can provide OSGi services
					IEclipseContext serviceContext = EclipseContextFactory.createServiceContext(Activator.getContext());
					IEclipseContext appContext = EclipseContextFactory.create("application", serviceContext, null); //$NON-NLS-1$
					if (cssURI != null) {
						WorkbenchStylingSupport.initializeStyling(display, cssURI, appContext);
					}
					IWorkbench wb = workbenchFactory
							.create(initialWorkbenchDefinitionInstance, appContext);
					workbench = (Workbench) wb;
					wb.run();
				} catch (ThreadDeath th) {
					throw th;
				} catch (Exception ex) {
					ex.printStackTrace();
				} catch (Error err) {
					err.printStackTrace();
				}
			}
		});
		return IApplication.EXIT_OK;
	}
	
	public void stop() {
	}

	public Location getInstanceLocation() {
		if (instanceLocation == null) {
			Filter filter = null;
			try {
				filter = context.createFilter(Location.INSTANCE_FILTER);
			} catch (InvalidSyntaxException e) {
				// ignore this. It should never happen as we have tested the
				// above format.
			}
			instanceLocation = new ServiceTracker(context, filter, null);
			instanceLocation.open();
		}
		return (Location) instanceLocation.getService();
	}

	private PackageAdmin getBundleAdmin() {
		if (bundleTracker == null) {
			if (context == null)
				return null;
			bundleTracker = new ServiceTracker(context, PackageAdmin.class
					.getName(), null);
			bundleTracker.open();
		}
		return (PackageAdmin) bundleTracker.getService();
	}

}
