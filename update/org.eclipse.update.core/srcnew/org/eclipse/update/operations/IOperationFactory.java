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
public interface IOperationFactory {
	public IOperation createConfigOperation(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeature feature);
	public IOperation createUnconfigOperation(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeature feature);
	public IOperation createInstallOperation(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeature feature,
		IFeatureReference[] optionalFeatures,
		IFeature[] unconfiguredOptionalFeatures,
		IVerificationListener verifier);
	public IOperation createUninstallOperation(
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeature feature);
	public IOperation createBatchInstallOperation(IInstallFeatureOperation[] operations);
	public IOperation createToggleSiteOperation(
		IConfiguredSite site);
	public IOperation createRevertConfigurationOperation(
		IInstallConfiguration config,
		IProblemHandler problemHandler);
}