/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui;

import java.util.Map;

/**
 * <p>
 * A listener to changes in a particular source of information. This listener is
 * notified as the source changes. Typically, workbench services will implement
 * this interface, and register themselves as listeners to the
 * <code>ISourceProvider</code> instances that are registered with them.
 * </p>
 * 
 * @since 3.1
 * @see org.eclipse.ui.ISources
 * @see org.eclipse.ui.ISourceProvider
 */
public interface ISourceProviderListener {

	/**
	 * Handles a change to multiple sources. The source priority should be a bit
	 * mask indicating the sources. The map will be used to construct the
	 * variables on an <code>IEvaluationContext</code>
	 * 
	 * @param sourcePriority
	 *            A bit mask of all the source priorities that have changed.
	 * @param sourceValuesByName
	 *            A mapping of the source names (<code>String</code>) to the
	 *            source values (<code>Object</code>). The names should
	 *            never be <code>null</code>, but the values may be. The map
	 *            must not be <code>null</code>, and should contain at least
	 *            two elements (one for each source).
	 * @see org.eclipse.core.expressions.IEvaluationContext
	 * @see ISources
	 */
	public void sourceChanged(final int sourcePriority,
			final Map sourceValuesByName);

	/**
	 * Handles a change to one source. The source priority should indicate the
	 * source, and the name-value pair will be used to create an
	 * <code>IEvaluationContext</code> with a single variable.
	 * 
	 * @param sourcePriority
	 *            A bit mask of all the source priorities that have changed.
	 * @param sourceName
	 *            The name of the source that changed; must not be
	 *            <code>null</code>.
	 * @param sourceValue
	 *            The new value for that source; may be <code>null</code>.
	 * @see org.eclipse.core.expressions.IEvaluationContext
	 * @see ISources
	 */
	public void sourceChanged(final int sourcePriority,
			final String sourceName, final Object sourceValue);
}
