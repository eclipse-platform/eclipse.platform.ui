/*******************************************************************************
 * Copyright (c) 2025 SAP S.E. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP S.E. - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.codemining;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineContentCodeMining;

/**
 * A code mining that draws zero-width characters (like zero-width spaces) as
 * line content code minings.
 *
 * @see ZeroWidthCharactersLineContentCodeMiningProvider
 */
class ZeroWidthCharactersLineContentCodeMining extends LineContentCodeMining {

	private static final String ZW_CHARACTERS_MINING = "ZWSP"; //$NON-NLS-1$

	public ZeroWidthCharactersLineContentCodeMining(int offset, ICodeMiningProvider provider) {
		super(new Position(offset, 1), true, provider);
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	@Override
	public String getLabel() {
		return ZW_CHARACTERS_MINING;
	}
}
