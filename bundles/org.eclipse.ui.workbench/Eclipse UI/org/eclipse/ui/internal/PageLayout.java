/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dan Rubel <dan_rubel@instantiations.com>
 *     - Fix for bug 11490 - define hidden view (placeholder for view) in plugin.xml
 *     Ted Stockwell <emorning@yahoo.com>
 *     - Fix for bug 63595 - IPageLayout.addFastView regression (3.0M8 to 3.0M9)
 *     Chris Gross <schtoo@schtoo.com> 
 *     - Fix for 99155 - allow standalone view placeholders
 *     Chris Gross chris.gross@us.ibm.com Bug 107443
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.IViewLayout;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.presentations.PresentationFactoryUtil;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

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
    private ArrayList actionSets = new ArrayList(3);

    private IPerspectiveDescriptor descriptor;

    private LayoutPart editorFolder;

    private boolean editorVisible = true;

    private boolean fixed;

    private ArrayList fastViews = new ArrayList(3);

    private Map mapIDtoFolder = new HashMap(10);

    private Map mapIDtoPart = new HashMap(10);

    private Map mapIDtoViewLayoutRec = new HashMap(10);
    
    private Map mapFolderToFolderLayout = new HashMap(10);

    private ArrayList newWizardShortcuts = new ArrayList(3);

    private ArrayList perspectiveShortcuts = new ArrayList(3);

    private ViewSashContainer rootLayoutContainer;

    private ArrayList showInPartIds = new ArrayList(3);

    private ArrayList showViewShortcuts = new ArrayList(3);

    private ViewFactory viewFactory;

	private List minimizedStacks = new ArrayList();

    /**
     * Constructs a new PageLayout for other purposes.
     */
    public PageLayout() {
        //no-op
    }

    /**
     * Constructs a new PageLayout for the normal case of creating a new
     * perspective.
     */
    public PageLayout(ViewSashContainer container, ViewFactory viewFactory,
            LayoutPart editorFolder, IPerspectiveDescriptor descriptor) {
        super();
        this.viewFactory = viewFactory;
        this.rootLayoutContainer = container;
        this.editorFolder = editorFolder;
        this.descriptor = descriptor;
        prefill();
    }

    /**
     * Adds the editor to a layout.
     */
    private void addEditorArea() {
        try {
            // Create the part.
            LayoutPart newPart = createView(ID_EDITOR_AREA);
            if (newPart == null) {
				// this should never happen as long as newID is the editor ID.
                return;
			}

            setRefPart(ID_EDITOR_AREA, newPart);

            // Add it to the layout.
            rootLayoutContainer.add(newPart);
        } catch (PartInitException e) {
            WorkbenchPlugin.log(getClass(), "addEditorArea()", e); //$NON-NLS-1$
        }
    }

    /**
     * Adds an action set to the page.
     * 
     * @param actionSetID Identifies the action set extension to use. It must
     *            exist within the workbench registry.
     */
    public void addActionSet(String actionSetID) {
        if (!actionSets.contains(actionSetID)) {
            actionSets.add(actionSetID);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#addFastView(java.lang.String)
     */
    public void addFastView(String id) {
        addFastView(id, INVALID_RATIO);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#addFastView(java.lang.String, float)
     */
    public void addFastView(String id, float ratio) {
        if (checkPartInLayout(id)) {
			return;
		}
        if (id != null) {
            try {
				IViewDescriptor viewDescriptor = viewFactory.getViewRegistry()
						.find(ViewFactory.extractPrimaryId(id));
				if (!WorkbenchActivityHelper.filterItem(viewDescriptor)) {
					IViewReference ref = viewFactory.createView(ViewFactory
							.extractPrimaryId(id), ViewFactory
							.extractSecondaryId(id));
					fastViews.add(ref);

					// force creation of the view layout rec
					ViewLayoutRec rec = getViewLayoutRec(id, true);

					// remember the ratio, if valid
					if (ratio >= IPageLayout.RATIO_MIN
							&& ratio <= IPageLayout.RATIO_MAX) {
						rec.fastViewWidthRatio = ratio;
					}
				}
			} catch (PartInitException e) {
				WorkbenchPlugin.log(getClass(), "addFastView", e); //$NON-NLS-1$
			}
        }
    }

    /**
	 * Check to see if the partId represents a fast view's id.
	 * 
	 * @param partId
	 *            The part's id.
	 * @return true if the partId is a fast view id.
	 */
    private boolean isFastViewId(String partId) {
        for (int i = 0; i < fastViews.size(); i++) {
			IViewReference ref = (IViewReference) fastViews.get(i);
			String secondaryId = ref.getSecondaryId();
			String refId = (secondaryId == null ? ref.getId() : ref.getId()
					+ ":" + secondaryId); //$NON-NLS-1$
			if (refId.equals(partId)) {
				return true;
			}
        }
        return false;
    }

    /**
	 * Returns the view layout record for the given view id, or null if not
	 * found. If create is true, the record is created if it doesn't already
	 * exist.
	 * 
	 * @since 3.0
	 */
    ViewLayoutRec getViewLayoutRec(String id, boolean create) {
        ViewLayoutRec rec = (ViewLayoutRec) mapIDtoViewLayoutRec.get(id);
        if (rec == null && create) {
            rec = new ViewLayoutRec();
            // set up the view layout appropriately if the page layout is fixed
            if (isFixed()) {
                rec.isCloseable = false;
                rec.isMoveable = false;
            }
            mapIDtoViewLayoutRec.put(id, rec);
        }
        return rec;
    }

    /**
     * Adds a creation wizard to the File New menu.
     * The id must name a new wizard extension contributed to the 
     * workbench's extension point (named <code>"org.eclipse.ui.newWizards"</code>).
     *
     * @param id the wizard id
     */
    public void addNewWizardShortcut(String id) {
        if (!newWizardShortcuts.contains(id)) {
            newWizardShortcuts.add(id);
        }
    }

    /**
     * Add the layout part to the page's layout
     */
    private void addPart(LayoutPart newPart, String partId, int relationship,
            float ratio, String refId) {

        setRefPart(partId, newPart);

        // If the referenced part is inside a folder,
        // then use the folder as the reference part.
        LayoutPart refPart = getFolderPart(refId);
        if (refPart == null) {
			refPart = getRefPart(refId);
		}

        // Add it to the layout.
        if (refPart != null) {
            ratio = normalizeRatio(ratio);
            rootLayoutContainer.add(newPart, getPartSashConst(relationship),
                    ratio, refPart);
        } else {
            WorkbenchPlugin.log(NLS.bind(WorkbenchMessages.PageLayout_missingRefPart,  refId )); 
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
        if (!perspectiveShortcuts.contains(id)) {
            perspectiveShortcuts.add(id);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#addPlaceholder(java.lang.String, int, float, java.lang.String)
     */
    public void addPlaceholder(String viewId, int relationship, float ratio,
            String refId) {
        if (!checkValidPlaceholderId(viewId)) {
            return;
        }

        // Create the placeholder.
        PartPlaceholder newPart = new PartPlaceholder(viewId);
        addPart(newPart, viewId, relationship, ratio, refId);
        // force creation of the view layout rec
        getViewLayoutRec(viewId, true);
    }

    /**
     * Checks whether the given id is a valid placeholder id.
     * A placeholder id may be simple or compound, and can optionally contain a wildcard.
     * 
     * @param id the placeholder id
     * @return <code>true</code> if the given id is a valid placeholder id, <code>false</code> otherwise
     */
    boolean checkValidPlaceholderId(String id) {
        // Check that view is not already in layout.
        // This check is done even if the id has a wildcard, since it's incorrect to create
        // multiple placeholders with the same id, wildcard or not.
        if (checkPartInLayout(id)) {
            return false;
        }

        // check that primary view id is valid, but only if it has no wildcard
        String primaryId = ViewFactory.extractPrimaryId(id);
        if (!ViewFactory.hasWildcard(primaryId)) {
	        IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
	        IViewDescriptor desc = reg.find(primaryId);
	        if (desc == null) {
	            // cannot safely open the dialog so log the problem
	            WorkbenchPlugin.log("Unable to find view with id: " + primaryId + ", when creating perspective " + getDescriptor().getId()); //$NON-NLS-1$ //$NON-NLS-2$
	            return false;
	        }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#addShowInPart(java.lang.String)
     */
    public void addShowInPart(String id) {
        if (!showInPartIds.contains(id)) {
            showInPartIds.add(id);
        }
    }

    /**
     * Adds a view to the Show View menu. The id must name a view extension
     * contributed to the workbench's extension point (named <code>"org.eclipse.ui.views"</code>).
     * 
     * @param id the view id
     */
    public void addShowViewShortcut(String id) {
        if (!showViewShortcuts.contains(id)) {
            showViewShortcuts.add(id);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#addView(java.lang.String, int, float, java.lang.String)
     */
    public void addView(String viewId, int relationship, float ratio,
            String refId) {
        addView(viewId, relationship, ratio, refId, false, false, true);
    }

    /**
     * Convenience method to allow setting the initial minimized
     * state if a new stack is created. Used by the 'perspectiveExtension'
     * reader.
     * 
     *  @since 3.3
     */
    public void addView(String viewId, int relationship, float ratio,
            String refId, boolean minimized) {
        addView(viewId, relationship, ratio, refId, minimized, false, true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#addView(java.lang.String, int, float, java.lang.String)
     */
    private void addView(String viewId, int relationship, float ratio,
            String refId, boolean minimized, boolean standalone, boolean showTitle) {
        if (checkPartInLayout(viewId)) {
			return;
		}

        try {
            // Create the part.
            LayoutPart newPart = createView(viewId);
            if (newPart == null) {
                addPlaceholder(viewId, relationship, ratio, refId);
                LayoutHelper.addViewActivator(this, viewId);
            } else {
                int appearance = PresentationFactoryUtil.ROLE_VIEW;
                if (standalone) {
                    if (showTitle) {
                        appearance = PresentationFactoryUtil.ROLE_STANDALONE;
                    } else {
                        appearance = PresentationFactoryUtil.ROLE_STANDALONE_NOTITLE;
                    }
                }

                ViewStack newFolder = new ViewStack(rootLayoutContainer.page,
                        true, appearance, null);
                newFolder.add(newPart);
                setFolderPart(viewId, newFolder);
                addPart(newFolder, viewId, relationship, ratio, refId);
                // force creation of the view layout rec
                getViewLayoutRec(viewId, true);
                
                // Capture any minimized stacks
                if (minimized) {
                	// Remember the minimized stacks so we can
                	// move them to the trim when the Perspective
                	// activates...
                	minimizedStacks.add(newFolder);
                }
            }
        } catch (PartInitException e) {
            WorkbenchPlugin.log(getClass(), "addView", e); //$NON-NLS-1$
        }
    }

    public List getMinimizedStacks() {
    	return minimizedStacks;
    }
    
    /**
     * Verify that the part is already present in the layout
     * and cannot be added again. Log a warning message.
     */
    /*package*/
    boolean checkPartInLayout(String partId) {
        if (getRefPart(partId) != null || isFastViewId(partId)) {
            WorkbenchPlugin.log(NLS.bind(WorkbenchMessages.PageLayout_duplicateRefPart,partId )); 
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#createFolder(java.lang.String, int, float, java.lang.String)
     */
    public IFolderLayout createFolder(String folderId, int relationship,
            float ratio, String refId) {
        if (checkPartInLayout(folderId)) {
            ViewStack folder = (ViewStack) getRefPart(folderId);
            
            return (IFolderLayout) mapFolderToFolderLayout.get(folder);
		}

        // Create the folder.
        ViewStack folder = new ViewStack(rootLayoutContainer.page);
        folder.setID(folderId);
        addPart(folder, folderId, relationship, ratio, refId);

        // Create a wrapper.
        FolderLayout layout = new FolderLayout(this, folder, viewFactory);
        
        mapFolderToFolderLayout.put(folder,layout);
        
        return layout;        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#createPlaceholderFolder(java.lang.String, int, float, java.lang.String)
     */
    public IPlaceholderFolderLayout createPlaceholderFolder(String folderId,
            int relationship, float ratio, String refId) {
        if (checkPartInLayout(folderId)) {
            ContainerPlaceholder folder = (ContainerPlaceholder) getRefPart(folderId);
            
            return (IPlaceholderFolderLayout) mapFolderToFolderLayout.get(folder);
		}

        // Create the folder.
        ContainerPlaceholder folder = new ContainerPlaceholder(null);
        folder.setContainer(rootLayoutContainer);
        folder.setRealContainer(new ViewStack(rootLayoutContainer.page));
        folder.setID(folderId);
        addPart(folder, folderId, relationship, ratio, refId);

        // Create a wrapper.
        IPlaceholderFolderLayout layout = new PlaceholderFolderLayout(this, folder);
        
        mapFolderToFolderLayout.put(folder,layout);
        
        return layout;
    }

    /**
     * Create a new <code>LayoutPart</code>.
     * 
     * @param partID the id of the part to create.
     * @return the <code>LayoutPart</code>, or <code>null</code> if it should not be
     * created because of activity filtering.
     * @throws PartInitException thrown if there is a problem creating the part.
     */
    private LayoutPart createView(String partID) throws PartInitException {
        if (partID.equals(ID_EDITOR_AREA)) {
            return editorFolder;
        }
		IViewDescriptor viewDescriptor = viewFactory.getViewRegistry()
		        .find(ViewFactory.extractPrimaryId(partID));
		if (WorkbenchActivityHelper.filterItem(viewDescriptor)) {
			return null;
		}
		return LayoutHelper.createView(getViewFactory(), partID);
    }

    /**
     * @return the action set list for the page. This is <code>List</code> of 
     * <code>String</code>s.
     */
    public ArrayList getActionSets() {
        return actionSets;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#getDescriptor()
     */
    public IPerspectiveDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * @return an identifier for the editor area. The editor area is
     * automatically added to each layout before any other part. It should be
     * used as a reference part for other views.
     */
    public String getEditorArea() {
        return ID_EDITOR_AREA;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#getEditorReuseThreshold()
     */
    public int getEditorReuseThreshold() {
        return -1;
    }

    /**
     * @return <code>ArrayList</code>
     */
    public ArrayList getFastViews() {
        return fastViews;
    }

    /**
     * @return the folder part containing the given view ID or <code>null</code>
     * if none (i.e. part of the page layout instead of a folder layout).
     */
    private ViewStack getFolderPart(String viewId) {
        return (ViewStack) mapIDtoFolder.get(viewId);
    }

    /**
     * @return the new wizard shortcuts associated with the page. This is a <code>List</code> of 
     * <code>String</code>s.
     */
    public ArrayList getNewWizardShortcuts() {
        return newWizardShortcuts;
    }

    /**
     * @return the part sash container const for a layout value.
     */
    private int getPartSashConst(int nRelationship) {
        return nRelationship;
    }

    /**
     * @return the perspective shortcuts associated with the page. This is a <code>List</code> of 
     * <code>String</code>s.
     */
    public ArrayList getPerspectiveShortcuts() {
        return perspectiveShortcuts;
    }

    /**
     * @return the part for a given ID.
     */
    /*package*/
    LayoutPart getRefPart(String partID) {
        return (LayoutPart) mapIDtoPart.get(partID);
    }

    /**
     * @return the top level layout container.
     */
    public ViewSashContainer getRootLayoutContainer() {
        return rootLayoutContainer;
    }

    /**
     * @return the ids of the parts to list in the Show In... prompter. This is
     * a <code>List</code> of <code>String</code>s.
     */
    public ArrayList getShowInPartIds() {
        return showInPartIds;
    }

    /**
     * @return the show view shortcuts associated with the page. This is a <code>List</code> of 
     * <code>String</code>s.
     */
    public ArrayList getShowViewShortcuts() {
        return showViewShortcuts;
    }

    /**
     * @return the <code>ViewFactory</code> for this <code>PageLayout</code>.
     * @since 3.0
     */
    /* package */
    ViewFactory getViewFactory() {
        return viewFactory;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#isEditorAreaVisible()
     */
    public boolean isEditorAreaVisible() {
        return editorVisible;
    }

    /**
     * Trim the ratio so that direct manipulation of parts is easy.
     * 
     * @param in the initial ratio.
     * @return the normalized ratio.
     */
    private float normalizeRatio(float in) {
        if (in < RATIO_MIN) {
			in = RATIO_MIN;
		}
        if (in > RATIO_MAX) {
			in = RATIO_MAX;
		}
        return in;
    }

    /**
     * Prefill the layout with required parts.
     */
    private void prefill() {
        addEditorArea();

        // Add default action sets.
        ActionSetRegistry reg = WorkbenchPlugin.getDefault()
                .getActionSetRegistry();
        IActionSetDescriptor[] array = reg.getActionSets();
        int count = array.length;
        for (int nX = 0; nX < count; nX++) {
            IActionSetDescriptor desc = array[nX];
            if (desc.isInitiallyVisible()) {
				addActionSet(desc.getId());
			}
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#setEditorAreaVisible(boolean)
     */
    public void setEditorAreaVisible(boolean showEditorArea) {
        editorVisible = showEditorArea;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#setEditorReuseThreshold(int)
     */
    public void setEditorReuseThreshold(int openEditors) {
        //no-op
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#setFixed(boolean)
     */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#getFixed()
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * Map the folder part containing the given view ID.
     * 
     * @param viewId the part ID.
     * @param container the <code>ContainerPlaceholder</code>.
     */
    /*package*/
    void setFolderPart(String viewId, ContainerPlaceholder container) {
        LayoutPart tabFolder = container.getRealContainer();
        mapIDtoFolder.put(viewId, tabFolder);
    }

    /**
     * Map the folder part containing the given view ID.
     * 
     * @param viewId the part ID.
     * @param folder the <code>ViewStack</code>.
     */
    /*package*/
    void setFolderPart(String viewId, ViewStack folder) {
        mapIDtoFolder.put(viewId, folder);
    }

    /**
     * Map an ID to a part.
     * 
     * @param partId the part ID.
     * @param part the <code>LayoutPart</code>.
     */
    /*package*/
    void setRefPart(String partID, LayoutPart part) {
        mapIDtoPart.put(partID, part);
    }

    // stackPart(Layoutpart, String, String) added by dan_rubel@instantiations.com
    /**
     * Stack a part on top of another.
     * 
     * @param newPart the new part.
     * @param viewId the view ID.
     * @param refId the reference ID.
     */
    private void stackPart(LayoutPart newPart, String viewId, String refId) {
        setRefPart(viewId, newPart);
        // force creation of the view layout rec
        getViewLayoutRec(viewId, true);

        // If ref part is in a folder than just add the
        // new view to that folder.
        ViewStack folder = getFolderPart(refId);
        if (folder != null) {
            folder.add(newPart);
            setFolderPart(viewId, folder);
            return;
        }

        // If the ref part is in the page layout then create
        // a new folder and add the new view.
        LayoutPart refPart = getRefPart(refId);
        if (refPart != null && (refPart instanceof PartPane || refPart instanceof PartPlaceholder)) {
            ViewStack newFolder = new ViewStack(rootLayoutContainer.page);
            rootLayoutContainer.replace(refPart, newFolder);
            newFolder.add(refPart);
            newFolder.add(newPart);
            setFolderPart(refId, newFolder);
            setFolderPart(viewId, newFolder);
            return;
        }

        // If ref part is not found then just do add.
        WorkbenchPlugin.log(NLS.bind(WorkbenchMessages.PageLayout_missingRefPart, refId )); 
        rootLayoutContainer.add(newPart);
    }

    // stackPlaceholder(String, String) added by dan_rubel@instantiations.com
    /**
     * Stack a placeholder on top of another.
     * 
     * @param viewId the view ID.
     * @param refId the reference ID.
     */
    public void stackPlaceholder(String viewId, String refId) {
        if (checkPartInLayout(viewId)) {
			return;
		}

        // Create the placeholder.
        PartPlaceholder newPart = new PartPlaceholder(viewId);

        LayoutPart refPart = getRefPart(refId);
        if (refPart != null) {
            newPart.setContainer(refPart.getContainer());
        }

        stackPart(newPart, viewId, refId);
    }

    // stackView(String, String) modified by dan_rubel@instantiations.com
    /**
     * Stack one view on top of another.
     * 
     * @param viewId the view ID.
     * @param refId the reference ID.
     */
    public void stackView(String viewId, String refId) {
        if (checkPartInLayout(viewId)) {
			return;
		}

        // Create the new part.
        try {
            LayoutPart newPart = createView(viewId);
            if (newPart == null) {
                stackPlaceholder(viewId, refId);
                LayoutHelper.addViewActivator(this, viewId);
            } else {
				stackPart(newPart, viewId, refId);
			}
        } catch (PartInitException e) {
            WorkbenchPlugin.log(getClass(), "stackView", e); //$NON-NLS-1$
        }
    }

    /**
     * Converts SWT position constants into layout position constants.
     * 
     * @param swtConstant one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, or SWT.RIGHT
     * @return one of IPageLayout.TOP, IPageLayout.BOTTOM, IPageLayout.LEFT, IPageLayout.RIGHT, or -1 indicating an
     * invalid input
     * 
     * @since 3.0
     */
    public static int swtConstantToLayoutPosition(int swtConstant) {
        switch (swtConstant) {
        case SWT.TOP:
            return IPageLayout.TOP;
        case SWT.BOTTOM:
            return IPageLayout.BOTTOM;
        case SWT.RIGHT:
            return IPageLayout.RIGHT;
        case SWT.LEFT:
            return IPageLayout.LEFT;
        }

        return -1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#addStandaloneView(java.lang.String, boolean, int, float, java.lang.String)
     * @since 3.0
     */
    public void addStandaloneView(String viewId, boolean showTitle,
            int relationship, float ratio, String refId) {
        addView(viewId, relationship, ratio, refId, false, true, showTitle);
        ViewLayoutRec rec = getViewLayoutRec(viewId, true);
        rec.isStandalone = true;
        rec.showTitle = showTitle;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#addStandaloneViewPlaceholder(java.lang.String, int, float, java.lang.String, boolean)
     */
    public void addStandaloneViewPlaceholder(String viewId, int relationship,
			float ratio, String refId, boolean showTitle) {

		String stackId = viewId + ".standalonefolder"; //$NON-NLS-1$

		// Check to see if the view is already in the layout
		if (!checkValidPlaceholderId(viewId)) {
			return;
		}

		// Create the folder.
		ContainerPlaceholder folder = new ContainerPlaceholder(null);
		folder.setContainer(rootLayoutContainer);
		int appearance = PresentationFactoryUtil.ROLE_STANDALONE;
		if (!showTitle) {
			appearance = PresentationFactoryUtil.ROLE_STANDALONE_NOTITLE;
		}
		folder.setRealContainer(new ViewStack(rootLayoutContainer.page, true,
				appearance, null));
		folder.setID(stackId);
		addPart(folder, stackId, relationship, ratio, refId);

		// Create a wrapper.
		PlaceholderFolderLayout placeHolder = new PlaceholderFolderLayout(this,
				folder);

		// Add the standalone view immediately
		placeHolder.addPlaceholder(viewId);

		ViewLayoutRec rec = getViewLayoutRec(viewId, true);
		rec.isStandalone = true;
		rec.showTitle = showTitle;
	}


    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPageLayout#getViewLayout(java.lang.String)
	 * @since 3.0
	 */
    public IViewLayout getViewLayout(String viewId) {
        ViewLayoutRec rec = getViewLayoutRec(viewId, true);
        if (rec == null) {
            return null;
        }
        return new ViewLayout(this, rec);
    }

    /**
     * @since 3.0
     */
    public Map getIDtoViewLayoutRecMap() {
        return mapIDtoViewLayoutRec;
    }

    /**
     * Removes any existing placeholder with the given id.
     * 
     * @param id the id for the placeholder
     * @since 3.1
     */
	public void removePlaceholder(String id) {
		LayoutPart part = getRefPart(id);
		if (part instanceof PartPlaceholder) {
			ViewStack stack = getFolderPart(id);
			if (stack != null) {
				stack.remove(part);
			}
			else {
				rootLayoutContainer.remove(part);
			}
			mapIDtoPart.remove(id);
			mapIDtoFolder.remove(id);
			mapIDtoViewLayoutRec.remove(id);
		}
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageLayout#getFolderForView(java.lang.String)
     */
    public IPlaceholderFolderLayout getFolderForView(String viewId)
    {
        if (!mapIDtoFolder.containsKey(viewId))
            return null;
                
        ViewStack folder = (ViewStack) mapIDtoFolder.get(viewId);
        IPlaceholderFolderLayout layout = null;
        if (!mapFolderToFolderLayout.containsKey(folder))
        {
            layout = new FolderLayout(this, folder, viewFactory);
            mapFolderToFolderLayout.put(folder, layout);
        }
        else
        {
            layout = (IPlaceholderFolderLayout)mapFolderToFolderLayout.get(folder);
        }
        return layout;
    }
}
