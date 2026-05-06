/*******************************************************************************
 * Copyright (c) 2026 Vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.util.function.Supplier;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * Wraps a {@link QuickAccessProvider} behind a {@link Supplier} so the wrapped
 * provider is constructed only when first queried for elements rather than at
 * dialog open time. Identity ({@code getId()}) and the
 * {@code requiresUiAccess()} flag are answered without forcing instantiation
 * so the caller can sort, filter, and route providers by thread without
 * triggering lazy construction.
 */
final class LazyQuickAccessProvider extends QuickAccessProvider {

	private final String id;
	private final boolean requiresUiAccess;
	private final Supplier<QuickAccessProvider> supplier;
	private volatile QuickAccessProvider delegate;

	LazyQuickAccessProvider(String id, boolean requiresUiAccess, Supplier<QuickAccessProvider> supplier) {
		this.id = id;
		this.requiresUiAccess = requiresUiAccess;
		this.supplier = supplier;
	}

	private QuickAccessProvider delegate() {
		QuickAccessProvider d = delegate;
		if (d == null) {
			synchronized (this) {
				d = delegate;
				if (d == null) {
					d = supplier.get();
					delegate = d;
				}
			}
		}
		return d;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean requiresUiAccess() {
		return requiresUiAccess;
	}

	@Override
	public String getName() {
		return delegate().getName();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return delegate().getImageDescriptor();
	}

	@Override
	public QuickAccessElement[] getElements() {
		return delegate().getElements();
	}

	@Override
	public QuickAccessElement[] getElementsSorted(String filter, IProgressMonitor monitor) {
		return delegate().getElementsSorted(filter, monitor);
	}

	@Override
	public QuickAccessElement findElement(String id, String filterText) {
		return delegate().findElement(id, filterText);
	}

	@Override
	protected void doReset() {
		QuickAccessProvider d = delegate;
		if (d != null) {
			d.reset();
		}
	}
}
