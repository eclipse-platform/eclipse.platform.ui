package org.eclipse.jface.databinding;

/**
 * 
 * 
 * @since 3.2
 */
public interface ITreeProvider {
	/**
	 * Creates a new IReadableSet representing the children of the given element. The caller
	 * is responsible for disposing the set when it is done with it.
	 * 
	 * @param element parent element to query
	 * @return a new IReadableSet representing the children of the given parent node
	 */
	IReadableSet createChildList(Object element);
}
