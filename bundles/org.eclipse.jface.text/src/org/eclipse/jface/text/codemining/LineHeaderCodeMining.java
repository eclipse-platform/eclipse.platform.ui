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

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.inlined.Positions;

/**
 * Abstract class for line header code mining.
 *
 * @since 3.13
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

	@Override
	public Point draw(GC gc, StyledText textWidget, Color color, int x, int y) {
		return draw(getLabel(), gc, textWidget, x, y, new Callable<Point>() {

			@Override
			public Point call() throws Exception {
				return LineHeaderCodeMining.super.draw(gc, textWidget, color, x, y);
			}
		});
	}

	static Point draw(String label, GC gc, StyledText textWidget, int x, int y, Callable<Point> superDrawCallable) {
		String title= label != null ? label : "no command"; //$NON-NLS-1$
		String[] lines= title.split("\\r?\\n|\\r"); //$NON-NLS-1$
		if (lines.length > 1) {
			Point result= new Point(0, 0);
			for (String line : lines) {
				gc.drawString(line, x, y, true);
				Point ext= gc.stringExtent(line);
				result.x= Math.max(result.x, ext.x);
				result.y+= ext.y;
				y+= ext.y + textWidget.getLineSpacing();
			}
			return result;
		} else {
			try {
				return superDrawCallable.call();
			} catch (Exception e) {
				return null;
			}
		}
	}
}
