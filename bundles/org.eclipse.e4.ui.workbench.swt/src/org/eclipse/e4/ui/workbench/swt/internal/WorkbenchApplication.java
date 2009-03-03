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

package org.eclipse.e4.ui.workbench.swt.internal;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.workbench.swt.Activator;
import org.eclipse.e4.workbench.ui.IResourceUtiltities;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.e4.workbench.ui.WorkbenchFactory;
import org.eclipse.e4.workbench.ui.internal.Workbench;
import org.eclipse.emf.common.util.URI;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
public class WorkbenchApplication implements IApplication {

	// TODO this is a hack until we can review testing
	public static Workbench workbench;

	public Object start(IApplicationContext applicationContext)
			throws Exception {

		final Display display = new Display();
		final WorkbenchFactory workbenchFactory = new WorkbenchFactory(
				Activator.getDefault().getInstanceLocation(), Activator
						.getDefault().getBundleAdmin(), RegistryFactory
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
		final String cssURI = product == null ? null : product
				.getProperty("applicationCSS"); //$NON-NLS-1$;
		Assert.isNotNull(appURI, "-applicationXMI argument missing"); //$NON-NLS-1$
		final URI initialWorkbenchDefinitionInstance = URI
				.createPlatformPluginURI(appURI, true);

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				try {
					// parent of the global workbench context is an OSGi service
					// context that can provide OSGi services
					IEclipseContext serviceContext = EclipseContextFactory
							.createServiceContext(Activator.getDefault()
									.getContext());
					IEclipseContext appContext = EclipseContextFactory.create(
							serviceContext, null);
					appContext.set(IContextConstants.DEBUG_STRING,
							"application"); //$NON-NLS-1$
					appContext.set(IResourceUtiltities.class.getName(), new ResourceUtility(Activator
							.getDefault().getBundleAdmin()));
					if (cssURI != null) {
						WorkbenchStylingSupport.initializeStyling(display,
								cssURI, appContext);
					} else {
						WorkbenchStylingSupport
								.initializeNullStyling(appContext);
					}
					IWorkbench wb = workbenchFactory.create(
							initialWorkbenchDefinitionInstance, appContext, new WorkbenchWindowHandler());
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

}
