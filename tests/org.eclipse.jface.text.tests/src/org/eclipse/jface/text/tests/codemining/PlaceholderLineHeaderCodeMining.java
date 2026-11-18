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
package org.eclipse.jface.text.tests.codemining;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.IPlaceholderCodeMining;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;

/**
 * {@link LineHeaderCodeMining} with a fixed placeholder label for tests.
 */
public class PlaceholderLineHeaderCodeMining extends LineHeaderCodeMining implements IPlaceholderCodeMining {

	private final String placeholderLabel;

	public PlaceholderLineHeaderCodeMining(int beforeLineNumber, IDocument document, ICodeMiningProvider provider, String placeholderLabel) throws BadLocationException {
		super(beforeLineNumber, document, provider);
		this.placeholderLabel= placeholderLabel;
	}

	@Override
	public String getPlaceholderLabel() {
		return placeholderLabel;
	}

}
