/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Alexander Kuppe, Versant GmbH - bug 215797
 *     Sascha Zak - bug 282874
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810, 440136
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;

/**
 * The ViewManager is a factory for workbench views.  
 */
public class Perspective {
    protected PerspectiveDescriptor descriptor;

    protected WorkbenchPage page;

    // Editor Area management
	// protected LayoutPart editorArea;
	// protected PartPlaceholder editorHolder;
    protected boolean editorHidden = false;
    protected boolean editorAreaRestoreOnUnzoom = false;
    protected int editorAreaState = IWorkbenchPage.STATE_RESTORED;

	// private ViewFactory viewFactory;
    
    protected ArrayList alwaysOnActionSets;

    protected ArrayList alwaysOffActionSets;
    
    /**	IDs of menu items the user has chosen to hide	*/
    protected Collection hideMenuIDs;
    
    /**	IDs of toolbar items the user has chosen to hide	*/
    protected Collection hideToolBarIDs;

    //private List fastViews;
	// protected FastViewManager fastViewManager = null;

    private Map mapIDtoViewLayoutRec;

    protected boolean fixed;

    protected ArrayList showInPartIds;

    protected HashMap showInTimes = new HashMap();

    protected IMemento memento;

    /**
     * Reference to the part that was previously active
     * when this perspective was deactivated.
     */
    private IWorkbenchPartReference oldPartRef = null;

    protected boolean shouldHideEditorsOnActivate = false;

	// protected PageLayout layout;
	protected MPerspective layout;

    /**
     * ViewManager constructor comment.
     */
	public Perspective(PerspectiveDescriptor desc, MPerspective layout, WorkbenchPage page) {
        this(page);
		this.layout = layout;
        descriptor = desc;
    }

	public void initActionSets() {
		if (descriptor != null) {
			List temp = new ArrayList();
			createInitialActionSets(temp,
					ModeledPageLayout.getIds(layout, ModeledPageLayout.ACTION_SET_TAG));
			for (Iterator iter = temp.iterator(); iter.hasNext();) {
				IActionSetDescriptor descriptor = (IActionSetDescriptor) iter.next();
				if (!alwaysOnActionSets.contains(descriptor)) {
					alwaysOnActionSets.add(descriptor);
				}
			}
		}

	}
    /**
     * ViewManager constructor comment.
     */
	protected Perspective(WorkbenchPage page) {
        this.page = page;
        alwaysOnActionSets = new ArrayList(2);
        alwaysOffActionSets = new ArrayList(2);
        hideMenuIDs = new HashSet();
        hideToolBarIDs = new HashSet();
        
        
        mapIDtoViewLayoutRec = new HashMap();
    }


    /**
     * Create the initial list of action sets.
     */
    protected void createInitialActionSets(List outputList, List stringList) {
        ActionSetRegistry reg = WorkbenchPlugin.getDefault()
                .getActionSetRegistry();
        Iterator iter = stringList.iterator();
        while (iter.hasNext()) {
            String id = (String) iter.next();
            IActionSetDescriptor desc = reg.findActionSet(id);
            if (desc != null) {
				outputList.add(desc);
			} else {
				// plugin with actionSet was removed
				// we remember then so it's available when added back
			}
        }
    }

    /**
     * Dispose the perspective and all views contained within.
     */
    public void dispose() {
        // Get rid of presentation.
		mapIDtoViewLayoutRec.clear();
    }

	/**
	 * Returns the perspective.
	 */
    public IPerspectiveDescriptor getDesc() {
        return descriptor;
    }


    /**
     * Returns the new wizard shortcuts associated with this perspective.
     * 
     * @return an array of new wizard identifiers
     */
    public String[] getNewWizardShortcuts() {
		return page.getNewWizardShortcuts();
    }

    /**
     * Returns the perspective shortcuts associated with this perspective.
     * 
     * @return an array of perspective identifiers
     */
    public String[] getPerspectiveShortcuts() {
		return page.getPerspectiveShortcuts();
    }

    /**
     * Returns the ids of the parts to list in the Show In... dialog.
     * This is a List of Strings.
     */
    public ArrayList getShowInPartIds() {
		return page.getShowInPartIds();
    }

    /**
     * Returns the time at which the last Show In was performed
     * for the given target part, or 0 if unknown.
     */
    public long getShowInTime(String partId) {
        Long t = (Long) showInTimes.get(partId);
        return t == null ? 0L : t.longValue();
    }

    /**
     * Returns the show view shortcuts associated with this perspective.
     * 
     * @return an array of view identifiers
     */
    public String[] getShowViewShortcuts() {
		return page.getShowViewShortcuts();
    }

    private void removeAlwaysOn(IActionSetDescriptor descriptor) {
        if (descriptor == null) {
            return;
        }
        if (!alwaysOnActionSets.contains(descriptor)) {
            return;
        }
        
        alwaysOnActionSets.remove(descriptor);
        if (page != null) {
            page.perspectiveActionSetChanged(this, descriptor, ActionSetManager.CHANGE_HIDE);
        }
    }
    
    protected void addAlwaysOff(IActionSetDescriptor descriptor) {
        if (descriptor == null) {
            return;
        }
        if (alwaysOffActionSets.contains(descriptor)) {
            return;
        }
        alwaysOffActionSets.add(descriptor);
        if (page != null) {
            page.perspectiveActionSetChanged(this, descriptor, ActionSetManager.CHANGE_MASK);
        }
        removeAlwaysOn(descriptor);
    }
    
