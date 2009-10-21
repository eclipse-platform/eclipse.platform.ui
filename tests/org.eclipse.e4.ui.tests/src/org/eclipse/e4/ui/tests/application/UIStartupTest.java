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

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.context.spi.ISchedulerStrategy;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.swt.Activator;
import org.eclipse.e4.workbench.ui.internal.UISchedulerStrategy;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

public abstract class UIStartupTest extends HeadlessStartupTest {

	protected Display display;

	@Override
	protected void setUp() throws Exception {
		display = Display.getDefault();
		super.setUp();
		while (display.readAndDispatch())
			;
	}

	@Override
	protected boolean needsActiveChildEventHandling() {
		return false;
	}

	@Override
	protected String getEngineURI() {
		return "platform:/plugin/org.eclipse.e4.ui.workbench.swt/org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine"; //$NON-NLS-1$
	}

	@Override
	public void testGet_ActiveChild() throws Exception {
		IEclipseContext context = application.getContext();

		assertNotNull(context.get(IContextConstants.ACTIVE_CHILD));
	}

	public void testGet_ActiveShell() throws Exception {
		IEclipseContext context = application.getContext();

		assertNull(context.get(IServiceConstants.ACTIVE_SHELL));
	}

	@Override
	public void testGet_ActivePart() throws Exception {
		IEclipseContext context = application.getContext();

		assertNotNull(context.get(IServiceConstants.ACTIVE_PART));
	}

	public void testGet_ActivePartId() throws Exception {
		IEclipseContext context = application.getContext();
		assertNotNull(context.get(IServiceConstants.ACTIVE_PART_ID));
	}

	public void testGet_ActiveContexts2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNotNull(context.get(IServiceConstants.ACTIVE_CONTEXTS));
	}

	public void testGet_Selection2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNull(context.get(IServiceConstants.SELECTION));
	}

	public void testGet_ActiveChild2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNotNull(context.get(IContextConstants.ACTIVE_CHILD));
	}

	public void testGet_ActivePart2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNotNull(context.get(IServiceConstants.ACTIVE_PART));
	}

	public void testGet_Input2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNull(context.get(IServiceConstants.INPUT));
	}

	public void testGet_ActiveShell2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNull(context.get(IServiceConstants.ACTIVE_SHELL));
	}

	public void testGet_PersistedState2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNull(context.get(IServiceConstants.PERSISTED_STATE));
	}

	@Override
	public void test_SwitchActivePartsInCode() throws Exception {
		final IEclipseContext context = application.getContext();

		final MPart[] parts = getTwoParts();

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				parts[0].getParent().setActiveChild(parts[0]);
				while (display.readAndDispatch())
					;
				assertEquals(parts[0].getId(), context
						.get(IServiceConstants.ACTIVE_PART_ID));

				parts[1].getParent().setActiveChild(parts[1]);
				while (display.readAndDispatch())
					;
				assertEquals(parts[1].getId(), context
						.get(IServiceConstants.ACTIVE_PART_ID));
			}
		});
	}

	public void test_SwitchActivePartsInCode2() throws Exception {
		final IEclipseContext context = getActiveChildContext(application);

		final MPart[] parts = getTwoParts();

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				parts[0].getParent().setActiveChild(parts[0]);
				while (display.readAndDispatch())
					;
				assertEquals(parts[0].getId(), context
						.get(IServiceConstants.ACTIVE_PART_ID));

				parts[1].getParent().setActiveChild(parts[1]);
				while (display.readAndDispatch())
					;
				assertEquals(parts[1].getId(), context
						.get(IServiceConstants.ACTIVE_PART_ID));
			}
		});
	}

	@Override
	public void test_SwitchActivePartsInContext() throws Exception {
		final IEclipseContext context = application.getContext();

		final MPart[] parts = getTwoParts();

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				context.set(IServiceConstants.ACTIVE_PART, parts[0]);
				while (display.readAndDispatch())
					;

				assertEquals(parts[0].getId(), context
						.get(IServiceConstants.ACTIVE_PART_ID));

				context.set(IServiceConstants.ACTIVE_PART, parts[1]);
				while (display.readAndDispatch())
					;
				assertEquals(parts[1].getId(), context
						.get(IServiceConstants.ACTIVE_PART_ID));
			}
		});
	}

	public void test_SwitchActivePartsInContext2() throws Exception {
		final IEclipseContext context = getActiveChildContext(application);

		final MPart[] parts = getTwoParts();

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				context.set(IServiceConstants.ACTIVE_PART, parts[0]);
				while (display.readAndDispatch())
					;

				assertEquals(parts[0].getId(), context
						.get(IServiceConstants.ACTIVE_PART_ID));

				context.set(IServiceConstants.ACTIVE_PART, parts[1]);
				while (display.readAndDispatch())
					;
				assertEquals(parts[1].getId(), context
						.get(IServiceConstants.ACTIVE_PART_ID));
			}
		});
	}

	private static MPSCElement getNonContainer(MPSCElement activeChild) {
		if (activeChild instanceof MElementContainer<?>) {
			activeChild = (MPSCElement) ((MElementContainer<?>) activeChild)
					.getActiveChild();
			assertNotNull(activeChild);

			activeChild = getNonContainer(activeChild);
		}
		return activeChild;
	}

	private static IEclipseContext getActiveChildContext(
			MApplication application) {
		MPSCElement nonContainer = getNonContainer(application.getActiveChild()
				.getActiveChild());
		return ((MContext) nonContainer).getContext();
	}

	private IEclipseContext createOSGiContext() {
		IEclipseContext serviceContext = EclipseContextFactory
				.createServiceContext(Activator.getDefault().getBundle()
						.getBundleContext());
		return serviceContext;
	}

	@Override
	protected IEclipseContext createAppContext() {
		final IEclipseContext[] contexts = new IEclipseContext[1];
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				contexts[0] = createAppContext(createOSGiContext());
			}
		});
		return contexts[0];
	}

	@Override
	protected ISchedulerStrategy getAppSchedulerStrategy() {
		return UISchedulerStrategy.getInstance();
	}

	@Override
	protected IEclipseContext createAppContext(IEclipseContext osgiContext) {
		IEclipseContext mainContext = super.createAppContext(osgiContext);
		mainContext.set(IStylingEngine.class.getName(), new IStylingEngine() {
			public void style(Object widget) {
				// no-op
			}

			public void setId(Object widget, String id) {
				// no-op
			}

			public void setClassname(Object widget, String classname) {
				// no-op
			}
		});
		return mainContext;
	}

	protected void createGUI(final MUIElement uiRoot) {
		final RuntimeException[] exception = new RuntimeException[1];
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				try {
					UIStartupTest.super.createGUI(uiRoot);
				} catch (RuntimeException e) {
					exception[0] = e;
				}
			}
		});
	}

}
