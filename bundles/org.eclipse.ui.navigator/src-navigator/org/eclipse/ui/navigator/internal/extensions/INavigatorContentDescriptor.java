package org.eclipse.ui.navigator.internal.extensions;

public interface INavigatorContentDescriptor {

	/**
	 * Returns the navgiator content extension id
	 * 
	 * @return the navgiator content extension id
	 */
	public abstract String getId();

	/**
	 * Returns the name of this navigator extension
	 * 
	 * @return the name of this navigator extension
	 */
	public abstract String getName();

	/**
	 * Returns the priority of the navigator content extension.
	 * 
	 * @return the priority of the navigator content extension. Returns 0 (zero) if no priority was
	 *         specified.
	 */
	public abstract int getPriority();

	/**
	 * Returns whether the receiver is a root navigator content extension. Navigator content
	 * extensions are root extensions if they are referenced in a navigator view extension.
	 * 
	 * @return true if the receiver is a root navigator extension false if the receiver is not a
	 *         root navigator extension
	 */
	public abstract boolean isRoot();

	/**
	 * @return
	 */
	public abstract boolean isEnabledByDefault();

	public abstract boolean hasLoadingFailed();

}