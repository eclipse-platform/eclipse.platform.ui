/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertEquals;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Bug308317Test {

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
	private EModelService ems;

	@Before
	public void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		ems = appContext.get(EModelService.class);
	}

	@After
	public void tearDown() throws Exception {
		appContext.dispose();
	}

	@Test
	public void testBug308317() throws Exception {
		MApplication application = ems.createModelElement(MApplication.class);
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stackA = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stackA);
		window.setSelectedElement(stackA);

		MPartStack stackB = ems.createModelElement(MPartStack.class);
		window.getChildren().add(stackB);

		MPart partA = ems.createModelElement(MPart.class);
		stackA.getChildren().add(partA);
		stackA.setSelectedElement(partA);

		MPart partB = ems.createModelElement(MPart.class);
		stackB.getChildren().add(partB);
		stackB.setSelectedElement(partB);

		IEclipseContext windowContext = appContext.createChild();
		windowContext.set(MWindow.class.getName(), window);
		window.setContext(windowContext);

		IEclipseContext partContextA = windowContext.createChild();
		partContextA.set(MPart.class.getName(), partA);
		partA.setContext(partContextA);

		IEclipseContext partContextB = windowContext.createChild();
		partContextB.set(MPart.class.getName(), partB);
		partB.setContext(partContextB);

		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		PartConsumer getter = ContextInjectionFactory.make(PartConsumer.class,
				window.getContext());

		partA.getContext().activate();
		assertEquals(partA, getter.part);

		partB.getContext().activate();
		assertEquals(partB, getter.part);

		partA.getContext().activate();
		assertEquals(partA, getter.part);

		partA.getContext().deactivate();
		assertEquals(null, getter.part);

		partB.getContext().dispose();

		partA.getContext().activate();
		assertEquals(partA, getter.part);
	}

}
