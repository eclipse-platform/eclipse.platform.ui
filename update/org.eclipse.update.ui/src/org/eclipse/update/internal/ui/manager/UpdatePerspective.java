package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.ui.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class UpdatePerspective implements IPerspectiveFactory {
	private static final String PREFIX = "org.eclipse.update.ui.";
	public static final String PERSPECTIVE_ID=PREFIX+"UpdatePerspective";
	public static final String ID_SITES = PREFIX+"SiteView";
	public static final String ID_LOCAL_SITE = PREFIX+"LocalSiteView";
	public static final String ID_HISTORY = PREFIX+"HistoryView";
	public static final String ID_DETAILS =PREFIX+"DetailsView";
	public static final String ID_CHECKLIST=PREFIX+"ChecklistView";

public UpdatePerspective() {
	super();
}

public void createInitialLayout(IPageLayout factory) {
	factory.setEditorAreaVisible(false);
	// Left folder.
	IFolderLayout left =
		factory.createFolder(
			"left",
			IPageLayout.LEFT,
			0.75f,
			factory.getEditorArea());
	left.addView(ID_LOCAL_SITE);
	left.addView(ID_SITES);
	left.addPlaceholder(ID_HISTORY);
	
	factory.addView(ID_DETAILS, IPageLayout.RIGHT, 0.33f, "left");
}
}
