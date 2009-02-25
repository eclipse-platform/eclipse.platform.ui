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
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.ApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.tests.Activator;
import org.eclipse.e4.workbench.ui.internal.Workbench;
import org.eclipse.e4.workbench.ui.renderers.swt.PartRenderer;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class MWindowTest extends TestCase {
	private IEclipseContext appContext;
	private IContributionFactory contributionFactory;

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
		public Object call(Object object, String uri, String methodName,
				IEclipseContext context, Object defaultValue) {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.e4.core.services.IContributionFactory#create(java.lang
		 * .String, org.eclipse.e4.core.services.context.IEclipseContext)
		 */
		public Object create(String uri, IEclipseContext context) {
			// TODO Auto-generated method stub
			return null;
		}

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
		Display display = Display.getCurrent();
		if (display == null) {
			display = new Display();
		}
		return display;
	}

	public void testCreateWindow() {
		Realm.runWithDefault(SWTObservables.getRealm(getDisplay()),
				new Runnable() {
					public void run() {
						MWindow<MPart<?>> window = ApplicationFactory.eINSTANCE
								.createMWindow();
						window.setHeight(300);
						window.setWidth(400);
						window.setName("MyWindow");
						IEclipseContext context = getAppContext();
						PartRenderer renderer = new PartRenderer(getCFactory(),
								context);
						Workbench.initializeRenderer(RegistryFactory
								.getRegistry(), renderer, appContext,
								getCFactory());
						Object o = renderer.createGui(window);
						assertNotNull(o);
						Widget widget = (Widget) o;
						assertTrue(widget instanceof Shell);
						assertEquals("MyWindow", ((Shell) widget).getText());
						widget.dispose();
					}
				});
	}
}
