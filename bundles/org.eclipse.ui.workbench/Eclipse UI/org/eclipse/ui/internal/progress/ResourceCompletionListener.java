/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.progress.IJobCompletionListener;

/**
 * ResourceCompletionListener is the class that adds the API
 * for a listener that has a result of type IResource.
 */
public abstract class ResourceCompletionListener
	implements IJobCompletionListener {

	IResource result;

	public void setResource(IResource resource) {
		result = resource;
	}

	public IResource getResource() {
		return result;
	}
}
