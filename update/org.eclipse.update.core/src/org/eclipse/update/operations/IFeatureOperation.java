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

package org.eclipse.update.operations;

import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;

/**
 * An operation that applies to a feature, such as install, uninstall, etc.
 */
public interface IFeatureOperation extends IOperation {
	/**
	 * Return the feature to which the operation applies.
	 * @return
	 */
	public abstract IFeature getFeature();
	/**
	 * Returns the site in which the operation is applied.
	 * @return
	 */
	public abstract IConfiguredSite getTargetSite();
	/**
	 * Returns the installation configuration in which the operation takes place.
	 * @return
	 */
	public abstract IInstallConfiguration getInstallConfiguration();
	/**
	 * Returns the previous version of the feature (if any).
	 * @return
	 */
	public abstract IFeature getOldFeature();
	/**
	 * Sets the site in which the feature is being operated on.
	 * @param targetSite
	 */
	public abstract void setTargetSite(IConfiguredSite targetSite);
	/**
	 * Sets the installation configuration in which the operation takes place.
	 * @param config
	 */
	public abstract void setInstallConfiguration(IInstallConfiguration config);
}