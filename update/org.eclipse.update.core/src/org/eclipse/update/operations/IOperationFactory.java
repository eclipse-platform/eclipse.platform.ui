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
 * A factory interface for creating operations.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @since 3.0
 */
public interface IOperationFactory {
	/**
	 * Creates a "configure feature" operation.
	 * @param targetSite
	 * @param feature
	 * @return
	 */
	public IConfigFeatureOperation createConfigOperation(
		IConfiguredSite targetSite,
		IFeature feature);
		
	/**
	 * Creates an "unconfigure feature" operation.
	 * @param targetSite
	 * @param feature
	 * @return
	 */
	public IUnconfigFeatureOperation createUnconfigOperation(
		IConfiguredSite targetSite,
		IFeature feature);
	
	/**
	 * Creates a "intall feature" operation.
	 * @param targetSite
	 * @param feature
	 * @param optionalFeatures
	 * @param unconfiguredOptionalFeatures
	 * @param verifier
	 * @return
	 */
	public IInstallFeatureOperation createInstallOperation(
		IConfiguredSite targetSite,
		IFeature feature,
		IFeatureReference[] optionalFeatures,
		IFeature[] unconfiguredOptionalFeatures,
		IVerificationListener verifier);
		
	/**
	 * Creates an "uninstall feature" operation.
	 * @param targetSite
	 * @param feature
	 * @return
	 */
	public IUninstallFeatureOperation createUninstallOperation(
		IConfiguredSite targetSite,
		IFeature feature);
		
	/**
	 * Creates a "revert feature version" operation.
	 * @param feature current feature
	 * @param anotherFeature the new feature to be swapped in
	 * @return
	 */
	public IConfigFeatureOperation createReplaceFeatureVersionOperation(
		IFeature feature,
		IFeature anotherFeature);
		
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
