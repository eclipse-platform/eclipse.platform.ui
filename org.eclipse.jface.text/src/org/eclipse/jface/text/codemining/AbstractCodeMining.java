/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide CodeMining support with CodeMiningManager - Bug 527720
 */
package org.eclipse.jface.text.codemining;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.inlined.Positions;

/**
 * Abstract class for {@link ICodeMining}.
 *
 * @since 3.13
 */
public abstract class AbstractCodeMining implements ICodeMining {

	/**
	 * The position where codemining must be drawn
	 */
	private final Position position;

	/**
	 * The owner codemining provider which creates this mining.
	 */
	private final ICodeMiningProvider provider;

	/**
	 * The future used to resolve mining.
	 */
	private CompletableFuture<Void> resolveFuture;

	/**
	 * The label of the resolved codemining.
	 */
	private String label;

	/**
	 * The action to execute when mining is clicked and null otherwise.
	 */
	private final Consumer<MouseEvent> action;

	/**
	 * CodeMining constructor to locate the code mining before the given line number.
	 *
	 * @param beforeLineNumber the line number where codemining must be drawn. Use 0 if you wish to
	 *            locate the code mining before the first line number (1).
	 * @param document the document.
	 * @param provider the owner codemining provider which creates this mining.
	 * @throws BadLocationException when line number doesn't exists
	 */
	public AbstractCodeMining(int beforeLineNumber, IDocument document, ICodeMiningProvider provider) throws BadLocationException {
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
	public AbstractCodeMining(int beforeLineNumber, IDocument document, ICodeMiningProvider provider, Consumer<MouseEvent> action)
			throws BadLocationException {
		this.position= Positions.of(beforeLineNumber, document, true);
		this.provider= provider;
		this.action= action;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public ICodeMiningProvider getProvider() {
		return provider;
	}

	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * Set the label mining.
	 *
	 * @param label the label mining.
	 */
	public void setLabel(String label) {
		this.label= label;
	}

	@Override
	public final CompletableFuture<Void> resolve(ITextViewer viewer, IProgressMonitor monitor) {
		if (resolveFuture == null) {
			resolveFuture= doResolve(viewer, monitor);
		}
		return resolveFuture;
	}

	/**
	 * Returns the future which resolved the content of mining and null otherwise. By default, the
	 * resolve do nothing.
	 *
	 * @param viewer the viewer
	 * @param monitor the monitor
	 * @return the future which resolved the content of mining and null otherwise.
	 */
	protected CompletableFuture<Void> doResolve(ITextViewer viewer, IProgressMonitor monitor) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public boolean isResolved() {
		return (resolveFuture != null && resolveFuture.isDone());
	}

	@Override
	public void dispose() {
		if (resolveFuture != null) {
			resolveFuture.cancel(true);
			resolveFuture= null;
		}
	}

	/**
	 * Draw the {@link #getLabel()} of mining with gray color. User can override this method to draw
	 * anything.
	 *
	 * @param gc the graphics context
	 * @param textWidget the text widget to draw on
	 * @param color the color of the line
	 * @param x the x position of the annotation
	 * @param y the y position of the annotation
	 * @return the size of the draw of mining.
	 */
	@Override
	public Point draw(GC gc, StyledText textWidget, Color color, int x, int y) {
		String title= getLabel() != null ? getLabel() : "no command"; //$NON-NLS-1$
		gc.drawText(title, x, y);
		return gc.stringExtent(title);
	}

	@Override
	public Consumer<MouseEvent> getAction() {
		return action;
	}
}
