/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.internal;

/**
 * Objects of classes that implement this interface can be registered for
 * certain object type in the IObjectContributorManager. Unlike with extenders,
 * all the matching contributors will be processed in a sequence.
 * <p>
 * By implementing 'isApplicableTo' method, a contributor can tell the manager
 * to skip it if the object is of the desired type, but its other properties do
 * not match additional requirements imposed by the contributor.
 */

public interface IObjectContributor {
	/**
	 * Returns true if this contributor should be considered for the given object.
	 *
	 * @param object the object to text
	 * @return boolean
	 */
	boolean isApplicableTo(Object object);

	/**
	 * Return whether or not the receiver can adapt to IResource.
	 *
	 * @return boolean
	 */
	boolean canAdapt();
}
