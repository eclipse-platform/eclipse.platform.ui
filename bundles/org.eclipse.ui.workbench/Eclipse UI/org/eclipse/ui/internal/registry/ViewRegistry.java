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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The central manager for view descriptors.
 */
public class ViewRegistry extends RegistryManager implements IViewRegistry {
    private List views;

    private boolean dirtyViews;

    private List categories;

    private List sticky;

    private boolean dirtyCategories;

    private boolean dirtySticky;

    private Category miscCategory;

    protected static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

    private class ViewRegistryElement {
        private List viewDescriptors;

        private List categoryDescriptors;

        private List stickyDescriptors;

        public ViewRegistryElement() {
            viewDescriptors = new ArrayList();
            categoryDescriptors = new ArrayList();
            stickyDescriptors = new ArrayList();
        }

        public void addCategory(Category element) {
            categoryDescriptors.add(element);
        }

        public void addViewDescriptor(IViewDescriptor element) {
            viewDescriptors.add(element);
        }

        public void addStickyView(IStickyViewDescriptor id) {
            stickyDescriptors.add(id);
        }

        public List getCategories() {
            return categoryDescriptors;
        }

        public List getViewDescriptors() {
            return viewDescriptors;
        }

        public List getStickyDescriptors() {
            return stickyDescriptors;
        }
    }

    /**
     * Create a new ViewRegistry.
     */
    public ViewRegistry() {
        super(WorkbenchPlugin.PI_WORKBENCH, IWorkbenchConstants.PL_VIEWS);
        views = new ArrayList();
        dirtyViews = true;
        categories = new ArrayList();
        dirtyCategories = true;
        sticky = new ArrayList();
        dirtySticky = true;
        Platform.getExtensionRegistry().addRegistryChangeListener(this);
    }

    /**
     * Add a category to the registry.
     */
    public void add(Category desc) {
        /* fix for 1877 */
        if (findCategory(desc.getId()) == null) {
            // Mark categories list as dirty
            dirtyCategories = true;
            ViewRegistryElement element = new ViewRegistryElement();
            element.addCategory(desc);
            add(element, desc.getPluginId());
        }
    }

    /**
     * Add a descriptor to the registry.
     */
    public void add(IViewDescriptor desc) {
        dirtyViews = true;
        ViewRegistryElement element = new ViewRegistryElement();
        element.addViewDescriptor(desc);
        add(element, desc.getConfigurationElement().getDeclaringExtension()
                .getNamespace());
    }

