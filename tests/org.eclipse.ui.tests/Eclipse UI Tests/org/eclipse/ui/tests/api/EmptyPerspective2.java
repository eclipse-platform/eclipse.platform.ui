package org.eclipse.ui.tests.api;

import org.eclipse.ui.*;

public class EmptyPerspective2 implements IPerspectiveFactory {
	
	public static final String PERSP_ID = "org.eclipse.ui.tests.api.EmptyPerspective2";
	
	public EmptyPerspective2() {
		super();
	}

	public void createInitialLayout(IPageLayout layout) {
	}
}