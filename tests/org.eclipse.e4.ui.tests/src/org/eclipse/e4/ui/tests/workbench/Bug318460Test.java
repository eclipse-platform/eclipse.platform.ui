/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import javax.inject.Inject;
import javax.inject.Named;
import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class Bug318460Test extends TestCase {
	protected IEclipseContext appContext;
	protected E4Workbench wb;

	@Override
	protected void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		appContext.set(E4Workbench.PRESENTATION_URI_ARG,
				PartRenderingEngine.engineURI);
	}

	@Override
	protected void tearDown() throws Exception {
		if (wb != null) {
			wb.close();
		}

		if (appContext instanceof IDisposable) {
			((IDisposable) appContext).dispose();
		}
	}

	public void testBug318460_A() {
		IEclipseContext context = EclipseContextFactory.create();

		context.set(EPartService.PART_SERVICE_ROOT, new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context) {
				IEclipseContext child = (IEclipseContext) context
						.getLocal(IContextConstants.ACTIVE_CHILD);
				while (child != null) {
					context = child;
					child = (IEclipseContext) context
							.getLocal(IContextConstants.ACTIVE_CHILD);
				}
				return context;
			}
		});

		RootContainerConsumer consumer = ContextInjectionFactory.make(
				RootContainerConsumer.class, context);
		Object o = ContextInjectionFactory.make(Object.class, context);

		IEclipseContext childContextA = context.createChild();
		IEclipseContext childContextB = context.createChild();

		context.set(IContextConstants.ACTIVE_CHILD, childContextA);
		assertEquals(childContextA, consumer.root);

		context.set(IContextConstants.ACTIVE_CHILD, childContextB);
		assertEquals(childContextB, consumer.root);

		ContextInjectionFactory.uninject(o, context);

		context.set(IContextConstants.ACTIVE_CHILD, childContextA);
		assertEquals(childContextA, consumer.root);
	}

	public void testBug318460_B() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		final MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		RootContainerConsumer consumer = ContextInjectionFactory.make(
				RootContainerConsumer.class, window.getContext());
		Object o = ContextInjectionFactory.make(Object.class, window
				.getContext());

		MPerspective perspectiveA = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		assertEquals(perspectiveA, consumer.root);

		MPerspective perspectiveB = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspectiveB);
		perspectiveStack.setSelectedElement(perspectiveB);

		assertEquals(perspectiveB, consumer.root);

		ContextInjectionFactory.uninject(o, window.getContext());

		perspectiveStack.setSelectedElement(perspectiveA);
		assertEquals(perspectiveA, consumer.root);
	}

	static class RootContainerConsumer {

		Object root;

		@Inject
		void inject(@Named(EPartService.PART_SERVICE_ROOT) @Optional Object root) {
			this.root = root;
		}

	}

}
