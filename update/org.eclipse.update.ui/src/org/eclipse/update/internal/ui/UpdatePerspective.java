package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.SWT;
import org.eclipse.ui.*;
import org.eclipse.jface.dialogs.MessageDialog;

public class UpdatePerspective implements IPerspectiveFactory {
	private static final String PREFIX = "org.eclipse.update.ui.";
	public static final String PERSPECTIVE_ID = PREFIX + "UpdatePerspective";
	public static final String ID_UPDATES = PREFIX + "UpdatesView";
	public static final String ID_CONFIGURATION = PREFIX + "ConfigurationView";
	public static final String ID_DETAILS = PREFIX + "DetailsView";
	public static final String ID_BROWSER = PREFIX + "WebBrowser";

	public UpdatePerspective() {
		super();
	}

	public void createInitialLayout(IPageLayout factory) {
		factory.setEditorAreaVisible(false);
		factory.addView(
			ID_CONFIGURATION,
			IPageLayout.LEFT,
			0.33f,
			factory.getEditorArea());
		factory.addView(ID_UPDATES, IPageLayout.BOTTOM, 0.5f, ID_CONFIGURATION);
		IFolderLayout folder = factory.createFolder("details", IPageLayout.RIGHT, 0.67f, factory.getEditorArea());
		folder.addView(ID_DETAILS);
		if (SWT.getPlatform().equals("win32"))
			folder.addPlaceholder(ID_BROWSER);
	}
}