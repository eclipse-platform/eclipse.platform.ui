/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.resource.IImageURLModifier;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Installs the highest ranked {@link IImageURLModifier} service in JFace.
 * Tracked because the contributing bundle may start after the workbench.
 */
class ImageURLModifierTracker extends ServiceTracker<IImageURLModifier, IImageURLModifier> {

	// own bookkeeping, as the superclass map is updated only after addingService returns
	private final Map<ServiceReference<IImageURLModifier>, IImageURLModifier> modifiers = new HashMap<>();

	ImageURLModifierTracker(BundleContext context) {
		super(context, IImageURLModifier.class, null);
	}

	@Override
	public IImageURLModifier addingService(ServiceReference<IImageURLModifier> reference) {
		IImageURLModifier modifier = super.addingService(reference);
		if (modifier != null) {
			synchronized (modifiers) {
				modifiers.put(reference, modifier);
				installBest();
			}
		}
		return modifier;
	}

	@Override
	public void modifiedService(ServiceReference<IImageURLModifier> reference, IImageURLModifier service) {
		synchronized (modifiers) {
			installBest();
		}
	}

	@Override
	public void removedService(ServiceReference<IImageURLModifier> reference, IImageURLModifier service) {
		synchronized (modifiers) {
			modifiers.remove(reference);
			installBest();
		}
		super.removedService(reference, service);
	}

	private void installBest() {
		ServiceReference<IImageURLModifier> best = null;
		for (ServiceReference<IImageURLModifier> reference : modifiers.keySet()) {
			if (best == null || reference.compareTo(best) > 0) {
				best = reference;
			}
		}
		ImageDescriptor.setURLModifier(best == null ? null : modifiers.get(best));
	}
}
