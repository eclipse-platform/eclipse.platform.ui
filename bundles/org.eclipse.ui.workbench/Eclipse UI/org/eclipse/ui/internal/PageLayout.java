package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
  Dan Rubel <dan_rubel@instantiations.com> 
    - Fix for bug 11490 - define hidden view (placeholder for view) in plugin.xml 
**********************************************************************/
 
import java.util.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.*;

/**
 * This factory is used to define the initial layout of a part sash container.
 * <p>
 * Design notes: The design of <code>IPageLayout</code> is a reflection of 
 * three requirements:
 * <ol>
 *   <li>A mechanism is required to define the initial layout for a page. </li>
 *   <li>The views and editors within a page will be persisted between 
 *     sessions.</li>
 *   <li>The view and editor lifecycle for (1) and (2) should be identical.</li>
 * </ol>
 * </p>
 * <p>
 * In reflection of these requirements, the following strategy has been 
 * implemented for layout definition.
 * <ol>
 *   <li>A view extension is added to the workbench registry for the view. 
 *     This extension defines the extension id and extension class.  </li>
 *   <li>A view is added to a page by invoking one of the add methods
 *     in <code>IPageLayout</code>. The type of view is passed as an 
 *     extension id, rather than a handle. The page layout will map 
 *     the extension id to a view class, create an instance of the class, 
 *     and then add the view to the page.</li>
 * </ol>
 * </p>
 */
public class PageLayout implements IPageLayout {
	private static final String MISSING_REF_PART = "Referenced part does not exist yet: "; //$NON-NLS-1$
	
