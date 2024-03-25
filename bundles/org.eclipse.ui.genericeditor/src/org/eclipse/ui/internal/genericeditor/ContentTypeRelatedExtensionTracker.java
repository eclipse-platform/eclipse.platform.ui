/*******************************************************************************
 * Copyright (c) 2021 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Christoph Läubrich - Inital API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.genericeditor.ContentTypeRelatedExtensionTracker.LazyServiceSupplier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * {@link ServiceTrackerCustomizer} that maps OSGi-Services to
 * ContentTypeRelatedExtensions
 *
 * @param <T> the type of extension to map
 */
final class ContentTypeRelatedExtensionTracker<T> implements ServiceTrackerCustomizer<T, LazyServiceSupplier<T>> {

	private BundleContext bundleContext;
	private ServiceTracker<T, LazyServiceSupplier<T>> serviceTracker;
	private Consumer<LazyServiceSupplier<T>> addAction;
	private Consumer<LazyServiceSupplier<T>> removeAction;
	private Display display;

	ContentTypeRelatedExtensionTracker(BundleContext bundleContext, Class<T> serviceType, Display display) {
		this.bundleContext = bundleContext;
		this.display = display;
		serviceTracker = new ServiceTracker<>(bundleContext, serviceType, this);
	}

	public void stopTracking() {
		serviceTracker.close();
	}

	@Override
	public LazyServiceSupplier<T> addingService(ServiceReference<T> reference) {
		LazyServiceSupplier<T> supplier = new LazyServiceSupplier<>(bundleContext, reference);
		if (addAction != null) {
			display.asyncExec(() -> addAction.accept(supplier));
		}
		return supplier;
	}

	@Override
	public void modifiedService(ServiceReference<T> reference, LazyServiceSupplier<T> service) {
		service.update();
	}

	@Override
	public void removedService(ServiceReference<T> reference, LazyServiceSupplier<T> service) {
		service.dispose();
		if (removeAction != null) {
			display.asyncExec(() -> removeAction.accept(service));
		}
	}

	public void onAdd(Consumer<LazyServiceSupplier<T>> action) {
		this.addAction = action;
	}

	public void onRemove(Consumer<LazyServiceSupplier<T>> action) {
		this.removeAction = action;
	}

	public void startTracking() {
		serviceTracker.open();
	}

	public Collection<LazyServiceSupplier<T>> getTracked() {
		return serviceTracker.getTracked().values();
	}

	public static final class LazyServiceSupplier<S> implements Supplier<S> {
		private ServiceReference<S> reference;
		private BundleContext bundleContext;
		private boolean disposed;
		private S serviceObject;
		private IContentType contentType;

		LazyServiceSupplier(BundleContext bundleContext, ServiceReference<S> reference) {
			this.reference = reference;
			this.bundleContext = bundleContext;
			update();
		}

		private String getProperty(String attribute) {
			return (String) reference.getProperty(attribute);
		}

		synchronized void update() {
			contentType = Platform.getContentTypeManager()
					.getContentType(getProperty(GenericContentTypeRelatedExtension.CONTENT_TYPE_ATTRIBUTE));
		}

		public synchronized IContentType getContentType() {
			return contentType;
		}

		synchronized void dispose() {
			disposed = true;
			if (serviceObject != null) {
				bundleContext.ungetService(reference);
			}
		}

		@Override
		public synchronized S get() {
			if (!disposed && serviceObject == null) {
				serviceObject = bundleContext.getService(reference);
			}
			return serviceObject;
		}

		public synchronized boolean isPresent() {
			return serviceObject != null;
		}
	}

}
