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
package org.eclipse.ui.internal;

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

	private ServiceReference<IImageURLModifier> installed;

	ImageURLModifierTracker(BundleContext context) {
		super(context, IImageURLModifier.class, null);
	}

	@Override
	public synchronized IImageURLModifier addingService(ServiceReference<IImageURLModifier> reference) {
		IImageURLModifier modifier = super.addingService(reference);
		// not tracked until this returns, so it cannot be picked by installBest()
		if (modifier != null && (installed == null || reference.compareTo(installed) > 0)) {
			installed = reference;
			ImageDescriptor.setURLModifier(modifier);
		}
		return modifier;
	}

	@Override
	public synchronized void modifiedService(ServiceReference<IImageURLModifier> reference,
			IImageURLModifier service) {
		installBest();
	}

	@Override
	public synchronized void removedService(ServiceReference<IImageURLModifier> reference,
			IImageURLModifier service) {
		super.removedService(reference, service);
		if (reference.equals(installed)) {
			installBest();
		}
	}

	private void installBest() {
		installed = getServiceReference();
		ImageDescriptor.setURLModifier(installed == null ? null : getService(installed));
	}
}
