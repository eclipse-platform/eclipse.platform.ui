/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.internal.workbench.UIEventPublisher;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.notify.Notifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Bug320857Test {

	private IEclipseContext applicationContext;

	private IPresentationEngine engine;

	private EModelService ems;

	@Before
	public void setUp() throws Exception {
		applicationContext = E4Application.createDefaultContext();
		ems = applicationContext.get(EModelService.class);
	}

	protected String getEngineURI() {
		return "bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.HeadlessContextPresentationEngine"; //$NON-NLS-1$
	}

	@After
	public void tearDown() throws Exception {
		applicationContext.dispose();
	}

	private void initialize(IEclipseContext applicationContext, MApplication application) {
		applicationContext.set(MApplication.class, application);
		application.setContext(applicationContext);
		final UIEventPublisher ep = new UIEventPublisher(applicationContext);
		((Notifier) application).eAdapters().add(ep);
		applicationContext.set(UIEventPublisher.class, ep);
	}

	private IPresentationEngine getEngine() {
		if (engine == null) {
			IContributionFactory contributionFactory = applicationContext.get(IContributionFactory.class);
			Object newEngine = contributionFactory.create(getEngineURI(), applicationContext);
			assertTrue(newEngine instanceof IPresentationEngine);
			applicationContext.set(IPresentationEngine.class.getName(), newEngine);

			engine = (IPresentationEngine) newEngine;
		}

		return engine;
	}

	public static class Bug320857 {
		static final String OUT_SELECTION = "output.selection"; //$NON-NLS-1$

		@Inject
		private IEclipseContext context;

		@Inject
		void setPart(
				@Optional @Named(IServiceConstants.ACTIVE_PART) final MPart part) {
			if (part != null) {
				IEclipseContext partContext = part.getContext();
				partContext.containsKey(OUT_SELECTION);
			}
		}

		public void setSelection(Object selection) {
			context.set(OUT_SELECTION, selection);
		}

	}

	@Test
	public void testBug320857() throws Exception {
		MApplication application = ems.createModelElement(MApplication.class);

		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		window.getChildren().add(part);
		window.setSelectedElement(part);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		applicationContext.set(Bug320857.class.getName(),
				new ContextFunction() {
					@Override
					public Object compute(IEclipseContext context,
							String contextKey) {
						return ContextInjectionFactory.make(Bug320857.class,
								context);
					}
				});

		IEclipseContext partContext = part.getContext();

		Bug320857 selectionServiceA = partContext.get(Bug320857.class);
		selectionServiceA.setSelection(new Object());

		Bug320857 selectionServiceB = partContext.get(Bug320857.class);
		assertEquals(selectionServiceA, selectionServiceB);
		assertSame(selectionServiceA, selectionServiceB);
	}

}
