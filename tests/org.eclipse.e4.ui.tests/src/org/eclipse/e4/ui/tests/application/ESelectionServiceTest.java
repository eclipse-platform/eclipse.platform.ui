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

import javax.inject.Inject;
import javax.inject.Named;
import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.UIEventPublisher;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.emf.common.notify.Notifier;

public class ESelectionServiceTest extends TestCase {

	private IEclipseContext applicationContext;

	private IPresentationEngine engine;

	@Override
	protected void setUp() throws Exception {
		applicationContext = E4Application.createDefaultContext();

		super.setUp();
	}

	protected String getEngineURI() {
		return "platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.HeadlessContextPresentationEngine"; //$NON-NLS-1$
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		applicationContext.dispose();
	}

	private IPresentationEngine getEngine() {
		if (engine == null) {
			IContributionFactory contributionFactory = (IContributionFactory) applicationContext
					.get(IContributionFactory.class.getName());
			Object newEngine = contributionFactory.create(getEngineURI(),
					applicationContext);
			assertTrue(newEngine instanceof IPresentationEngine);
			applicationContext.set(IPresentationEngine.class.getName(),
					newEngine);

			engine = (IPresentationEngine) newEngine;
		}

		return engine;
	}

	public void testGetSelection() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA"); //$NON-NLS-1$
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB"); //$NON-NLS-1$
		window.getChildren().add(partB);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		IEclipseContext contextA = partA.getContext();
		IEclipseContext contextB = partB.getContext();
		IEclipseContext windowContext = window.getContext();

		ESelectionService serviceA = (ESelectionService) contextA
				.get(ESelectionService.class.getName());
		ESelectionService serviceB = (ESelectionService) contextB
				.get(ESelectionService.class.getName());
		ESelectionService windowService = (ESelectionService) windowContext
				.get(ESelectionService.class.getName());
		EPartService partService = (EPartService) windowContext
				.get(EPartService.class.getName());

		Object selection1 = new Object();
		Object selection2 = new Object();

		serviceA.setSelection(selection1);

		assertEquals(selection1, windowService.getSelection());
		assertEquals(selection1, serviceA.getSelection());
		assertEquals(selection1, serviceB.getSelection());

		serviceB.setSelection(selection2);

		assertEquals(selection1, windowService.getSelection());
		assertEquals(selection1, serviceA.getSelection());
		assertEquals(selection1, serviceB.getSelection());

		partService.activate(partB);

