/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding;

import java.util.Map;

/**
 * A factory for creating updatable objects from description objects.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 */
public interface IUpdatableFactory {

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
	 */
	IUpdatable createUpdatable(Map properties, Object description, IDataBindingContext bindingContext);
}
