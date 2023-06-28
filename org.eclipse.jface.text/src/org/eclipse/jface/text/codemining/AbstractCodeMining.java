/**
 *  Copyright (c) 2017 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
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

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;

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
	 * CodeMining constructor to locate the code mining in a given position.
	 *
	 * @param position the position where the mining must be drawn.
	 * @param provider the owner codemining provider which creates this mining.
	 * @param action the action to execute when mining is clicked and null otherwise.
	 */
	protected AbstractCodeMining(Position position, ICodeMiningProvider provider, Consumer<MouseEvent> action) {
		this.position= position;
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
		gc.drawString(title, x, y, true);
		return gc.stringExtent(title);
	}

	@Override
	public Consumer<MouseEvent> getAction() {
		return action;
	}
}
