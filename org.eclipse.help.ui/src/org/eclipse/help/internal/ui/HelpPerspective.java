package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.search.ui.SearchUI;

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
		layout.setEditorAreaVisible(false);
		layout.addView(EmbeddedHelpView.ID, IPageLayout.TOP, 0.80f, editorArea);
// Uncomment when search becomes available		
//		IFolderLayout bottomFolder = layout.createFolder("bottom",IPageLayout.BOTTOM, 0.22f, editorArea);
//		bottomFolder.addView(SearchUI.SEARCH_RESULT_VIEW_ID);
//		bottomFolder.addPlaceholder(RelatedTopicsView.ID);
// Comment when search becomes available
		layout.addPlaceholder(RelatedTopicsView.ID, IPageLayout.BOTTOM, 0.22f, editorArea);
		// Actions
		layout.addShowViewShortcut(EmbeddedHelpView.ID);
		layout.addShowViewShortcut(RelatedTopicsView.ID);	
	}
}