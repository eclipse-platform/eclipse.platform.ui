package org.eclipse.jface.databinding;

/**
 * Class UpdatableCollection.  Provides a base class for clients that wish to
 * implement IUpdatableCollection and permits evolution of the IUpdatableCollection
 * interface over time.
 * 
 * @since 3.2
 */
public abstract class UpdatableCollection extends Updatable implements IUpdatableCollection {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableCollection#getSize()
	 */
	public abstract int getSize();

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
	public abstract Object getElement(int index);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableCollection#getElementType()
	 */
	public abstract Class getElementType();
}
