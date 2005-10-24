package org.eclipse.jface.binding;

/**
 * @since 3.2
 *
 */
public interface IUpdatableFactory2 {

	/**
	 * Returns an updatable for the given description, or null if this factory
	 * cannot create updatables for this description. The BindingException is
	 * only thrown in error cases, e.g. if the description itself is invalid, or
	 * if an error occurred during the creation of the updatable.
	 * 
	 * @param description
	 * @return an updatable
	 * @throws BindingException
	 */
	IUpdatable createUpdatable(Object description) throws BindingException;
}
