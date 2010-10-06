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
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.services.IServiceConstants;

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
		appContext.dispose();
	}

	public void testBug308317() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stackA = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stackA);
		window.setSelectedElement(stackA);

		MPartStack stackB = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stackB);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		stackA.getChildren().add(partA);
		stackA.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
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
		appContext.set(MApplication.class.getName(), application);

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
