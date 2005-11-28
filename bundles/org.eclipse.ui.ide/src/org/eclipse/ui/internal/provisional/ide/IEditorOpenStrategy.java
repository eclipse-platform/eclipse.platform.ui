package org.eclipse.ui.internal.provisional.ide;

/**
 * A strategy object for opening editors on model elements.
 * 
 * @since 3.2
 */
public interface IEditorOpenStrategy {

	/**
	 * Returns an info object representing the editors that can be used to edit
	 * the given model element, and which one is the default.
	 * 
	 * @param element the model element 
	 * @return the open with info
	 */
	OpenWithInfo getOpenWithInfo(Object element);

}