	private ViewFactory viewFactory;
	private LayoutPart editorFolder;
	private boolean editorVisible = true;
	private RootLayoutContainer rootLayoutContainer;
	private Map mapIDtoPart = new HashMap(10);
	private Map mapIDtoFolder = new HashMap(10);
	private Map mapFastViewToWidthRatio = new HashMap(10);
	private ArrayList actionSets = new ArrayList(3);
	private ArrayList newWizardActionIds = new ArrayList(3);
	private ArrayList showViewActionIds = new ArrayList(3);
	private ArrayList perspectiveActionIds = new ArrayList(3);
	private ArrayList fastViews = new ArrayList(3);
	private ArrayList showInPartIds = new ArrayList(3);

/**
 * Constructs a new PageLayout for the normal case of creating a new perspective.
 */
public PageLayout(RootLayoutContainer container, ViewFactory viewFactory, LayoutPart editorFolder) {
	super();
	this.viewFactory = viewFactory;
	this.rootLayoutContainer = container;
	this.editorFolder = editorFolder;
	prefill();
}

/**
 * Constructs a new PageLayout for other purposes.
 */
public PageLayout() {
}

/**
 * Adds the initial part to a layout.
 */
private void add(String newID) {
	try {
		// Create the part.
		LayoutPart newPart = createView(newID);
		setRefPart(newID, newPart);

		// Add it to the layout.
		rootLayoutContainer.add(newPart);
	} catch (PartInitException e) {
		WorkbenchPlugin.log(e.getMessage());
	}
}
/**
 * Adds an action set to the page.
 *
 * @param actionSetID Identifies the action set extension to use.  
 *   It must exist within the workbench registry.
 */
public void addActionSet(String actionSetID) {
	if (!actionSets.contains(actionSetID)) {
		actionSets.add(actionSetID);
	}
}
/**
 * @see IPageLayout
 */
public void addFastView(String id) {
	addFastView(id, INVALID_RATIO);
}
/**
 * @see IPageLayout
 */
public void addFastView(String id, float ratio) {
	if (checkPartInLayout(id))
		return;
	if (id != null) {
		try {
			IViewReference ref = viewFactory.createView(id);
			fastViews.add(ref);
			if(ratio >= IPageLayout.RATIO_MIN && ratio <= IPageLayout.RATIO_MAX)
				mapFastViewToWidthRatio.put(id, new Float(ratio));
		} catch(PartInitException e) {
			WorkbenchPlugin.log(e.getMessage());	
		}
	}
}
/**
 * Adds a creation wizard to the File New menu.
 * The id must name a new wizard extension contributed to the 
 * workbench's extension point (named <code>"org.eclipse.ui.newWizards"</code>).
 *
 * @param id the wizard id
 */
public void addNewWizardShortcut(String id) {	
	if (!newWizardActionIds.contains(id)) {
		newWizardActionIds.add(id);
	}
}
/**
 * Add the layout part to the page's layout
 */
private void addPart(LayoutPart newPart, String partId, int relationship, float ratio, String refId) {
	setRefPart(partId, newPart);

	// If the referenced part is inside a folder,
	// then use the folder as the reference part.
	LayoutPart refPart = getFolderPart(refId);
	if (refPart == null)
		refPart = getRefPart(refId);
			
	// Add it to the layout.
	if (refPart != null) {
		ratio = normalizeRatio(ratio);
		rootLayoutContainer.add(newPart, getPartSashConst(relationship), ratio, refPart);
	} else {
		WorkbenchPlugin.log(MISSING_REF_PART + refId);//$NON-NLS-1$
		rootLayoutContainer.add(newPart);
	}
}
/**
 * Adds a perspective shortcut to the Perspective menu.
 * The id must name a perspective extension contributed to the 
 * workbench's extension point (named <code>"org.eclipse.ui.perspectives"</code>).
 *
 * @param id the perspective id
 */
public void addPerspectiveShortcut(String id) {
	if (!perspectiveActionIds.contains(id)) {
		perspectiveActionIds.add(id);
	}
}
/**
 * @see IPageLayout
 */
public void addPlaceholder(String viewId, int relationship, float ratio, String refId) {
	if (checkPartInLayout(viewId))
		return;
			
	// Create the placeholder.
	PartPlaceholder newPart = new PartPlaceholder(viewId);
	addPart(newPart, viewId, relationship, ratio, refId);
}
/**
 * @see IPageLayout#addShowInPart
 */
public void addShowInPart(String id) {	
	if (!showInPartIds.contains(id)) {
		showInPartIds.add(id);
	}
}
/**
 * Adds a view to the Show View menu.
 * The id must name a view extension contributed to the 
 * workbench's extension point (named <code>"org.eclipse.ui.views"</code>).
 *
 * @param id the view id
 */
public void addShowViewShortcut(String id) {	
	if (!showViewActionIds.contains(id)) {
		showViewActionIds.add(id);
	}
}
/**
 * @see IPageLayout
 */
public void addView(String viewId, int relationship, float ratio, String refId) {
	if (checkPartInLayout(viewId))
		return;
	
	try {
		// Create the part.
		LayoutPart newPart = createView(viewId);
		addPart(newPart, viewId, relationship, ratio, refId);
	} catch (PartInitException e) {
		WorkbenchPlugin.log(e.getMessage());
	}
}
/**
 * Verify that the part is already present in the layout
 * and cannot be added again. Log a warning message.
 */
/*package*/ boolean checkPartInLayout(String partId) {
	if (getRefPart(partId) != null) {
		WorkbenchPlugin.log("Part already exists in page layout: " + partId);//$NON-NLS-1$
		return true;
	}
	for(int i = 0; i<fastViews.size(); i++) {
		if(((IViewReference)fastViews.get(i)).getId().equals(partId))
			return true;
	}
	
	return false;
}

/**
 * @see IPageLayout
 */
public IFolderLayout createFolder(String folderId, int relationship, float ratio, String refId) {
	if (checkPartInLayout(folderId))
		return new FolderLayout(this, (PartTabFolder) getRefPart(folderId), viewFactory);

	// Create the folder.
	PartTabFolder folder = new PartTabFolder();
	folder.setID(folderId);
	addPart(folder, folderId, relationship, ratio, refId);
	
	// Create a wrapper.
	return new FolderLayout(this, folder, viewFactory);
}
/**
 * @see IPageLayout
 */
public IPlaceholderFolderLayout createPlaceholderFolder(String folderId, int relationship, float ratio, String refId) {
	if (checkPartInLayout(folderId))
		return new PlaceholderFolderLayout(this, (ContainerPlaceholder) getRefPart(folderId));

	// Create the folder.
	ContainerPlaceholder folder = new ContainerPlaceholder(null);
	folder.setContainer(rootLayoutContainer);
	folder.setRealContainer(new PartTabFolder());
	folder.setID(folderId);
	addPart(folder, folderId, relationship, ratio, refId);
	
	// Create a wrapper.
	return new PlaceholderFolderLayout(this, folder);
}

/**
 * Create the given view.
 */
private LayoutPart createView(String partID)
	throws PartInitException
{
	if (partID.equals(ID_EDITOR_AREA)) {
		return editorFolder;
	} else {
		WorkbenchPartReference ref = (WorkbenchPartReference)viewFactory.createView(partID);
		ViewPane newPart = (ViewPane)ref.getPane();
		if(newPart == null) {
			newPart = new ViewPane((IViewReference)ref,(WorkbenchPage)ref.getPage());
			ref.setPane(newPart);
		}
		return newPart;
	}
}
/**
 * Returns the action set list for the page.
 * This is List of Strings.
 */
public ArrayList getActionSets() {
	return actionSets;
}
/**
 * Returns an identifier for the editor area.  The editor area is automatically added to each
 * layout before any other part.  It should be used as a reference part for other views.
 */
public String getEditorArea() {
	return ID_EDITOR_AREA;
}
/**
 * Returns the fast views.
 */
public ArrayList getFastViews() {
	return fastViews;
}
/**
 * Returns a map of fast view ids to width ratios.
 */
/*package*/ Map getFastViewToWidthRatioMap() {
	return mapFastViewToWidthRatio;
}
/**
 * Returns the new wizard actions the page.
 * This is List of Strings.
 */
public ArrayList getNewWizardActionIds() {
	return newWizardActionIds;
}
/**
 * Answer the part sash container const for a layout value.
 */
private int getPartSashConst(int nRelationship) {
	return nRelationship;
}
/**
 * Returns the perspective actions.
 * This is List of Strings.
 */
public ArrayList getPerspectiveActionIds() {
	return perspectiveActionIds;
}
/**
 * Answer the part for a given ID.
 */
/*package*/ LayoutPart getRefPart(String partID) {
	return (LayoutPart)mapIDtoPart.get(partID);
}
/**
 * Answer the folder part containing the given view ID
 * or <code>null</code> if none (i.e. part of the
 * page layout instead of a folder layout).
 */
private PartTabFolder getFolderPart(String viewId) {
	return (PartTabFolder)mapIDtoFolder.get(viewId);
}
/**
 * Answer the top level layout container
 */
public RootLayoutContainer getRootLayoutContainer() {
	return rootLayoutContainer;
}
/**
 * Returns the ids of the parts to list in the Show In... prompter.
 * This is a List of Strings.
 */
public ArrayList getShowInPartIds() {
	return showInPartIds;
}
/**
 * Returns the ids of the views to list in the Show View shortcuts.
 * This is a List of Strings.
 */
public ArrayList getShowViewActionIds() {
	return showViewActionIds;
}
/**
 * Answer the label for a view.
 */
private String getViewLabel(String partID) {
	IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
	IViewDescriptor desc = reg.find(partID);
	if (desc != null)
		return desc.getLabel();
	else {
		// cannot safely open the dialog so log the problem
		WorkbenchPlugin.log("Unable to find view label: " + partID);//$NON-NLS-1$
		return partID;
	}
}
/**
 * See IPageLayout.
 */
public boolean isEditorAreaVisible() {
	return editorVisible;
}
/**
 * Trim the ratio so that direct manipulation of parts is easy.
 */
private float normalizeRatio(float in) {
	if (in < RATIO_MIN)
		in = RATIO_MIN;
	if (in > RATIO_MAX)
		in = RATIO_MAX;
	return in;
}
/**
 * Prefill the layout with required parts.
 */
private void prefill() {
	// Editors are king.
	add(ID_EDITOR_AREA);

	// Add default action sets.
	ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
	IActionSetDescriptor [] array = reg.getActionSets();
	int count = array.length;
	for (int nX = 0; nX < count; nX ++) {
		IActionSetDescriptor desc = array[nX];
		if (desc.isInitiallyVisible())
			addActionSet(desc.getId());
	}
}
/**
 * See IPageLayout.
 */
public void setEditorAreaVisible(boolean showEditorArea) {
	editorVisible = showEditorArea;
}
/**
 * Map an ID to a part.
 */
/*package*/ void setRefPart(String partID, LayoutPart part) {
	mapIDtoPart.put(partID, part);
}
/**
 * Map the folder part containing the given view ID.
 */
/*package*/ void setFolderPart(String viewId, PartTabFolder folder) {
	mapIDtoFolder.put(viewId, folder);
}
/**
 * Map the folder part containing the given view ID.
 */
/*package*/ void setFolderPart(String viewId, ContainerPlaceholder container) {
	LayoutPart tabFolder = container.getRealContainer();
	mapIDtoFolder.put(viewId, tabFolder);
}
// stackPart(Layoutpart, String, String) added by dan_rubel@instantiations.com
/**
 * Stack a part on top of another.
 */
private void stackPart(LayoutPart newPart, String viewId, String refId) {
	setRefPart(viewId, newPart);

	// If ref part is in a folder than just add the
	// new view to that folder.
	PartTabFolder folder = getFolderPart(refId);
	if (folder != null) {
		folder.add(newPart);
		setFolderPart(viewId, folder);
		return;
	}
	
	// If the ref part is in the page layout then create
	// a new folder and add the new view.
	LayoutPart refPart = getRefPart(refId);
	if (refPart != null) {
		PartTabFolder newFolder = new PartTabFolder();
		rootLayoutContainer.replace(refPart, newFolder);
		newFolder.add(refPart);
		newFolder.add(newPart);
		setFolderPart(refId, newFolder);
		setFolderPart(viewId, newFolder);
		return;
	}

	// If ref part is not found then just do add.
	WorkbenchPlugin.log(MISSING_REF_PART + refId);//$NON-NLS-1$
	rootLayoutContainer.add(newPart);
}
// stackPlaceholder(String, String) added by dan_rubel@instantiations.com
/**
 * Stack a placeholder on top of another.
 */
public void stackPlaceholder(String viewId, String refId) {
	if (checkPartInLayout(viewId))
		return;
			
	// Create the placeholder.
	PartPlaceholder newPart = new PartPlaceholder(viewId);
	stackPart(newPart, viewId, refId);
}
// stackView(String, String) modified by dan_rubel@instantiations.com
/**
 * Stack one view on top of another.
 */
public void stackView(String viewId, String refId) {
	if (checkPartInLayout(viewId))
		return;
	
	// Create the new part.
	try {
		LayoutPart newPart = createView(viewId);
		stackPart(newPart, viewId, refId);
	} catch (PartInitException e) {
		WorkbenchPlugin.log(e.getMessage());
	}
}
/**
 * @see IPageLayout
 */
public int getEditorReuseThreshold() {
	return -1;
}
/**
 * @see IPageLayout
 */
public void setEditorReuseThreshold(int openEditors) {
}
}
