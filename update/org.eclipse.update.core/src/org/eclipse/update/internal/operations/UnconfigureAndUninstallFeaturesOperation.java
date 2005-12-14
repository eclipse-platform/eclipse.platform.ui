package org.eclipse.update.internal.operations;

import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.operations.IOperation;
import org.eclipse.update.operations.OperationsManager;

public class UnconfigureAndUninstallFeaturesOperation extends
		BatchFeatureOperation implements
		IUnconfigureAndUninstallFeaturesOperation {

	public UnconfigureAndUninstallFeaturesOperation(IConfiguredSite[] targetSites,
			IFeature[] features) {
		super(targetSites, features);
	}

	protected IOperation createOperation(IConfiguredSite targetSite, IFeature feature) {
		return ((OperationFactory)OperationsManager.getOperationFactory()).createUnconfigureAndUninstallFeatureOperation(targetSite, feature);
	}
}
