package org.eclipse.ui.tests.api;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * This class tests the persistance of a perspective.
 */
public class SessionPerspective implements IPerspectiveFactory {

	public static String ID = "org.eclipse.ui.tests.api.SessionPerspective";
	
	/**
	 * @see IPerspectiveFactory#createInitialLayout(IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.addView(SessionView.VIEW_ID, IPageLayout.LEFT,
			0.33f, editorArea);
	}

}

