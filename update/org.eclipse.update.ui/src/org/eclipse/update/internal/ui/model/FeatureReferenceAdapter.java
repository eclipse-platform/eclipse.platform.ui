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
package org.eclipse.update.internal.ui.model;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.UpdateUI;

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
				monitor.beginTask(UpdateUI.getString("SiteBookmark.downloading"), included.length);
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
