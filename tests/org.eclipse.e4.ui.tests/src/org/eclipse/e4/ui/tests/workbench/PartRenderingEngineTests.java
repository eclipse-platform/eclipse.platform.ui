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
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.widgets.CTabItem;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		MPart part = (MPart) stack.getChildren().get(0);

		IPresentationEngine renderer = (IPresentationEngine) appContext
				.get(IPresentationEngine.class.getName());
		renderer.removeGui(part);
		renderer.removeGui(window);

		while (Display.getCurrent().readAndDispatch()) {
			// spin the event loop
		}
	}

	public void testAddWindowBug299219() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(window.getWidget());

		MWindow window2 = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window2);

		assertNotNull(window2.getWidget());
	}

	public void testPartStack_SetActiveChildBug299379() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		stack.getChildren().add(partA);
		stack.getChildren().add(partB);
		stack.setSelectedElement(partA);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder tabFolder = (CTabFolder) stack.getWidget();
		assertEquals(0, tabFolder.getSelectionIndex());

		EPartService service = (EPartService) window.getContext().get(
				EPartService.class.getName());
		service.showPart(partB.getElementId(), PartState.ACTIVATE);
		assertEquals("Showing a part should alter the tab folder's selection",
				1, tabFolder.getSelectionIndex());
	}

	public void testPartStack_SetActiveChild3Bug299379() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MPartDescriptor descriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		descriptor
				.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		descriptor.setElementId("part");
		descriptor.setCategory("aStack");
		application.getDescriptors().add(descriptor);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partA);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertEquals(partA, stack.getSelectedElement());

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partB);

		assertEquals(
				"Adding a part to a stack should not cause the stack's active child to change",
				partA, stack.getSelectedElement());
		assertNull("The object should not have been instantiated",
				partB.getObject());
	}

	public void testPartStack_SetActiveChild6Bug298797() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partA);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder tabFolder = (CTabFolder) stack.getWidget();
		assertEquals(0, tabFolder.getSelectionIndex());
		assertEquals(partA, stack.getSelectedElement());

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partB);

		assertEquals(0, tabFolder.getSelectionIndex());
		assertEquals(partA, stack.getSelectedElement());

		stack.setSelectedElement(partB);
		assertEquals(1, tabFolder.getSelectionIndex());
		assertEquals(partB, stack.getSelectedElement());
	}

	public void testCreateGuiBug301021() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		// create two descriptors
		MPartDescriptor descriptor = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		descriptor
				.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		descriptor.setElementId("part");
		descriptor.setCategory("aStack");
		application.getDescriptors().add(descriptor);

		MPartDescriptor descriptor2 = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		descriptor2
				.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		descriptor2.setElementId("part2");
		descriptor2.setCategory("aStack");
		application.getDescriptors().add(descriptor2);

		// make a window with a sash container and a stack inside, this will
		// force the stack to have SashFormData
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MPartSashContainer partSashContainer = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		// assign the stack with the category id of the descriptors above
		stack.setElementId("aStack");
		partSashContainer.getChildren().add(stack);
		window.getChildren().add(partSashContainer);
		application.getChildren().add(window);

		// make a new window with nothing
		MWindow window2 = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window2);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);
		IPresentationEngine engine = appContext.get(IPresentationEngine.class);
		engine.createGui(window2);

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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPartSashContainer container = (MPartSashContainer) window
				.getChildren().get(0);
		MPartStack stack = (MPartStack) container.getChildren().get(0);
		MPart part = (MPart) stack.getChildren().get(0);

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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		final MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getChildren().add(part);

		IPresentationEngine renderer = (IPresentationEngine) appContext
				.get(IPresentationEngine.class.getName());
		renderer.createGui(part);
		renderer.removeGui(part);

		while (Display.getCurrent().readAndDispatch()) {
			// spin the event loop
		}
	}

	public void testRemoveGuiBug307578() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		final MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		// create a stack
		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		// put two parts in it
		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partA);
		stack.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder folder = (CTabFolder) stack.getWidget();
		// two parts, two items
		assertEquals(2, folder.getItemCount());

		// this part shouldn't have anything created yet because it's not the
		// stack's selected element
		assertNull(partB.getRenderer());
		assertNull(partB.getObject());
		assertNull(partB.getWidget());

		// try to remove the tab
		IPresentationEngine renderer = (IPresentationEngine) appContext
				.get(IPresentationEngine.class.getName());
		renderer.removeGui(partB);

		// item removed, one item
		assertEquals(1, folder.getItemCount());
	}

	public void testRemoveGuiBug324033() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		// put two parts in it
		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(partA);
		partStack.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(partB);

		// make a third random part that's not in the UI
		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		partC.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(partA.getObject());
		assertNull(partB.getObject());

		// ask the renderer to remove the random part so that it will
		// incorrectly record it
		appContext.get(IPresentationEngine.class).removeGui(partC);
		// remove the part stack
		appContext.get(IPresentationEngine.class).removeGui(partStack);

		assertNull(partA.getObject());
		assertNull(partB.getObject());
	}

	public void testRemoveGuiBug323496() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getSharedElements().add(part);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPlaceholder placeholder = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholder.setRef(part);
		part.setCurSharedRef(placeholder);
		perspective.getChildren().add(placeholder);
		perspective.setSelectedElement(placeholder);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(part.getObject());

		appContext.get(IPresentationEngine.class).removeGui(perspective);

		assertNull(part.getObject());
	}

	public void testBug324839() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		// put two parts in it
		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(partA);
		partStack.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(partB);

		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(partA.getObject());
		assertNull(partB.getObject());

		// hide and remove the selected part with the EPS
		appContext.get(EPartService.class).hidePart(partA, true);

		assertNull(partA.getObject());
		assertNotNull(partB.getObject());
	}

	public void testBug317591_NonSharedPart() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		final MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partA);
		stack.setSelectedElement(partA);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(partA.getObject());

		stack.setToBeRendered(false);

		assertNull(partA.getObject());
	}

	public void testBug317591_SharedPart() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MPartDescriptor descriptorA = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		descriptorA.setElementId("sharedA");
		descriptorA
				.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		application.getDescriptors().add(descriptorA);
		MPartDescriptor descriptorB = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		descriptorB.setElementId("sharedB");
		descriptorB
				.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		application.getDescriptors().add(descriptorB);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		EPartService partService = window.getContext().get(EPartService.class);

		MPlaceholder placeholderA = partService.createSharedPart("sharedA");
		stack.getChildren().add(placeholderA);
		stack.setSelectedElement(placeholderA);

		MPart partA = (MPart) placeholderA.getRef();

		assertNotNull(partA.getObject());

		stack.setToBeRendered(false);

		assertNull(partA.getObject());
	}

	public void testRemoveGuiBug324228_1() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getSharedElements().add(part);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveA.setElementId("perspectiveA"); //$NON-NLS-1$
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderA.setRef(part);
		part.setCurSharedRef(placeholderA);
		perspectiveA.getChildren().add(placeholderA);
		perspectiveA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveB.setElementId("perspectiveB"); //$NON-NLS-1$
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderB = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderB.setRef(part);
		perspectiveB.getChildren().add(placeholderB);
		perspectiveB.setSelectedElement(placeholderB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		SampleView view = (SampleView) part.getObject();
		assertFalse(view.isDestroyed());

		perspectiveStack.setSelectedElement(perspectiveB);
		appContext.get(EModelService.class).removePerspectiveModel(
				perspectiveB, window);
		assertFalse(view.isDestroyed());
		assertNotNull(part.getContext().getParent());
		assertEquals(perspectiveA.getContext(), part.getContext().getParent());
	}

	public void testRemoveGuiBug324228_2() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getSharedElements().add(part);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveA.setElementId("perspectiveA"); //$NON-NLS-1$
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderA.setRef(part);
		part.setCurSharedRef(placeholderA);
		perspectiveA.getChildren().add(placeholderA);
		perspectiveA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveB.setElementId("perspectiveB"); //$NON-NLS-1$
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderB = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderB.setRef(part);
		perspectiveB.getChildren().add(placeholderB);
		perspectiveB.setSelectedElement(placeholderB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		SampleView view = (SampleView) part.getObject();
		assertFalse(view.isDestroyed());

		perspectiveStack.setSelectedElement(perspectiveB);
		placeholderB.setToBeRendered(false);
		assertFalse(view.isDestroyed());
		assertNotNull(part.getContext().getParent());
		assertEquals(perspectiveA.getContext(), part.getContext().getParent());
	}

	public void testRemoveGuiBug324228_3() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getSharedElements().add(partStack);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(part);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveA.setElementId("perspectiveA"); //$NON-NLS-1$
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderA.setRef(partStack);
		partStack.setCurSharedRef(placeholderA);
		perspectiveA.getChildren().add(placeholderA);
		perspectiveA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveB.setElementId("perspectiveB"); //$NON-NLS-1$
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderB = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderB.setRef(partStack);
		perspectiveB.getChildren().add(placeholderB);
		perspectiveB.setSelectedElement(placeholderB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		SampleView view = (SampleView) part.getObject();
		assertFalse(view.isDestroyed());

		perspectiveStack.setSelectedElement(perspectiveB);
		placeholderB.setToBeRendered(false);
		assertFalse(view.isDestroyed());
		assertNotNull(part.getContext().getParent());
		assertEquals(perspectiveA.getContext(), part.getContext().getParent());
	}

	public void testRemoveGuiBug324228_4() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getSharedElements().add(partStack);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(part);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveA.setElementId("perspectiveA"); //$NON-NLS-1$
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder placeholderA = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderA.setRef(partStack);
		partStack.setCurSharedRef(placeholderA);
		perspectiveA.getChildren().add(placeholderA);
		perspectiveA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveB.setElementId("perspectiveB"); //$NON-NLS-1$
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderB = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderB.setRef(partStack);
		perspectiveB.getChildren().add(placeholderB);
		perspectiveB.setSelectedElement(placeholderB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		SampleView view = (SampleView) part.getObject();
		assertFalse(view.isDestroyed());

		perspectiveStack.setSelectedElement(perspectiveB);
		appContext.get(EModelService.class).removePerspectiveModel(
				perspectiveB, window);
		assertFalse(view.isDestroyed());
		assertNotNull(part.getContext().getParent());
		assertEquals(perspectiveA.getContext(), part.getContext().getParent());
	}

	public void testRemoveGuiBug324230() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartSashContainer sashContainer = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(sashContainer);
		window.setSelectedElement(sashContainer);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setToBeRendered(false);
		// add an element into the container that's not being rendered
		sashContainer.getChildren().add(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(sashContainer.getWidget());

		appContext.get(IPresentationEngine.class).removeGui(sashContainer);

		assertNull(sashContainer.getWidget());
	}

	public void testBug317849() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartSashContainer sashContainer = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(sashContainer);
		window.setSelectedElement(sashContainer);

		MPlaceholder sharedAreaPlaceholder = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		sashContainer.getChildren().add(sharedAreaPlaceholder);
		sashContainer.setSelectedElement(sharedAreaPlaceholder);

		MPartSashContainer sharedSashContainer = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		sharedAreaPlaceholder.setRef(sharedSashContainer);
		sharedSashContainer.setCurSharedRef(sharedAreaPlaceholder);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		sharedSashContainer.getChildren().add(partStack);
		sharedSashContainer.setSelectedElement(partStack);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(partStack.getWidget());
		assertNotNull(sharedSashContainer.getWidget());
		assertNotNull(sashContainer.getWidget());
	}

	private MWindow createWindowWithOneView(String partName) {
		final MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		window.setHeight(300);
		window.setWidth(400);
		window.setLabel("MyWindow");
		MPartSashContainer sash = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(sash);
		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		sash.getChildren().add(stack);
		MPart contributedPart = BasicFactoryImpl.eINSTANCE.createPart();
		stack.getChildren().add(contributedPart);
		contributedPart.setLabel(partName);
		contributedPart
				.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		return window;
	}

}
