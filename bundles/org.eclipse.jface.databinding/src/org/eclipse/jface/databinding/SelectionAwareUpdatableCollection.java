package org.eclipse.jface.databinding;

import java.util.List;


/**
 * Class SelectionAwareUpdatableCollection.  Base class for implementing
 * SelectionAwareUpdatableCollections.
 * 
 * @since 3.2
 */
public abstract class SelectionAwareUpdatableCollection extends UpdatableCollection {
	/**
	 * Method getSelectedObject.  Returns the object that has the current
	 * selection in the underlying collection.  If there is no selection null is returned.
	 * This method is not intended to be called directly by clients. 
	 * @return the selected object or null if nothing is selected
	 */
	public abstract Object getSelectedObject();
	
	/**
	 * Method setSelectedObject.  Sets the selected object in the underlying
	 * collection.  If the selected object isn't in the underlying collection, then
	 * the collection's actual selection will not be changed.  If object is
	 * null, then the collection's selection will be removed. This method is not intended
	 * to be called directly by clients.
	 * 
	 * @param object The new selection.
	 */
	public abstract void setSelectedObject(Object object);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableCollection#setElements(java.util.List)
	 */
	public void setElements(List elements) {
		Object selectedObject = getSelectedObject();		
		super.setElements(elements);
		if (selectedObject != null) {
			boolean found = false;
			for (int i = 0; i < elements.size(); i++) {
				Object object = elements.get(i);
				if (selectedObject.equals(object)) {
					found = true;
					selectedObject = object;
					break;
				}
			}
			if (found) {
				setSelectedObject(selectedObject);
			} else {
				setSelectedObject(null);
			}
		}
	}
	
	
}
