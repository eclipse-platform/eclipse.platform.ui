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

package org.eclipse.e4.ui.tests.workbench;

import junit.framework.TestCase;

import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.workbench.swt.internal.E4Application;
import org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine;
import org.eclipse.e4.workbench.modeling.EPartService;
import org.eclipse.e4.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;

public class PartRenderingEngineTests extends TestCase {
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

	public void testCreateViewBug298415() {
		final MWindow window = createWindowWithOneView("Part Name");
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		MPart part = stack.getChildren().get(0);

		IPresentationEngine renderer = (IPresentationEngine) appContext
				.get(IPresentationEngine.class.getName());
		renderer.removeGui(part);
		renderer.removeGui(window);

		while (Display.getCurrent().readAndDispatch()) {
			// spin the event loop
		}
	}

	public void testAddWindowBug299219() throws Exception {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.setContext(appContext);

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(window.getWidget());

		MWindow window2 = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window2);

		assertNotNull(window2.getWidget());
	}

	public void testPartStack_SetActiveChildBug299379() throws Exception {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId("partA");
		partA.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setId("partB");
		partB.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		stack.getChildren().add(partA);
		stack.getChildren().add(partB);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder tabFolder = (CTabFolder) stack.getWidget();
		assertEquals(0, tabFolder.getSelectionIndex());

		EPartService service = (EPartService) window.getContext().get(
				EPartService.class.getName());
		service.activate(partB);
		assertEquals(
				"Activating another part should've altered the tab folder's selection",
				1, tabFolder.getSelectionIndex());
	}

	public void testPartStack_SetActiveChild2Bug299379() throws Exception {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId("partA");
		partA.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setId("partB");
		partB.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		stack.getChildren().add(partA);
		stack.getChildren().add(partB);
		stack.setSelectedElement(partA);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder tabFolder = (CTabFolder) stack.getWidget();
		assertEquals(0, tabFolder.getSelectionIndex());

		EPartService service = (EPartService) window.getContext().get(
				EPartService.class.getName());
		service.showPart(partB.getId(), PartState.ACTIVATE);
		assertEquals("Showing a part should alter the tab folder's selection",
				1, tabFolder.getSelectionIndex());
	}

	public void testPartStack_SetActiveChild3Bug299379() throws Exception {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MPartDescriptor descriptor = MApplicationFactory.eINSTANCE
				.createPartDescriptor();
		descriptor
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		descriptor.setId("part");
		descriptor.setCategory("aStack");
		application.getDescriptors().add(descriptor);

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.getTags().add("aStack");
		window.getChildren().add(stack);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder tabFolder = (CTabFolder) stack.getWidget();
		assertEquals(0, tabFolder.getItemCount());

		EPartService service = (EPartService) window.getContext().get(
				EPartService.class.getName());
		MPart shownPart = service.showPart("part", PartState.ACTIVATE);

		assertEquals(1, tabFolder.getItemCount());
		assertEquals(0, tabFolder.getSelectionIndex());
		assertEquals("The shown part should be the active part", shownPart,
				stack.getSelectedElement());
	}

	public void testPartStack_SetActiveChild4Bug299379() throws Exception {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId("partA");
		partA.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setId("partB");
		partB.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		stack.getChildren().add(partA);
		stack.getChildren().add(partB);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder tabFolder = (CTabFolder) stack.getWidget();
		assertEquals(0, tabFolder.getSelectionIndex());

		stack.setSelectedElement(partB);
		assertEquals(
				"Switching the active child should've changed the folder's selection",
				1, tabFolder.getSelectionIndex());
	}

	public void testPartStack_SetActiveChild5Bug295250() throws Exception {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partA);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertEquals(partA, stack.getSelectedElement());

		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partB);

		assertEquals(
				"Adding a part to a stack should not cause the stack's active child to change",
				partA, stack.getSelectedElement());
		assertNull("The object should not have been instantiated", partB
				.getObject());
	}

	public void testPartStack_SetActiveChild6Bug298797() throws Exception {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partA);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder tabFolder = (CTabFolder) stack.getWidget();
		assertEquals(0, tabFolder.getSelectionIndex());
		assertEquals(partA, stack.getSelectedElement());

		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partB);

		assertEquals(0, tabFolder.getSelectionIndex());
		assertEquals(partA, stack.getSelectedElement());

		stack.setSelectedElement(partB);
		assertEquals(1, tabFolder.getSelectionIndex());
		assertEquals(partB, stack.getSelectedElement());
	}

	public void testCreateGuiBug301021() throws Exception {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		// create two descriptors
		MPartDescriptor descriptor = MApplicationFactory.eINSTANCE
				.createPartDescriptor();
		descriptor
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		descriptor.setId("part");
		descriptor.setCategory("aStack");
		application.getDescriptors().add(descriptor);

		MPartDescriptor descriptor2 = MApplicationFactory.eINSTANCE
				.createPartDescriptor();
		descriptor2
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		descriptor2.setId("part2");
		descriptor2.setCategory("aStack");
		application.getDescriptors().add(descriptor2);

		// make a window with a sash container and a stack inside, this will
		// force the stack to have SashFormData
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		MPartSashContainer partSashContainer = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		// assign the stack with the category id of the descriptors above
		stack.setId("aStack");
		partSashContainer.getChildren().add(stack);
		window.getChildren().add(partSashContainer);
		application.getChildren().add(window);

		// make a new window with nothing
		MWindow window2 = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window2);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);
		wb.createAndRunUI(window2);

		// try to show the parts in the second window, a new stack should be
		// created in the second window instead of trying to reuse the one in
		// the first window
		EPartService service = (EPartService) window2.getContext().get(
				EPartService.class.getName());
		service.showPart("part", EPartService.PartState.VISIBLE);
		service.showPart("part", EPartService.PartState.CREATE);

		service.showPart("part2", EPartService.PartState.CREATE);

		while (Display.getDefault().readAndDispatch())
			;
	}

	public void testPart_ToBeRendered() {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId("partA");
		partA.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setId("partB");
		partB.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		stack.getChildren().add(partA);
		stack.getChildren().add(partB);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder tabFolder = (CTabFolder) stack.getWidget();
		assertEquals(0, tabFolder.getSelectionIndex());

		EPartService service = (EPartService) window.getContext().get(
				EPartService.class.getName());
		service.activate(partB);
		assertEquals(1, tabFolder.getSelectionIndex());

		// set the currently active part to not be rendered
		partB.setToBeRendered(false);
		assertEquals(1, tabFolder.getItemCount());
		assertEquals(0, tabFolder.getSelectionIndex());
		assertEquals(partA, stack.getSelectedElement());
	}

	public void testPart_ToBeRendered2() throws Exception {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId("partA");
		partA.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setId("partB");
		partB.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partB.setToBeRendered(false);

		stack.getChildren().add(partA);
		stack.getChildren().add(partB);
		stack.setSelectedElement(partA);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder tabFolder = (CTabFolder) stack.getWidget();
		assertEquals(1, tabFolder.getItemCount());
		assertEquals(0, tabFolder.getSelectionIndex());

		partB.setToBeRendered(true);
		assertEquals(
				"Rendering another part in the stack should not change the selection",
				0, tabFolder.getSelectionIndex());
		assertEquals(partA, stack.getSelectedElement());
		assertEquals(2, tabFolder.getItemCount());
		assertNotNull(partB.getObject());
	}

	public void testClientObjectUnsetWhenNotRenderedBug301439() {
		final MWindow window = createWindowWithOneView("");
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		MPart part = stack.getChildren().get(0);

		assertNotNull(part.getWidget());
		assertNotNull(part.getRenderer());
		assertNotNull(part.getObject());

		part.setToBeRendered(false);

		CTabFolder tabFolder = (CTabFolder) stack.getWidget();

		assertNull(part.getWidget());
		assertNull(part.getRenderer());
		assertNull(part.getObject());
		assertEquals(0, tabFolder.getItemCount());
	}

	public void testCTabItem_SetControl_Bug304211() {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId("partA");
		partA.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		stack.getChildren().add(partA);
		stack.setSelectedElement(partA);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);
		IPresentationEngine engine = (IPresentationEngine) appContext
				.get(IPresentationEngine.class.getName());

		CTabFolder folder = (CTabFolder) stack.getWidget();
		CTabItem itemA = folder.getItem(0);
		assertEquals(
				"The presentation engine should have created the part and set it",
				partA.getWidget(), itemA.getControl());

		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setId("partB");
		partB.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		// add this new part to the stack
		stack.getChildren().add(partB);

		CTabItem item2 = folder.getItem(1);
		assertNull(
				"For a stack, the object will not be rendered unless explicitly required",
				item2.getControl());

		// ask the engine to render the part
		engine.createGui(partB);

		assertEquals(
				"The presentation engine should have created the part and set it",
				partB.getWidget(), item2.getControl());

		// select the new part to display it to the user
		stack.setSelectedElement(partB);

		assertEquals("Selecting the element should not have changed anything",
				partB.getWidget(), item2.getControl());
	}

	public void testToBeRenderedCausesSelectionChanges() {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = MApplicationFactory.eINSTANCE.createPart();
		partA.setId("partA");
		partA.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = MApplicationFactory.eINSTANCE.createPart();
		partB.setId("partB");
		partB.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		stack.getChildren().add(partA);
		stack.getChildren().add(partB);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder tabFolder = (CTabFolder) stack.getWidget();
		assertEquals(0, tabFolder.getSelectionIndex());

		EPartService service = (EPartService) window.getContext().get(
				EPartService.class.getName());
		service.activate(partB);
		assertEquals(1, tabFolder.getSelectionIndex());

		service.activate(partA);

		partA.setToBeRendered(false);
		assertEquals(1, tabFolder.getItemCount());
		assertEquals(0, tabFolder.getSelectionIndex());
		assertEquals(partB, stack.getSelectedElement());
	}

	public void testCreateGuiBug301950() {
		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		final MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getChildren().add(part);

		IPresentationEngine renderer = (IPresentationEngine) appContext
				.get(IPresentationEngine.class.getName());
		renderer.createGui(part);
		renderer.removeGui(part);

		while (Display.getCurrent().readAndDispatch()) {
			// spin the event loop
		}
	}

	private MWindow createWindowWithOneView(String partName) {
		final MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		window.setHeight(300);
		window.setWidth(400);
		window.setLabel("MyWindow");
		MPartSashContainer sash = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(sash);
		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		sash.getChildren().add(stack);
		MPart contributedPart = MApplicationFactory.eINSTANCE.createPart();
		stack.getChildren().add(contributedPart);
		contributedPart.setLabel(partName);
		contributedPart
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		return window;
	}

}
