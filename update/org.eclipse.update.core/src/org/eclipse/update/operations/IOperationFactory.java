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
 * A factory interface for creating operations.
 */
public interface IOperationFactory {
	/**
	 * Creates a "configure feature" operation.
	 * @param config
	 * @param targetSite
	 * @param feature
	 * @return
	 */
	public IConfigFeatureOperation createConfigOperation(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeature feature);
		
	/**
	 * Creates an "unconfigure feature" operation.
	 * @param config
	 * @param targetSite
	 * @param feature
	 * @return
	 */
	public IUnconfigFeatureOperation createUnconfigOperation(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeature feature);
	
	/**
	 * Creates a "intall feature" operation.
	 * @param config
	 * @param targetSite
	 * @param feature
	 * @param optionalFeatures
	 * @param unconfiguredOptionalFeatures
	 * @param verifier
	 * @return
	 */
	public IInstallFeatureOperation createInstallOperation(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeature feature,
		IFeatureReference[] optionalFeatures,
		IFeature[] unconfiguredOptionalFeatures,
		IVerificationListener verifier);
		
	/**
	 * Creates an "uninstall feature" operation.
	 * @param config
	 * @param targetSite
	 * @param feature
	 * @return
	 */
	public IUninstallFeatureOperation createUninstallOperation(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeature feature);
		
	/**
	 * Creates a "batch install" operation.
	 * @param operations
	 * @return
	 */
	public IBatchOperation createBatchInstallOperation(IInstallFeatureOperation[] operations);
	
	/**
	 * Creates an "enable/disable site" operation.
	 * @param site
	 * @return
	 */
	public IToggleSiteOperation createToggleSiteOperation(
		IConfiguredSite site);
		
	/**
	 * Creates a "revert to a previous configuration" operation.
	 * @param config
	 * @param problemHandler
	 * @return
	 */
	public IRevertConfigurationOperation createRevertConfigurationOperation(
		IInstallConfiguration config,
		IProblemHandler problemHandler);
}