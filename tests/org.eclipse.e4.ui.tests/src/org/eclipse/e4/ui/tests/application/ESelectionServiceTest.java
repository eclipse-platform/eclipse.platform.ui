/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.internal.workbench.UIEventPublisher;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.emf.common.notify.Notifier;

public class ESelectionServiceTest extends UITest {

	public void testGetSelection() {
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

		initialize();
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

		initialize();
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

		initialize();
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

		initialize();
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

		initialize();
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

		assertNull(listener.getPart());
		assertNull(listener.getSelection());

		listener.reset();
		serviceA.setSelection(selectionB);

		assertNull(listener.getPart());
		assertNull(listener.getSelection());
	}

	public void testBug314538() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		initialize();
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

		initialize();
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

		assertNull(listener.getPart());
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

		initialize();
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

		initialize();
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
		public void setInput(
				@Named(IServiceConstants.ACTIVE_SELECTION) Object current) {
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
				@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object s) {
			selection = s;
		}
	}

	public void testOnePartSelection() throws Exception {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);
		window.setSelectedElement(part);

		initialize();
		getEngine().createGui(window);

		ProviderPart p = new ProviderPart();
		ContextInjectionFactory.inject(p, part.getContext());

		assertNull(p.input);

		Object selection = new Object();

		p.setSelection(selection);
		assertEquals(selection, p.input);
		p.setSelection(null);
		assertNull(p.input);
	}

	public void testTwoPartHandlerExecute() throws Exception {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partA);
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partB);
		window.setSelectedElement(partA);

		initialize();
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
		// assertNull(handler.selection); // incorrect: should be the window
		// selection

		EPartService partService = (EPartService) windowContext
				.get(EPartService.class.getName());
		partService.activate(partB);

		ContextInjectionFactory.invoke(handler, Execute.class,
				applicationContext, null);
		// assertNull(handler.selection); // partB does not post a selection
		handler.selection = null;

		ContextInjectionFactory.invoke(handler, Execute.class, windowContext,
				null);
		// assertNull(handler.selection); // partB does not post a selection
		handler.selection = null;

		ContextInjectionFactory.invoke(handler, Execute.class, partContextA,
				null);
		// assertEquals(selection, handler.selection); // incorrect;
		// selection is at window level and active part did not change
		handler.selection = null;

		ContextInjectionFactory.invoke(handler, Execute.class, partContextB,
				null);
		// assertNull(handler.selection); // incorrect; should be selection
	}

	public void testThreePartSelection() throws Exception {
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

		initialize();
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
		// assertNull(partTwoImpl.input); // incorrect
		// assertNull(partThreeImpl.input); // incorrect

		partThreeImpl.setSelection(selection2);
		assertEquals(selection, windowService.getSelection());
		assertEquals(selection, partOneImpl.input);
		// assertNull(partTwoImpl.input); // incorrect
		// assertEquals(selection2, partThreeImpl.input); // incorrect, it is
		// not active

		partService.activate(partB);
		// assertNull(windowService.getSelection()); // partB does not post
		// a selection
		// assertEquals(selection, partOneImpl.input); // incorrect
		// assertNull(partTwoImpl.input);// partB does not post a selection
		// assertEquals(selection2, partThreeImpl.input); // incorrect

		partService.activate(partC);
		assertEquals(selection2, windowService.getSelection());
		// assertEquals(selection, partOneImpl.input); // incorrect
		// assertNull(partTwoImpl.input); // incorrect
		assertEquals(selection2, partThreeImpl.input);
	}

	public void testPartOneTracksPartThree() throws Exception {
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

		initialize();
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
		// assertNull(partTwoImpl.input); // incorrect
		// assertEquals(selection2, partThreeImpl.input); // incorrect

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
		// assertNull(partTwoImpl.input); // incorrect
		// assertEquals(selection2, partThreeImpl.input); // incorrect

		partThreeImpl.setSelection(selection);
		assertEquals(selection, partOneImpl.input);
		assertEquals(selection, partOneImpl.otherSelection);
		// assertNull(partTwoImpl.input); // incorrect
		// assertEquals(selection, partThreeImpl.input); // incorrect

		partThreeImpl.setSelection(null);
		assertEquals(selection, partOneImpl.input);
		assertNull(partOneImpl.otherSelection);
		// assertNull(partTwoImpl.input); // incorrect
		// assertNull(partThreeImpl.input); // incorrect
	}

	public void testPartOneTracksPartThree2() throws Exception {
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

		initialize();
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
		// assertNull(partTwoImpl.input); // incorrect
		// assertEquals(selection2, partThreeImpl.input); // incorrect

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
		// assertNull(partTwoImpl.input); // incorrect
		// assertEquals(selection3, partThreeImpl.input); // incorrect

		partThreeImpl.setSelection(selection);
		assertEquals(selection, partOneImpl.input);
		assertEquals(selection, partOneImpl.otherSelection);
		// assertNull(partTwoImpl.input); // incorrect
		// assertEquals(selection, partThreeImpl.input); // incorrect

		partThreeImpl.setSelection(null);
		assertEquals(selection, partOneImpl.input);
		assertNull(partOneImpl.otherSelection);
		// assertNull(partTwoImpl.input); // incorrect
		// assertNull(partThreeImpl.input); // incorrect
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
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partA);
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partB);
		window.setSelectedElement(partA);

		initialize();
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

	public void testBug343003() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		perspective.getChildren().add(partA);
		perspective.setSelectedElement(partA);

		initialize();
		getEngine().createGui(window);

		window.getContext().get(EPartService.class).activate(partA);

		ESelectionService selectionServiceA = partA.getContext().get(
				ESelectionService.class);
		SelectionListener listener = new SelectionListener();
		selectionServiceA.addSelectionListener("partB", listener); //$NON-NLS-1$

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		window.getSharedElements().add(partB);

		MPlaceholder placeholder = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholder.setRef(partB);
		partB.setCurSharedRef(placeholder);
		perspective.getChildren().add(placeholder);

		Object o = new Object();
		ESelectionService selectionServiceB = partB.getContext().get(
				ESelectionService.class);
		selectionServiceB.setSelection(o);

		assertEquals(partB, listener.getPart());
		assertEquals(o, listener.getSelection());
	}

	public void testBug343984() throws Exception {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);
		window.setSelectedElement(part);

		initialize();
		applicationContext.set(UISynchronize.class, new JobUISynchronizeImpl());
		getEngine().createGui(window);

		IEclipseContext context = part.getContext();
		Bug343984Listener listener = new Bug343984Listener();
		listener.context = context;
		ESelectionService selectionService = context
				.get(ESelectionService.class);
		selectionService.addSelectionListener(listener);

		selectionService.setSelection(new Object());
		Thread.sleep(1000);
		assertTrue(listener.success);

		listener.reset();
		selectionService.setSelection(new Object());
		Thread.sleep(1000);
		assertTrue(listener.success);
	}

	public void testBug393137() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partA);
		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(partB);
		window.setSelectedElement(partA);

		initialize();
		getEngine().createGui(window);

		IEclipseContext windowContext = window.getContext();
		IEclipseContext partContextB = partB.getContext();

		EPartService partService = windowContext.get(EPartService.class);
		partService.activate(partA);
		ESelectionService selectionServiceB = partContextB
				.get(ESelectionService.class);

		Object o = new Target("");
		selectionServiceB.setSelection(o);
		selectionServiceB.setPostSelection(o);

		SelectionListener listener = new SelectionListener();
		SelectionListener postListener = new SelectionListener();
		selectionServiceB.addSelectionListener(listener);
		selectionServiceB.addPostSelectionListener(postListener);
		partService.activate(partB);
		assertEquals(1, listener.count);
		assertEquals(1, postListener.count);
	}

	private void initialize() {
		applicationContext.set(MApplication.class.getName(), application);
		applicationContext.set(UISynchronize.class, new UISynchronize() {
			@Override
			public void syncExec(Runnable runnable) {
				runnable.run();
			}

			@Override
			public void asyncExec(final Runnable runnable) {
				runnable.run();
			}
		});
		application.setContext(applicationContext);
		final UIEventPublisher ep = new UIEventPublisher(applicationContext);
		((Notifier) application).eAdapters().add(ep);
		applicationContext.set(UIEventPublisher.class, ep);
	}

	static class SelectionListener implements ISelectionListener {

		private MPart part;
		private Object selection;
		private int count;

		public void reset() {
			part = null;
			selection = null;
			count = 0;
		}

		public void selectionChanged(MPart part, Object selection) {
			this.part = part;
			this.selection = selection;
			this.count++;
		}

		public MPart getPart() {
			return part;
		}

		public Object getSelection() {
			return selection;
		}

		public int getCount() {
			return count;
		}
	}

	static class Bug343984Listener implements ISelectionListener {

		IEclipseContext context;
		int count = 0;
		boolean success = false;

		public void reset() {
			count = 0;
			success = false;
		}

		public void selectionChanged(MPart part, Object selection) {
			if (count > 0) {
				success = false;
				return;
			}

			success = true;
			count++;

			context.get("a");
			context.set("a", new Object());

			context.get("b");
			context.set("b", new Object());
		}

	}

	static class JobUISynchronizeImpl extends UISynchronize {
		@Override
		public void syncExec(Runnable runnable) {
			runnable.run();
		}

		@Override
		public void asyncExec(final Runnable runnable) {
			Job job = new Job("") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					runnable.run();
					return Status.OK_STATUS;
				}
			};
			job.setPriority(Job.INTERACTIVE);
			job.schedule();
		}
	}
}
