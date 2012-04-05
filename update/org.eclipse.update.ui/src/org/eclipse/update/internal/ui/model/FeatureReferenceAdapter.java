/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.model;
import java.lang.reflect.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;

public class FeatureReferenceAdapter extends FeatureAdapter {
	private IFeatureReference featureRef;
	private boolean touched;

	public FeatureReferenceAdapter(IFeatureReference featureRef) {
		this.featureRef = featureRef;
		setIncluded(featureRef instanceof IIncludedFeatureReference);
	}
	
	public IFeature getFeature(IProgressMonitor monitor) throws CoreException {
		return featureRef.getFeature(monitor);
	}
	
	public String getFastLabel() {
		return featureRef.getURL().toString();
	}
	
	public ISite getSite() {
		return featureRef.getSite();
	}
	
	public URL getURL() {
		return featureRef.getURL();
	}
	
	public boolean isOptional() {
		return featureRef instanceof IIncludedFeatureReference ? 
			((IIncludedFeatureReference)featureRef).isOptional():false;
	}
	
	public void touchIncludedFeatures(IRunnableContext context) {
		if (touched) return;
		final IFeatureReference [] included;
		
		try {
			included = 	getFeature(null).getIncludedFeatureReferences();
		}
		catch (CoreException e) {
			return;
		}
		if (included.length == 0) return;
		IRunnableWithProgress op = new IRunnableWithProgress () {
			public void run(IProgressMonitor monitor) {
				monitor.beginTask(UpdateUIMessages.SiteBookmark_downloading, included.length); 
				for (int i=0; i<included.length; i++) {
					IFeatureReference ref = included[i];
					try {
						monitor.subTask(ref.getURL().toString());
						ref.getFeature(new SubProgressMonitor(monitor, 1));
						//monitor.worked(1);
					}
					catch (CoreException e) {
					}
				}
				monitor.done();
			}
		};
		try {
			context.run(true, false, op);
			touched=true;
		}
		catch (InvocationTargetException e) {
		}
		catch (InterruptedException e) {
		}
	}

	public IFeatureAdapter[] getIncludedFeatures(IProgressMonitor monitor) {
		try {
			IFeatureReference[] included =
				getFeature(monitor).getIncludedFeatureReferences();
			FeatureReferenceAdapter[] result =
				new FeatureReferenceAdapter[included.length];
			for (int i = 0; i < included.length; i++) {
				result[i] = new FeatureReferenceAdapter(included[i]);
			}
			return result;
		} catch (CoreException e) {
			return new IFeatureAdapter[0];
		}
	}

	public IFeatureReference getFeatureReference() {
		return featureRef;
	}
}
