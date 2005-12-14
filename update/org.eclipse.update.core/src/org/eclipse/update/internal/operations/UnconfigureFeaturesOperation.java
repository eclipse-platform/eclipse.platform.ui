package org.eclipse.update.internal.operations;

import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.operations.IOperation;
import org.eclipse.update.operations.OperationsManager;

public class UnconfigureFeaturesOperation 
	extends BatchFeatureOperation implements IUnconfigureFeaturesOperation {

	public UnconfigureFeaturesOperation(IConfiguredSite[] targetSites, IFeature[] features) {
		super(targetSites, features);
	}
	
	protected IOperation createOperation(IConfiguredSite targetSite, IFeature feature) {
		return OperationsManager.getOperationFactory().createUnconfigOperation(targetSite, feature);
	}

}
