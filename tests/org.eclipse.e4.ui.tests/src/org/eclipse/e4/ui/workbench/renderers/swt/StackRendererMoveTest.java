package org.eclipse.e4.ui.workbench.renderers.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.tests.rules.WorkbenchContextExtension;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class StackRendererMoveTest {

	@RegisterExtension
	public WorkbenchContextExtension contextRule = new WorkbenchContextExtension();

	@Inject
	private EModelService ems;

	@Inject
	private MApplication application;

	private MWindow window;
	private MPartStack partStack;

	@BeforeEach
	public void setUp() throws Exception {
		window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);
	}

	@Test
	public void testPartMoveUpdatesWidget() throws Exception {
		// Create two parts
		MPart part1 = ems.createModelElement(MPart.class);
		part1.setLabel("Part 1");
		partStack.getChildren().add(part1);

		MPart part2 = ems.createModelElement(MPart.class);
		part2.setLabel("Part 2");
		partStack.getChildren().add(part2);

		// Render the window (and thus the stack and parts)
		contextRule.createAndRunWorkbench(window);

		CTabFolder tabFolder = (CTabFolder) partStack.getWidget();
		assertEquals(2, tabFolder.getItemCount());

		CTabItem item1 = tabFolder.getItem(0);
		CTabItem item2 = tabFolder.getItem(1);

		assertEquals(part1, item1.getData(AbstractPartRenderer.OWNING_ME));
		assertEquals(part2, item2.getData(AbstractPartRenderer.OWNING_ME));
		assertEquals(item1.getControl(), part1.getWidget());
		assertEquals(item2.getControl(), part2.getWidget());

		// Move part1 to the end (index 1)
		// We use model service to move to ensure events are fired
		ems.move(part1, partStack, 1);

		// Verify model update
		List<org.eclipse.e4.ui.model.application.ui.basic.MStackElement> children = partStack.getChildren();
		assertEquals(part2, children.get(0));
		assertEquals(part1, children.get(1));

		// Verify UI update
		assertEquals(2, tabFolder.getItemCount());
		CTabItem newItem1 = tabFolder.getItem(1);
		CTabItem newItem2 = tabFolder.getItem(0);

		// The old item1 should be disposed
		assertTrue(item1.isDisposed(), "Old item for part1 should be disposed");
		assertFalse(item2.isDisposed(), "Item2 should not be disposed");

		// part1 should have a NEW widget item, but the part's widget (content) should be preserved
		assertNotSame(item1, newItem1);
		assertEquals(part1, newItem1.getData(AbstractPartRenderer.OWNING_ME), "New item should have OWNING_ME set");
		assertEquals(part1.getWidget(), newItem1.getControl(), "Part1 widget should be the control of the new item");
		
		// part2 should still be valid and same widget
		assertEquals(item2, newItem2);
		assertEquals(part2, newItem2.getData(AbstractPartRenderer.OWNING_ME));
	}
}
