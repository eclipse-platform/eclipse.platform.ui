package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;

public interface ISite extends IPluginContainer {
	IFeature [] getFeatures();
	IFeature createExecutableFeature(IFeature feature);
	void install(IFeature feature, IProgressMonitor monitor) throws CoreException;
	void remove(IFeature feature, IProgressMonitor monitor) throws CoreException;
}