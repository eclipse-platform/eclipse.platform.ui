/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.internal.util.Util;

/**
 * A working set holds a number of IAdaptable elements. 
 * A working set is intended to group elements for presentation to 
 * the user or for operations on a set of elements.
 * 
 * @see org.eclipse.ui.IWorkingSet
 * @since 2.0
 */
public class WorkingSet extends AbstractWorkingSet {
    private String editPageId;
    
    /**
     * Creates a new working set.
     * 
     * @param name the name of the new working set. Should not have 
     * 	leading or trailing whitespace.
     * @param uniqueId the unique id
     * @param element the content of the new working set. 
     * 	May be empty but not <code>null</code>.
     */
    public WorkingSet(String name, String uniqueId, IAdaptable[] elements) {
        super(name, uniqueId);
        internalSetElements(elements);
    }

    /**
     * Creates a new working set from a memento.
     * 
     * @param name the name of the new working set. Should not have 
     * 	leading or trailing whitespace.
     * @param memento persistence memento containing the elements of  
     * 	the working set.
     */
    WorkingSet(String name, String label, IMemento memento) {
        super(name, label);
        workingSetMemento = memento;
    }

    /**
     * Tests the receiver and the object for equality
     * 
     * @param object object to compare the receiver to
     * @return true=the object equals the receiver, the name is the same.
     * 	false otherwise
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof WorkingSet) {
            WorkingSet workingSet = (WorkingSet) object;
            return Util.equals(workingSet.getLabel(), getLabel())
                    && Util.equals(workingSet.getElementsArray(), getElementsArray())
                    && Util.equals(workingSet.getId(), getId());
        }
        return false;
    }

    /**
	 * {@inheritDoc}
	 */
	public boolean isEditable() {
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		String id= getId();
		if (id == null)
			return false;
		WorkingSetDescriptor descriptor= registry.getWorkingSetDescriptor(id);
		if (descriptor == null)
			return false;
		return descriptor.isEditable();
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSet
     */
    public String getId() {
        return editPageId;
    }

    /**
     * Returns the working set icon.
     * Currently, this is one of the icons specified in the extensions 
     * of the org.eclipse.ui.workingSets extension point. 
     * The extension is identified using the value returned by
     * <code>getId()</code>. 
     * Returns <code>null</code> if no icon has been specified in the 
     * extension or if <code>getId()</code> returns <code>null</code>. 
     * 
     * @return the working set icon or <code>null</code>.
     * @since 2.1 
     */
    public ImageDescriptor getImage() {
        WorkingSetRegistry registry = WorkbenchPlugin.getDefault()
                .getWorkingSetRegistry();
        WorkingSetDescriptor descriptor = null;

        if (editPageId == null)
			editPageId = "org.eclipse.ui.resourceWorkingSetPage"; //$NON-NLS-1$

		descriptor = registry.getWorkingSetDescriptor(editPageId);
		if (descriptor == null) {
			return null;
		}
        return descriptor.getIcon();
    }

    /**
     * Returns the hash code.
     * 
     * @return the hash code.
     */
    public int hashCode() {
        int hashCode = getName().hashCode() & getElementsArray().hashCode();

        if (editPageId != null) {
            hashCode &= editPageId.hashCode();
        }
        return hashCode;
    }

    /**
     * Recreates the working set elements from the persistence memento.
     */
    void restoreWorkingSet() {
        IMemento[] itemMementos = workingSetMemento
                .getChildren(IWorkbenchConstants.TAG_ITEM);
        Set items = new HashSet();
        for (int i = 0; i < itemMementos.length; i++) {
            IMemento itemMemento = itemMementos[i];
            String factoryID = itemMemento
                    .getString(IWorkbenchConstants.TAG_FACTORY_ID);

            if (factoryID == null) {
                WorkbenchPlugin
                        .log("Unable to restore working set item - no factory ID."); //$NON-NLS-1$
                continue;
            }
            IElementFactory factory = PlatformUI.getWorkbench()
                    .getElementFactory(factoryID);
            if (factory == null) {
                WorkbenchPlugin
                        .log("Unable to restore working set item - cannot instantiate factory: " + factoryID); //$NON-NLS-1$
                continue;
            }
            IAdaptable item = factory.createElement(itemMemento);
            if (item == null) {
                WorkbenchPlugin
                        .log("Unable to restore working set item - cannot instantiate item: " + factoryID); //$NON-NLS-1$
                continue;
            }
            items.add(item);
        }
        internalSetElements((IAdaptable[]) items.toArray(new IAdaptable[items
                .size()]));
    }

    /**
     * Implements IPersistableElement.
     * Persist the working set name and working set contents. 
     * The contents has to be either IPersistableElements or provide 
     * adapters for it to be persistent.
     * 
     * @see org.eclipse.ui.IPersistableElement#saveState(IMemento)
     */
    public void saveState(IMemento memento) {
        if (workingSetMemento != null) {
            // just re-save the previous memento if the working set has 
            // not been restored
            memento.putMemento(workingSetMemento);
        } else {
            memento.putString(IWorkbenchConstants.TAG_NAME, getName());
            memento.putString(IWorkbenchConstants.TAG_LABEL, getLabel());
            memento.putString(IWorkbenchConstants.TAG_EDIT_PAGE_ID, editPageId);
            Iterator iterator = elements.iterator();
            while (iterator.hasNext()) {
                IAdaptable adaptable = (IAdaptable) iterator.next();
                IPersistableElement persistable = (IPersistableElement) adaptable
                        .getAdapter(IPersistableElement.class);
                if (persistable != null) {
                    IMemento itemMemento = memento
                            .createChild(IWorkbenchConstants.TAG_ITEM);

                    itemMemento.putString(IWorkbenchConstants.TAG_FACTORY_ID,
                            persistable.getFactoryId());
                    persistable.saveState(itemMemento);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSet
     */
    public void setElements(IAdaptable[] newElements) {
        internalSetElements(newElements);
        fireWorkingSetChanged(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE, null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSet
     */
    public void setId(String pageId) {
        editPageId = pageId;
    }

	public boolean isVisible() {
		return true;
	}
	
	public boolean isSelfUpdating() {
		if (editPageId == null)
			return false;
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		WorkingSetDescriptor descriptor= registry.getWorkingSetDescriptor(editPageId);
		return descriptor != null && descriptor.getUpdaterClassName() != null;
	}
	
	public boolean isAggregateWorkingSet() {
		return false;
	}
}
