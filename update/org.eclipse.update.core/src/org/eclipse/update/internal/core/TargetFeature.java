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
package org.eclipse.update.internal.core;
import org.eclipse.core.runtime.*;
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
