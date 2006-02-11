package org.eclipse.jface.internal.databinding.api.observable.list;

public class ListDiffEntry implements IListDiffEntry {

	private int position;
	private boolean isAddition;
	private Object element;

	/**
	 * @param position
	 * @param isAddition
	 * @param element
	 */
	public ListDiffEntry(int position, boolean isAddition, Object element) {
		this.position = position;
		this.isAddition = isAddition;
		this.element = element;
	}

	public int getPosition() {
		return position;
	}

	public boolean isAddition() {
		return isAddition;
	}

	public Object getElement() {
		return element;
	}

}
