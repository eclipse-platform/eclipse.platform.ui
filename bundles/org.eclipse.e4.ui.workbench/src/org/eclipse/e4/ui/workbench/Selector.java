/*******************************************************************************
 * Copyright (c) 2014, 2015 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench;

import org.eclipse.e4.ui.model.application.MApplicationElement;

/**
 * Selector to find element
 *
 * @since 1.1
 */
public interface Selector {
	/**
	 * Call for each element to find matching elements
	 *
	 * @param element
	 *            the element
	 * @return <code>true</code> if matches else <code>false</code>
	 */
	public boolean select(MApplicationElement element);
}