package org.eclipse.update.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.*;

/**
 * ContentConsumer Implementation of FeatureExecutable
 */

public class FeatureExecutableContentConsumer extends FeatureContentConsumer {

	private IFeature feature;
	private boolean closed= false;
	private boolean aborted= false;	
	private ISiteContentConsumer contentConsumer;
	private IFeatureContentConsumer parent = null;
	private List /* of IFeatureContentCOnsumer */ children;

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
	 * @see IContentConsumer#addChild(IFeature)
	 */
	public void addChild(IFeature child) throws CoreException {
		IFeatureContentConsumer childConsumer = child.getFeatureContentConsumer();
		childConsumer.setParent(this);
		if (children==null) children = new ArrayList();
		children.add(childConsumer);
		return;
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
		
		if (!closed && getParent()!=null){
			closed=true;
			return null;
		}
		
		IFeatureContentConsumer[] children = getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].close();
		}
				
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
	 * Sets the parent 
	 */
	public void setParent(IFeatureContentConsumer featureContentConsumer) {
		this.parent= featureContentConsumer;
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
	public void abort() throws CoreException {

		if (aborted) return;
		
		IFeatureContentConsumer[] children = getChildren();
		for (int i = 0; i < children.length; i++) {
			try {
			children[i].abort();
			} catch (Exception e){
				//do Nothing
			}
		}

		//FIXME implement the cleanup
		
		aborted = true;
		throw Utilities.newCoreException("",null);

	}

	/*
	 * @see IFeatureContentConsumer#getFeature()
	 */
	public IFeature getFeature(){
		return feature;
	}

	/*
	 * @see IFeatureContentConsumer#getParent()
	 */
	public IFeatureContentConsumer getParent(){
		return parent;
	}

	/*
	 * @see IFeatureContentConsumer#getChildren()
	 */
	public IFeatureContentConsumer[] getChildren(){
		if (children==null)
			return new IFeatureContentConsumer[0];

		return (IFeatureContentConsumer[]) children.toArray(arrayTypeFor(children));
	}


}