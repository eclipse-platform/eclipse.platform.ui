/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;

/**
 * <p>
 * Interface that describes a mechanism that may be provided by working set
 * extensions to help manage the addition of elements to working sets. Instances
 * of this class are capable of transforming possible working set content into
 * the most applicable form.
 * </p>
 * 
 * <p>
 * Usage of this interface is achieved via the <code>elementAdapterClass</code>
 * attribute of the <code>org.eclipse.ui.workingSets</code> extension point.
 * Usage of this interface in <code>org.eclipse.ui.workingSets</code>
 * extensions is optional.
 * </p>
 * 
 * @since 3.3
 */
public interface IWorkingSetElementAdapter {

	/**
	 * Converts the given elements for addition to/removal from the working set.
	 * 
	 * @param ws
	 *            the target working set that elements should be adapted for
	 * @param elements
	 *            the elements to adapt
	 * @return the (possibly adapted) elements to add to/remove from the working
	 *         set
	 */
	IAdaptable[] adaptElements(IWorkingSet ws, IAdaptable[] elements);

	/**
	 * Disposes of this element adaptor.
	 */
	void dispose();
}

