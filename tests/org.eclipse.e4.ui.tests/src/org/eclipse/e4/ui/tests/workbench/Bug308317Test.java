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

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.swt.internal.E4Application;

public class Bug308317Test extends TestCase {

	static class PartConsumer {

		private MPart part;

		@Inject
		void setPart(@Optional @Named(IServiceConstants.ACTIVE_PART) MPart part) {
			this.part = part;
			if (part != null) {
				// reach into the injected part's context
				part.getContext().get("abc");
			}
		}
	}

	protected IEclipseContext appContext;

	@Override
	protected void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
	}

	@Override
	protected void tearDown() throws Exception {
		if (appContext instanceof IDisposable) {
			((IDisposable) appContext).dispose();
		}
	}

	public void testBug308317() throws Exception {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stackA = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stackA);
		window.setSelectedElement(stackA);

		MPartStack stackB = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stackB);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		stackA.getChildren().add(partA);
		stackA.setSelectedElement(partA);

		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		stackB.getChildren().add(partB);
		stackB.setSelectedElement(partB);

		IEclipseContext windowContext = EclipseContextFactory.create(
				appContext, null);
		windowContext.set(MWindow.class.getName(), window);
		window.setContext(windowContext);

		IEclipseContext partContextA = EclipseContextFactory.create(
				windowContext, null);
		partContextA.set(MPart.class.getName(), partA);
		partA.setContext(partContextA);

		IEclipseContext partContextB = EclipseContextFactory.create(
				windowContext, null);
		partContextB.set(MPart.class.getName(), partB);
		partB.setContext(partContextB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		PartConsumer getter = (PartConsumer) ContextInjectionFactory.make(
				PartConsumer.class, window.getContext());

		window.getContext().set(IContextConstants.ACTIVE_CHILD,
				partA.getContext());
		assertEquals(partA, getter.part);

		window.getContext().set(IContextConstants.ACTIVE_CHILD,
				partB.getContext());
		assertEquals(partB, getter.part);

		window.getContext().set(IContextConstants.ACTIVE_CHILD,
				partA.getContext());
		assertEquals(partA, getter.part);

		window.getContext().set(IContextConstants.ACTIVE_CHILD, null);
		assertEquals(null, getter.part);

		((IDisposable) partB.getContext()).dispose();

		window.getContext().set(IContextConstants.ACTIVE_CHILD,
				partA.getContext());
		assertEquals(partA, getter.part);
	}

}
