package org.eclipse.jface.binding;

import java.util.Map;

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
	 * @param properties
	 *            a mapping from context-defined properties to values, e.g. for
	 *            passing policies to the factory.
	 * @param description
	 * @return an updatable
	 * @throws BindingException
	 */
	IUpdatable createUpdatable(Map properties, Object description)
			throws BindingException;
}
