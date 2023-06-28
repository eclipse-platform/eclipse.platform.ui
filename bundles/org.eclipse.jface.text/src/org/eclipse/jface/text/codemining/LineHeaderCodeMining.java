/**
 *  Copyright (c) 2018, 2021 Angelo ZERR and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] CodeMining should support line header/content annotation type both - Bug 529115
 *  Christoph Läubrich - Bug 570606 - [codemining] LineHeaderCodeMining allow constructor with explicit position
 */
package org.eclipse.jface.text.codemining;

import java.util.function.Consumer;

import org.eclipse.swt.events.MouseEvent;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.inlined.Positions;

/**
 * Abstract class for line header code mining.
 *
 * @since 3.13
 *
 */
public abstract class LineHeaderCodeMining extends AbstractCodeMining {

	/**
	 * CodeMining constructor to locate the code mining before the given line number.
	 *
	 * @param beforeLineNumber the line number where codemining must be drawn. Use 0 if you wish to
	 *            locate the code mining before the first line number (1).
	 * @param document the document.
	 * @param provider the owner codemining provider which creates this mining.
	 * @throws BadLocationException when line number doesn't exists
	 */
	public LineHeaderCodeMining(int beforeLineNumber, IDocument document, ICodeMiningProvider provider) throws BadLocationException {
		this(beforeLineNumber, document, provider, null);
	}

	/**
	 * CodeMining constructor to locate the code mining before the given line number.
	 *
	 * @param beforeLineNumber the line number where codemining must be drawn. Use 0 if you wish to
	 *            locate the code mining before the first line number (1).
	 * @param document the document.
	 * @param provider the owner codemining provider which creates this mining.
	 * @param action the action to execute when mining is clicked and null otherwise.
	 * @throws BadLocationException when line number doesn't exists
	 */
	public LineHeaderCodeMining(int beforeLineNumber, IDocument document, ICodeMiningProvider provider, Consumer<MouseEvent> action)
			throws BadLocationException {
		this(Positions.of(beforeLineNumber, document, true), provider, action);
	}

	/**
	 * CodeMining constructor to locate the code mining before the given line at the supplied
	 * position.
	 *
	 * @param position the position where the mining must be drawn
	 * @param provider the owner codemining provider which creates this mining.
	 * @param action the action to execute when mining is clicked and null otherwise.
	 * @throws BadLocationException when line number doesn't exists
	 * @since 3.17
	 */
	public LineHeaderCodeMining(Position position, ICodeMiningProvider provider, Consumer<MouseEvent> action)
			throws BadLocationException {
		super(position, provider, action);
	}

}