		assertEquals(selection2, windowService.getSelection());
		assertEquals(selection2, serviceA.getSelection());
		assertEquals(selection2, serviceB.getSelection());
	}

	public void testGetSelection_Id() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA"); //$NON-NLS-1$
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB"); //$NON-NLS-1$
		window.getChildren().add(partB);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		IEclipseContext contextA = partA.getContext();
		IEclipseContext contextB = partB.getContext();
		IEclipseContext windowContext = window.getContext();

		ESelectionService serviceA = (ESelectionService) contextA
				.get(ESelectionService.class.getName());
		ESelectionService serviceB = (ESelectionService) contextB
				.get(ESelectionService.class.getName());
		ESelectionService windowService = (ESelectionService) windowContext
				.get(ESelectionService.class.getName());

		Object selection1 = new Object();
		Object selection2 = new Object();

		serviceA.setSelection(selection1);

		assertEquals(selection1, windowService.getSelection("partA")); //$NON-NLS-1$
		assertEquals(selection1, serviceA.getSelection("partA")); //$NON-NLS-1$
		assertEquals(selection1, serviceB.getSelection("partA")); //$NON-NLS-1$
		assertNull(windowService.getSelection("partB")); //$NON-NLS-1$
		assertNull(serviceA.getSelection("partB")); //$NON-NLS-1$
		assertNull(serviceB.getSelection("partB")); //$NON-NLS-1$

		serviceB.setSelection(selection2);

		assertEquals(selection1, windowService.getSelection("partA")); //$NON-NLS-1$
		assertEquals(selection1, serviceA.getSelection("partA")); //$NON-NLS-1$
		assertEquals(selection1, serviceB.getSelection("partA")); //$NON-NLS-1$
		assertEquals(selection2, windowService.getSelection("partB")); //$NON-NLS-1$
		assertEquals(selection2, serviceA.getSelection("partB")); //$NON-NLS-1$
		assertEquals(selection2, serviceB.getSelection("partB")); //$NON-NLS-1$
	}

	public void testSelectionListener() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA"); //$NON-NLS-1$
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB"); //$NON-NLS-1$
		window.getChildren().add(partB);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		IEclipseContext contextB = partB.getContext();
		IEclipseContext windowContext = window.getContext();

		ESelectionService serviceB = (ESelectionService) contextB
				.get(ESelectionService.class.getName());
		ESelectionService windowService = (ESelectionService) windowContext
				.get(ESelectionService.class.getName());
		EPartService partService = (EPartService) windowContext
				.get(EPartService.class.getName());

		Object selection = new Object();

		SelectionListener listener = new SelectionListener();
		windowService.addSelectionListener(listener);

		serviceB.setSelection(selection);

		listener.reset();
		partService.activate(partB);

		assertEquals(partB, listener.getPart());
		assertEquals(selection, listener.getSelection());

		windowService.removeSelectionListener(listener);

		listener.reset();
		partService.activate(partA);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());

		listener.reset();
		partService.activate(partB);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());
	}

	public void testSelectionListener2() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA"); //$NON-NLS-1$
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB"); //$NON-NLS-1$
		window.getChildren().add(partB);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		IEclipseContext contextA = partA.getContext();
		IEclipseContext contextB = partB.getContext();
		IEclipseContext windowContext = window.getContext();

		ESelectionService serviceA = (ESelectionService) contextA
				.get(ESelectionService.class.getName());
		ESelectionService serviceB = (ESelectionService) contextB
				.get(ESelectionService.class.getName());
		ESelectionService windowService = (ESelectionService) windowContext
				.get(ESelectionService.class.getName());
		EPartService partService = (EPartService) windowContext
				.get(EPartService.class.getName());

		Object selectionA = new Object();
		Object selectionB = new Object();

		SelectionListener listener = new SelectionListener();
		windowService.addSelectionListener(listener);

		serviceA.setSelection(selectionA);

		assertEquals(partA, listener.getPart());
		assertEquals(selectionA, listener.getSelection());

		listener.reset();
		serviceA.setSelection(selectionA);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());

		listener.reset();
		serviceB.setSelection(selectionB);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());

		listener.reset();
		partService.activate(partB);

		assertEquals(partB, listener.getPart());
		assertEquals(selectionB, listener.getSelection());

		windowService.removeSelectionListener(listener);

		listener.reset();
		partService.activate(partA);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());

		listener.reset();
		partService.activate(partB);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());
	}

	public void testSelectionListener3() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA"); //$NON-NLS-1$
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB"); //$NON-NLS-1$
		window.getChildren().add(partB);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		IEclipseContext contextA = partA.getContext();
		IEclipseContext windowContext = window.getContext();

		ESelectionService serviceA = (ESelectionService) contextA
				.get(ESelectionService.class.getName());
		ESelectionService windowService = (ESelectionService) windowContext
				.get(ESelectionService.class.getName());
		EPartService partService = (EPartService) windowContext
				.get(EPartService.class.getName());

		Object selectionA = new Object();
		Object selectionB = new Object();

		SelectionListener listener = new SelectionListener();
		windowService.addSelectionListener(listener);

		serviceA.setSelection(selectionA);

		assertEquals(partA, listener.getPart());
		assertEquals(selectionA, listener.getSelection());

		listener.reset();
		serviceA.setSelection(selectionA);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());

		listener.reset();
		partService.activate(partB);

		assertEquals(partB, listener.getPart());
		assertNull(listener.getSelection());

		listener.reset();
		serviceA.setSelection(selectionB);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());
	}

	public void testBug314538() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		IEclipseContext windowContext = window.getContext();
		ESelectionService windowService = (ESelectionService) windowContext
				.get(ESelectionService.class.getName());
		EPartService partService = (EPartService) windowContext
				.get(EPartService.class.getName());

		SelectionListener listener = new SelectionListener();
		windowService.addSelectionListener(listener);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA"); //$NON-NLS-1$
		perspective.getChildren().add(partA);
		perspective.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB"); //$NON-NLS-1$
		perspective.getChildren().add(partB);

		IEclipseContext contextB = partB.getContext();

		ESelectionService serviceB = (ESelectionService) contextB
				.get(ESelectionService.class.getName());

		Object selection = new Object();

		serviceB.setSelection(selection);

		listener.reset();
		partService.activate(partB);

		assertEquals(partB, listener.getPart());
		assertEquals(selection, listener.getSelection());

		windowService.removeSelectionListener(listener);

		listener.reset();
		partService.activate(partA);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());

		listener.reset();
		partService.activate(partB);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());
	}

	public void testSelectionListener_Id() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA"); //$NON-NLS-1$
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB"); //$NON-NLS-1$
		window.getChildren().add(partB);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		IEclipseContext contextA = partA.getContext();
		IEclipseContext contextB = partB.getContext();
		IEclipseContext windowContext = window.getContext();

		ESelectionService serviceA = (ESelectionService) contextA
				.get(ESelectionService.class.getName());
		ESelectionService serviceB = (ESelectionService) contextB
				.get(ESelectionService.class.getName());
		ESelectionService windowService = (ESelectionService) windowContext
				.get(ESelectionService.class.getName());
		EPartService partService = (EPartService) windowContext
				.get(EPartService.class.getName());

		Object selectionA = new Object();
		Object selectionB = new Object();

		SelectionListener listener = new SelectionListener();
		windowService.addSelectionListener("partB", listener); //$NON-NLS-1$

		serviceA.setSelection(selectionA);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());

		listener.reset();
		partService.activate(partB);

		assertEquals(partB, listener.getPart());
		assertNull(listener.getSelection());

		listener.reset();
		serviceB.setSelection(selectionB);

		assertEquals(partB, listener.getPart());
		assertEquals(selectionB, listener.getSelection());

		listener.reset();
		serviceA.setSelection(selectionB);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());

		listener.reset();
		windowService.removeSelectionListener("partB", listener); //$NON-NLS-1$
		serviceB.setSelection(selectionA);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());
	}

	public void testSelectionListener_Id2() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA"); //$NON-NLS-1$
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB"); //$NON-NLS-1$
		window.getChildren().add(partB);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		IEclipseContext contextB = partB.getContext();
		IEclipseContext windowContext = window.getContext();

		ESelectionService serviceB = (ESelectionService) contextB
				.get(ESelectionService.class.getName());
		ESelectionService windowService = (ESelectionService) windowContext
				.get(ESelectionService.class.getName());
		EPartService partService = (EPartService) windowContext
				.get(EPartService.class.getName());

		Object selectionB = new Object();

		SelectionListener listener = new SelectionListener();
		windowService.addSelectionListener("partB", listener); //$NON-NLS-1$

		partService.activate(partA);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());

		listener.reset();
		serviceB.setSelection(selectionB);

		assertEquals(partB, listener.getPart());
		assertEquals(selectionB, listener.getSelection());
	}

	public void testSelectionListener_Id3() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA"); //$NON-NLS-1$
		window.getChildren().add(partA);
		window.setSelectedElement(partA);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB"); //$NON-NLS-1$
		partStack.getChildren().add(partB);
		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		partC.setElementId("partC"); //$NON-NLS-1$
		partStack.getChildren().add(partC);
		partStack.setSelectedElement(partB);
		window.getChildren().add(partStack);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		IEclipseContext windowContext = window.getContext();

		ESelectionService windowService = (ESelectionService) windowContext
				.get(ESelectionService.class.getName());
		EPartService partService = (EPartService) windowContext
				.get(EPartService.class.getName());

		Object selection = new Object();

		SelectionListener listener = new SelectionListener();
		windowService.addSelectionListener("partC", listener); //$NON-NLS-1$

		partService.showPart("partC", PartState.CREATE); //$NON-NLS-1$

		assertNull(listener.getPart());
		assertNull(listener.getSelection());

		IEclipseContext contextC = partC.getContext();
		ESelectionService serviceC = (ESelectionService) contextC
				.get(ESelectionService.class.getName());

		listener.reset();
		serviceC.setSelection(selection);

		assertEquals(partC, listener.getPart());
		assertEquals(selection, listener.getSelection());
	}

	static class ConsumerPart {
		public Object input;

		@Inject
		@Optional
		public void setInput(@Named(ESelectionService.SELECTION) Object current) {
			input = current;
		}
	}

	static class ProviderPart extends ConsumerPart {
		private ESelectionService selectionService;

		@Inject
		public void setSelectionService(ESelectionService s) {
			selectionService = s;
		}

		public void setSelection(Object selection) {
			selectionService.setSelection(selection);
		}
	}

	static class TrackingProviderPart extends ProviderPart {
		public Object otherSelection;

		public void setOtherSelection(Object selection) {
			otherSelection = selection;
		}
	}

	static class UseSelectionHandler {
		public Object selection;

		@Execute
		public void execute(
				@Optional @Named(ESelectionService.SELECTION) Object s) {
			selection = s;
		}
	}

	public void testOnePartSelection() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		ProviderPart p = new ProviderPart();
		ContextInjectionFactory.inject(p, window.getContext());

		assertNull(p.input);

		Object selection = new Object();

		p.setSelection(selection);
		assertEquals(selection, p.input);
		p.setSelection(null);
		assertNull(p.input);
	}

	public void testTwoPartHandlerExecute() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partA);
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partB);
		window.setSelectedElement(partA);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		IEclipseContext windowContext = window.getContext();

		IEclipseContext partContextA = partA.getContext();
		IEclipseContext partContextB = partB.getContext();

		ProviderPart partOneImpl = new ProviderPart();
		ContextInjectionFactory.inject(partOneImpl, partContextA);

		ConsumerPart partTwoImpl = new ConsumerPart();
		ContextInjectionFactory.inject(partTwoImpl, partContextB);

		Object selection = new Object();

		partOneImpl.setSelection(selection);

		UseSelectionHandler handler = new UseSelectionHandler();
		assertNull(handler.selection);

		ContextInjectionFactory.invoke(handler, Execute.class,
				applicationContext, null);
		assertEquals(selection, handler.selection);
		handler.selection = null;

		ContextInjectionFactory.invoke(handler, Execute.class, windowContext,
				null);
		assertEquals(selection, handler.selection);
		handler.selection = null;

		ContextInjectionFactory.invoke(handler, Execute.class, partContextA,
				null);
		assertEquals(selection, handler.selection);
		handler.selection = null;

		ContextInjectionFactory.invoke(handler, Execute.class, partContextB,
				null);
		assertNull(handler.selection);

		EPartService partService = (EPartService) windowContext
				.get(EPartService.class.getName());
		partService.activate(partB);

		ContextInjectionFactory.invoke(handler, Execute.class,
				applicationContext, null);
		assertNull(handler.selection);
		handler.selection = null;

		ContextInjectionFactory.invoke(handler, Execute.class, windowContext,
				null);
		assertNull(handler.selection);
		handler.selection = null;

		ContextInjectionFactory.invoke(handler, Execute.class, partContextA,
				null);
		assertEquals(selection, handler.selection);
		handler.selection = null;

		ContextInjectionFactory.invoke(handler, Execute.class, partContextB,
				null);
		assertNull(handler.selection);
	}

	public void testThreePartSelection() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partA);
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partB);
		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partC);
		window.setSelectedElement(partA);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		IEclipseContext windowContext = window.getContext();
		IEclipseContext partContextA = partA.getContext();
		IEclipseContext partContextB = partB.getContext();
		IEclipseContext partContextC = partC.getContext();

		ProviderPart partOneImpl = new ProviderPart();
		ContextInjectionFactory.inject(partOneImpl, partContextA);

		ConsumerPart partTwoImpl = new ConsumerPart();
		ContextInjectionFactory.inject(partTwoImpl, partContextB);

		ProviderPart partThreeImpl = new ProviderPart();
		ContextInjectionFactory.inject(partThreeImpl, partContextC);

		ESelectionService windowService = (ESelectionService) windowContext
				.get(ESelectionService.class.getName());
		EPartService partService = (EPartService) windowContext
				.get(EPartService.class.getName());

		Object selection = new Object();
		Object selection2 = new Object();

		assertNull(windowService.getSelection());
		assertNull(partOneImpl.input);
		assertNull(partTwoImpl.input);
		assertNull(partThreeImpl.input);

		partOneImpl.setSelection(selection);
		assertEquals(selection, windowService.getSelection());
		assertEquals(selection, partOneImpl.input);
		assertNull(partTwoImpl.input);
		assertNull(partThreeImpl.input);

		partThreeImpl.setSelection(selection2);
		assertEquals(selection, windowService.getSelection());
		assertEquals(selection, partOneImpl.input);
		assertNull(partTwoImpl.input);
		assertEquals(selection2, partThreeImpl.input);

		partService.activate(partB);
		assertNull(windowService.getSelection());
		assertEquals(selection, partOneImpl.input);
		assertNull(partTwoImpl.input);
		assertEquals(selection2, partThreeImpl.input);

		partService.activate(partC);
		assertEquals(selection2, windowService.getSelection());
		assertEquals(selection, partOneImpl.input);
		assertNull(partTwoImpl.input);
		assertEquals(selection2, partThreeImpl.input);
	}

	public void testPartOneTracksPartThree() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partA);
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partB);
		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		partC.setElementId("partC");
		window.getChildren().add(partC);
		window.setSelectedElement(partA);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		final IEclipseContext partContextA = partA.getContext();
		IEclipseContext partContextB = partB.getContext();
		final IEclipseContext partContextC = partC.getContext();

		final TrackingProviderPart partOneImpl = new TrackingProviderPart();
		ContextInjectionFactory.inject(partOneImpl, partContextA);

		ConsumerPart partTwoImpl = new ConsumerPart();
		ContextInjectionFactory.inject(partTwoImpl, partContextB);

		ProviderPart partThreeImpl = new ProviderPart();
		ContextInjectionFactory.inject(partThreeImpl, partContextC);

		Object selection = new Object();
		Object selection2 = new Object();

		partOneImpl.setSelection(selection);
		partThreeImpl.setSelection(selection2);
		assertEquals(selection, partOneImpl.input);
		assertNull(partOneImpl.otherSelection);
		assertNull(partTwoImpl.input);
		assertEquals(selection2, partThreeImpl.input);

		// part one tracks down part three. this could just as easily be
		// fronted by the mediator.addSelectionListener(*)
		partContextC.runAndTrack(new RunAndTrack() {
			public boolean changed(IEclipseContext context) {
				ESelectionService s = (ESelectionService) partContextA
						.get(ESelectionService.class.getName());
				partOneImpl.setOtherSelection(s.getSelection("partC"));
				return true;
			}
		});

		assertEquals(selection, partOneImpl.input);
		assertEquals(selection2, partOneImpl.otherSelection);
		assertNull(partTwoImpl.input);
		assertEquals(selection2, partThreeImpl.input);

		partThreeImpl.setSelection(selection);
		assertEquals(selection, partOneImpl.input);
		assertEquals(selection, partOneImpl.otherSelection);
		assertNull(partTwoImpl.input);
		assertEquals(selection, partThreeImpl.input);

		partThreeImpl.setSelection(null);
		assertEquals(selection, partOneImpl.input);
		assertNull(partOneImpl.otherSelection);
		assertNull(partTwoImpl.input);
		assertNull(partThreeImpl.input);
	}

	public void testPartOneTracksPartThree2() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partA);
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partB);
		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		partC.setElementId("partC");
		window.getChildren().add(partC);
		window.setSelectedElement(partA);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		final IEclipseContext partContextA = partA.getContext();
		IEclipseContext partContextB = partB.getContext();
		final IEclipseContext partContextC = partC.getContext();

		final TrackingProviderPart partOneImpl = new TrackingProviderPart();
		ContextInjectionFactory.inject(partOneImpl, partContextA);

		ConsumerPart partTwoImpl = new ConsumerPart();
		ContextInjectionFactory.inject(partTwoImpl, partContextB);

		ProviderPart partThreeImpl = new ProviderPart();
		ContextInjectionFactory.inject(partThreeImpl, partContextC);

		Object selection = new Object();
		Object selection2 = new Object();
		Object selection3 = new Object();

		partOneImpl.setSelection(selection);
		partThreeImpl.setSelection(selection2);
		assertEquals(selection, partOneImpl.input);
		assertNull(partOneImpl.otherSelection);
		assertNull(partTwoImpl.input);
		assertEquals(selection2, partThreeImpl.input);

		ESelectionService selectionService = (ESelectionService) partContextA
				.get(ESelectionService.class.getName());
		selectionService.addSelectionListener(partC.getElementId(),
				new ISelectionListener() {
					public void selectionChanged(MPart part, Object selection) {
						partOneImpl.setOtherSelection(selection);
					}
				});

		partThreeImpl.setSelection(selection3);

		assertEquals(selection, partOneImpl.input);
		assertEquals(selection3, partOneImpl.otherSelection);
		assertNull(partTwoImpl.input);
		assertEquals(selection3, partThreeImpl.input);

		partThreeImpl.setSelection(selection);
		assertEquals(selection, partOneImpl.input);
		assertEquals(selection, partOneImpl.otherSelection);
		assertNull(partTwoImpl.input);
		assertEquals(selection, partThreeImpl.input);

		partThreeImpl.setSelection(null);
		assertEquals(selection, partOneImpl.input);
		assertNull(partOneImpl.otherSelection);
		assertNull(partTwoImpl.input);
		assertNull(partThreeImpl.input);
	}

	static class Target {
		Target(String s) {

		}
	}

	static class InjectPart {

		Object selection;

		@Inject
		void setSelection(
				@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Target selection) {
			this.selection = selection;
		}
	}

	public void testInjection() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partA);
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partB);
		window.setSelectedElement(partA);

		initialize(applicationContext, application);
		getEngine().createGui(window);

		IEclipseContext windowContext = window.getContext();
		IEclipseContext partContextA = partA.getContext();
		IEclipseContext partContextB = partB.getContext();

		EPartService partService = windowContext.get(EPartService.class);
		partService.activate(partA);
		ESelectionService selectionServiceA = partContextA
				.get(ESelectionService.class);
		ESelectionService selectionServiceB = partContextB
				.get(ESelectionService.class);

		InjectPart injectPart = ContextInjectionFactory.make(InjectPart.class,
				partContextA);
		assertNull(injectPart.selection);

		Object o = new Target("");
		selectionServiceA.setSelection(o);

		assertEquals(o, injectPart.selection);

		partService.activate(partB);
		assertEquals("Part B doesn't post a selection, no change", o,
				injectPart.selection);

		partService.activate(partA);
		assertEquals(o, injectPart.selection);

		Object o2 = new Target("");
		selectionServiceB.setSelection(o2);

		assertEquals(o, injectPart.selection);

		partService.activate(partB);
		assertEquals(o2, injectPart.selection);

		partService.activate(partA);
		assertEquals(o, injectPart.selection);
	}

	private void initialize(IEclipseContext applicationContext,
			MApplication application) {
		applicationContext.set(MApplication.class.getName(), application);
		application.setContext(applicationContext);
		E4Workbench.processHierarchy(application);
		((Notifier) application).eAdapters().add(
				new UIEventPublisher(applicationContext));
	}

	static class SelectionListener implements ISelectionListener {

		private MPart part;
		private Object selection;

		public void reset() {
			part = null;
			selection = null;
		}

		public void selectionChanged(MPart part, Object selection) {
			this.part = part;
			this.selection = selection;
		}

		public MPart getPart() {
			return part;
		}

		public Object getSelection() {
			return selection;
		}

	}
}
