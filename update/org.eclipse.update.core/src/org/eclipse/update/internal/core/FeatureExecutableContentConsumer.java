package org.eclipse.update.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.*;

/**
 * ContentConsumer Implementation of FeatureExecutable
 */

public class FeatureExecutableContentConsumer extends FeatureContentConsumer {

	private IFeature feature;
	private boolean closed= false;
	private ISiteContentConsumer contentConsumer;

	/*
	 * @see IContentConsumer#open(INonPluginEntry)
	 */
	public IContentConsumer open(INonPluginEntry nonPluginEntry)
		throws CoreException {
		return new NonPluginEntryContentConsumer(
			getContentConsumer().open(nonPluginEntry));
	}

	/*
	 * @see IContentConsumer#open(IPluginEntry)
	 */
	public IContentConsumer open(IPluginEntry pluginEntry) throws CoreException {
		return new PluginEntryContentConsumer(getContentConsumer().open(pluginEntry));
	}

	/*
	 * @see IContentConsumer#open(IFeatureReference)
	 */
	public IFeatureContentConsumer open(IFeatureReference featureReference)
		throws CoreException{
		return null;
	}


	/*
	 * @see IFeatureContentConsumer#store(ContentReference, IProgressMonitor)
	 */
	public void store(ContentReference contentReference, IProgressMonitor monitor)
		throws CoreException {
		getContentConsumer().store(contentReference, monitor);
	}

	/*
	 * @see IFeatureContentConsumer#close()
	 */
	public IFeatureReference close() throws CoreException {
		closed= true;
		if (contentConsumer != null)
			return contentConsumer.close();
		return null;
	}

	/*
	 * @see IFeatureContentConsumer#setFeature(IFeature)
	 */
	public void setFeature(IFeature feature) {
		this.feature= feature;
	}

	/*
	 * returns the Content Consumer for the feature
	 * 
	 * Right now we are the only one to implement SiteContentConsumer
	 * Need to be exposed as API post v2.0
	 */
	private ISiteContentConsumer getContentConsumer() throws CoreException {
		if (contentConsumer == null)
			if (feature.getSite() instanceof SiteFile) {
				SiteFile site= (SiteFile) feature.getSite();
				contentConsumer= site.createSiteContentConsumer(feature);
			} else {
				throw new UnsupportedOperationException();
			}
		return contentConsumer;
	}

	/*
	 * @see IFeatureContentConsumer#abort()
	 */
	public void abort() {
		//FIXME implement the abort
	}

	/*
	 * @see IFeatureContentConsumer#getFeature()
	 */
	public IFeature getFeature(){
		return null;
	}

	/*
	 * @see IFeatureContentConsumer#getParent()
	 */
	public IFeatureContentConsumer getParent(){
		return null;
	}

	/*
	 * @see IFeatureContentConsumer#getChildren()
	 */
	public IFeatureContentConsumer[] getChildren(){
		return null;
	}


}