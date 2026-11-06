/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.codemining;

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.ITextViewer;

/**
 * A placeholder code mining is a code mining which displays a {@linkplain #getPlaceholderLabel()
 * placeholder label} while its actual {@linkplain #getLabel() label} is being computed.
 * <p>
 * A placeholder code mining can be used to prevent editor jumping for code minings with expensive
 * {@link #resolve(ITextViewer, IProgressMonitor)} implementations, especially for
 * {@linkplain LineHeaderCodeMining}s.
 * <p>
 * Implementations must adhere to the following contract for {@link #getLabel()}:
 * <ul>
 * <li>{@link #getLabel()} must return {@code null} until the label is computed. The placeholder is
 * only used while {@link #getLabel()} returns {@code null} and the mining is not
 * {@linkplain #isResolved() resolved}. If a non-{@code null} label is set before resolving, the
 * placeholder is never displayed.</li>
 * <li>The label must be set during resolution, i.e. after
 * {@link #resolve(ITextViewer, IProgressMonitor)} is invoked and before the future returned by it
 * completes.</li>
 * <li>If resolution fails or is cancelled, the label should remain {@code null}. The mining is then
 * treated as resolved with an error, see {@link ICodeMining#getLabel()}.</li>
 * </ul>
 *
 * @see ICodeMining
 * @since 3.32
 */
public interface IPlaceholderCodeMining extends ICodeMining {

	/**
	 * Returns the placeholder label, which is displayed while {@link #getLabel()} returns
	 * {@code null} and the mining is not yet {@linkplain #isResolved() resolved}.
	 * <p>
	 * If the placeholder label is {@code null} or empty, no placeholder is displayed.
	 *
	 * @return the placeholder label, may be {@code null} or empty.
	 */
	String getPlaceholderLabel();

	/**
	 * Returns the future to resolve the content of the mining.
	 * <p>
	 * The {@linkplain #getLabel() label} must be computed and set during resolution, i.e. before
	 * the returned future completes.
	 * <p>
	 * If the resolution fails or is cancelled, the label should remain {@code null}.
	 *
	 * @param viewer {@inheritDoc}
	 * @param monitor {@inheritDoc}
	 * @return the future to resolve the content of the mining.
	 */
	@Override
	CompletableFuture<Void> resolve(ITextViewer viewer, IProgressMonitor monitor);

}
