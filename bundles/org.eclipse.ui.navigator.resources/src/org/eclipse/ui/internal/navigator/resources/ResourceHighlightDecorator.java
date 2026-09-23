/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.ui.internal.navigator.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.themes.IThemeManager;

/**
 * Label decorator that highlights resources with a foreground color when marked
 * with a highlight marker.
 */
public class ResourceHighlightDecorator implements ILightweightLabelDecorator {

	public static final String ID = "org.eclipse.ui.navigator.resources.highlightDecorator"; //$NON-NLS-1$

	public static final String HIGHLIGHT_COLOR_ID = "org.eclipse.ui.navigator.resources.highlightColor"; //$NON-NLS-1$

	private final Set<ILabelProviderListener> listeners = new CopyOnWriteArraySet<>();

	private final org.eclipse.jface.util.IPropertyChangeListener themeListener = event -> {
		String prop = event.getProperty();
		if (HIGHLIGHT_COLOR_ID.equals(prop) || IThemeManager.CHANGE_CURRENT_THEME.equals(prop)) {
			IDecoratorManager dm = PlatformUI.getWorkbench().getDecoratorManager();
			dm.update(ID);
		}
	};

	private final IResourceChangeListener resourceChangeListener = event -> {
		IResourceDelta delta = event.getDelta();
		if (delta == null) {
			return;
		}
		List<IResource> changed = new ArrayList<>();
		try {
			delta.accept(d -> {
				IMarkerDelta[] markerDeltas = d.getMarkerDeltas();
				for (IMarkerDelta markerDelta : markerDeltas) {
					if (markerDelta.isSubtypeOf(IMarker.BOOKMARK)) {
						IResource res = d.getResource();
						if (res != null) {
							changed.add(res);
						}
						break;
					}
				}
				return true;
			});
		} catch (CoreException e) {
			WorkbenchNavigatorPlugin.log("Error processing marker delta for highlight decorator", e.getStatus()); //$NON-NLS-1$
		}

		if (!changed.isEmpty()) {
			fireLabelProviderChanged(changed.toArray(new Object[0]));
		}
	};

	public ResourceHighlightDecorator() {
		PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(themeListener);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener,
				IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		IResource resource = Adapters.adapt(element, IResource.class);
		if (resource == null || !resource.isAccessible()) {
			return;
		}

		boolean hasBookmark = hasBookmarkMarker(resource);
		boolean isHighlighted = ResourceHighlightManager.isHighlighted(resource);
		if (hasBookmark || isHighlighted) {
			ImageDescriptor overlay = PlatformUI.getWorkbench().getSharedImages()
					.getImageDescriptor(IDE.SharedImages.IMG_OBJS_BKMRK_TSK);
			if (overlay != null) {
				decoration.addOverlay(overlay, IDecoration.TOP_RIGHT);
			}
		}

		if (isHighlighted) {
			Display display = PlatformUI.isWorkbenchRunning() ? PlatformUI.getWorkbench().getDisplay() : null;
			if (display != null && !display.isDisposed()) {
				final Color[] colorHolder = new Color[1];

				Runnable fetcher = () -> {
					if (!display.isDisposed()) {
						colorHolder[0] = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme()
								.getColorRegistry().get(HIGHLIGHT_COLOR_ID);
					}
				};

				if (Thread.currentThread() == display.getThread()) {
					fetcher.run();
				} else {
					display.syncExec(fetcher);
				}

				if (colorHolder[0] != null) {
					decoration.setForegroundColor(colorHolder[0]);
				}
			}
		}
	}

	private static boolean hasBookmarkMarker(IResource resource) {
		try {
			return resource.findMarkers(IMarker.BOOKMARK, true, IResource.DEPTH_ZERO).length > 0;
		} catch (CoreException e) {
			return false;
		}
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}

	private void fireLabelProviderChanged(Object[] elements) {
		LabelProviderChangedEvent event = new LabelProviderChangedEvent(this, elements);
		for (ILabelProviderListener listener : listeners) {
			listener.labelProviderChanged(event);
		}
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		if (PlatformUI.isWorkbenchRunning()) {
			PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(themeListener);
		}
		listeners.clear();
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}
}
