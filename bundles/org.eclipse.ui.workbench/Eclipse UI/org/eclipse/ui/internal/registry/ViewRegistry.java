/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.experimental.IConfigurationElementAdditionHandler;
import org.eclipse.ui.internal.registry.experimental.IConfigurationElementRemovalHandler;
import org.eclipse.ui.internal.registry.experimental.IConfigurationElementTracker;

/**
 * The central manager for view descriptors.
 */
public class ViewRegistry implements IViewRegistry, IConfigurationElementRemovalHandler, IConfigurationElementAdditionHandler {
	
	private static String EXTENSIONPOINT_UNIQUE_ID = WorkbenchPlugin.PI_WORKBENCH + "." + IWorkbenchConstants.PL_VIEWS; //$NON-NLS-1$
	
	/**
	 * A set that will only ever contain ViewDescriptors.
	 */
    private SortedSet views = new TreeSet(new Comparator() {
		public int compare(Object o1, Object o2) {
			String id1 = ((ViewDescriptor) o1).getId();
			String id2 = ((ViewDescriptor) o2).getId();
			
			return id1.compareTo(id2);
		}});

    private List categories;

    private List sticky;

    private Category miscCategory;

    protected static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
    
    private ViewRegistryReader reader = new ViewRegistryReader();

	private boolean dirtyViewCategoryMappings = true;

    /**
     * Create a new ViewRegistry.
     */
    public ViewRegistry() {
        super();    
        categories = new ArrayList();       
        sticky = new ArrayList();        
        Workbench.getInstance().getConfigurationElementTracker().registerRemovalHandler(this);
        Workbench.getInstance().getConfigurationElementTracker().registerAdditionHandler(this);
        reader.readViews(Platform.getExtensionRegistry(), this);
    }

    /**
     * Add a category to the registry.
     */
    public void add(Category desc) {
        /* fix for 1877 */
		if (findCategory(desc.getId()) == null) {
			dirtyViewCategoryMappings = true;
			// Mark categories list as dirty
			categories.add(desc);
			Workbench.getInstance().getConfigurationElementTracker()
					.registerObject(
							(IConfigurationElement) desc
									.getAdapter(IConfigurationElement.class),
							desc);
		}
    }

    /**
     * Add a descriptor to the registry.
     */
    public void add(IViewDescriptor desc) {
    	if (views.add(desc)) {
    		dirtyViewCategoryMappings = true;
			Workbench.getInstance().getConfigurationElementTracker()
					.registerObject(
							(IConfigurationElement) desc.getConfigurationElement(),
							desc);
    	}
    }

    /**
     * Add a sticky descriptor to the registry.
     */
    public void add(StickyViewDescriptor desc) {
    	if (!sticky.contains(desc)) {
	        sticky.add(desc);
	        Workbench.getInstance().getConfigurationElementTracker()
			.registerObject(
					(IConfigurationElement) desc.getConfigurationElement(),
					desc);
    	}
    }

    /**
     * @param id
     * @return
     */
    private IStickyViewDescriptor findSticky(String id) {
        for (Iterator i = sticky.iterator(); i.hasNext();) {
            IStickyViewDescriptor desc = (IStickyViewDescriptor) i.next();
            if (id.equals(desc.getId()))
                return desc;
        }
        return null;
    }

    /**
     * Find a descriptor in the registry.
     */
    public IViewDescriptor find(String id) {
        Iterator itr = views.iterator();
        while (itr.hasNext()) {
            IViewDescriptor desc = (IViewDescriptor) itr.next();
            if (id.equals(desc.getID())) {
                return desc;
            }
        }
        return null;
    }

    /**
     * Find a category with a given name.
     */
    public Category findCategory(String id) {
    	mapViewsToCategories();
        Iterator itr = categories.iterator();
        while (itr.hasNext()) {
            Category cat = (Category) itr.next();
            if (id.equals(cat.getRootPath())) {
                return cat;
            }
        }
        return null;
    }

    /**
     * Get the list of view categories.
     */
    public Category[] getCategories() {
    	mapViewsToCategories();
        int nSize = categories.size();
        Category[] retArray = new Category[nSize];
        categories.toArray(retArray);
        return retArray;
    }

