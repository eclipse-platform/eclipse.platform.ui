/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.model;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Instances of this interface represent a contribution within the workbench.
 * This interface is typically implemented by the workbench itself although
 * extension developers may wish to implement it as well for their own
 * extensions.
 *
 * @since 3.4
 */
public interface IComparableContribution extends IAdaptable {

	/**
	 * The default priority (0);
	 */
	int PRIORITY_DEFAULT = 0;

	/**
	 * Return the priority of this contribution. Lower values constitute higher
	 * priorities.
	 *
	 * @return the priority
	 */
	int getPriority();

	/**
	 * Return the human readable label for this contribution. Must not be
	 * <code>null</code>.
	 *
	 * @return the label for this contribution
	 */
	String getLabel();

	/**
	 * Possible adaptations include:
	 * <dl>
	 * <dt>{@link org.eclipse.core.runtime.IConfigurationElement}</dt>
	 * <dd>useful for contributions that eventually work back to registry elements.
	 * The majority of {@link IComparableContribution} instances provided by the
	 * platform will provide this adapter.</dd>
	 * </dl>
	 */
	@Override
	<T> T getAdapter(Class<T> adapter);

}
