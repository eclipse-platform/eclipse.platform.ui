/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess.providers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import org.eclipse.e4.ui.model.LocalizationHelper;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.internal.quickaccess.QuickAccessMessages;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * @since 3.3
 *
 */
public class ViewElement extends QuickAccessElement {

	private MWindow window;
	private MPartDescriptor viewDescriptor;
	private ImageDescriptor imageDescriptor;

	public ViewElement(MWindow window, MPartDescriptor descriptor) {
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
		 * org.eclipse.ui.handlers.ShowViewHandler#openView() and should be refactored
		 * to some user friendly API
		 */
		String id = viewDescriptor.getElementId();
		if (id != null) {
			if (CompatibilityPart.COMPATIBILITY_VIEW_URI.equals(viewDescriptor.getContributionURI())) {
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
		String viewLabel = LocalizationHelper.getLocalized(viewDescriptor.getLabel(), viewDescriptor,
				window.getContext());
		String categoryLabel = LocalizationHelper.getLocalized(viewDescriptor.getCategory(), viewDescriptor,
				window.getContext());
		if (categoryLabel != null) {
			viewLabel = NLS.bind(QuickAccessMessages.QuickAccess_ViewWithCategory, viewLabel, categoryLabel);
		}
		String description = LocalizationHelper.getLocalized(viewDescriptor.getTooltip(), viewDescriptor,
				window.getContext());
		if (description != null && !description.isEmpty()) {
			return viewLabel + separator + description;
		}
		return viewLabel;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(viewDescriptor);
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
		return Objects.equals(viewDescriptor, other.viewDescriptor);
	}
}
