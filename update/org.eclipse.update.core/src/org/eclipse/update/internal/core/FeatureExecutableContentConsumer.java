package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.*;

/**
 * Default implementation of an Executable DefaultFeature
 */

public class FeatureExecutableContentConsumer extends FeatureContentConsumer {


	/**
	 * Feature
	 */
	private IFeature feature;
	
	/**
	 * 
	 */
	private boolean closed = false;
	
	private ISiteContentConsumer contentConsumer;
	
	
	/*
	 * @see IContentConsumer#open(INonPluginEntry)
	 */
	public IContentConsumer open(INonPluginEntry nonPluginEntry) throws CoreException {
		return new NonPluginEntryContentConsumer(getContentConsumer().open(nonPluginEntry));
	}

	/*
	 * @see IContentConsumer#open(IPluginEntry)
	 */
	public IContentConsumer open(IPluginEntry pluginEntry) throws CoreException {
		return new PluginEntryContentConsumer(getContentConsumer().open(pluginEntry));
	}

	/*
	 * @see IFeatureContentConsumer#store(ContentReference, IProgressMonitor)
	 */
	public void store(ContentReference contentReference, IProgressMonitor monitor) throws CoreException {
		getContentConsumer().store(contentReference,monitor);
	}

	/*
	 * @see IFeatureContentConsumer#close()
	 */
	public IFeatureReference close() throws CoreException {
		closed = true;
		if (contentConsumer!=null)	return contentConsumer.close();
		return null;
	}

	/*
	 * @see IFeatureContentConsumer#setFeature(IFeature)
	 */
	public void setFeature(IFeature feature) {
		this.feature = feature;
	}

	/**
	 * returns the Content Consumer for the feature
	 */
	private ISiteContentConsumer getContentConsumer() throws CoreException {
		if (contentConsumer==null)
				contentConsumer = feature.getSite().createSiteContentConsumer(feature);	
		return contentConsumer;
	}


	/*
	 * @see IFeatureContentConsumer#abort()
	 */
	public void abort() {
	}

}
