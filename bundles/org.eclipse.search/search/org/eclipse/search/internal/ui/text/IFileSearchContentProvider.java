/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

public interface IFileSearchContentProvider {

	public abstract void elementsChanged(Object[] updatedElements);

	public abstract void clear();

	/**
	 * @param parentElement
	 *            parent element or input
	 * @return number of leaf elements in the tree maintained by the provider
	 */
	public abstract int getLeafCount(Object parentElement);

}