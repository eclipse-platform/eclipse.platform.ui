package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;

/**
 */
public class ResourcePerspective implements IPerspectiveFactory {
/**
 * Constructs a new Default layout engine.
 */
public ResourcePerspective() {
	super();
}
/**
 * Defines the initial layout for a perspective.  
 *
 * Implementors of this method may add additional views to a
 * perspective.  The perspective already contains an editor folder
 * with <code>ID = ILayoutFactory.ID_EDITORS</code>.  Add additional views
 * to the perspective in reference to the editor folder.
 *
 * This method is only called when a new perspective is created.  If
 * an old perspective is restored from a persistence file then
 * this method is not called.
 *
 * @param factory the factory used to add views to the perspective
 */
public void createInitialLayout(IPageLayout layout) {
	defineActions(layout);
	defineLayout(layout);
}
/**
 * Defines the initial actions for a page.  
 */
public void defineActions(IPageLayout layout) {
	// Add "new wizards".
	layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
	layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$

	// Add "show views".
	layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
	layout.addShowViewShortcut(IPageLayout.ID_BOOKMARKS);
	layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
	layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
	layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
	
	layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
	
	layout.addShowInPart(IPageLayout.ID_RES_NAV);	
}
/**
 * Defines the initial layout for a page.  
 */
public void defineLayout(IPageLayout layout) {
	// Editors are placed for free.
	String editorArea = layout.getEditorArea();

	// Top left.
	IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, (float)0.26, editorArea);//$NON-NLS-1$
	topLeft.addView(IPageLayout.ID_RES_NAV);
	topLeft.addPlaceholder(IPageLayout.ID_BOOKMARKS);

	// Bottom left.
	IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, (float)0.50,//$NON-NLS-1$
		"topLeft");//$NON-NLS-1$
	bottomLeft.addView(IPageLayout.ID_OUTLINE);

	// Bottom right.
	layout.addView(IPageLayout.ID_TASK_LIST, IPageLayout.BOTTOM, (float)0.66, editorArea);
}
}