    /**
     * Add a sticky descriptor to the registry.
     */
    public void add(IStickyViewDescriptor desc) {
        dirtySticky = true;
        ViewRegistryElement element = new ViewRegistryElement();
        element.addStickyView(desc);
        add(element, desc.getNamespace());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.aaRegistryCacheaa#buildNewCacheObject(org.eclipse.core.runtime.IExtensionDelta)
     */
    public Object[] buildNewCacheObject(IExtensionDelta delta) {
        IExtension extension = delta.getExtension();
        if (extension == null)
            return null;
        IConfigurationElement[] elements = extension.getConfigurationElements();
        ViewRegistryElement regElement = new ViewRegistryElement();
        for (int i = 0; i < elements.length; i++) {
            IConfigurationElement singleton = elements[i];
            String id = singleton.getAttribute(IWorkbenchConstants.TAG_ID);
            if (singleton.getName().equals(ViewRegistryReader.TAG_VIEW)) {
                // We want to create a view descriptor
                if (find(id) != null)
                    // This view already exists.  Ignore this new one.
                    continue;
                try {
                    String descText = ""; //$NON-NLS-1$
                    IConfigurationElement[] children = singleton
                            .getChildren(TAG_DESCRIPTION);
                    if (children.length >= 1) {
                        descText = children[0].getValue();
                    }
                    ViewDescriptor desc = new ViewDescriptor(singleton,
                            descText);
                    regElement.addViewDescriptor(desc);
                    dirtyViews = true;
                } catch (CoreException e) {
                    // log an error since its not safe to open a dialog here
                    WorkbenchPlugin.log(
                            "Unable to create view descriptor.", e.getStatus());//$NON-NLS-1$
                }
            } else if (singleton.getName().equals(
                    ViewRegistryReader.TAG_CATEGORY)) { //$NON-NLS-1$
                try {
                    // We want to create a category
                    if (findCategory(id) != null)
                        // This category already exists.  Ignore this new one.
                        continue;
                    Category category = new Category(singleton);
                    regElement.addCategory(category);
                    dirtyCategories = true;
                } catch (WorkbenchException e) {
                    WorkbenchPlugin.log(
                            "Unable to create view category.", e.getStatus());//$NON-NLS-1$
                }
            } else if (singleton.getName().equals(
                    ViewRegistryReader.TAG_STICKYVIEW)) { //$NON-NLS-1$
                if (findSticky(id) != null) {
                    continue;
                }
                try {
                    StickyViewDescriptor desc = new StickyViewDescriptor(
                            singleton);
                    regElement.addStickyView(desc);
                    dirtySticky = true;
                } catch (CoreException e) {
                    // log an error since its not safe to open a dialog here
                    WorkbenchPlugin
                            .log(
                                    "Unable to create sticky view descriptor.", e.getStatus());//$NON-NLS-1$
                }
            }
        }
        List categories = regElement.getCategories();
        List views = regElement.getViewDescriptors();
        List sticky = regElement.getStickyDescriptors();
        if ((categories == null || categories.size() == 0)
                && (views == null || views.size() == 0)
                && (sticky == null || sticky.size() == 0)) {
            return null;
        }
        Object retArray[] = new Object[1];
        retArray[0] = regElement;
        return retArray;
    }

    /**
     * @param id
     * @return
     */
    private IStickyViewDescriptor findSticky(String id) {
        buildSticky();
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
        buildViews();
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
        buildCategories();
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
        buildCategories();
        int nSize = categories.size();
        Category[] retArray = new Category[nSize];
        categories.toArray(retArray);
        return retArray;
    }

    /**
     * Get the list of sticky views.
     */
    public IStickyViewDescriptor[] getStickyViews() {
        buildSticky();
        return (IStickyViewDescriptor[]) sticky
                .toArray(new IStickyViewDescriptor[sticky.size()]);
    }

    /**
     * Return the view category count.
     */
    public int getCategoryCount() {
        buildCategories();
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
        buildViews();
        return views.size();
    }

    /**
     * Get an enumeration of view descriptors.
     */
    public IViewDescriptor[] getViews() {
        buildViews();
        int nSize = views.size();
        IViewDescriptor[] retArray = new IViewDescriptor[nSize];
        views.toArray(retArray);
        return retArray;
    }

    /**
     * Adds each view in the registry to a particular category.
     * The view category may be defined in xml.  If not, the view is
     * added to the "misc" category.
     */
    public void mapViewsToCategories() {
        buildCategories();
        buildViews();
        Iterator itr = views.iterator();
        while (itr.hasNext()) {
            IViewDescriptor desc = (IViewDescriptor) itr.next();
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
                    buildCategories();
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

    private void buildViews() {
        if (dirtyViews) {
            // Build up the view arraylist
            views = new ArrayList();
            Object[] regElements = getRegistryObjects();
            if (regElements == null) {
                dirtyViews = false;
                return;
            }
            for (int i = 0; i < regElements.length; i++) {
                ViewRegistryElement element = (ViewRegistryElement) regElements[i];
                List viewDescs = element.getViewDescriptors();
                if (viewDescs != null && viewDescs.size() != 0) {
                    Iterator iter = viewDescs.iterator();
                    while (iter.hasNext()) {
                        IViewDescriptor view = (IViewDescriptor) iter.next();
                        views.add(view);
                    }
                }
            }
            dirtyViews = false;
        }
    }

    private void buildCategories() {
        if (dirtyCategories) {
            // Build up the categories arraylist
            categories = new ArrayList();
            Object[] regElements = getRegistryObjects();
            if (regElements == null) {
                dirtyCategories = false;
                return;
            }
            for (int i = 0; i < regElements.length; i++) {
                ViewRegistryElement element = (ViewRegistryElement) regElements[i];
                List tempCategories = element.getCategories();
                if (tempCategories != null && tempCategories.size() != 0) {
                    Iterator iter = tempCategories.iterator();
                    while (iter.hasNext()) {
                        Category category = (Category) iter.next();
                        categories.add(category);
                    }
                }
            }
            dirtyCategories = false;
        }
    }

    /**
     * 
     */
    private void buildSticky() {
        if (dirtySticky) {
            // Build up the categories arraylist
            sticky = new ArrayList();
            Object[] regElements = getRegistryObjects();
            if (regElements == null) {
                dirtySticky = false;
                return;
            }
            for (int i = 0; i < regElements.length; i++) {
                ViewRegistryElement element = (ViewRegistryElement) regElements[i];
                List tempSticky = element.getStickyDescriptors();
                if (tempSticky != null && tempSticky.size() != 0) {
                    Iterator iter = tempSticky.iterator();
                    while (iter.hasNext()) {
                        IStickyViewDescriptor desc = (IStickyViewDescriptor) iter
                                .next();
                        sticky.add(desc);
                    }
                }
            }
            dirtySticky = false;
        }
    }

    public void postChangeProcessing() {
        super.postChangeProcessing();
        mapViewsToCategories();
    }

    public void dispose() {
        Platform.getExtensionRegistry().removeRegistryChangeListener(this);
    }
}