package org.eclipse.update.ui.internal.manager;

import org.eclipse.update.ui.internal.parts.MultiPageEditor;

public class UpdateManager extends MultiPageEditor {
	public static final String MAIN_PAGE = "MainPage";
	
	public UpdateManager() {
	}

	public void createPages() {
		firstPageId = MAIN_PAGE;
		formWorkbook.setFirstPageSelected(false);
		MainPage mainPage =
			new MainPage(this, "Overview");
		addPage(MAIN_PAGE, mainPage);
	}
}