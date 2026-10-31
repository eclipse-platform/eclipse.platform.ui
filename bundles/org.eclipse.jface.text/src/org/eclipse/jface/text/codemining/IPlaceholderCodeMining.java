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

/**
 * A placeholder code mining is a code mining, whose label is interpreted as a placeholder while the
 * mining is not yet resolved. The placeholder {@link #getLabel()} is displayed until
 * {@link #isResolved()} is {@code true}, unless the placeholder label is itself {@code null} or
 * empty.
 * <p>
 * A placeholder code mining can be used to prevent editor jumping for code minings with expensive
 * {@link #resolve(org.eclipse.jface.text.ITextViewer, org.eclipse.core.runtime.IProgressMonitor)}
 * implementations, especially for {@linkplain LineHeaderCodeMining}s.
 *
 * @see ICodeMining
 * @since 3.32
 */
public interface IPlaceholderCodeMining extends ICodeMining {

	/**
	 * {@inheritDoc}
	 *
	 * If not yet {@linkplain #isResolved() resolved}, the returned value is treated as a
	 * placeholder value. The placeholder is only displayed if non-empty.
	 * <p>
	 * Once the mining is resolved, the value should be updated to the resolved value as described
	 * above.
	 *
	 * @return The label. Treated as a placeholder if the mining is not yet resolved.
	 */
	@Override
	String getLabel();

}
