/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import junit.framework.TestCase;
import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.services.ContextServiceAddon;
import org.eclipse.e4.ui.tests.Activator;

public abstract class HeadlessStartupTest extends TestCase {

	protected IEclipseContext osgiContext;

	protected IEclipseContext applicationContext;

	@Override
	protected void setUp() throws Exception {
		createOSGiContext();
		applicationContext = createApplicationContext();
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		applicationContext.dispose();

		// if (osgiContext instanceof IDisposable) {
		// ((IDisposable) osgiContext).dispose();
		// }
	}

	private IEclipseContext createOSGiContext() {
		osgiContext = EclipseContextFactory.getServiceContext(Activator
				.getDefault().getBundle().getBundleContext());
		return osgiContext;
	}

	protected IEclipseContext createApplicationContext() {
		final IEclipseContext appContext = E4Application.createDefaultContext();
		ContextInjectionFactory.make(CommandServiceAddon.class, appContext);
		ContextInjectionFactory.make(ContextServiceAddon.class, appContext);
		return appContext;
	}
}
