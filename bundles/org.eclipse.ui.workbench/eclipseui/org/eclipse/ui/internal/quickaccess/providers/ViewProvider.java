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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess.providers;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.internal.quickaccess.QuickAccessMessages;
import org.eclipse.ui.internal.quickaccess.QuickAccessProvider;
import org.eclipse.ui.quickaccess.QuickAccessElement;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * @since 3.3
 */
public class ViewProvider extends QuickAccessProvider {

	/**
	 * URI of any view on the platform has this prefix. ex:
	 * bundleclass://org.eclipse.pde.spy.bundle/org.eclipse.pde.spy.bundle.BundleSpyPart
	 */
	private static final String BUNDLE_CLASS_SCHEME = "bundleclass://"; //$NON-NLS-1$
	private MApplication application;
	private MWindow window;
	private Map<String, QuickAccessElement> idToElement = new HashMap<>();
	private IViewRegistry viewRegistry;

	public ViewProvider(MApplication application, MWindow window) {
		this.application = application;
		this.window = window;
		this.viewRegistry = PlatformUI.getWorkbench().getViewRegistry();
	}

	@Override
	public String getId() {
		return "org.eclipse.e4.ui.parts"; //$NON-NLS-1$
	}

	@Override
	public QuickAccessElement findElement(String id, String filter) {
		getElements();
		return idToElement.get(id);
	}

	@Override
	public QuickAccessElement[] getElements() {
		IWorkbenchWindow workbenchWindow = window.getContext().get(IWorkbenchWindow.class);
		if (workbenchWindow == null || workbenchWindow.getActivePage() == null) {
			return new QuickAccessElement[0];
		}

		if (idToElement.isEmpty()) {
			IActivityManager activityManager = PlatformUI.getWorkbench().getActivitySupport().getActivityManager();
			for (MPartDescriptor descriptor : application.getDescriptors()) {
				String uri = descriptor.getContributionURI();
				if (uri != null) {
					String id = descriptor.getElementId();
					if (id != null && !id.equals(CompatibilityEditor.MODEL_ELEMENT_ID)) {
						ViewElement element = new ViewElement(window, descriptor);
						if (uri.equals(CompatibilityPart.COMPATIBILITY_VIEW_URI)) {
							IViewDescriptor viewDescriptor = viewRegistry.find(element.getId());
							// Ignore if restricted
							if (viewDescriptor == null)
								continue;
							// Ignore if filtered
							if (!WorkbenchActivityHelper.filterItem(viewDescriptor)) {
								idToElement.put(element.getId(), element);
							}
						} else {
							if (uri.startsWith(BUNDLE_CLASS_SCHEME)) {
								String viewQualUri = uri.substring(BUNDLE_CLASS_SCHEME.length());
								IIdentifier identifier = activityManager.getIdentifier(viewQualUri);
								if (identifier.isEnabled()) {
									idToElement.put(element.getId(), element);
								}
							} else {
								idToElement.put(element.getId(), element);
							}
						}
					}
				}
			}
		}
		return idToElement.values().toArray(new QuickAccessElement[idToElement.size()]);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_VIEW_DEFAULTVIEW_MISC);
	}

	@Override
	public String getName() {
		return QuickAccessMessages.QuickAccess_Views;
	}

	@Override
	protected void doReset() {
		idToElement.clear();
	}
}
