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

package org.eclipse.e4.ui.tests.workbench;

import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.addons.cleanupaddon.CleanupAddon;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

public class PartRenderingEngineTests extends TestCase {
	protected IEclipseContext appContext;
	protected E4Workbench wb;

	private LogListener listener = new LogListener() {
		public void logged(LogEntry entry) {
			if (!logged) {
				logged = entry.getLevel() == LogService.LOG_ERROR;
			}
		}
	};
	private boolean logged = false;

	@Override
	protected void setUp() throws Exception {
		logged = false;
		appContext = E4Application.createDefaultContext();
		appContext.set(E4Workbench.PRESENTATION_URI_ARG,
				PartRenderingEngine.engineURI);
		LogReaderService logReaderService = appContext
				.get(LogReaderService.class);
		logReaderService.addLogListener(listener);
	}

	@Override
	protected void tearDown() throws Exception {
		LogReaderService logReaderService = appContext
				.get(LogReaderService.class);
		logReaderService.removeLogListener(listener);
		if (wb != null) {
			wb.close();
		}
		appContext.dispose();
	}

	private void checkLog() {
		try {
			// sleep a bit because notifications are done on another thread
			Thread.sleep(100);
		} catch (Exception e) {
			// ignored
		}
		assertFalse(logged);
	}

	private void spinEventLoop() {
		while (Display.getCurrent().readAndDispatch()) {
			// spin the event loop
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
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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
				.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partA);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertEquals(partA, stack.getSelectedElement());

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partA);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder tabFolder = (CTabFolder) stack.getWidget();
		assertEquals(0, tabFolder.getSelectionIndex());
		assertEquals(partA, stack.getSelectedElement());

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
				.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		descriptor.setElementId("part");
		descriptor.setCategory("aStack");
		application.getDescriptors().add(descriptor);

		MPartDescriptor descriptor2 = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		descriptor2
				.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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

	public void testSetSelectedElement() {
		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		stack.getChildren().add(partA);
		stack.getChildren().add(partB);

		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		// You can set the selected element to a child
		boolean causedException = false;
		try {
			stack.setSelectedElement(partA);
		} catch (IllegalArgumentException e) {
			causedException = true;
		}
		assertFalse("Exception should not have been thrown", causedException);

		// You can *not* set the selected element to a non-child
		causedException = false;
		try {
			stack.setSelectedElement(partC);
		} catch (IllegalArgumentException e) {
			causedException = true;
		}
		assertTrue("Exception should have been thrown", causedException);
	}

	public void testSelectedElementNullingTBR() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartSashContainer container = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(container);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		partC.setElementId("partC");
		partC.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		container.getChildren().add(partA);
		container.getChildren().add(partB);
		container.getChildren().add(partC);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		// Ensure that changing the state of an element that is *not*
		// the selected element doesn't change its value
		container.setSelectedElement(partA);
		partB.setToBeRendered(false);
		assertTrue(
				"Changing the TBR of a non-selected element should not change the value of the container's seletedElement",
				container.getSelectedElement() == partA);

