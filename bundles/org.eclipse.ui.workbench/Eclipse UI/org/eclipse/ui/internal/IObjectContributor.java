/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;


/**
 * Objects of classes that implement this interface
 * can be registered for certain object type
 * in the IObjectContributorManager. Unlike with extenders,
 * all the matching contributors will be processed
 * in a sequence.
 * <p>By implementing 'isApplicableTo' method,
 * a contributor can tell the manager to skip it
 * if the object is of the desired type, but its
 * other properties do not match additional
 * requirements imposed by the contributor.
 *
 * @see IObjectContributorManager
 */

public interface IObjectContributor {
/**
 * Returns true if this contributor should be considered
 * for the given object.
 */
public boolean isApplicableTo(Object object);

/**
 * Return whether or not the receiver can adapt to IResource.
 */
public boolean canAdapt();
}
