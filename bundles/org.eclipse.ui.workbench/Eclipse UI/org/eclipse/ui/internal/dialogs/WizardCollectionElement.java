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
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * Instances of this class are a collection of WizardCollectionElements,
 * thereby facilitating the definition of tree structures composed of these
 * elements. Instances also store a list of wizards.
 */
public class WizardCollectionElement extends AdaptableList implements 
		IPluginContribution, IWizardCategory {
    private String id;

    private String pluginId;

    private String name;

    private WizardCollectionElement parent;

    private AdaptableList wizards = new AdaptableList();

	private IConfigurationElement configElement;

    /**
     * Creates a new <code>WizardCollectionElement</code>. Parent can be
     * null.
     * @param id the id
     * @param pluginId the plugin
     * @param name the name
     * @param parent the parent
     */
    public WizardCollectionElement(String id, String pluginId, String name,
            WizardCollectionElement parent) {
        this.name = name;
        this.id = id;
        this.pluginId = pluginId;
        this.parent = parent;
    }

    /**
     * Creates a new <code>WizardCollectionElement</code>. Parent can be
     * null.
     * 
     * @param element
     * @param parent
     * @since 3.1
     */
    public WizardCollectionElement(IConfigurationElement element, WizardCollectionElement parent) {
		configElement = element;
		id = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID); 
		this.parent = parent;
	}

	/**
     * Adds a wizard collection to this collection.
     */
    public AdaptableList add(IAdaptable a) {
        if (a instanceof WorkbenchWizardElement) {
            wizards.add(a);
        } else {
            super.add(a);
        }
        return this;
    }
    
    
    /**
     * Remove a wizard from this collection.
     */
    public void remove(IAdaptable a) {
        if (a instanceof WorkbenchWizardElement) {
            wizards.remove(a);
        } else {
            super.remove(a);
        }		
	}

	/**
     * Returns the wizard collection child object corresponding to the passed
     * path (relative to this object), or <code>null</code> if such an object
     * could not be found.
     * 
     * @param searchPath
     *            org.eclipse.core.runtime.IPath
     * @return WizardCollectionElement
     */
    public WizardCollectionElement findChildCollection(IPath searchPath) {
        Object[] children = getChildren(null);
        String searchString = searchPath.segment(0);
        for (int i = 0; i < children.length; ++i) {
            WizardCollectionElement currentCategory = (WizardCollectionElement) children[i];
            if (currentCategory.getId().equals(searchString)) {
                if (searchPath.segmentCount() == 1) {
					return currentCategory;
				}

                return currentCategory.findChildCollection(searchPath
                        .removeFirstSegments(1));
            }
        }

        return null;
    }

    /**
     * Returns the wizard category corresponding to the passed
     * id, or <code>null</code> if such an object could not be found.
     * This recurses through child categories.
     * 
     * @param id the id for the child category
     * @return the category, or <code>null</code> if not found
     * @since 3.1
     */
    public WizardCollectionElement findCategory(String id) {
        Object[] children = getChildren(null);
        for (int i = 0; i < children.length; ++i) {
            WizardCollectionElement currentCategory = (WizardCollectionElement) children[i];
            if (id.equals(currentCategory.getId())) {
                    return currentCategory;
            }
            WizardCollectionElement childCategory = currentCategory.findCategory(id);
            if (childCategory != null) {
                return childCategory;
            }
        }
        return null;
    }

    /**
     * Returns this collection's associated wizard object corresponding to the
     * passed id, or <code>null</code> if such an object could not be found.
     * 
     * @param searchId the id to search on
     * @param recursive whether to search recursivly
     * @return the element
     */
    public WorkbenchWizardElement findWizard(String searchId, boolean recursive) {
        Object[] wizards = getWizards();
        for (int i = 0; i < wizards.length; ++i) {
            WorkbenchWizardElement currentWizard = (WorkbenchWizardElement) wizards[i];
            if (currentWizard.getId().equals(searchId)) {
				return currentWizard;
			}
        }
        if (!recursive) {
			return null;
		}
        for (Iterator iterator = children.iterator(); iterator.hasNext();) {
            WizardCollectionElement child = (WizardCollectionElement) iterator
                    .next();
            WorkbenchWizardElement result = child.findWizard(searchId, true);
            if (result != null) {
				return result;
			}
        }
        return null;
    }

    /**
     * Returns an object which is an instance of the given class associated
     * with this object. Returns <code>null</code> if no such object can be
     * found.
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IWorkbenchAdapter.class) {
            return this;
        }
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    /**
     * Returns the unique ID of this element.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the label for this collection.
     */
    public String getLabel(Object o) {
    	return configElement != null ? configElement
				.getAttribute(IWorkbenchRegistryConstants.ATT_NAME) : name;
    }

    /**
     * Returns the logical parent of the given object in its tree.
     */
    public Object getParent(Object o) {
        return parent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.wizards.IWizardCategory#getPath()
     */
    public IPath getPath() {
        if (parent == null) {
			return new Path(""); //$NON-NLS-1$
		}

        return parent.getPath().append(getId());
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.wizards.IWizardCategory#getWizards()
     */
    public IWizardDescriptor [] getWizards() {
		return getWizardsExpression((IWizardDescriptor[]) wizards
				.getTypedChildren(IWizardDescriptor.class));
	}

    /**
     * Takes an array of <code>IWizardDescriptor</code> and removes all
     * entries which fail the Expressions check.
     * 
     * @param wizardDescriptors Array of <code>IWizardDescriptor</code>.
     * @return The array minus the elements which faled the Expressions check.
     */
    private IWizardDescriptor[] getWizardsExpression(IWizardDescriptor[] wizardDescriptors) {
        int size = wizardDescriptors.length;
        List result = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            if (!WorkbenchActivityHelper.restrictUseOf(
            		(WorkbenchWizardElement)wizardDescriptors[i]))
                result.add(wizardDescriptors[i]);
        }
        return (IWizardDescriptor[])result
                    .toArray(new IWizardDescriptor[result.size()]);
    }   
    
    /**
     * Return the wizards minus the wizards which failed the expressions check.
     * 
     * @return the wizards
     * @since 3.1
     */
    public WorkbenchWizardElement [] getWorkbenchWizardElements() {        
    	return getWorkbenchWizardElementsExpression(
    	    (WorkbenchWizardElement[]) wizards
				.getTypedChildren(WorkbenchWizardElement.class));
    }
    
    /**
     * Takes an array of <code>WorkbenchWizardElement</code> and removes all
     * entries which fail the Expressions check.
     * 
     * @param workbenchWizardElements Array of <code>WorkbenchWizardElement</code>.
     * @return The array minus the elements which faled the Expressions check.
     */
    private WorkbenchWizardElement[] getWorkbenchWizardElementsExpression(
        WorkbenchWizardElement[] workbenchWizardElements) {
        int size = workbenchWizardElements.length;
        List result = new ArrayList(size);
        for (int i=0; i<size; i++) {
            WorkbenchWizardElement element = workbenchWizardElements[i];
            if (!WorkbenchActivityHelper.restrictUseOf(element))
                result.add(element);
        }
        return (WorkbenchWizardElement[])result.toArray(new WorkbenchWizardElement[result.size()]);
    }


    /**
     * Returns true if this element has no children and no wizards.
     * 
     * @return whether it is empty
     */
    public boolean isEmpty() {
        return size() == 0 && wizards.size() == 0;
    }

    /**
     * For debugging purposes.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("WizardCollection, "); //$NON-NLS-1$
        buf.append(children.size());
        buf.append(" children, "); //$NON-NLS-1$
        buf.append(wizards.size());
        buf.append(" wizards"); //$NON-NLS-1$
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.activities.support.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.activities.support.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return configElement != null ? configElement.getNamespace() : pluginId;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.wizards.IWizardCategory#getParent()
     */
    public IWizardCategory getParent() {
		return parent;
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.wizards.IWizardCategory#getCategories()
     */
    public IWizardCategory[] getCategories() {		
		return (IWizardCategory []) getTypedChildren(IWizardCategory.class);
	}
    
    /**
     * Return the collection elements.
     * 
     * @return the collection elements
     * @since 3.1
     */
    public WizardCollectionElement [] getCollectionElements() {
    	return (WizardCollectionElement[]) getTypedChildren(WizardCollectionElement.class);
    }
    
    /**
     * Return the raw adapted list of wizards.
     * 
     * @return the list of wizards
     * @since 3.1
     */
    public AdaptableList getWizardAdaptableList() {
    	return wizards;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.wizards.IWizardCategory#getLabel()
     */
    public String getLabel() {
		return getLabel(this);
	}
    
    /**
     * Return the configuration element.
     * 
     * @return the configuration element
     * @since 3.1
     */
    public IConfigurationElement getConfigurationElement() {
    	return configElement;
    }

    /**
     * Return the parent collection element.
     * 
     * @return the parent
     * @since 3.1
     */
	public WizardCollectionElement getParentCollection() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardCategory#findWizard(java.lang.String)
	 */
	public IWizardDescriptor findWizard(String id) {
		return findWizard(id, true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardCategory#findCategory(org.eclipse.core.runtime.IPath)
	 */
	public IWizardCategory findCategory(IPath path) {
		return findChildCollection(path);
	}
}