		// Ensure that changing the TBR state of the selected element results in
		// it going null
		container.setSelectedElement(partA);
		partA.setToBeRendered(false);
		assertTrue(
				"Changing the TBR of the selected element should have set the field to null",
				container.getSelectedElement() == null);
	}

	public void testSelectedElementNullingParentChange() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);

		MPartSashContainer container = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(container);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setElementId("partA");
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setElementId("partB");
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		partC.setElementId("partC");
		partC.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		container.getChildren().add(partA);
		container.getChildren().add(partB);
		container.getChildren().add(partC);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		// Ensure that changing the state of an element that is *not*
		// the selected element doesn't change its value
		container.setSelectedElement(partA);
		container.getChildren().remove(partB);
		assertTrue(
				"Changing the parent of a non-selected element should not change the value of the container's seletedElement",
				container.getSelectedElement() == partA);

		// Ensure that changing the parent of the selected element
		// results in it going null
		container.setSelectedElement(partA);
		container.getChildren().remove(partA);
		assertTrue(
				"Changing the parent of the selected element should have set the field to null",
				container.getSelectedElement() == null);
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
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(partA);
		stack.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(partA);
		partStack.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(partB);

		// make a third random part that's not in the UI
		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		partC.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

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
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(partA);
		partStack.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
				.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		application.getDescriptors().add(descriptorA);
		MPartDescriptor descriptorB = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		descriptorB.setElementId("sharedB");
		descriptorB
				.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
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
		window.getSharedElements().add(sharedSashContainer);
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

	public void testBug326087() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartSashContainer sashContainer = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(sashContainer);
		window.setSelectedElement(sashContainer);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		sashContainer.getChildren().add(partStack);
		sashContainer.setSelectedElement(partStack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(partA);
		partStack.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(partB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(partA.getWidget());
		assertNull(partB.getWidget());

		partStack.setSelectedElement(partB);
		assertNotNull(partA.getWidget());
		assertNotNull(partB.getWidget());

		application.getContext().get(IPresentationEngine.class)
				.removeGui(sashContainer);

		assertEquals(partB, partStack.getSelectedElement());
	}

	public void testBug327701() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getSharedElements().add(partStack);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(part1);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(part2);
		partStack.setSelectedElement(part2);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
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

		// select and activate the other perspective so that it is rendered and
		// appropriate references are generated and instantiated
		perspectiveStack.setSelectedElement(perspectiveB);
		perspectiveB.getContext().activate();

		// unrender the perspective
		perspectiveB.setToBeRendered(false);
		// we expect the part to have been reparented to another valid context,
		// that being perspectiveA's
		assertEquals(perspectiveA.getContext(), part2.getContext().getParent());
	}

	public void testBug326699() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getSharedElements().add(partStack);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPlaceholder partStackPlaceholderA = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		partStack.setCurSharedRef(partStackPlaceholderA);
		partStackPlaceholderA.setRef(partStack);
		perspectiveA.getChildren().add(partStackPlaceholderA);
		perspectiveA.setSelectedElement(partStackPlaceholderA);

		MPerspective perspectiveB = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder partStackPlaceholderB = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		partStackPlaceholderB.setRef(partStack);
		perspectiveB.getChildren().add(partStackPlaceholderB);
		perspectiveB.setSelectedElement(partStackPlaceholderB);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		part1.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(part1);
		partStack.setSelectedElement(part1);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		part2.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(part2);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(part1);
		partService.activate(part2);
		partService.activate(part1);
		partService.switchPerspective(perspectiveB);
		partService.switchPerspective(perspectiveA);

		SampleView view1 = (SampleView) part1.getObject();
		SampleView view2 = (SampleView) part2.getObject();

		appContext.get(IPresentationEngine.class).removeGui(window);
		assertFalse(view1.nullParentContext);
		assertFalse(view2.nullParentContext);
	}

	public void testBug327807() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(part1);
		partStack.setSelectedElement(part1);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(part2);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertEquals(part1, partStack.getSelectedElement());

		appContext.get(IPresentationEngine.class).removeGui(window);
		assertEquals(part1, partStack.getSelectedElement());
	}

	public void testBug328629() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartSashContainer partSashContainer = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		partSashContainer.setToBeRendered(false);
		window.getChildren().add(partSashContainer);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		partSashContainer.setToBeRendered(true);
	}

	public void test331685() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);
		window.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		IPresentationEngine engine = appContext.get(IPresentationEngine.class);
		engine.removeGui(part);
		assertNull(part.getWidget());

		engine.createGui(part, null, window.getContext());
		engine.removeGui(part);
	}

	public void testBug331795_1() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getChildren().add(part);
		window.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(part.getObject());
		assertNotNull(part.getContext());

		SampleView view = (SampleView) part.getObject();
		view.errorOnWidgetDisposal = true;

		part.setToBeRendered(false);
		assertTrue("The view should have been destroyed", view.isDestroyed());
		assertNull(part.getObject());
		assertNull(part.getContext());
	}

	public void testBug331795_2() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getChildren().add(part);
		window.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(part.getObject());
		assertNotNull(part.getContext());

		SampleView view = (SampleView) part.getObject();
		view.errorOnPreDestroy = true;

		part.setToBeRendered(false);
		assertTrue("The view should have been destroyed", view.isDestroyed());
		assertNull(part.getObject());
		assertNull(part.getContext());
	}

	public void testBug329079() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setVisible(false);
		window.getChildren().add(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);
	}

	public void testRemoveGui_Bug332163() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getSharedElements().add(part);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective1 = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspective1);
		perspectiveStack.setSelectedElement(perspective1);

		MPlaceholder partPlaceholderA1 = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		partPlaceholderA1.setRef(part);
		part.setCurSharedRef(partPlaceholderA1);
		perspective1.getChildren().add(partPlaceholderA1);
		perspective1.setSelectedElement(partPlaceholderA1);

		MPerspective perspective2 = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspective2);

		MPlaceholder partPlaceholder2 = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		partPlaceholder2.setRef(part);
		perspective2.getChildren().add(partPlaceholder2);
		perspective2.setSelectedElement(partPlaceholder2);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		IEclipseContext perspectiveContext1 = perspective1.getContext();
		IEclipseContext partContext = part.getContext();

		assertEquals(perspectiveContext1, partContext.getParent());
		assertEquals(partContext, perspectiveContext1.getActiveChild());

		EPartService partService = window.getContext().get(EPartService.class);
		partService.switchPerspective(perspective2);

		IEclipseContext perspectiveContext2 = perspective2.getContext();

		assertEquals(partContext, perspectiveContext1.getActiveChild());
		assertEquals(perspectiveContext2, partContext.getParent());
		assertEquals(partContext, perspectiveContext2.getActiveChild());

		partService.hidePart(part);

		assertEquals(perspectiveContext1, partContext.getParent());
		assertEquals(partContext, perspectiveContext1.getActiveChild());
		assertNull(
				"perspective2 doesn't have any parts, it should not have an active child context",
				perspectiveContext2.getActiveChild());
	}

	public void testBug334644_01() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		window.setToBeRendered(false);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(application);

		assertNull("No widget for an unrendered window", window.getWidget());
		assertNull("No context for an unrendered window", window.getContext());

		window.setToBeRendered(true);

		assertNotNull("Rendered window should have a widget",
				window.getWidget());
		assertNotNull("Rendered window should have a context",
				window.getContext());
	}

	public void testBug334644_02() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		window.setToBeRendered(true);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull("Rendered window should have a widget",
				window.getWidget());
		assertNotNull("Rendered window should have a context",
				window.getContext());

		window.setToBeRendered(false);

		assertNull("No widget for an unrendered window", window.getWidget());
		assertNull("No context for an unrendered window", window.getContext());
	}

	public void testRemoveGui_Bug334577_01() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

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

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		perspective.getWindows().add(detachedWindow);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(detachedWindow.getContext());
		assertNotNull(detachedWindow.getWidget());

		perspective.setToBeRendered(false);

		assertNull(detachedWindow.getContext());
		assertNull(detachedWindow.getWidget());
	}

	public void testRemoveGui_Bug334577_02() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		window.getWindows().add(detachedWindow);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(detachedWindow.getContext());
		assertNotNull(detachedWindow.getWidget());

		window.setToBeRendered(false);

		assertNull(detachedWindow.getContext());
		assertNull(detachedWindow.getWidget());
	}

	/**
	 * Test to ensure that we don't get an exception while rendering a child of
	 * an MTrimBar that doesn't have its element id set.
	 */
	public void testBug336139() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MTrimmedWindow window = BasicFactoryImpl.eINSTANCE
				.createTrimmedWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MTrimBar trimBar = BasicFactoryImpl.eINSTANCE.createTrimBar();
		window.getTrimBars().add(trimBar);

		MToolControl toolControl = MenuFactoryImpl.eINSTANCE
				.createToolControl();
		trimBar.getChildren().add(toolControl);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);
	}

	public void testBut336225() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MTrimmedWindow window = BasicFactoryImpl.eINSTANCE
				.createTrimmedWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MTrimBar trimBar = BasicFactoryImpl.eINSTANCE.createTrimBar();
		window.getTrimBars().add(trimBar);

		MToolControl toolControl = MenuFactoryImpl.eINSTANCE
				.createToolControl();
		toolControl.setContributionURI(SampleToolControl.CONTRIBUTION_URI);
		trimBar.getChildren().add(toolControl);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		SampleToolControl impl = (SampleToolControl) toolControl.getObject();

		appContext.get(IPresentationEngine.class).removeGui(window);
		assertFalse("The shell should not have been disposed first",
				impl.shellEagerlyDestroyed);
	}

	public void testBug330662() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MArea area = AdvancedFactoryImpl.eINSTANCE.createArea();
		window.getSharedElements().add(area);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		area.getChildren().add(partStack);
		area.setSelectedElement(partStack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(partA);
		partStack.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(partB);

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
		placeholderA.setRef(area);
		area.setCurSharedRef(placeholderA);
		perspectiveA.getChildren().add(placeholderA);
		perspectiveA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveB.setElementId("perspectiveB"); //$NON-NLS-1$
		perspectiveStack.getChildren().add(perspectiveB);

		MPlaceholder placeholderB = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderB.setRef(area);
		perspectiveB.getChildren().add(placeholderB);
		perspectiveB.setSelectedElement(placeholderB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.showPart(partB, PartState.ACTIVATE);

		partService.switchPerspective(perspectiveB);

		window.getContext().get(IPresentationEngine.class)
				.removeGui(perspectiveA);

		assertEquals(perspectiveB.getContext(), partA.getContext().getParent());
		assertEquals(perspectiveB.getContext(), partB.getContext().getParent());
	}

	/**
	 * Ensure that adding a detached window to a window will cause it to get
	 * rendered automatically.
	 */
	public void testBug335444_A() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		window.getWindows().add(detachedWindow);

		assertNotNull(detachedWindow.getContext());
		assertNotNull(detachedWindow.getWidget());
		assertNotNull(detachedWindow.getRenderer());
	}

	/**
	 * Ensure that adding a detached window to a perspective will cause it to
	 * get rendered automatically.
	 */
	public void testBug335444_B() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
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

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		perspective.getWindows().add(detachedWindow);

		assertNotNull(detachedWindow.getContext());
		assertNotNull(detachedWindow.getWidget());
		assertNotNull(detachedWindow.getRenderer());
	}

	/**
	 * Ensure that switching the state of the 'toBeRendered' flag of a detached
	 * window of a window will cause it to be rendered.
	 */
	public void testBug335444_C() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		detachedWindow.setToBeRendered(false);
		window.getWindows().add(detachedWindow);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		detachedWindow.setToBeRendered(true);

		assertNotNull(detachedWindow.getContext());
		assertNotNull(detachedWindow.getWidget());
		assertNotNull(detachedWindow.getRenderer());
	}

	/**
	 * Ensure that switching the state of the 'toBeRendered' flag of a detached
	 * window of a perspective will cause it to be rendered.
	 */
	public void testBug335444_D() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
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

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		detachedWindow.setToBeRendered(false);
		perspective.getWindows().add(detachedWindow);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		detachedWindow.setToBeRendered(true);

		assertNotNull(detachedWindow.getContext());
		assertNotNull(detachedWindow.getWidget());
		assertNotNull(detachedWindow.getRenderer());
	}

	public void testBug326175(boolean visible) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);
		window.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		detachedWindow.setVisible(visible);
		window.getWindows().add(detachedWindow);
		appContext.get(IPresentationEngine.class).createGui(detachedWindow);

		if (visible) {
			assertEquals(detachedWindow.getContext(), window.getContext()
					.getActiveChild());
		} else {
			assertEquals(part.getContext(), window.getContext()
					.getActiveChild());
		}
	}

	public void TODOtestBug326175_True() {
		testBug326175(true);
	}

	public void testBug326175_False() {
		testBug326175(false);
	}

	public void testCreateGui_Bug319004() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		final MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);
		window.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		IPresentationEngine engine = appContext.get(IPresentationEngine.class);
		engine.createGui(toolBar);
	}

	public void testBug339286() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		partStack.getChildren().add(partA);
		partStack.setSelectedElement(partA);

		MToolBar toolBarA = MenuFactoryImpl.eINSTANCE.createToolBar();
		partA.setToolbar(toolBarA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		partStack.getChildren().add(partB);

		MToolBar toolBarB = MenuFactoryImpl.eINSTANCE.createToolBar();
		partB.setToolbar(toolBarB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Widget widgetA = (Widget) toolBarA.getWidget();
		Widget widgetB = (Widget) toolBarB.getWidget();
		assertNotNull(widgetA);
		assertFalse(widgetA.isDisposed());
		assertNull(widgetB);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB);

		widgetA = (Widget) toolBarA.getWidget();
		widgetB = (Widget) toolBarB.getWidget();
		assertNotNull(widgetA);
		assertFalse(widgetA.isDisposed());
		assertNotNull(widgetB);
		assertFalse(widgetB.isDisposed());
	}

	public void testBug334580_01() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		window.getSharedElements().add(part);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveA.setElementId("perspectiveA"); //$NON-NLS-1$
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPartStack partStackA = BasicFactoryImpl.eINSTANCE.createPartStack();
		perspectiveA.getChildren().add(partStackA);
		perspectiveA.setSelectedElement(partStackA);

		MPlaceholder placeholderA = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderA.setRef(part);
		part.setCurSharedRef(placeholderA);
		partStackA.getChildren().add(placeholderA);
		partStackA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveB.setElementId("perspectiveB"); //$NON-NLS-1$
		perspectiveStack.getChildren().add(perspectiveB);

		MPartStack partStackB = BasicFactoryImpl.eINSTANCE.createPartStack();
		perspectiveB.getChildren().add(partStackB);
		perspectiveB.setSelectedElement(partStackB);

		MPlaceholder placeholderB = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderB.setRef(part);
		partStackB.getChildren().add(placeholderB);
		partStackB.setSelectedElement(placeholderB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Shell limboShell = (Shell) appContext.get("limbo");
		assertNotNull(limboShell);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.switchPerspective(perspectiveB);
		partService.switchPerspective(perspectiveA);

		Control control = (Control) toolBar.getWidget();
		assertNotNull(control);
		assertFalse(control.isDisposed());

		partService.hidePart(part);
		control = (Control) toolBar.getWidget();
		assertNotNull(control);
		assertFalse(control.isDisposed());
		assertEquals(limboShell, control.getShell());

		partService.switchPerspective(perspectiveB);
		partService.hidePart(part);
		assertTrue(control.isDisposed());
		assertNull(toolBar.getWidget());
	}

	public void testBug334580_02() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		partStack.getChildren().add(partA);
		partStack.setSelectedElement(partA);

		MToolBar toolBarA = MenuFactoryImpl.eINSTANCE.createToolBar();
		partA.setToolbar(toolBarA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		partStack.getChildren().add(partB);

		MToolBar toolBarB = MenuFactoryImpl.eINSTANCE.createToolBar();
		partB.setToolbar(toolBarB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Shell limboShell = (Shell) appContext.get("limbo");
		assertNotNull(limboShell);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB);
		partService.activate(partA);

		Control controlA = (Control) toolBarA.getWidget();
		Control controlB = (Control) toolBarB.getWidget();
		assertNotNull(controlA);
		assertFalse(controlA.isDisposed());
		assertNotNull(controlB);
		assertFalse(controlB.isDisposed());

		partService.hidePart(partA);
		controlB = (Control) toolBarB.getWidget();
		assertNull(toolBarA.getWidget());
		assertTrue(controlA.isDisposed());
		assertNotNull(controlB);
		assertFalse(controlB.isDisposed());

		partService.hidePart(partB);
		assertNull(toolBarA.getWidget());
		assertNull(toolBarB.getWidget());
		assertTrue(controlB.isDisposed());
	}

	public void testBug334580_03() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(partA);
		partStack.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(partB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Shell limboShell = (Shell) appContext.get("limbo");
		assertNotNull(limboShell);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.activate(partB);
		partService.activate(partA);
		partService.hidePart(partA);

		assertFalse(logged);
	}

	public void testBug342439_01() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setVisible(false);
		partStack.getChildren().add(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(partB);
		partStack.setSelectedElement(partB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder folder = (CTabFolder) partStack.getWidget();
		assertEquals(1, folder.getItemCount());
		assertNull(partA.getWidget());

		partA.setVisible(true);
		assertEquals(2, folder.getItemCount());
		assertNull(partA.getWidget());

		window.getContext().get(EPartService.class)
				.showPart(partA, PartState.ACTIVATE);

		assertEquals(2, folder.getItemCount());
		assertNotNull(partA.getWidget());
	}

	public void testBug342439_02() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setVisible(false);
		partStack.getChildren().add(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(partB);
		partStack.setSelectedElement(partB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder folder = (CTabFolder) partStack.getWidget();
		assertEquals(1, folder.getItemCount());

		partA.setToBeRendered(false);
		assertEquals(1, folder.getItemCount());

		partA.setVisible(true);
		assertEquals(1, folder.getItemCount());

		partA.setVisible(false);
		assertEquals(1, folder.getItemCount());
	}

	public void testBug342366() throws Exception {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(partB);
		partStack.setSelectedElement(partB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		CTabFolder folder = (CTabFolder) partStack.getWidget();
		assertEquals(2, folder.getItemCount());

		partA.setVisible(false);
		assertEquals(1, folder.getItemCount());

		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		partStack.getChildren().add(partC);

		checkLog();
	}

	public void testBug343305() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getSharedElements().add(part);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspectiveA = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspectiveA);
		perspectiveStack.setSelectedElement(perspectiveA);

		MPartStack partStackA = BasicFactoryImpl.eINSTANCE.createPartStack();
		perspectiveA.getChildren().add(partStackA);
		perspectiveA.setSelectedElement(partStackA);

		MPlaceholder placeholderA = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderA.setRef(part);
		part.setCurSharedRef(placeholderA);
		partStackA.getChildren().add(placeholderA);
		partStackA.setSelectedElement(placeholderA);

		MPerspective perspectiveB = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspectiveB);

		MPartStack partStackB = BasicFactoryImpl.eINSTANCE.createPartStack();
		perspectiveB.getChildren().add(partStackB);
		perspectiveB.setSelectedElement(partStackB);

		MPlaceholder placeholderB = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderB.setToBeRendered(false);
		placeholderB.setRef(part);
		partStackB.getChildren().add(placeholderB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.switchPerspective(perspectiveB);
		partService.switchPerspective(perspectiveA);

		Control control = (Control) toolBar.getWidget();
		Control stackIntermediate = control.getParent();
		Control parent = (Control) partStackA.getWidget();
		assertEquals(parent, stackIntermediate.getParent());

		partStackB.setToBeRendered(false);
		stackIntermediate = control.getParent();
		assertEquals(parent, stackIntermediate.getParent());
	}

	public void testBug343442() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getSharedElements().add(part);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		MPerspective perspective = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		MPlaceholder placeholder = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholder.setRef(part);
		part.setCurSharedRef(placeholder);
		partStack.getChildren().add(placeholder);
		partStack.setSelectedElement(placeholder);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		partStack.getChildren().remove(placeholder);
		partStack.getChildren().add(placeholder);
		partStack.setSelectedElement(placeholder);

		// stack renderers place a Composite between the CTF and the toolbar
		assertEquals(partStack.getWidget(), ((Control) toolBar.getWidget())
				.getParent().getParent());
	}

	public void testBug343524() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MTrimmedWindow window = BasicFactoryImpl.eINSTANCE
				.createTrimmedWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MTrimBar trimBar = BasicFactoryImpl.eINSTANCE.createTrimBar();
		window.getTrimBars().add(trimBar);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(trimBar.getWidget());

		trimBar.setToBeRendered(false);
		assertNull(trimBar.getWidget());

		trimBar.setToBeRendered(true);
		assertNotNull(trimBar.getWidget());
	}

	public void testBug332463() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MArea area = AdvancedFactoryImpl.eINSTANCE.createArea();
		window.getChildren().add(area);
		window.setSelectedElement(area);

		MPartSashContainer sashContainer = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		area.getChildren().add(sashContainer);
		area.setSelectedElement(sashContainer);

		MPartStack partStackA = BasicFactoryImpl.eINSTANCE.createPartStack();
		sashContainer.getChildren().add(partStackA);
		sashContainer.setSelectedElement(partStackA);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partStackA.getChildren().add(partA);
		partStackA.setSelectedElement(partA);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partStackA.getChildren().add(partB);
		partStackA.setSelectedElement(partB);

		MPartStack partStackB = BasicFactoryImpl.eINSTANCE.createPartStack();
		sashContainer.getChildren().add(partStackB);
		sashContainer.setSelectedElement(partStackB);

		MPart partC = BasicFactoryImpl.eINSTANCE.createPart();
		partStackB.getChildren().add(partC);
		partStackB.setSelectedElement(partC);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		ContextInjectionFactory.make(CleanupAddon.class, appContext);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.hidePart(partB);
		spinEventLoop();

		partService.hidePart(partA, true);
		spinEventLoop();

		partService.hidePart(partC, true);
		spinEventLoop();

		assertNotNull(area.getWidget());
		assertTrue(area.isToBeRendered());
	}

	public void testBug348215_PartOnlyContextReparent() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		window.getWindows().add(detachedWindow);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		detachedWindow.getChildren().add(part);
		detachedWindow.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertTrue(part.getContext() != null);
		assertTrue(part.getContext().getParent() == detachedWindow.getContext());

		detachedWindow.getChildren().remove(part);
		window.getChildren().add(part);

		assertTrue(part.getContext() != null);
		assertTrue(part.getContext().getParent() == window.getContext());
	}

	public void testBug348215_PartContextReparent() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		window.getWindows().add(detachedWindow);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		detachedWindow.getChildren().add(stack);
		detachedWindow.setSelectedElement(stack);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		stack.getChildren().add(part);
		stack.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertTrue(part.getContext() != null);
		assertTrue(part.getContext().getParent() == detachedWindow.getContext());

		detachedWindow.getChildren().remove(stack);
		window.getChildren().add(stack);

		assertTrue(part.getContext() != null);
		assertTrue(part.getContext().getParent() == window.getContext());
	}

	public void testBug348215_PartPlaceholderContextReparent() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getSharedElements().add(part);

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		window.getWindows().add(detachedWindow);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		detachedWindow.getChildren().add(stack);
		detachedWindow.setSelectedElement(stack);

		MPlaceholder ph = AdvancedFactoryImpl.eINSTANCE.createPlaceholder();
		ph.setRef(part);
		stack.getChildren().add(ph);
		stack.setSelectedElement(ph);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertTrue(part.getContext() != null);
		assertTrue(part.getContext().getParent() == detachedWindow.getContext());

		detachedWindow.getChildren().remove(stack);
		window.getChildren().add(stack);

		assertTrue(part.getContext() != null);
		assertTrue(part.getContext().getParent() == window.getContext());
	}

	public void testBug349076() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getSharedElements().add(part);

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPlaceholder ph = AdvancedFactoryImpl.eINSTANCE.createPlaceholder();
		ph.setRef(part);
		part.setCurSharedRef(ph);
		stack.getChildren().add(ph);
		stack.setSelectedElement(ph);

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		window.getWindows().add(detachedWindow);

		MPartStack detachedStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		detachedWindow.getChildren().add(detachedStack);
		detachedWindow.setSelectedElement(detachedStack);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertTrue(part.getContext() != null);
		assertTrue(part.getContext().getParent() == window.getContext());

		stack.getChildren().remove(ph);
		detachedStack.getChildren().add(ph);

		assertTrue(part.getContext() != null);
		assertTrue(part.getContext().getParent() == detachedWindow.getContext());
	}

	public void testBug369229() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();

		MPartSashContainer container = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(container);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setContributionURI(LayoutView.CONTRIBUTION_URI);
		container.getChildren().add(partA);

		MPartSashContainer innerContainer = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		container.getChildren().add(innerContainer);

		MPartSashContainer innerContainer2 = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		innerContainer.getChildren().add(innerContainer2);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI(LayoutView.CONTRIBUTION_URI);
		innerContainer.getChildren().add(partB);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(partA.getWidget());
		assertNotNull(partB.getWidget());
	}

	public void testBug348069_01() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getChildren().add(part);
		window.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		SampleView view = (SampleView) part.getObject();
		assertFalse(view.isDestroyed());

		((Shell) window.getWidget()).close();
		assertTrue(view.isDestroyed());
		assertTrue(application.getChildren().contains(window));
	}

	public void testBug348069_02() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow windowA = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(windowA);
		application.setSelectedElement(windowA);

		MPart partA = BasicFactoryImpl.eINSTANCE.createPart();
		partA.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		windowA.getChildren().add(partA);
		windowA.setSelectedElement(partA);

		MWindow windowB = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(windowB);

		MPart partB = BasicFactoryImpl.eINSTANCE.createPart();
		partB.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		windowB.getChildren().add(partB);
		windowB.setSelectedElement(partB);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(windowA);
		wb.createAndRunUI(windowB);

		SampleView viewA = (SampleView) partA.getObject();
		assertFalse(viewA.isDestroyed());
		SampleView viewB = (SampleView) partB.getObject();
		assertFalse(viewB.isDestroyed());

		((Shell) windowB.getWidget()).close();
		assertTrue(viewB.isDestroyed());
		assertFalse(windowB.isToBeRendered());
		assertFalse(application.getChildren().contains(windowB));

		((Shell) windowA.getWidget()).close();
		assertTrue(viewA.isDestroyed());
		assertTrue(windowA.isToBeRendered());
		assertTrue(application.getChildren().contains(windowA));
	}

	public void testBug348069_DetachedWindow_01() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		window.getWindows().add(detachedWindow);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		detachedWindow.getChildren().add(part);
		detachedWindow.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		SampleView view = (SampleView) part.getObject();
		assertFalse(view.isDestroyed());

		((Shell) detachedWindow.getWidget()).close();
		assertTrue(view.isDestroyed());
		assertTrue(window.getWindows().contains(detachedWindow));
	}

	public void testBug348069_DetachedWindow_02() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		window.getWindows().add(detachedWindow);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		detachedWindow.getChildren().add(part);
		detachedWindow.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		SampleView view = (SampleView) part.getObject();
		assertFalse(view.isDestroyed());

		detachedWindow.setSelectedElement(null);
		detachedWindow.getChildren().remove(part);
		window.getChildren().add(part);

		((Shell) detachedWindow.getWidget()).close();
		assertFalse(view.isDestroyed());
		assertFalse(window.getWindows().contains(detachedWindow));
	}

	public void testBug348069_DetachedWindow_03() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		window.getWindows().add(detachedWindow);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		detachedWindow.getChildren().add(part);
		detachedWindow.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		SampleView view = (SampleView) part.getObject();
		assertFalse(view.isDestroyed());

		((Shell) detachedWindow.getWidget()).close();
		assertTrue(view.isDestroyed());
		assertTrue(window.getWindows().contains(detachedWindow));
	}

	private void testBug348069_DetachedPerspectiveWindow_01(
			boolean createPlaceholder) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

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

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		perspective.getWindows().add(detachedWindow);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		detachedWindow.getChildren().add(partStack);
		detachedWindow.setSelectedElement(partStack);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		if (createPlaceholder) {
			window.getSharedElements().add(part);

			MPlaceholder placeholder = AdvancedFactoryImpl.eINSTANCE
					.createPlaceholder();
			placeholder.setRef(part);
			part.setCurSharedRef(placeholder);

			partStack.getChildren().add(placeholder);
			partStack.setSelectedElement(placeholder);
		} else {
			partStack.getChildren().add(part);
			partStack.setSelectedElement(part);
		}

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		SampleView view = (SampleView) part.getObject();
		assertFalse(view.isDestroyed());

		((Shell) detachedWindow.getWidget()).close();
		assertTrue(view.isDestroyed());
		assertTrue(perspective.getWindows().contains(detachedWindow));
	}

	public void testBug348069_DetachedPerspectiveWindow_01_TRUE() {
		testBug348069_DetachedPerspectiveWindow_01(true);
	}

	public void testBug348069_DetachedPerspectiveWindow_01_FALSE() {
		testBug348069_DetachedPerspectiveWindow_01(false);
	}

	private void testBug348069_DetachedPerspectiveWindow_02(
			boolean createPlaceholder) {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

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

		MWindow detachedWindow = BasicFactoryImpl.eINSTANCE.createWindow();
		perspective.getWindows().add(detachedWindow);

		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		detachedWindow.getChildren().add(partStack);
		detachedWindow.setSelectedElement(partStack);

		MPlaceholder placeholder = null;
		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		if (createPlaceholder) {
			window.getSharedElements().add(part);

			placeholder = AdvancedFactoryImpl.eINSTANCE.createPlaceholder();
			placeholder.setRef(part);
			part.setCurSharedRef(placeholder);

			partStack.getChildren().add(placeholder);
			partStack.setSelectedElement(placeholder);
		} else {
			partStack.getChildren().add(part);
			partStack.setSelectedElement(part);
		}

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		SampleView view = (SampleView) part.getObject();
		assertFalse(view.isDestroyed());

		if (createPlaceholder) {
			partStack.setSelectedElement(null);
			partStack.getChildren().remove(placeholder);
			perspective.getChildren().add(placeholder);
		} else {
			partStack.setSelectedElement(null);
			partStack.getChildren().remove(part);
			perspective.getChildren().add(part);
		}

		((Shell) detachedWindow.getWidget()).close();
		assertFalse(view.isDestroyed());
		assertFalse(perspective.getWindows().contains(detachedWindow));
	}

	public void testBug348069_DetachedPerspectiveWindow_02_TRUE() {
		testBug348069_DetachedPerspectiveWindow_02(true);
	}

	public void testBug348069_DetachedPerspectiveWindow_02_FALSE() {
		testBug348069_DetachedPerspectiveWindow_02(false);
	}

	public void testBug371100() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();

		MPartStack stack = BasicFactoryImpl.eINSTANCE.createPartStack();
		window.getChildren().add(stack);
		window.setSelectedElement(stack);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setVisible(false);
		stack.getChildren().add(part);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		stack.setSelectedElement(part);
		assertFalse(logged);
	}

	public void testBug372226() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		Shell subShell = new Shell((Shell) window.getWidget());

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getSharedElements().add(part);

		appContext.get(EModelService.class).hostElement(part, window, subShell,
				window.getContext());
		Control control = (Control) part.getWidget();
		assertEquals(subShell, control.getParent());

		appContext.get(EPartService.class).activate(part);
		assertEquals(subShell, control.getParent());
	}

	public void testBug374326() {
		MTrimmedWindow window = BasicFactoryImpl.eINSTANCE
				.createTrimmedWindow();
		MTrimBar trim = BasicFactoryImpl.eINSTANCE.createTrimBar();
		window.getTrimBars().add(trim);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		trim.getChildren().add(toolBar);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		MToolControl toolControl = MenuFactoryImpl.eINSTANCE
				.createToolControl();
		toolControl.setVisible(false);
		toolControl
				.setContributionURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.Bug374326");
		toolBar.getChildren().add(toolControl);
		assertNull(toolControl.getObject());

		toolControl.setVisible(true);

		Bug374326 obj = (Bug374326) toolControl.getObject();
		Shell shell = (Shell) window.getWidget();
		assertEquals(shell, obj.getControl().getShell());
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
				.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		return window;
	}

	public void test369434() {
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();

		MPerspectiveStack perspectiveStack = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);

		MPerspective perspective = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspective.setVisible(false);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertFalse(logged);
	}

	public void test_persistState_371087() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getChildren().add(part);
		window.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(part.getObject());
		assertNotNull(part.getContext());

		SampleView view = (SampleView) part.getObject();
		view.errorOnWidgetDisposal = true;

		part.setToBeRendered(false);
		assertTrue("The view should have been destroyed",
				view.isStatePersisted());
		assertNull(part.getObject());
		assertNull(part.getContext());
	}

	public void test_persistState_371087_1() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getChildren().add(part);
		window.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertNotNull(part.getObject());
		assertNotNull(part.getContext());

		SampleView view = (SampleView) part.getObject();
		view.errorOnWidgetDisposal = true;

		window.setToBeRendered(false);
		assertTrue("The view should have been destroyed",
				view.isStatePersisted());
		assertNull(part.getObject());
		assertNull(part.getContext());
	}
}
