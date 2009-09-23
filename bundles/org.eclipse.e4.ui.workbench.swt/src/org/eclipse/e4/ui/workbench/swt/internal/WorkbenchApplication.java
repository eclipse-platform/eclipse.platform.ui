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

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.services.events.EventBrokerFactory;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.swt.Activator;
import org.eclipse.e4.workbench.ui.IResourceUtiltities;
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

	private static final String APPLICATION_CSS_RESOURCES_ARG = "-applicationCSSResources";
	private static final String APPLICATION_CSS_RESOURCES = "applicationCSSResources";
	private static final String APPLICATION_CSS_ARG = "-applicationCSS";
	private static final String APPLICATION_CSS = "applicationCSS";
	private static final String APPLICATION_XMI_ARG = "-applicationXMI";
	private static final String APPLICATION_XMI = "applicationXMI";
	// TODO this is a hack until we can review testing
	public static Workbench workbench;

	public Object start(IApplicationContext applicationContext)
			throws Exception {

		final Display display = new Display();
		String[] args = (String[]) applicationContext.getArguments().get(
				"application.args"); //$NON-NLS-1$
		Map<String, String> argsList = processArgs(args);
		String appURI = argsList.get(APPLICATION_XMI);
		String cssURIr = argsList.get(APPLICATION_CSS);
		String cssResourcesURIr = argsList.get(APPLICATION_CSS_RESOURCES);
		IProduct product = Platform.getProduct();
		if (product != null) {
			if (appURI == null) {
				appURI = product.getProperty(APPLICATION_XMI); //$NON-NLS-1$
			}
			if (cssURIr == null) {
				cssURIr = product.getProperty(APPLICATION_CSS);
			}
			if (cssResourcesURIr == null) {
				cssResourcesURIr = product
						.getProperty(APPLICATION_CSS_RESOURCES);
			}
		}
		final String cssURI = cssURIr;
		final String cssResourcesURI = cssResourcesURIr;

		Assert.isNotNull(appURI, "-applicationXMI argument missing"); //$NON-NLS-1$
		final URI initialWorkbenchDefinitionInstance = URI
				.createPlatformPluginURI(appURI, true);

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				try {
					String engineURI = "platform:/plugin/org.eclipse.e4.ui.workbench.swt/"; //$NON-NLS-1$
					engineURI += "org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine"; //$NON-NLS-1$

					//					String engineURI = "platform:/plugin/org.eclipse.e4.ui.workbench.swt.engine/"; //$NON-NLS-1$
					//					engineURI += "org.eclipse.e4.ui.workbench.swt.engine.internal.WorkbenchRenderingEngine"; //$NON-NLS-1$

					// parent of the global workbench context is an OSGi service
					// context that can provide OSGi services
					IEclipseContext serviceContext = EclipseContextFactory
							.createServiceContext(Activator.getDefault()
									.getContext());
					IEclipseContext appContext = EclipseContextFactory.create(
							serviceContext, null);
					appContext.set(IContextConstants.DEBUG_STRING,
							"WorkbenchAppContext"); //$NON-NLS-1$
					appContext.set(IResourceUtiltities.class.getName(),
							new ResourceUtility(Activator.getDefault()
									.getBundleAdmin()));
					appContext.set(IEventBroker.class.getName(),
							EventBrokerFactory.newEventBroker());
					Workbench wb = new Workbench(Activator.getDefault()
							.getInstanceLocation(), RegistryFactory
							.getRegistry(), Activator.getDefault()
							.getBundleAdmin(), appContext,
							new WorkbenchWindowHandler(), engineURI);
					wb.setWorkbenchModelURI(initialWorkbenchDefinitionInstance);
					if (cssURI != null) {
						CSSStylingSupport.initializeStyling(display, cssURI,
								cssResourcesURI, wb.getContext());
					}
					wb.createUIFromModel();
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

	private Map<String, String> processArgs(String[] args) {
		HashMap<String, String> argsList = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++) {
			if (APPLICATION_XMI_ARG.equals(args[i])) {
				argsList.put(APPLICATION_XMI, args[++i]);
			} else if (APPLICATION_CSS_ARG.equals(args[i])) {
				argsList.put(APPLICATION_CSS, args[++i]);
			} else if (APPLICATION_CSS_RESOURCES_ARG.equals(args[i])) {
				argsList.put(APPLICATION_CSS_RESOURCES, args[++i]);
			}
		}
		return argsList;
	}

	public void stop() {
	}

}
