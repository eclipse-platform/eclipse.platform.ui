/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.model.WorkbenchWorkingSet;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class WorkingSet implements IWorkingSet, IAdaptable, IPersistableElement {
	private static final String FACTORY_ID = "org.eclipse.ui.internal.WorkingSetFactory";//$NON-NLS-1$
	
	String name;
	Set items; // of IAdaptable
	private ListenerList propertyChangeListeners = new ListenerList();

	public WorkingSet(String name, IAdaptable[] elements) {
		Assert.isNotNull(name, "name must not be null"); //$NON-NLS-1$
		this.name = name;
		setItems(elements, true);
	}
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}
	public boolean equals(Object o) {
		return (o instanceof IWorkingSet) && ((IWorkingSet) o).getName().equals(getName());
	}

	private void firePropertyChange(String changeId, Object oldValue, Object newValue) {
		Object[] listeners = propertyChangeListeners.getListeners();
		PropertyChangeEvent event = new PropertyChangeEvent(this, changeId, oldValue, newValue);

		for (int i = 0; i < listeners.length; i++) {
			((IPropertyChangeListener) listeners[i]).propertyChange(event);
		}
	}
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {		
			return new WorkbenchWorkingSet(null, (IAdaptable[]) items.toArray(new IAdaptable[items.size()]), name);
		}
		return null;
	}
	/*
	 * @see IWorkingSet#getName()
	 */
	public String getName() {
		return name;
	}
	/*
	 * @see IWorkingSet#getItems()
	 */
	public IAdaptable[] getItems() {
		return (IAdaptable[]) items.toArray(new IAdaptable[items.size()]);
	}
	public String getFactoryId() {
		return FACTORY_ID;
	}
	public int hashCode() {
		return name.hashCode();
	}
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}
	public void saveState(IMemento memento) {
		Iterator iterator = items.iterator();
		
		memento.putString(IWorkbenchConstants.TAG_NAME, name);
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
	/*
	 * Public for use by org.eclipse.ui.internal.dialogs.WorkingSetDialog.
	 */
	public void setItems(IAdaptable[] elements) {
		setItems(elements, false);
		firePropertyChange(CHANGE_WORKING_SET_CONTENT_CHANGE, this, this);		
	}
	private void setItems(IAdaptable[] elements, boolean internal) {
		Assert.isNotNull(elements, "IPath array must not be null"); //$NON-NLS-1$
		items = new HashSet(elements.length);
		for (int i = 0; i < elements.length; i++) {
			Assert.isTrue(!items.contains(elements[i]), "elements must only contain each element once"); //$NON-NLS-1$
			items.add(elements[i]);
		}
	}
	/*
	 * Public for use by org.eclipse.ui.internal.dialogs.WorkingSetDialog.
	 */
	public void setName(String name) {
		Assert.isNotNull(name, "name must not be null"); //$NON-NLS-1$
		this.name = name;
		firePropertyChange(CHANGE_WORKING_SET_NAME_CHANGE, this, this);
	}
	//--- Persistency -----------------------------------------------


}