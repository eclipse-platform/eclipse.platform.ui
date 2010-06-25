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
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * FIXME Eric/Boris what is this needed for????
 */
public class ModeledLabelProvider extends LabelProvider {
	private ModelService modelSvc;

	public ModeledLabelProvider(ModelService modelSvc) {
		this.modelSvc = modelSvc;
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		String label = (String) modelSvc.getProperty(element, "Label"); //$NON-NLS-1$
		if (label == null)
			label = super.getText(element);
		
		return label;
	}

}
