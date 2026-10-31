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

import java.util.function.Consumer;

import org.eclipse.swt.events.MouseEvent;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.IPlaceholderCodeMining;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;

public class PlaceholderLineHeaderCodeMining extends LineHeaderCodeMining implements IPlaceholderCodeMining {

	public PlaceholderLineHeaderCodeMining(int beforeLineNumber, IDocument document, ICodeMiningProvider provider, Consumer<MouseEvent> action) throws BadLocationException {
		super(beforeLineNumber, document, provider, action);
	}

	public PlaceholderLineHeaderCodeMining(int beforeLineNumber, IDocument document, ICodeMiningProvider provider) throws BadLocationException {
		super(beforeLineNumber, document, provider);
	}

	public PlaceholderLineHeaderCodeMining(Position position, ICodeMiningProvider provider, Consumer<MouseEvent> action) throws BadLocationException {
		super(position, provider, action);
	}

}
