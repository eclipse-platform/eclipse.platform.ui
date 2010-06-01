/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.*;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.swt.internal.ResourceUtility;
import org.eclipse.e4.workbench.modeling.EPartService;
import org.eclipse.e4.workbench.ui.IResourceUtiltities;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

public abstract class UIStartupTest extends HeadlessApplicationTest {

	protected Display display;

	@Override
	protected void setUp() throws Exception {
		display = Display.getDefault();
		super.setUp();
		while (display.readAndDispatch())
			;
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
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

	public void testGet_ActiveShell2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNull(context.get(IServiceConstants.ACTIVE_SHELL));
	}

	public void testGet_PersistedState2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNull(context.get(IServiceConstants.PERSISTED_STATE));
	}

	public void testGetFirstPart_GetContext() {
		// need to wrap this since the renderer will try build the UI for the
		// part if it hasn't been built
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				UIStartupTest.super.testGetFirstPart_GetContext();
			}
		});
	}

	public void testGetSecondPart_GetContext() {
		// need to wrap this since the renderer will try build the UI for the
		// part if it hasn't been built
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				UIStartupTest.super.testGetSecondPart_GetContext();
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

				assertEquals(parts[0].getElementId(),
						context.get(IServiceConstants.ACTIVE_PART_ID));

				context.set(IServiceConstants.ACTIVE_PART, parts[1]);
				while (display.readAndDispatch())
					;
				assertEquals(parts[1].getElementId(),
						context.get(IServiceConstants.ACTIVE_PART_ID));
			}
		});
	}

	public void test_SwitchActivePartsInContext2() throws Exception {
		final IEclipseContext context = getActiveChildContext(application);

		final MPart[] parts = getTwoParts();

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				EPartService service = (EPartService) context
						.get(EPartService.class.getName());
				service.activate(parts[0]);
				while (display.readAndDispatch())
					;

				assertEquals(parts[0].getElementId(),
						context.get(IServiceConstants.ACTIVE_PART_ID));

				service.activate(parts[1]);
				while (display.readAndDispatch())
					;
				assertEquals(parts[1].getElementId(),
						context.get(IServiceConstants.ACTIVE_PART_ID));
			}
		});
	}

	// @Override
	// public void test_SwitchActiveChildInContext() {
	// // need to wrap this since the renderer will try build the UI for the
	// // part if it hasn't been built
	// Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
	// public void run() {
	// UIStartupTest.super.test_SwitchActiveChildInContext();
	// }
	// });
	// }

	private static MWindowElement getNonContainer(MWindowElement activeChild) {
		if (activeChild instanceof MElementContainer<?>) {
			activeChild = (MWindowElement) ((MElementContainer<?>) activeChild)
					.getSelectedElement();
			assertNotNull(activeChild);

			activeChild = getNonContainer(activeChild);
		}
		return activeChild;
	}

	private static IEclipseContext getActiveChildContext(
			MApplication application) {
		MWindowElement nonContainer = getNonContainer(application
				.getSelectedElement().getSelectedElement());
		return ((MContext) nonContainer).getContext();
	}

	@Override
	protected IEclipseContext createApplicationContext(
			final IEclipseContext osgiContext) {
		final IEclipseContext[] contexts = new IEclipseContext[1];
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				contexts[0] = UIStartupTest.super
						.createApplicationContext(osgiContext);
				contexts[0].set(IResourceUtiltities.class.getName(),
						new ResourceUtility());
				contexts[0].set(IStylingEngine.class.getName(),
						new IStylingEngine() {
							public void style(Object widget) {
								// no-op
							}

							public void setId(Object widget, String id) {
								// no-op
							}

							public void setClassname(Object widget,
									String classname) {
								// no-op
							}
						});
			}
		});
		return contexts[0];
	}

	protected void createGUI(final MUIElement uiRoot) {
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				UIStartupTest.super.createGUI(uiRoot);
			}
		});
	}

}
