/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.debug.core.model;

/**
 * Provides the ability to filter out steps based on some object. Associated
 * with a step filter extension.
 *
 * <p>
 * The following is an example of a step filter extension:
 *
 * <pre>
 *  &lt;extension point=&quot;org.eclipse.debug.core.stepFilters&quot;&gt;
 *   &lt;stepFilters
 *    class=&quot;com.example.ExampleStepFilters&quot;
 *    modelIdentifier=&quot;com.example.debug.model&quot;&gt;
 *   &lt;/stepFilters&gt;
 * &lt;/extension&gt;
 * </pre>
 *
 * </p>
 * In the example above, the specified step filter will be used for the
 * <code>com.example.debug.model</code> debug model. </p>
 *
 * <p>
 * Clients contributing step filters must implement this interface.
 * </p>
 *
 * @since 3.10
 * @see org.eclipse.debug.core.model.IStep
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IStepFilter {

	/**
	 * Returns whether the step for the given object should be filtered.
	 *
	 * @param object the object to filter
	 * @return whether the step for the given object should be filtered.
	 */
	public boolean isFiltered(Object object);

}
