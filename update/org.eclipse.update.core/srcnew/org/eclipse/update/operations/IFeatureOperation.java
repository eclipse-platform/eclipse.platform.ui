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
 * IOperation
 */
public interface IFeatureOperation extends IOperation {
	public abstract IFeature getFeature();
	public abstract IConfiguredSite getTargetSite();
	public abstract IInstallConfiguration getInstallConfiguration();
	public abstract IFeature getOldFeature();
	public abstract void setTargetSite(IConfiguredSite targetSite);
	public abstract void setInstallConfiguration(IInstallConfiguration config);
}