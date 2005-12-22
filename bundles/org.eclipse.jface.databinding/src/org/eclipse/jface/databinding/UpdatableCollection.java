package org.eclipse.jface.databinding;

/**
 * @since 3.2
 */
public abstract class UpdatableCollection extends Updatable implements IUpdatableCollection {

	public abstract int getSize();

	public abstract int addElement(Object value, int index);

	public abstract void removeElement(int index);

	public abstract void setElement(int index, Object value);

	public abstract Object getElement(int index);

	public abstract Class getElementType();
}
