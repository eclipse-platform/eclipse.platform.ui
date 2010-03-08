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

package org.eclipse.e4.ui.tests.application;

import junit.framework.TestCase;

import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.internal.services.EclipseAdapter;
import org.eclipse.e4.core.services.Adapter;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.IRunAndTrack;
import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.context.spi.IEclipseContextStrategy;
import org.eclipse.e4.core.services.context.spi.ISchedulerStrategy;
import org.eclipse.e4.core.services.injector.IObjectProvider;
import org.eclipse.e4.core.services.internal.context.ObjectProviderContext;
import org.eclipse.e4.ui.internal.services.ActiveContextsFunction;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.swt.Activator;
import org.eclipse.e4.workbench.ui.IExceptionHandler;
import org.eclipse.e4.workbench.ui.internal.ActivePartLookupFunction;
import org.eclipse.e4.workbench.ui.internal.ExceptionHandler;
import org.eclipse.e4.workbench.ui.internal.ReflectionContributionFactory;
import org.eclipse.e4.workbench.ui.internal.WorkbenchLogger;

public abstract class HeadlessStartupTest extends TestCase {

	protected IEclipseContext osgiContext;

	protected IEclipseContext applicationContext;

	@Override
	protected void setUp() throws Exception {
		applicationContext = createApplicationContext();
		applicationContext.set(IContextConstants.DEBUG_STRING,
				"Application Context"); //$NON-NLS-1$

		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		if (applicationContext instanceof IDisposable) {
			((IDisposable) applicationContext).dispose();
		}

		// if (osgiContext instanceof IDisposable) {
		// ((IDisposable) osgiContext).dispose();
		// }
	}

	private IEclipseContext createOSGiContext() {
		osgiContext = EclipseContextFactory.getServiceContext(Activator
				.getDefault().getBundle().getBundleContext());
		return osgiContext;
	}

	private IEclipseContext createApplicationContext() {
		return createApplicationContext(createOSGiContext());
	}

	protected ISchedulerStrategy getApplicationSchedulerStrategy() {
		return null;
	}

	protected IEclipseContext createApplicationContext(
			IEclipseContext osgiContext) {
		assertNotNull(osgiContext);

		final IEclipseContext appContext = createContext(osgiContext, null);

		appContext.set(IEclipseContext.class.getName(), appContext);

		appContext.set(IContributionFactory.class.getName(),
				new ReflectionContributionFactory(
						(IExtensionRegistry) appContext
								.get(IExtensionRegistry.class.getName())));
		appContext.set(IExceptionHandler.class.getName(),
				new ExceptionHandler());
		// TODO is there a reason the logger isn't injected with the context?
		appContext.set(Logger.class.getName(), new WorkbenchLogger());

		appContext.set(Adapter.class.getName(), ContextInjectionFactory.inject(
				new EclipseAdapter(), appContext));
		appContext.set(ContextManager.class.getName(), new ContextManager());

		appContext.set(IServiceConstants.ACTIVE_CONTEXTS,
				new ActiveContextsFunction());
		appContext.set(IServiceConstants.ACTIVE_PART,
				new ActivePartLookupFunction());
		appContext.runAndTrack(new IRunAndTrack() {
			public boolean notify(ContextChangeEvent event) {
				IObjectProvider provider = event.getContext();
				IEclipseContext eventsContext = ((ObjectProviderContext) provider)
						.getContext();
				Object o = eventsContext.get(IServiceConstants.ACTIVE_PART);
				if (o instanceof MPart) {
					eventsContext.set(IServiceConstants.ACTIVE_PART_ID,
							((MPart) o).getId());
				}
				return true;
			}

			@Override
			public String toString() {
				return "HeadlessStartupTest$RunAndTrack[" //$NON-NLS-1$
						+ IServiceConstants.ACTIVE_PART_ID + ']';
			}
		}, null);
		appContext.set(IServiceConstants.INPUT, new ContextFunction() {
			public Object compute(IEclipseContext context, Object[] arguments) {
				Class<?> adapterType = null;
				if (arguments.length > 0 && arguments[0] instanceof Class<?>) {
					adapterType = (Class<?>) arguments[0];
				}
				Object newInput = null;
				Object newValue = context.get(IServiceConstants.SELECTION);
				if (adapterType == null || adapterType.isInstance(newValue)) {
					newInput = newValue;
				} else if (newValue != null && adapterType != null) {
					IAdapterManager adapters = (IAdapterManager) context
							.get(IAdapterManager.class.getName());
					if (adapters != null) {
						Object adapted = adapters.loadAdapter(newValue,
								adapterType.getName());
						if (adapted != null) {
							newInput = adapted;
						}
					}
				}
				return newInput;
			}
		});

		return appContext;
	}

	private IEclipseContext createContext(IEclipseContext parent,
			IEclipseContextStrategy strategy) {
		IEclipseContext eclipseContext = EclipseContextFactory.create(parent,
				strategy);
		return eclipseContext;
	}
}
