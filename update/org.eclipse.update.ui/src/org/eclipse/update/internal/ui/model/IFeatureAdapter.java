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

import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

/**
 * @version 	1.0
 * @author
 */
public interface IFeatureAdapter {
	public URL getURL();
	public ISite getSite();
	public IFeature getFeature(IProgressMonitor monitor) throws CoreException;
	public IFeatureAdapter [] getIncludedFeatures(IProgressMonitor monitor);
	public boolean hasIncludedFeatures(IProgressMonitor monitor);
	public boolean isIncluded();
	public boolean isOptional();
	public String getFastLabel();
}
