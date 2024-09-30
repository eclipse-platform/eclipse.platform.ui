/**
 *  Copyright (c) 2018, Angelo ZERR and others.
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
 */
package org.eclipse.jface.text.codemining;

import java.util.function.Consumer;

import org.eclipse.swt.events.MouseEvent;

import org.eclipse.jface.text.Position;

/**
 * Abstract class for line content code mining.
 *
 * @since 3.13
 */
public abstract class LineContentCodeMining extends AbstractCodeMining {

	private final boolean afterPosition;

	/**
	 * CodeMining constructor to locate the code mining in a given position.
	 *
	 * @param position the position where the mining must be drawn.
	 * @param provider the owner codemining provider which creates this mining.
	 */
	public LineContentCodeMining(Position position, ICodeMiningProvider provider) {
		this(position, provider, null);
	}

	/**
	 * CodeMining constructor to locate the code mining in a given position.
	 *
	 * @param position the position where the mining must be drawn.
	 * @param afterPosition if true code mining is treated as suffix code mining where cursor and
	 *            selection is not including the mining
	 * @param provider the owner codemining provider which creates this mining.
	 *
	 * @since 3.26
	 */
	public LineContentCodeMining(Position position, boolean afterPosition, ICodeMiningProvider provider) {
		this(position, afterPosition, provider, null);
	}

	/**
	 * CodeMining constructor to locate the code mining in a given position.
	 *
	 * @param position the position where the mining must be drawn.
	 * @param provider the owner codemining provider which creates this mining.
	 * @param action the action to execute when mining is clicked and null otherwise.
	 */
	public LineContentCodeMining(Position position, ICodeMiningProvider provider, Consumer<MouseEvent> action) {
		this(position, false, provider, action);
	}

	/**
	 * CodeMining constructor to locate the code mining in a given position.
	 *
	 * @param position the position where the mining must be drawn.
	 * @param provider the owner codemining provider which creates this mining.
	 * @param action the action to execute when mining is clicked and null otherwise.
	 * @param afterPosition if true code mining is treated as suffix code mining where cursor and
	 *            selection is not including the mining
	 *
	 * @since 3.26
	 */
	public LineContentCodeMining(Position position, boolean afterPosition, ICodeMiningProvider provider, Consumer<MouseEvent> action) {
		super(position, provider, action);
		this.afterPosition= afterPosition;
	}

	/**
	 * indicates if code mining should be rendered after given position; cursor and selection does
	 * not include the code mining if set to true.
	 *
	 * @since 3.26
	 */
	public boolean isAfterPosition() {
		return afterPosition;
	}

}
