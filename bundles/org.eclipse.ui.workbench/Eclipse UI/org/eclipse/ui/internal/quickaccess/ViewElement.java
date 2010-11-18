/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @since 3.3
 * 
 */
public class ViewElement extends QuickAccessElement {

	private MWindow window;
	private MPartDescriptor viewDescriptor;
	private ImageDescriptor imageDescriptor;

	public ViewElement(QuickAccessProvider provider, MWindow window, MPartDescriptor descriptor) {
		super(provider);
		this.window = window;
		this.viewDescriptor = descriptor;

		try {
			imageDescriptor = ImageDescriptor.createFromURL(new URL(descriptor.getIconURI()));
		} catch (MalformedURLException e) {
			imageDescriptor = null;
		}
	}

	@Override
	public void execute() {
		EPartService partService = window.getContext().get(EPartService.class);
		String id = viewDescriptor.getElementId();
		if (id != null) {
			MPart part = partService.findPart(id);
			if (part == null) {
				part = partService.createPart(id);
			}
			partService.showPart(part, PartState.ACTIVATE);
		}
	}

	@Override
	public String getId() {
		return viewDescriptor.getElementId();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

	@Override
	public String getLabel() {
		return viewDescriptor.getLabel();
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((viewDescriptor == null) ? 0 : viewDescriptor.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ViewElement other = (ViewElement) obj;
		if (viewDescriptor == null) {
			if (other.viewDescriptor != null)
				return false;
		} else if (!viewDescriptor.equals(other.viewDescriptor))
			return false;
		return true;
	}
}
