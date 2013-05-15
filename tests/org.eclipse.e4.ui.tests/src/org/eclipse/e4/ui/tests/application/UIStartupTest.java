/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
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
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.ResourceUtility;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSStyleDeclaration;

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
		return "bundleclass://org.eclipse.e4.ui.workbench.swt/org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine"; //$NON-NLS-1$
	}

	@Override
	public void testGet_ActiveChild() throws Exception {
		IEclipseContext context = application.getContext();

		assertNotNull(context.getActiveChild());
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

	public void testGet_ActiveContexts2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNotNull(context.get(IServiceConstants.ACTIVE_CONTEXTS));
	}

	public void testGet_Selection2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNull(context.get(IServiceConstants.ACTIVE_SELECTION));
	}

	public void testGet_ActiveChild2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNotNull(context.getActiveChild());
	}

	public void testGet_ActivePart2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNotNull(context.get(IServiceConstants.ACTIVE_PART));
	}

	public void testGet_ActiveShell2() throws Exception {
		IEclipseContext context = getActiveChildContext(application);

		assertNull(context.get(IServiceConstants.ACTIVE_SHELL));
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
	protected IEclipseContext createApplicationContext() {
		final IEclipseContext[] contexts = new IEclipseContext[1];
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				contexts[0] = UIStartupTest.super.createApplicationContext();
				contexts[0].set(IResourceUtilities.class.getName(),
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

							public CSSStyleDeclaration getStyle(Object widget) {
								// TODO Auto-generated method stub
								return null;
							}

							public void setClassnameAndId(Object widget,
									String classname, String id) {
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
