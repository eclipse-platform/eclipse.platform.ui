/*******************************************************************************
* Copyright (c) 2025 Oliver Lins and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which accompanies this distribution,
* and is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.dndaddon;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

public class StackDropAgentTest {

	private static final int TAB_ITEM_HEIGHT = 20;
	private static final Point CURSOR_POSITION = new Point(15, 15);
	private static final String ELEMENT_ID = "elementId";

	@Mock
	private DnDManager dndManagerMock;
	@Mock
	private MPart testDragMPartElementMock;
	@Mock
	private MPartStack testDragMPartStackElementMock;
	@Mock
	private DnDInfo dndInfoMock;
	@Mock
	private MPartStack dropStackMock;
	@Mock
	private CTabFolder dropCTFMock;
	@Mock
	private MWindow windowMock;
	@Mock
	private EModelService modelServiceMock;

	private StackDropAgent testee;

	private AutoCloseable openedMocks;

	@BeforeEach
	public void setUp() throws Exception {
		openedMocks = MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	void afterEach() throws Exception {
		openedMocks.close();
	}

	@Test
	public void testCanDrop() {
		try (MockedStatic<Display> staticDisplayMock = mockStatic(Display.class)) {
			Rectangle areaMock = mock(Rectangle.class);
			Display displayMock = mock(Display.class);

			testee = createCanDropTestee();

			when(areaMock.contains(nullable(Point.class))).thenReturn(true);

			when(displayMock.map(nullable(Control.class), nullable(Control.class), any(Rectangle.class)))
					.thenReturn(areaMock);
			staticDisplayMock.when(Display::getCurrent).thenReturn(displayMock);

			when(modelServiceMock.getTopLevelWindowFor(any(MUIElement.class))).thenReturn(windowMock);

			when(dndManagerMock.getModelService()).thenReturn(modelServiceMock);

			when(dropStackMock.getTags()).thenReturn(List.of());
			when(dropStackMock.getWidget()).thenReturn(dropCTFMock);

			dndInfoMock.curElement = dropStackMock;

			boolean drop = testee.canDrop(testDragMPartElementMock, dndInfoMock);

			assertTrue(drop);
			verify(areaMock).contains(nullable(Point.class));
		}
	}

	@Test
	public void testCanDrop_NoDropOntoNonPartStack() {

		testee = createCanDropTestee();

		dndInfoMock.curElement = mock(MPerspectiveStack.class);
		boolean drop = testee.canDrop(testDragMPartElementMock, dndInfoMock);

		assertFalse(drop);
		verify(dropStackMock, never()).getTags();
	}

	@Test
	public void testCanDrop_NoDropInStandalone() {

		testee = createCanDropTestee();

		when(dropStackMock.getTags()).thenReturn(List.of(IPresentationEngine.STANDALONE));

		dndInfoMock.curElement = dropStackMock;
		boolean drop = testee.canDrop(testDragMPartElementMock, dndInfoMock);

		assertFalse(drop);
		verify(dropStackMock).getTags();
		verify(dropStackMock, never()).getWidget();
	}

	@Test
	public void testCanDrop_NoDropTargetNoTabFolder() {

		testee = createCanDropTestee();

		when(dropStackMock.getTags()).thenReturn(List.of());
		when(dropStackMock.getWidget()).thenReturn(mock(CTabItem.class));

		dndInfoMock.curElement = dropStackMock;
		boolean drop = testee.canDrop(testDragMPartElementMock, dndInfoMock);

		assertFalse(drop);
		verify(dropStackMock).getWidget();
	}

	@Test
	public void testCanDrop_NoDropOntoItself() {

		testee = createCanDropTestee();

		when(testDragMPartStackElementMock.getTags()).thenReturn(List.of());
		when(testDragMPartStackElementMock.getWidget()).thenReturn(mock(CTabFolder.class));

		dndInfoMock.curElement = testDragMPartStackElementMock;
		boolean drop = testee.canDrop(testDragMPartStackElementMock, dndInfoMock);

		assertFalse(drop);
		verify(testDragMPartStackElementMock).getWidget();
		verify(dndManagerMock, never()).getModelService();
	}

	@Test
	public void testCanDrop_NoDropDiffTopWindows() {

		testee = createCanDropTestee();

		when(dropStackMock.getTags()).thenReturn(List.of());
		when(dropStackMock.getWidget()).thenReturn(mock(CTabItem.class));
		when(dropStackMock.getWidget()).thenReturn(dropCTFMock);

		when(modelServiceMock.getTopLevelWindowFor(eq(testDragMPartElementMock))).thenReturn(windowMock);
		when(modelServiceMock.getTopLevelWindowFor(eq(dropStackMock))).thenReturn(mock(MWindow.class));

		when(dndManagerMock.getModelService()).thenReturn(modelServiceMock);

		dndInfoMock.curElement = dropStackMock;
		boolean drop = testee.canDrop(testDragMPartElementMock, dndInfoMock);

		assertFalse(drop);
		verify(modelServiceMock).getTopLevelWindowFor(eq(testDragMPartElementMock));
		verify(modelServiceMock).getTopLevelWindowFor(eq(dropStackMock));
	}

	@Test
	public void testCanDrop_NoDropOutsideTabFolderArea() {
		try (MockedStatic<Display> staticDisplayMock = mockStatic(Display.class)) {
			Rectangle areaMock = mock(Rectangle.class);
			Display displayMock = mock(Display.class);

			testee = createCanDropTestee();

			when(areaMock.contains(nullable(Point.class))).thenReturn(false);

			when(displayMock.map(nullable(Control.class), nullable(Control.class), any(Rectangle.class)))
					.thenReturn(areaMock);
			staticDisplayMock.when(Display::getCurrent).thenReturn(displayMock);

			when(modelServiceMock.getTopLevelWindowFor(any(MUIElement.class))).thenReturn(windowMock);

			when(dndManagerMock.getModelService()).thenReturn(modelServiceMock);

			when(dropStackMock.getTags()).thenReturn(List.of());
			when(dropStackMock.getWidget()).thenReturn(dropCTFMock);

			dndInfoMock.curElement = dropStackMock;
			boolean drop = testee.canDrop(testDragMPartElementMock, dndInfoMock);

			assertFalse(drop);
			verify(areaMock).contains(nullable(Point.class));
			verify(dropStackMock, times(2)).getWidget();
		}
	}

	@Test
	public void testDrop_MPart() throws Exception {
		List<MStackElement> children = new ArrayList<>();

		when(dropStackMock.getChildren()).thenReturn(children);

		doTestDrop(testDragMPartElementMock, 1);
	}

	@Test
	public void testDrop_MultipleMPartsSameElementId_EditorTag() throws Exception {
		List<MStackElement> children = new ArrayList<>();

		MPart part1 = mock(MPart.class);
		when(part1.getElementId()).thenReturn(ELEMENT_ID);
		children.add(part1);
		MPart part2 = mock(MPart.class);
		when(part2.getElementId()).thenReturn(ELEMENT_ID);
		children.add(part2);
		when(dropStackMock.getChildren()).thenReturn(children);

		when(testDragMPartElementMock.getElementId()).thenReturn(ELEMENT_ID);
		when(testDragMPartElementMock.getTags()).thenReturn(List.of("Editor"));

		doTestDrop(testDragMPartElementMock, 3);
	}

	@Test
	public void testDrop_MultipleMPartsMPlaceholderSameElementId_EditorTag() throws Exception {
		List<MStackElement> children = new ArrayList<>();

		MPart part1 = mock(MPart.class);
		when(part1.getElementId()).thenReturn(ELEMENT_ID);
		children.add(part1);
		MPart part2 = mock(MPart.class);
		when(part2.getElementId()).thenReturn(ELEMENT_ID);
		children.add(part2);
		MPlaceholder placeholder = mock(MPlaceholder.class);
		when(placeholder.getElementId()).thenReturn(ELEMENT_ID);
		children.add(placeholder);
		when(dropStackMock.getChildren()).thenReturn(children);

		when(testDragMPartElementMock.getElementId()).thenReturn(ELEMENT_ID);
		when(testDragMPartElementMock.getTags()).thenReturn(List.of("Editor"));

		doTestDrop(testDragMPartElementMock, 4);
		assertEquals(1, children.stream().filter(e -> e instanceof MPlaceholder).count(), "MPlaceholder");
	}

	@Test
	public void testDrop_MultipleMPartsMPlaceholderSameElementId() throws Exception {
		List<MStackElement> children = new ArrayList<>();

		MPart part1 = mock(MPart.class);
		when(part1.getElementId()).thenReturn(ELEMENT_ID);
		children.add(part1);
		MPart part2 = mock(MPart.class);
		when(part2.getElementId()).thenReturn(ELEMENT_ID);
		children.add(part2);
		MPlaceholder placeholder = mock(MPlaceholder.class);
		when(placeholder.getElementId()).thenReturn(ELEMENT_ID);
		children.add(placeholder);
		when(dropStackMock.getChildren()).thenReturn(children);

		when(testDragMPartElementMock.getElementId()).thenReturn(ELEMENT_ID);

		doTestDrop(testDragMPartElementMock, 3);
		assertEquals(0, children.stream().filter(e -> e instanceof MPlaceholder).count(), "MPlaceholder");
	}

	@Test
	public void testDrop_MultipleMPartsMPlaceholderSameElementId_AnotherMPlaceholder() throws Exception {
		List<MStackElement> children = new ArrayList<>();

		MPart part1 = mock(MPart.class);
		when(part1.getElementId()).thenReturn(ELEMENT_ID);
		children.add(part1);
		MPart part2 = mock(MPart.class);
		when(part2.getElementId()).thenReturn(ELEMENT_ID);
		children.add(part2);
		MPlaceholder placeholder1 = mock(MPlaceholder.class);
		when(placeholder1.getElementId()).thenReturn(ELEMENT_ID);
		children.add(placeholder1);
		MPlaceholder placeholder2 = mock(MPlaceholder.class);
		when(placeholder2.getElementId()).thenReturn(ELEMENT_ID + "-Other");
		children.add(placeholder2);
		when(dropStackMock.getChildren()).thenReturn(children);

		when(testDragMPartElementMock.getElementId()).thenReturn(ELEMENT_ID);

		doTestDrop(testDragMPartElementMock, 4);
		assertEquals(1, children.stream().filter(e -> e instanceof MPlaceholder).count(), "MPlaceholder");
	}

	@Test
	public void testDrop_MPartStack() throws Exception {
		List<MStackElement> dropStackChildren = new ArrayList<>();
		List<MStackElement> dragStackChildren = new ArrayList<>();

		when(dropStackMock.getChildren()).thenReturn(dropStackChildren);

		dragStackChildren.add(testDragMPartElementMock);
		when(testDragMPartStackElementMock.getChildren()).thenReturn(dragStackChildren);
		when(testDragMPartStackElementMock.getSelectedElement()).thenReturn(testDragMPartElementMock);

		doTestDrop(testDragMPartStackElementMock, 1);
	}

	private void doTestDrop(MUIElement dragElement, int expectedChildren) throws Exception {
		Rectangle areaMock = mock(Rectangle.class);
		Display displayMock = mock(Display.class);

		testee = createDropTestee();

		when(areaMock.contains(nullable(Point.class))).thenReturn(true);

		when(displayMock.map(any(CTabFolder.class), nullable(Control.class), any(Rectangle.class)))
				.thenReturn(new Rectangle(10, 5, 150, 100));

		when(dropCTFMock.getDisplay()).thenReturn(displayMock);
		when(dropCTFMock.getChildren()).thenReturn(new Control[] { mock(Control.class) });

		when(dropStackMock.getWidget()).thenReturn(dropCTFMock);

		when(modelServiceMock.getTopLevelWindowFor(any(MUIElement.class))).thenReturn(windowMock);

		when(dndManagerMock.getModelService()).thenReturn(modelServiceMock);
		when(dndManagerMock.getFeedbackStyle()).thenReturn(DnDManager.SIMPLE);

		dndInfoMock.cursorPos = CURSOR_POSITION;
		boolean drop = testee.drop(dragElement, dndInfoMock);

		assertTrue(drop);
		verify(dropStackMock).setSelectedElement(any(MStackElement.class));
		assertEquals(expectedChildren, dropStackMock.getChildren().size(), "drop stack children");
	}

	@Test
	public void testTrack() throws Exception {
		List<MStackElement> children = new ArrayList<>();
		children.add(testDragMPartElementMock);

		when(dropStackMock.getChildren()).thenReturn(children);

		doTestTrack(testDragMPartElementMock);
	}

	private void doTestTrack(MUIElement dragElement) throws Exception {
		try (MockedStatic<Display> staticDisplayMock = mockStatic(Display.class)) {
			Rectangle areaMock = mock(Rectangle.class);
			Display displayMock = mock(Display.class);

			testee = createDropTestee();

			when(areaMock.contains(nullable(Point.class))).thenReturn(true);

			when(displayMock.map(any(CTabFolder.class), nullable(Control.class), any(Rectangle.class)))
					.thenReturn(new Rectangle(10, 5, 150, 100));
			staticDisplayMock.when(Display::getCurrent).thenReturn(displayMock);

			when(dropCTFMock.getDisplay()).thenReturn(displayMock);

			when(dropStackMock.isToBeRendered()).thenReturn(true);

			dndInfoMock.cursorPos = CURSOR_POSITION;
			boolean drop = testee.track(dragElement, dndInfoMock);

			assertTrue(drop);
			verify(dropStackMock).getChildren();
			verify(dndManagerMock, never()).getFeedbackStyle();
		}
	}

	@Test
	public void testDragLeave_Hosted() {

		testee = createCanDropTestee();

		when(dndManagerMock.getFeedbackStyle()).thenReturn(DnDManager.HOSTED);

		when(testDragMPartElementMock.getParent()).thenReturn(mock(MElementContainer.class));

		testee.dragLeave(testDragMPartElementMock, dndInfoMock);

		verify(dndManagerMock).clearOverlay();
		verify(dndManagerMock).getFeedbackStyle();
		verify(testDragMPartElementMock).getParent();
		verify(dndManagerMock, never()).setHostBounds(null);
		verify(dndManagerMock).hostElement(eq(testDragMPartElementMock), eq(16), eq(10));
	}

	@Test
	public void testDragLeave_NotHosted() {

		testee = createCanDropTestee();

		when(dndManagerMock.getFeedbackStyle()).thenReturn(DnDManager.SIMPLE);

		testee.dragLeave(testDragMPartElementMock, dndInfoMock);

		verify(dndManagerMock).clearOverlay();
		verify(dndManagerMock).getFeedbackStyle();
		verify(dndManagerMock).setHostBounds(null);
		verify(dndManagerMock, never()).hostElement(eq(testDragMPartElementMock), eq(16), eq(10));
	}

	private StackDropAgent createCanDropTestee() {
		mockTabFolder();
		return new StackDropAgent(dndManagerMock);
	}

	private StackDropAgent createDropTestee() throws Exception {
		mockTabFolder();

		Class<?> testeeClass = Class.forName("org.eclipse.e4.ui.workbench.addons.dndaddon.StackDropAgent");
		StackDropAgent testee = (StackDropAgent) testeeClass.getDeclaredConstructor(DnDManager.class)
				.newInstance(dndManagerMock);
		Field dropCTFField = testeeClass.getDeclaredField("dropCTF");
		dropCTFField.setAccessible(true);
		dropCTFField.set(testee, dropCTFMock);
		Field tabAreaField = testeeClass.getDeclaredField("tabArea");
		tabAreaField.setAccessible(true);
		tabAreaField.set(testee, new Rectangle(10, 5, 100, TAB_ITEM_HEIGHT));
		Field dropStackField = testeeClass.getDeclaredField("dropStack");
		dropStackField.setAccessible(true);
		dropStackField.set(testee, dropStackMock);

		return testee;
	}

	private void mockTabFolder() {
		CTabItem tabItemMock = mock(CTabItem.class);
		when(tabItemMock.isShowing()).thenReturn(true);
		when(tabItemMock.getBounds()).thenReturn(new Rectangle(10, 5, 80, TAB_ITEM_HEIGHT));
		when(tabItemMock.isShowing()).thenReturn(true);

		when(dropCTFMock.getBounds()).thenReturn(new Rectangle(10, 5, 300, 200));
		when(dropCTFMock.getItems()).thenReturn(new CTabItem[] { tabItemMock });
		when(dropCTFMock.getTabHeight()).thenReturn(TAB_ITEM_HEIGHT);
	}
}
