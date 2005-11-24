/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class NavigatorExtensionLabelProvider extends LabelProvider {
	
	WorkbenchLabelProvider provider = new WorkbenchLabelProvider();

	public NavigatorExtensionLabelProvider() {
	}

	public String getText(Object object) {
		if (object instanceof ModelProvider) {
			ModelProvider provider = (ModelProvider) object;
			return provider.getDescriptor().getLabel();
		}
		return provider.getText(object);
	}

	public Image getImage(Object object) {
		if (object instanceof ModelProvider) {
			// ModelProvider provider = (ModelProvider) object;
			// TODO: provider.getDescriptor().getImage();
		}
		return provider.getImage(object);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#dispose()
	 */
	public void dispose() {
		provider.dispose();
		super.dispose();
	}
	
}
