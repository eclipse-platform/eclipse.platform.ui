package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.*;

/**
 * Perspective for holding the help view
 */
public class HelpPerspective implements IPerspectiveFactory {
	public static final String ID = "org.eclipse.help.internal.ui.HelpPerspective";

	/**
	 * Defines the initial layout for a perspective.
	 * This method is only called when a new perspective is created.  If
	 * an old perspective is restored from a persistence file then
	 * this method is not called.
	 *
	 * @param factory the factory used to add views to the perspective
	 */
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.addView(EmbeddedHelpView.ID, IPageLayout.TOP, 0.50f, editorArea);
		layout.setEditorAreaVisible(false);
		layout.addShowViewShortcut(EmbeddedHelpView.ID);
	}
	/**
	 * Returns the visible set of action bars for a perspective.
	 */
	public String[] getVisibleActionSets() {
		return new String[0];
	}
}
