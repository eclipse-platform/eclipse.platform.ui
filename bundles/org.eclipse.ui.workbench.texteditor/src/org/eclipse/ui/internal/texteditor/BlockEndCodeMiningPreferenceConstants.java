/*******************************************************************************
 * Copyright (c) 2026 Eclipse Platform contributors.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

/**
 * Preference keys for the block-end code mining that is contributed by
 * {@code org.eclipse.ui.internal.texteditor.codemining.BlockEndCodeMiningProvider}.
 * <p>
 * The keys are shared between the provider (which reads them) and the
 * {@code org.eclipse.ui.editors} preference page and default initializer (which
 * expose and write them). They therefore live in this package which is a friend
 * of {@code org.eclipse.ui.editors}.
 * </p>
 */
public final class BlockEndCodeMiningPreferenceConstants {

	/**
	 * Boolean preference that controls whether a code mining echoing a block's
	 * opening line is shown at its closing brace. The default value is
	 * <code>false</code>.
	 */
	public static final String SHOW_BLOCK_END_CODE_MINING= "showBlockEndCodeMining"; //$NON-NLS-1$

	/**
	 * Integer preference for the minimum number of lines a block must span before a
	 * block-end code mining is shown for it.
	 */
	public static final String BLOCK_END_CODE_MINING_MIN_LINES= "blockEndCodeMiningMinLines"; //$NON-NLS-1$

	/**
	 * Default value for {@link #BLOCK_END_CODE_MINING_MIN_LINES}.
	 */
	public static final int DEFAULT_MIN_LINES= 20;

	private BlockEndCodeMiningPreferenceConstants() {
		// Prevent instantiation
	}
}
