/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.update.operations;

import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;

/**
 * An operation that applies to a feature, such as install, uninstall, etc.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @since 3.0
 */
public interface IFeatureOperation extends IOperation {
	/**
	 * Returns the feature to operate on.
	 * @return the feature to operate on.
	 */
	public abstract IFeature getFeature();
	/**
	 * Returns the site in which the operation is applied.
	 * @return the site that owns or will own the feature.
	 */
	public abstract IConfiguredSite getTargetSite();
	/**
	 * Returns the previous version of the feature (if any).
	 * @return the previous installed version of a feature (if any).
	 */
	public abstract IFeature getOldFeature();
	/**
	 * Sets the site in which the feature is being operated on.
	 * @param targetSite the site in which the featre is being operated on.
	 */
	public abstract void setTargetSite(IConfiguredSite targetSite);
}