    protected void addAlwaysOn(IActionSetDescriptor descriptor) {
        if (descriptor == null) {
            return;
        }
        if (alwaysOnActionSets.contains(descriptor)) {
            return;
        }
        alwaysOnActionSets.add(descriptor);
        if (page != null) {
            page.perspectiveActionSetChanged(this, descriptor, ActionSetManager.CHANGE_SHOW);
        }
        removeAlwaysOff(descriptor);
    }
    
    private void removeAlwaysOff(IActionSetDescriptor descriptor) {
        if (descriptor == null) {
            return;
        }
        if (!alwaysOffActionSets.contains(descriptor)) {
            return;
        }
        alwaysOffActionSets.remove(descriptor);
        if (page != null) {
            page.perspectiveActionSetChanged(this, descriptor, ActionSetManager.CHANGE_UNMASK);
        }
    }
    
    /**
     * Returns the ActionSets read from perspectiveExtensions in the registry.  
     */
    protected ArrayList getPerspectiveExtensionActionSets() {
		return page.getPerspectiveExtensionActionSets(descriptor.getOriginalId());
    }
    
    public void turnOnActionSets(IActionSetDescriptor[] newArray) {
        for (int i = 0; i < newArray.length; i++) {
            IActionSetDescriptor descriptor = newArray[i];
            
			addActionSet(descriptor);
        }
    }
    
    public void turnOffActionSets(IActionSetDescriptor[] toDisable) {
        for (int i = 0; i < toDisable.length; i++) {
            IActionSetDescriptor descriptor = toDisable[i];
            
            turnOffActionSet(descriptor);
        }
    }

    public void turnOffActionSet(IActionSetDescriptor toDisable) {
		removeActionSet(toDisable);
    }
    


    /**
     * Returns the old part reference.
     * Returns null if there was no previously active part.
     * 
     * @return the old part reference or <code>null</code>
     */
    public IWorkbenchPartReference getOldPartRef() {
        return oldPartRef;
    }

    /**
     * Sets the old part reference.
     * 
     * @param oldPartRef The old part reference to set, or <code>null</code>
     */
    public void setOldPartRef(IWorkbenchPartReference oldPartRef) {
        this.oldPartRef = oldPartRef;
    }

    //for dynamic UI
    protected void addActionSet(IActionSetDescriptor newDesc) {
    	IContextService service = page.getWorkbenchWindow().getService(IContextService.class);
    	try {
			service.deferUpdates(true);
			for (int i = 0; i < alwaysOnActionSets.size(); i++) {
				IActionSetDescriptor desc = (IActionSetDescriptor) alwaysOnActionSets
						.get(i);
				if (desc.getId().equals(newDesc.getId())) {
					removeAlwaysOn(desc);
					removeAlwaysOff(desc);
					break;
				}
			}
			addAlwaysOn(newDesc);
			final String actionSetID = newDesc.getId();

			// Add Tags
			String tag = ModeledPageLayout.ACTION_SET_TAG + actionSetID;
			if (!layout.getTags().contains(tag)) {
				layout.getTags().add(tag);
			}
		} finally {
    		service.deferUpdates(false);
    	}
    }

    // for dynamic UI
	protected void removeActionSet(IActionSetDescriptor toRemove) {
		String id = toRemove.getId();
    	IContextService service = page.getWorkbenchWindow().getService(IContextService.class);
    	try {
			service.deferUpdates(true);
			for (int i = 0; i < alwaysOnActionSets.size(); i++) {
				IActionSetDescriptor desc = (IActionSetDescriptor) alwaysOnActionSets
						.get(i);
				if (desc.getId().equals(id)) {
					removeAlwaysOn(desc);
					break;
				}
			}

			for (int i = 0; i < alwaysOffActionSets.size(); i++) {
				IActionSetDescriptor desc = (IActionSetDescriptor) alwaysOffActionSets
						.get(i);
				if (desc.getId().equals(id)) {
					removeAlwaysOff(desc);
					break;
				}
			}
			addAlwaysOff(toRemove);
			// remove tag
			String tag = ModeledPageLayout.ACTION_SET_TAG + id;
			if (layout.getTags().contains(tag)) {
				layout.getTags().remove(tag);
			}
		} finally {
    		service.deferUpdates(false);
    	}
    }
    
    public IActionSetDescriptor[] getAlwaysOnActionSets() {
        return (IActionSetDescriptor[]) alwaysOnActionSets.toArray(new IActionSetDescriptor[alwaysOnActionSets.size()]);
    }
    
    public IActionSetDescriptor[] getAlwaysOffActionSets() {
        return (IActionSetDescriptor[]) alwaysOffActionSets.toArray(new IActionSetDescriptor[alwaysOffActionSets.size()]);
    }
	
	/**	@return a Collection of IDs of items to be hidden from the menu bar	*/
	public Collection getHiddenMenuItems() {
		return hideMenuIDs;
	}
	
	/**	@return a Collection of IDs of items to be hidden from the tool bar	*/
	public Collection getHiddenToolbarItems() {
		return hideToolBarIDs;
	}
	
	public void updateActionBars() {
		page.getActionBars().getMenuManager().updateAll(true);
		page.resetToolBarLayout();
	}

}
