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
	private IAdaptable[] elements;
	private String editPageId;
	private ListenerList propertyChangeListeners = new ListenerList();

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
	 * Implements IWorkingSet
	 * 
	 * @see org.eclipse.ui.IWorkingSet#addPropertyChangeListener(IPropertyChangeListener)
	 * @deprecated use IWorkingSetManager.addPropertyChangeListener instead.
	 *	newValue of the PropertyChangeEvent will be the changed working set.
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}
	/**
	 * Tests the receiver and the object for equality
	 * 
	 * @param object object to compare the receiver to
	 * @return true=the object equals the receiver, the name is the same.
	 * 	false otherwise
	 */
	public boolean equals(Object object) {
		return (object instanceof IWorkingSet) && ((IWorkingSet) object).getName().equals(getName());
	}
	/**
	 * Notify property change listeners about a working set change.
	 * 
	 * @param changeId one of IWorkingSet.CHANGE_WORKING_SET_CONTENT_CHANGE and
	 * 	IWorkingSet.CHANGE_WORKING_SET_NAME_CHANGE 
	 * @param oldValue the old property value
	 * @param newValue the new property value 
	 */
	private void firePropertyChange(String changeId, Object oldValue, Object newValue) {
		final PropertyChangeEvent event = new PropertyChangeEvent(this, changeId, oldValue, newValue);

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				Object[] listeners = propertyChangeListeners.getListeners();		
				for (int i = 0; i < listeners.length; i++) {
					((IPropertyChangeListener) listeners[i]).propertyChange(event);
				}
			}
		});
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
		IAdaptable[] result = new IAdaptable[elements.length];
		System.arraycopy(elements, 0, result, 0, elements.length);

		return result;
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
		return name.hashCode() & elements.hashCode();
	}
	/** 
	 * Implements IWorkingSet
	 * 
	 * @see org.eclipse.ui.IWorkingSet#removePropertyChangeListener(IPropertyChangeListener)
	 * @deprecated use IWorkingSetManager.removePropertyChangeListener instead.
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
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
		for (int i = 0; i < elements.length; i++) {
			IPersistableElement persistable = (IPersistableElement) elements[i].getAdapter(IPersistableElement.class);
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
		IAdaptable[] oldElements = elements;
		
		internalSetElements(newElements);
		WorkingSetManager workingSetManager = (WorkingSetManager) WorkbenchPlugin.getDefault().getWorkingSetManager();	
		workingSetManager.workingSetChanged(this, IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE);
		// deprecated event notification		
		firePropertyChange(CHANGE_WORKING_SET_CONTENT_CHANGE, oldElements, newElements);
	}
	/**
	 * Create a copy of the elements to store in the receiver.
	 * 
	 * @param elements the elements to store a copy of in the 
	 * 	receiver.
	 */
	private void internalSetElements(IAdaptable[] elements) {
		Assert.isNotNull(elements, "Working set elements array must not be null"); //$NON-NLS-1$
		
		this.elements = new IAdaptable[elements.length];
		System.arraycopy(elements, 0, this.elements, 0, elements.length);
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
		// deprecated event notification		
		firePropertyChange(CHANGE_WORKING_SET_NAME_CHANGE, oldName, newName);
	}
}