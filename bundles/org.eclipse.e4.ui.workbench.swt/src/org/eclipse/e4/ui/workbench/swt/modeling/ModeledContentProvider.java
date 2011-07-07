/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.modeling;

import org.eclipse.e4.ui.workbench.modeling.ModelService;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * FIXME Eric/Boris what is this needed for????
 * 
 */
public class ModeledContentProvider implements ITreeContentProvider {

	private ModelService modelSvc;

	public ModeledContentProvider(ModelService modelSvc) {
		this.modelSvc = modelSvc;
	}
	
	public Object[] getChildren(Object parentElement) {
		return modelSvc.getChildren(parentElement, "Children"); //$NON-NLS-1$
	}

	public Object getParent(Object element) {
		return modelSvc.getProperty(element, "Parent"); //$NON-NLS-1$
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
