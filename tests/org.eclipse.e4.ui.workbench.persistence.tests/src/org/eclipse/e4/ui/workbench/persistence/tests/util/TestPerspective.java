package org.eclipse.e4.ui.workbench.persistence.tests.util;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class TestPerspective implements IPerspectiveFactory {

	public static final String ID = "org.eclipse.e4.ui.workbench.persistence.tests.TestPerspective";
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addView(TestViewPart.ID, IPageLayout.LEFT, 1, IPageLayout.ID_EDITOR_AREA);
	}

}
