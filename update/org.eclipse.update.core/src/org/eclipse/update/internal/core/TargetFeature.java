package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;

/**
 * 
 */
public class TargetFeature extends Feature {


	/**
	 * The content consumer of the DefaultFeature
	 */
	private IFeatureContentConsumer contentConsumer;
	

	/**
	 * Constructor for TargetFeature.
	 */
	public TargetFeature() {
		super();
	}

	/**
	 * Sets the content Consumer
	 */
	public void setContentConsumer(IFeatureContentConsumer contentConsumer) {
		this.contentConsumer = contentConsumer;
		contentConsumer.setFeature(this);
	}

	/*
	 * @see IFeature#getFeatureContentConsumer()
	 */
	public IFeatureContentConsumer getFeatureContentConsumer() throws CoreException {
		if (this.contentConsumer == null) {
			throw Utilities.newCoreException( Policy.bind("Feature.NoFeatureContentConsumer", getURL().toExternalForm()), null); //$NON-NLS-1$
		}
		return contentConsumer;
	}

}
