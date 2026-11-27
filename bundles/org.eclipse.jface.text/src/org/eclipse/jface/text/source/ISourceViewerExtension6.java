/*******************************************************************************
 * Copyright (c) 2026 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.source;

import java.util.function.Predicate;

import org.eclipse.jface.text.codemining.ICodeMining;

/**
 * Extension interface for {@link ISourceViewer} supporting temporary, per-viewer
 * code mining filters. Installed providers and editor preferences are not changed.
 *
 * @since 3.32
 */
public interface ISourceViewerExtension6 {

	/**
	 * Adds a filter. A mining is displayed only if every registered filter accepts
	 * it. Registering the same filter instance again has no effect. Filters are
	 * retained when code mining providers are reconfigured.
	 * <p>
	 * Filters are evaluated after providers have computed their minings and before
	 * resolution and rendering. Null entries returned by providers are ignored and
	 * are never passed to filters. Rejected minings are disposed. A filter must be
	 * thread-safe and must not access SWT widgets or modify the viewer. Its result
	 * must remain stable until it is removed. Adding a filter cancels pending
	 * updates, removes annotations containing rejected minings and requests fresh
	 * minings. Annotations containing only accepted minings remain until refreshed.
	 * </p>
	 * <p>
	 * Call this method on the viewer's UI thread. Clients must remove their filter
	 * when it is no longer needed; filters are released when the viewer is disposed.
	 * </p>
	 *
	 * @param filter the filter to add, not {@code null}; {@code true} accepts a mining
	 * @throws org.eclipse.swt.SWTException if called from a thread other than the
	 *         viewer's UI thread or after the viewer's text widget has been disposed
	 */
	void addCodeMiningFilter(Predicate<? super ICodeMining> filter);

	/**
	 * Removes the specified filter instance without affecting other filters or
	 * disposing installed providers. If registered, pending updates are canceled,
	 * current accepted mining annotations are retained and fresh minings are requested.
	 * Removing an unregistered filter has no effect.
	 * <p>
	 * Call this method on the viewer's UI thread.
	 * </p>
	 *
	 * @param filter the filter instance previously added, not {@code null}
	 * @throws org.eclipse.swt.SWTException if called from a thread other than the
	 *         viewer's UI thread or after the viewer's text widget has been disposed
	 */
	void removeCodeMiningFilter(Predicate<? super ICodeMining> filter);
}
