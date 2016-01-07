/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.LocalizationHelper;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;

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

		imageDescriptor = createImageDescriptor();
	}

	private ImageDescriptor createImageDescriptor() {
		String iconURI = viewDescriptor.getIconURI();
		if (iconURI == null) {
			return null;
		}

		try {
			return ImageDescriptor.createFromURL(new URL(iconURI));
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public void execute() {
		/*
		 * TODO: see bug 483699: the code below duplicates the code in
		 * org.eclipse.ui.handlers.ShowViewHandler#openView() and should be
		 * refactored to some user friendly API
		 */
		String id = viewDescriptor.getElementId();
		if (id != null) {
			if (CompatibilityPart.COMPATIBILITY_VIEW_URI
					.equals(viewDescriptor.getContributionURI())) {
				IWorkbenchWindow workbenchWindow = window.getContext().get(IWorkbenchWindow.class);
				IWorkbenchPage page = workbenchWindow.getActivePage();
				if (page != null) {
					try {
						page.showView(viewDescriptor.getElementId());
					} catch (PartInitException e) {
						WorkbenchPlugin.log(e);
					}
				}
			} else {
				EPartService partService = window.getContext().get(EPartService.class);
				MPart part = partService.findPart(id);
				if (part == null) {
					MPlaceholder placeholder = partService.createSharedPart(id);
					part = (MPart) placeholder.getRef();
				}
				partService.showPart(part, PartState.ACTIVATE);
			}
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
		return LocalizationHelper.getLocalized(viewDescriptor.getLabel(), viewDescriptor, window.getContext());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((viewDescriptor == null) ? 0 : viewDescriptor.hashCode());
		return result;
	}

	@Override
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