    /**
     * Get the list of sticky views.
     */
    public IStickyViewDescriptor[] getStickyViews() {
        return (IStickyViewDescriptor[]) sticky
                .toArray(new IStickyViewDescriptor[sticky.size()]);
    }

    /**
     * Return the view category count.
     */
    public int getCategoryCount() {
        return categories.size();
    }

    /**
     * Returns the Misc category.
     * This may be null if there are no miscellaneous views.
     */
    public Category getMiscCategory() {
        return miscCategory;
    }

    /**
     * Return the view count.
     */
    public int getViewCount() {
        return views.size();
    }

    /**
     * Get an enumeration of view descriptors.
     */
    public IViewDescriptor[] getViews() {
    	return (IViewDescriptor []) views.toArray(new IViewDescriptor [views.size()]);
    }

    /**
     * Adds each view in the registry to a particular category.
     * The view category may be defined in xml.  If not, the view is
     * added to the "misc" category.
     */
    public void mapViewsToCategories() {
    	if (dirtyViewCategoryMappings) {
    		dirtyViewCategoryMappings = false;
	    	// clear all category mappings
	    	for (Iterator i = categories.iterator(); i.hasNext(); ) {
	    		Category category = (Category) i.next();
	    		category.clear(); // this is bad    		
	    	}
	    	
	    	if (miscCategory != null) {
	    		miscCategory.clear();
	    	}
	    	
	    	for (Iterator i = views.iterator(); i.hasNext(); ) {
	            IViewDescriptor desc = (IViewDescriptor) i.next();
	            Category cat = null;
	            String[] catPath = desc.getCategoryPath();
	            if (catPath != null) {
	                String rootCat = catPath[0];
	                cat = (Category) findCategory(rootCat);
	            }
	            if (cat != null) {
	                if (!cat.hasElement(desc)) {
	                    cat.addElement(desc);
	                }
	            } else {
	                if (miscCategory == null) {
	                    miscCategory = new Category();
	                    add(miscCategory);                    
	                }
	                if (catPath != null) {
	                    // If we get here, this view specified a category which
	                    // does not exist. Add this view to the 'Other' category
	                    // but give out a message (to the log only) indicating 
	                    // this has been done.
	                    String fmt = "Category {0} not found for view {1}.  This view added to ''{2}'' category."; //$NON-NLS-1$
	                    WorkbenchPlugin.log(MessageFormat
	                            .format(fmt, new Object[] { catPath[0],
	                                    desc.getID(), miscCategory.getLabel() })); //$NON-NLS-1$
	                }
	                miscCategory.addElement(desc);
	            }
	        }	        
    	}
    }

    public void dispose() {
    	Workbench.getInstance().getConfigurationElementTracker().unregisterRemovalHandler(this);
    	Workbench.getInstance().getConfigurationElementTracker().unregisterAdditionHandler(this);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.experimental.IConfigurationElementRemovalHandler#removeInstance(org.eclipse.core.runtime.IConfigurationElement, java.lang.Object)
	 */
	public void removeInstance(IConfigurationElement source, Object object) {
		if (object instanceof StickyViewDescriptor) {			
			sticky.remove(object);
		}
		else if (object instanceof ViewDescriptor) {
			views.remove(object);
			dirtyViewCategoryMappings = true;
		}
		else if (object instanceof Category) {
			categories.remove(object);
			dirtyViewCategoryMappings = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.experimental.IConfigurationElementAdditionHandler#addInstance(org.eclipse.ui.internal.registry.experimental.IConfigurationElementTracker, org.eclipse.core.runtime.IConfigurationElement)
	 */
	public void addInstance(IConfigurationElementTracker tracker, IConfigurationElement element) {
		if (!element.getDeclaringExtension()
				.getExtensionPointUniqueIdentifier().equals(
						EXTENSIONPOINT_UNIQUE_ID))
			return;

		if (element.getName().equals(ViewRegistryReader.TAG_VIEW)) {
			reader.readView(element);
		} else if (element.getName().equals(ViewRegistryReader.TAG_CATEGORY)) {
			reader.readCategory(element);
		} else if (element.getName().equals(ViewRegistryReader.TAG_STICKYVIEW)) {
			reader.readSticky(element);
		}			
	}
}