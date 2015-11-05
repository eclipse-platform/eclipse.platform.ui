/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
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
public class WizardCollectionElement extends AdaptableList implements IPluginContribution,
		IWizardCategory {
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

	private WizardCollectionElement(WizardCollectionElement input, AdaptableList wizards) {
		this(input.id, input.pluginId, input.name, input.parent);
		this.configElement = input.configElement;
		this.wizards = wizards;

		for (Object child : input.children) {
			children.add(child);
		}
	}

	/**
     * Adds a wizard collection to this collection.
     */
    @Override
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
    @Override
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
    @Override
	public Object getAdapter(Class adapter) {
        if (adapter == IWorkbenchAdapter.class) {
            return this;
        }
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    /**
     * Returns the unique ID of this element.
     */
    @Override
	public String getId() {
        return id;
    }

    /**
     * Returns the label for this collection.
     */
    @Override
	public String getLabel(Object o) {
    	return configElement != null ? configElement
				.getAttribute(IWorkbenchRegistryConstants.ATT_NAME) : name;
    }

    /**
     * Returns the logical parent of the given object in its tree.
     */
    @Override
	public Object getParent(Object o) {
        return parent;
    }

    @Override
	public IPath getPath() {
        if (parent == null) {
			return new Path(""); //$NON-NLS-1$
		}

        return parent.getPath().append(getId());
    }


    @Override
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
            if (!WorkbenchActivityHelper.restrictUseOf(wizardDescriptors[i]))
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
    @Override
	public String toString() {
        StringBuffer buf = new StringBuffer("WizardCollection, "); //$NON-NLS-1$
        buf.append(children.size());
        buf.append(" children, "); //$NON-NLS-1$
        buf.append(wizards.size());
        buf.append(" wizards"); //$NON-NLS-1$
        return buf.toString();
    }

    @Override
	public ImageDescriptor getImageDescriptor(Object object) {
        return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
    }

    @Override
	public String getLocalId() {
        return getId();
    }

    @Override
	public String getPluginId() {
        return configElement != null ? configElement.getNamespace() : pluginId;
    }


    @Override
	public IWizardCategory getParent() {
		return parent;
	}

    @Override
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

    @Override
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

	@Override
	public IWizardDescriptor findWizard(String id) {
		return findWizard(id, true);
	}

	@Override
	public IWizardCategory findCategory(IPath path) {
		return findChildCollection(path);
	}

	/**
	 * The helper method used to filter <code>WizardCollectionElement</code>
	 * using <code>ViewerFilter</code>.<br>
	 * It returns the result in the following way:<br>
	 * - if some of the wizards from the input collection is skipped by the
	 * viewerFilter then the modified copy of the collection (without skipped
	 * wizards) is returned<br>
	 * - when all wizards are skipped then null will be returned<br>
	 * - if none of the wizards is skipped during filtering then the original
	 * input collection is returned
	 *
	 * @param viewer
	 *            the Viewer used by <code>ViewerFilter.select</code> method
	 * @param viewerFilter
	 *            the ViewerFilter
	 * @param inputCollection
	 *            collection to filter
	 * @return inputCollection, modified copy of inputCollection or null
	 *
	 */
	static WizardCollectionElement filter(Viewer viewer, ViewerFilter viewerFilter,
			WizardCollectionElement inputCollection) {
		AdaptableList wizards = null;
		for (Object child : inputCollection.getWizardAdaptableList().getChildren()) {
			if (viewerFilter.select(viewer, inputCollection, child)) {
				if (wizards == null) {
					wizards = new AdaptableList();
				}
				wizards.add((IAdaptable) child);
			}
		}

		if (wizards == null) {
			if (inputCollection.getChildren().length > 0) {
				return new WizardCollectionElement(inputCollection, new AdaptableList());
			}
			return null;
		}

		if (inputCollection.getWizardAdaptableList().size() == wizards.size()) {
			return inputCollection;
		}
		return new WizardCollectionElement(inputCollection, wizards);
	}
}
