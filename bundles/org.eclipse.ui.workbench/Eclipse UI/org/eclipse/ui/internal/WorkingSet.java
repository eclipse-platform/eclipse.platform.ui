/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;

/**
 * A working set holds a number of IAdaptable elements. 
 * A working set is intended to group elements for presentation to 
 * the user or for operations on a set of elements.
 * 
 * @see org.eclipse.ui.IWorkingSet
 * @since 2.0
 */
public class WorkingSet implements IAdaptable, IPersistableElement, IWorkingSet {
	private static final String FACTORY_ID = "org.eclipse.ui.internal.WorkingSetFactory";//$NON-NLS-1$
	
	private String name;
	private ArrayList elements;
	private String editPageId;

	private IMemento workingSetMemento;
	
	/**
	 * Creates a new working set
	 * 
	 * @param name the name of the new working set. Should not have 
	 * 	leading or trailing whitespace.
	 * @param element the content of the new working set. 
	 * 	May be empty but not null.
	 */
	public WorkingSet(String name, IAdaptable[] elements) {
		Assert.isNotNull(name, "name must not be null"); //$NON-NLS-1$
		this.name = name;
		internalSetElements(elements);
	}
	/**
	 * Creates a new working set
	 * 
	 * @param name the name of the new working set. Should not have 
	 * 	leading or trailing whitespace.
	 * @param memento persistence memento containing the elements of  
	 * 	the working set.
	 */
	WorkingSet(String name, IMemento memento) {
		Assert.isNotNull(name, "name must not be null"); //$NON-NLS-1$
		this.name = name;
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
			String objectPageId = workingSet.getId();
			String pageId = getId();
			boolean pageIdEqual = (objectPageId == null && pageId == null) || (objectPageId != null && objectPageId.equals(pageId));
			return workingSet.getName().equals(getName()) && workingSet.getElementsArray().equals(getElementsArray()) && pageIdEqual;
		}
		return false;
	}
	/**
	 * Returns the receiver if the requested type is either IWorkingSet 
	 * or IPersistableElement.
	 * 
	 * @param adapter the requested type
	 * @return the receiver if the requested type is either IWorkingSet 
	 * 	or IPersistableElement.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkingSet.class || adapter == IPersistableElement.class) {
			return this;
		}
		return null;
	}
	/** 
	 * Implements IWorkingSet
	 * 
	 * @see org.eclipse.ui.IWorkingSet#getElements()
	 */
	public IAdaptable[] getElements() {
		ArrayList elements = getElementsArray();
		
		return (IAdaptable[]) elements.toArray(new IAdaptable[elements.size()]);
	}
	/**
	 * Returns the elements array list. Lazily restores the elements from
	 * persistence memento. 
	 * 
	 * @return the elements array list
	 */
	private ArrayList getElementsArray() {
		if (elements == null) {
			restoreWorkingSet();
			workingSetMemento = null;
		}
		return elements;
	}
	/**
	 * Implements IPersistableElement
	 * 
	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
	 */
	public String getFactoryId() {
		return FACTORY_ID;
	}
	/**
	 * Implements IWorkingSet
	 * 
	 * @see org.eclipse.ui.IWorkingSet#getId()
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage
	 * @since 2.1
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
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		WorkingSetDescriptor descriptor = null;
		
		if (editPageId != null) {
			descriptor = registry.getWorkingSetDescriptor(editPageId);
		}		
		if (descriptor == null) {
			return null;
		}					
		return descriptor.getIcon();
	}
	/** 
	 * Implements IWorkingSet
	 * 
	 * @see org.eclipse.ui.IWorkingSet#getName()
	 */
	public String getName() {
		return name;
	}
	/**
	 * Returns the hash code.
	 * 
	 * @return the hash code.
	 */
	public int hashCode() {
		int hashCode = name.hashCode() & getElementsArray().hashCode();
		
		if (editPageId != null) {
			hashCode &= editPageId.hashCode();
		}
		return hashCode;
	}
	/**
	 * Recreates the working set elements from the persistence memento.
	 */
	private void restoreWorkingSet() {
		IMemento[] itemMementos = workingSetMemento.getChildren(IWorkbenchConstants.TAG_ITEM);
		Set items = new HashSet();
		for (int i = 0; i < itemMementos.length; i++) {
			IMemento itemMemento = itemMementos[i];
			String factoryID = itemMemento.getString(IWorkbenchConstants.TAG_FACTORY_ID);

			if (factoryID == null) {
				WorkbenchPlugin.log("Unable to restore working set item - no factory ID."); //$NON-NLS-1$
				continue;
			}
			IElementFactory factory = WorkbenchPlugin.getDefault().getElementFactory(factoryID);
			if (factory == null) {
				WorkbenchPlugin.log("Unable to restore working set item - cannot instantiate factory: " + factoryID); //$NON-NLS-1$
				continue;
			}
			IAdaptable item = factory.createElement(itemMemento);
			if (item == null) {
				WorkbenchPlugin.log("Unable to restore working set item - cannot instantiate item: " + factoryID); //$NON-NLS-1$
				continue;
			}
			items.add(item);
		}
		internalSetElements((IAdaptable[]) items.toArray(new IAdaptable[items.size()]));
	}
	/**
	 * Implements IPersistableElement.
	 * Persist the working set name and working set contents. 
	 * The contents has to be either IPersistableElements or provide 
	 * adapters for it to be persistet.
	 * 
	 * @see org.eclipse.ui.IPersistableElement#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
		if (workingSetMemento != null) {
			// just re-save the previous memento if the working set has 
			// not been restored
			memento.putMemento(workingSetMemento);
		}
		else {
			memento.putString(IWorkbenchConstants.TAG_NAME, name);
			memento.putString(IWorkbenchConstants.TAG_EDIT_PAGE_ID, editPageId);
			Iterator iterator = elements.iterator();
			while (iterator.hasNext()) {
				IAdaptable adaptable = (IAdaptable) iterator.next();
				IPersistableElement persistable = (IPersistableElement) adaptable.getAdapter(IPersistableElement.class);
				if (persistable != null) {
					IMemento itemMemento = memento.createChild(IWorkbenchConstants.TAG_ITEM);
					
					itemMemento.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
					persistable.saveState(itemMemento);
				}	
			}
		}
	}
	/** 
	 * Implements IWorkingSet
	 * 
	 * @see org.eclipse.ui.IWorkingSet#setElements(IAdaptable[])
	 */
	public void setElements(IAdaptable[] newElements) {
		internalSetElements(newElements);
		WorkingSetManager workingSetManager = (WorkingSetManager) WorkbenchPlugin.getDefault().getWorkingSetManager();	
		workingSetManager.workingSetChanged(this, IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE);
	}
	/**
	 * Create a copy of the elements to store in the receiver.
	 * 
	 * @param elements the elements to store a copy of in the 
	 * 	receiver.
	 */
	private void internalSetElements(IAdaptable[] newElements) {
		Assert.isNotNull(newElements, "Working set elements array must not be null"); //$NON-NLS-1$
		
		elements = new ArrayList(newElements.length);
		for (int i = 0; i < newElements.length; i++) {
			elements.add(newElements[i]);
		}
	}
	/**
	 * Implements IWorkingSet
	 * 
	 * @see org.eclipse.ui.IWorkingSet#setId(String)
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage
	 * @since 2.1
	 */
	public void setId(String pageId) {
		editPageId = pageId;
	}
	/** 
	 * Implements IWorkingSet
	 * 
	 * @see org.eclipse.ui.IWorkingSet#setName(String)
	 */
	public void setName(String newName) {

		Assert.isNotNull(newName, "Working set name must not be null"); //$NON-NLS-1$
		name = newName;
		WorkingSetManager workingSetManager = (WorkingSetManager) WorkbenchPlugin.getDefault().getWorkingSetManager();	
		workingSetManager.workingSetChanged(this, IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE);
	}
}