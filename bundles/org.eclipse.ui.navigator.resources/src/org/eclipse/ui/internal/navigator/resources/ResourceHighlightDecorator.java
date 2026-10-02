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

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;

/**
 * Label decorator that highlights resources with a foreground color
 * when marked with a highlight marker.
 */
public class ResourceHighlightDecorator implements ILightweightLabelDecorator {

	public static final String ID = "org.eclipse.ui.navigator.resources.highlightDecorator"; //$NON-NLS-1$

	public static final String HIGHLIGHT_COLOR_ID = "org.eclipse.ui.navigator.resources.highlightColor"; //$NON-NLS-1$

	private final Set<ILabelProviderListener> listeners = new CopyOnWriteArraySet<>();

	private final org.eclipse.jface.util.IPropertyChangeListener themeListener = event -> {
		String prop = event.getProperty();
		if (HIGHLIGHT_COLOR_ID.equals(prop)) {
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
					if (markerDelta.isSubtypeOf(ResourceHighlightManager.HIGHLIGHT_MARKER_TYPE)) {
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

		if (ResourceHighlightManager.isHighlighted(resource)) {
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
