package org.eclipse.update.internal.core;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.ContentReference;
import org.eclipse.update.core.FeatureContentProvider;
import org.eclipse.update.core.INonPluginEntry;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.IVerifier;
import org.eclipse.update.core.InstallMonitor;

public class DigestContentProvider extends FeatureContentProvider {

	public DigestContentProvider(URL base) {
		super(base);
		// TODO Auto-generated constructor stub
	}
	
	
	public ContentReference asLocalReference( ContentReference ref,
			InstallMonitor monitor) throws IOException, CoreException {
		return super.asLocalReference(ref, monitor);
	}

	public ContentReference[] getArchiveReferences(InstallMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public ContentReference[] getFeatureEntryArchiveReferences(
			InstallMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public ContentReference[] getFeatureEntryContentReferences(
			InstallMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public ContentReference getFeatureManifestReference(InstallMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public ContentReference[] getNonPluginEntryArchiveReferences(
			INonPluginEntry nonPluginEntry, InstallMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public ContentReference[] getPluginEntryArchiveReferences(
			IPluginEntry pluginEntry, InstallMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public ContentReference[] getPluginEntryContentReferences(
			IPluginEntry pluginEntry, InstallMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public IVerifier getVerifier() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
