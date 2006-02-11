package org.eclipse.jface.internal.databinding.api.observable.list;

public class ListDiff implements IListDiff {

	private IListDiffEntry[] differences;

	/**
	 * @param differences
	 */
	public ListDiff(IListDiffEntry difference) {
		this.differences = new IListDiffEntry[] { difference };
	}

	/**
	 * @param differences
	 */
	public ListDiff(IListDiffEntry difference1, IListDiffEntry difference2) {
		this.differences = new IListDiffEntry[] { difference1, difference2 };
	}

	/**
	 * @param differences
	 */
	public ListDiff(IListDiffEntry[] differences) {
		this.differences = differences;
	}

	public IListDiffEntry[] getDifferences() {
		return differences;
	}

}
