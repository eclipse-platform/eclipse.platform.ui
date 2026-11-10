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
 * Preference keys of the block end code mining, shared with {@code org.eclipse.ui.editors} which
 * contributes their defaults and their preference page.
 */
public final class BlockEndCodeMiningPreferenceConstants {

	/** Whether a block's opening line is echoed at its closing brace. Off by default. */
	public static final String SHOW_BLOCK_END_CODE_MINING= "showBlockEndCodeMining"; //$NON-NLS-1$

	/** The minimum number of lines a block must span to be annotated. */
	public static final String BLOCK_END_CODE_MINING_MIN_LINES= "blockEndCodeMiningMinLines"; //$NON-NLS-1$

	/** Default value for {@link #BLOCK_END_CODE_MINING_MIN_LINES}. */
	public static final int DEFAULT_MIN_LINES= 20;

	private BlockEndCodeMiningPreferenceConstants() {
		// Prevent instantiation
	}
}
