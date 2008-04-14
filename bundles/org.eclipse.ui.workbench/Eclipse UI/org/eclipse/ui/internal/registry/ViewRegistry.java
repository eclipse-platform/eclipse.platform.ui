/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.views.IStickyViewDescriptor;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

import com.ibm.icu.text.MessageFormat;

/**
 * The central manager for view descriptors.
 */
public class ViewRegistry implements IViewRegistry, IExtensionChangeHandler {
	
    /**
	 * @since 3.4
	 *
	 */
	private static final class ViewDescriptorComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			String id1 = ((ViewDescriptor) o1).getId();
			String id2 = ((ViewDescriptor) o2).getId();
			
			return id1.compareTo(id2);
		}
	}

	/**
     * Proxies a Category implementation.
     * 
     * @since 3.1
     */
    private static class ViewCategoryProxy implements IViewCategory, IPluginContribution {

        private Category rawCategory;

        /**
         * Create a new instance of this class
         * 
         * @param rawCategory the category
         */
        public ViewCategoryProxy(Category rawCategory) {
            this.rawCategory = rawCategory;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.views.IViewCategory#getViews()
         */
        public IViewDescriptor[] getViews() {
            ArrayList elements = rawCategory.getElements();
            if (elements == null) {
                return new IViewDescriptor[0];
            }
            // Returns the views of this category,
            // minus the one which failed the evaluation check.
            Collection descs = WorkbenchActivityHelper.restrictCollection(elements, new ArrayList());
            return (IViewDescriptor[])descs.toArray(new IViewDescriptor[descs.size()]);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.views.IViewCategory#getId()
         */
        public String getId() {
            return rawCategory.getId();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.views.IViewCategory#getPath()
         */
        public IPath getPath() {
            String rawParentPath = rawCategory.getRawParentPath();
            if (rawParentPath == null) {
				return new Path(""); //$NON-NLS-1$
			}
            return new Path(rawParentPath);
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.views.IViewCategory#getLabel()
         */
        public String getLabel() {
            return rawCategory.getLabel();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IPluginContribution#getLocalId()
         */
        public String getLocalId() {
            return getId();
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.IPluginContribution#getPluginId()
         */
        public String getPluginId() {
            return rawCategory.getPluginId();
        }
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o) {
			if (o instanceof IViewCategory) {
				return getId().equals(((IViewCategory)o).getId());
			}
			return false;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return getId().hashCode();
		}
    }
    
	private static String EXTENSIONPOINT_UNIQUE_ID = WorkbenchPlugin.PI_WORKBENCH + "." + IWorkbenchRegistryConstants.PL_VIEWS; //$NON-NLS-1$
	
	/**
	 * A set that will only ever contain ViewDescriptors.
	 */
    private SortedSet views = new TreeSet(new ViewDescriptorComparator());

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
        PlatformUI.getWorkbench().getExtensionTracker().registerHandler(this, ExtensionTracker.createExtensionPointFilter(getExtensionPointFilter()));
        reader.readViews(Platform.getExtensionRegistry(), this);
    }

    /**
     * Add a category to the registry.
     * 
     * @param desc the descriptor to add
     */
    public void add(Category desc) {
        /* fix for 1877 */
		if (internalFindCategory(desc.getId()) == null) {
			dirtyViewCategoryMappings = true;
			// Mark categories list as dirty
			categories.add(desc);
			IConfigurationElement element = (IConfigurationElement) Util.getAdapter(desc, IConfigurationElement.class);
			if (element == null) {
				return;
			}
			PlatformUI.getWorkbench().getExtensionTracker()
					.registerObject(
							element.getDeclaringExtension(),
							desc,
							IExtensionTracker.REF_WEAK);
		}
    }

    /**
     * Add a descriptor to the registry.
     * 
     * @param desc the descriptor to add
     */
    public void add(ViewDescriptor desc) {
    	if (views.add(desc)) {
            dirtyViewCategoryMappings = true;
            PlatformUI.getWorkbench().getExtensionTracker().registerObject(
                    desc.getConfigurationElement().getDeclaringExtension(),
                    desc, IExtensionTracker.REF_WEAK);
        }
    }
    
    /**
     * Add a sticky descriptor to the registry.
     * 
     * @param desc the descriptor to add
     */
    public void add(StickyViewDescriptor desc) {
    	if (!sticky.contains(desc)) {
	        sticky.add(desc);
	        PlatformUI.getWorkbench().getExtensionTracker()
			.registerObject(
					desc.getConfigurationElement().getDeclaringExtension(),
					desc, 
					IExtensionTracker.REF_WEAK);
    	}
    }

//    /**
//     * Return the sticky view descriptor.
//     * 
//     * @param id the id to searc for 
//     * @return the sticky view descriptor
//     */
//    private IStickyViewDescriptor findSticky(String id) {
//        for (Iterator i = sticky.iterator(); i.hasNext();) {
//            IStickyViewDescriptor desc = (IStickyViewDescriptor) i.next();
//            if (id.equals(desc.getId()))
//                return desc;
//        }
//        return null;
//    }

    /**
     * Find a descriptor in the registry.
     * 
     * @return The descriptor. But even if the descriptor exists, it returns 
     * 		   <code>null</code> if the descriptor fails the Expressions check. 
     */
    public IViewDescriptor find(String id) {
        Iterator itr = views.iterator();
        while (itr.hasNext()) {
            IViewDescriptor desc = (IViewDescriptor) itr.next();
            if (id.equals(desc.getId())) {
                if (WorkbenchActivityHelper.restrictUseOf(desc)) {
                    return null;
                }
                return desc;
            }
        }
        return null;
    }

    /**
     * Find a category with a given name.
     * 
     * @param id the id to search for
     * @return the category or <code>null</code>
     */
    public IViewCategory findCategory(String id) {
    	mapViewsToCategories();
        Category category = internalFindCategory(id);
        if (category == null) {
			return null;
		}
        return new ViewCategoryProxy(category);
    }

    /**
     * Returns the category with no updating of the view/category mappings.
     *
	 * @param id the category id
	 * @return the Category
     * @since 3.1
	 */
	private Category internalFindCategory(String id) {
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
    public IViewCategory[] getCategories() {
    	mapViewsToCategories();
        int nSize = categories.size();
        IViewCategory[] retArray = new IViewCategory[nSize];
        int i = 0;
        for (Iterator itr = categories.iterator(); itr.hasNext();) {
            retArray[i++] = new ViewCategoryProxy((Category) itr.next());
        }
        return retArray;
    }

    /**
     * Get the list of sticky views minus the sticky views which failed the
     * Expressions check.
     */
    public IStickyViewDescriptor[] getStickyViews() {
    	Collection descs = WorkbenchActivityHelper.restrictCollection(sticky, new ArrayList());
    	return (IStickyViewDescriptor[]) descs.toArray(new IStickyViewDescriptor[descs.size()]);
    }

    /**
     * Returns the Misc category. This may be <code>null</code> if there are
     * no miscellaneous views.
     * 
     * @return the misc category or <code>null</code>
     */
    public Category getMiscCategory() {
        return miscCategory;
    }

    /**
     * Get an enumeration of view descriptors.
     * 
     * Returns an enumeration of view descriptors, but without the view
     * descriptors which failed the test. 
     */
    public IViewDescriptor[] getViews() {
    	Collection descs = WorkbenchActivityHelper.restrictCollection(views, new TreeSet(new ViewDescriptorComparator()));
    	return (IViewDescriptor[]) descs.toArray(new IViewDescriptor[descs.size()]);
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
	                cat = internalFindCategory(rootCat);
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
	                                    desc.getId(), miscCategory.getLabel() }));
	                }
	                miscCategory.addElement(desc);
	            }
	        }	        
    	}
    }

    /**
     * Dispose of this registry.
     */
    public void dispose() {
    	PlatformUI.getWorkbench().getExtensionTracker().unregisterHandler(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
     */
    public void removeExtension(IExtension extension,Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof StickyViewDescriptor) {           
                sticky.remove(objects[i]);
            }
            else if (objects[i] instanceof ViewDescriptor) {
                views.remove(objects[i]);
                dirtyViewCategoryMappings = true;
            }
            else if (objects[i] instanceof Category) {
                categories.remove(objects[i]);
                dirtyViewCategoryMappings = true;
            }
        }

	}

    private IExtensionPoint getExtensionPointFilter() {
      return Platform.getExtensionRegistry().getExtensionPoint(EXTENSIONPOINT_UNIQUE_ID);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker, org.eclipse.core.runtime.IExtension)
     */
    public void addExtension(IExtensionTracker tracker,IExtension addedExtension){
        IConfigurationElement[] addedElements = addedExtension.getConfigurationElements();
        for (int i = 0; i < addedElements.length; i++) {
            IConfigurationElement element = addedElements[i];
    		if (element.getName().equals(IWorkbenchRegistryConstants.TAG_VIEW)) {
    			reader.readView(element);
    		} else if (element.getName().equals(IWorkbenchRegistryConstants.TAG_CATEGORY)) {
    			reader.readCategory(element);
    		} else if (element.getName().equals(IWorkbenchRegistryConstants.TAG_STICKYVIEW)) {
    			reader.readSticky(element);
    		}			
        }
	}
}
