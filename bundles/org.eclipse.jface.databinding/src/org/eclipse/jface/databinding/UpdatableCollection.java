package org.eclipse.jface.databinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Class UpdatableCollection.  Provides a base class for clients that wish to
 * implement IUpdatableCollection and permits evolution of the IUpdatableCollection
 * interface over time.
 * 
 * @since 3.2
 */
public abstract class UpdatableCollection extends WritableUpdatable implements IUpdatableCollection {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableCollection#getSize()
	 */
	public final int getSize() {
		UpdatableTracker.getterCalled(this);
		return computeSize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableCollection#getSize()
	 */
	protected abstract int computeSize();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableCollection#addElement(java.lang.Object, int)
	 */
	public abstract int addElement(Object value, int index);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableCollection#removeElement(int)
	 */
	public abstract void removeElement(int index);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableCollection#setElement(int, java.lang.Object)
	 */
	public abstract void setElement(int index, Object value);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableCollection#getElement(int)
	 */
	public final Object getElement(int index) {
		UpdatableTracker.getterCalled(this);
		
		return computeElement(index);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableCollection#getElement(int)
	 */
	protected abstract Object computeElement(int index);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableCollection#getElementType()
	 */
	public abstract Class getElementType();

	public List getElements() {
		List elements = new ArrayList();
		for (int i = 0; i < getSize(); i++) {
			elements.add(getElement(i));
		}
		return elements;
	}

	public void setElements(List elements) {
		while (getSize() > 0) {
			removeElement(0);
		}
		
		for (int i = 0; i < elements.size(); i++) {
			addElement(elements.get(i), i);
		}
	}
	
	
}
