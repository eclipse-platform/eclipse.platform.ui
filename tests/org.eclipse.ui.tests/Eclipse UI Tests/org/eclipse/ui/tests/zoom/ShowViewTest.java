package org.eclipse.ui.tests.zoom;

import junit.framework.Assert;
import org.eclipse.ui.IViewPart;

public class ShowViewTest extends ZoomTestCase {
	public ShowViewTest(String name) {
		super(name);	
	}
	
	// a view is zoomed, a regular view is shown
	public void test1() {
		IViewPart view1 = showRegularView(view1Id);
		IViewPart view2 = showRegularView(view2Id);
		zoom(view1);
		Assert.assertTrue(isZoomed(view1));
		showRegularView(view2Id);
		Assert.assertTrue(noZoom());
	}
	// a view is zoomed, a fast view is shown
	public void test2() {
		IViewPart view1 = showRegularView(view1Id);
		zoom(view1);
		Assert.assertTrue(isZoomed(view1));
		showFastView(view2Id);
		Assert.assertTrue(isZoomed(view1));
	}
	// an editor is zoomed, a regular view is shown
	public void test3() {
		zoom(editor1);
		Assert.assertTrue(isZoomed(editor1));
		showRegularView(view1Id);
		Assert.assertTrue(noZoom());
	}
	// an editor is zoomed, a fast view is shown
	public void test4() {
		zoom(editor1);
		Assert.assertTrue(isZoomed(editor1));
		showFastView(view1Id);
		Assert.assertTrue(isZoomed(editor1));		
	}
}
