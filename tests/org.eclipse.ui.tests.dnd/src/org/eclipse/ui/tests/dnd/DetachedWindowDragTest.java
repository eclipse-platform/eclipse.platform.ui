package org.eclipse.ui.tests.dnd;

import org.eclipse.ui.IViewPart;

public class DetachedWindowDragTest	extends DragTest {

	public DetachedWindowDragTest(TestDragSource dragSource,
			TestDropLocation dropTarget) {
		super(dragSource, dropTarget);
	}

	@Override
	public void doSetUp() throws Exception {
		super.doSetUp();

		page.showView(DragDropPerspectiveFactory.dropViewId2);
		page.showView(DragDropPerspectiveFactory.dropViewId1);
		page.showView(DragDropPerspectiveFactory.dropViewId3);

		IViewPart viewPart = page.showView(DragDropPerspectiveFactory.dropViewId1);
		DragOperations.drag(viewPart, new DetachedDropTarget(), true);

		viewPart = page.showView(DragDropPerspectiveFactory.dropViewId3);
		DragOperations.drag(viewPart, new DetachedDropTarget(), false);
	}
}