package org.eclipse.e4.workbench.ui.behaviors;

import org.eclipse.core.runtime.Platform;

/**
 * Interface implemented by classes who want to receive an input.
 * <p>
 * If workbench parts implement this interface their input is renewed when the
 * workbench selection changes. If the selection is not an instance of the type
 * defined by {@link #getInputType()} and could not be adapted to this type
 * using {@link Platform#getAdapterManager()} <code>null</code> is passed.
 * </p>
 */
public interface IHasInput {
	/**
	 * The input type accepted.
	 * 
	 * @return the input type to pass in
	 */
	public Class getInputType();

	/**
	 * Updates the input of the instance
	 * 
	 * @param input
	 *            the new input or <code>null</code>
	 */
	public void setInput(Object input);
}
