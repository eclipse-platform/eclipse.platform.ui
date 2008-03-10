package org.eclipse.ui.examples.contributions.rcp;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.examples.contributions.view.InfoView;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		
		layout.addStandaloneView(InfoView.ID,  true, IPageLayout.LEFT, 0.25f, editorArea);
		
		layout.getViewLayout(InfoView.ID).setCloseable(false);
	}
}
