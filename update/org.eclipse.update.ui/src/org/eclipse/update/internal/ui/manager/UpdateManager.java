package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.MultiPageEditor;

public class UpdateManager extends MultiPageEditor {
	public static final String MAIN_PAGE = "MainPage";
	public static final String UPDATE_PAGE = "UpdatePage";
	public static final String INSTALL_PAGE = "InstallPage";
	public static final String REMOVE_PAGE = "RemovePage";
	public static final String HISTORY_PAGE = "HistoryPage";
		
	public UpdateManager() {
	}

	public void createPages() {
		firstPageId = MAIN_PAGE;
		formWorkbook.setFirstPageSelected(false);
		MainPage mainPage =
			new MainPage(this, "Overview");
		addPage(MAIN_PAGE, mainPage);
		UpdatesPage updatesPage =
			new UpdatesPage(this, "Update");
		addPage(UPDATE_PAGE, updatesPage);
		InstallPage installPage =
			new InstallPage(this, "Install");
		addPage(INSTALL_PAGE, installPage);
		RemovePage removePage =
			new RemovePage(this, "Uninstall");
		addPage(REMOVE_PAGE, removePage);
		HistoryPage historyPage =
			new HistoryPage(this, "History");
		addPage(HISTORY_PAGE, historyPage);
	}
}