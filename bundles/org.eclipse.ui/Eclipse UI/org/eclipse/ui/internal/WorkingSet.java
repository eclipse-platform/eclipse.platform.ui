package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * A working set holds a number of IAdaptable elements. 
 * A working set is intended to group elements for presentation to 
 * the user or for operations on the set of elements.
 * 
 * @see org.eclipse.ui.IWorkingSet
 * @since 2.0
 */
public class WorkingSet implements IAdaptable, IPersistableElement, IWorkingSet {
	private static final String FACTORY_ID = "org.eclipse.ui.internal.WorkingSetFactory";//$NON-NLS-1$
	
	private String name;
	private ArrayList elements;
	private String editPageId;

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
			String objectPageId = workingSet.getEditPageId();
			String pageId = getEditPageId();
			boolean pageIdEqual = (objectPageId == null && pageId == null) || (objectPageId != null && objectPageId.equals(pageId));
			return workingSet.getName().equals(getName()) && workingSet.elements.equals(elements) && pageIdEqual;
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
	 * @see org.eclipse.ui.IWorkingSet#getName()
	 */
	public String getName() {
		return name;
	}
	/**
	 * Returns the id of the working set page that was used to
	 * create the receiver.
	 * 
	 * @return the id of the working set page.
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage
	 */
	public String getEditPageId() {
		return editPageId;
	}
	/** 
	 * Implements IWorkingSet
	 * 
	 * @see org.eclipse.ui.IWorkingSet#getElements()
	 */
	public IAdaptable[] getElements() {
		return (IAdaptable[]) elements.toArray(new IAdaptable[elements.size()]);
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
	 * Returns the hash code.
	 * 
	 * @return the hash code.
	 */
	public int hashCode() {
		int hashCode = name.hashCode() & elements.hashCode();
		
		if (editPageId != null) {
			hashCode &= editPageId.hashCode();
		}
		return hashCode;
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
	/** 
	 * Implements IWorkingSet
	 * 
	 * @see org.eclipse.ui.IWorkingSet#setElements(IAdaptable[])
	 */
	public void setElements(IAdaptable[] newElements) {
		IAdaptable[] oldElements = getElements();
		
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
	 * Sets the id of the working set page that was used to 
	 * create the receiver.
	 * 
	 * @param pageId the id of the working set page.
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage
	 */
	public void setEditPageId(String pageId) {
		editPageId = pageId;
	}
	/** 
	 * Implements IWorkingSet
	 * 
	 * @see org.eclipse.ui.IWorkingSet#setName(String)
	 */
	public void setName(String newName) {
		String oldName = name;

		Assert.isNotNull(newName, "Working set name must not be null"); //$NON-NLS-1$
		name = newName;
		WorkingSetManager workingSetManager = (WorkingSetManager) WorkbenchPlugin.getDefault().getWorkingSetManager();	
		workingSetManager.workingSetChanged(this, IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE);
	}
}