package org.eclipse.update.internal.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.operations.IOperation;
import org.eclipse.update.operations.IOperationListener;

public abstract class BatchFeatureOperation extends Operation implements IBatchFeatureOperation {

	private IFeature[] features;
	private IConfiguredSite[] targetSites;
	
	
	public BatchFeatureOperation(IConfiguredSite[] targetSites, IFeature[] features) {
		super();
		this.features = features;
		this.targetSites = targetSites;
	}
	
	public IFeature[] getFeatures() {
		return features;
	}

	public IConfiguredSite[] getTargetSites() {
		return targetSites;
	}

	public void setTargetSites(IConfiguredSite[] targetSites) {
		this.targetSites = targetSites;
		
	}
	
	public boolean execute(IProgressMonitor pm, IOperationListener listener)
		throws CoreException, InvocationTargetException {

		if (getFeatures() == null || getFeatures().length == 0)
			return false;
		IOperation[] operations = new IOperation[getFeatures().length];
		
		for ( int i = 0; i < getFeatures().length; i ++) { 
			operations[i] = createOperation(getTargetSites()[i], getFeatures()[i]);
		}
		
		boolean restartNeeded = false;

		for ( int i = 0; i < operations.length; i ++) { 
			try {
				boolean status = operations[i].execute(pm, listener);
				if (status)
					restartNeeded = true; 
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}	

		return restartNeeded;

	}

	protected abstract IOperation createOperation(IConfiguredSite targetSite, IFeature feature);

}
