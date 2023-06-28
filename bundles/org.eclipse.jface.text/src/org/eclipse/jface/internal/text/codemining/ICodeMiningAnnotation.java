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
package org.eclipse.jface.internal.text.codemining;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.codemining.ICodeMining;

/**
 * Internal Code Mining annotation API used by the {@link CodeMiningManager}.
 *
 * @since 3.13
 */
public interface ICodeMiningAnnotation {

	/**
	 * Update code minings.
	 *
	 * @param minings the minings to update.
	 * @param monitor the monitor
	 */
	void update(List<ICodeMining> minings, IProgressMonitor monitor);

	/**
	 * Redraw the codemining annotation.
	 */
	void redraw();

	/**
	 * Return whether the annotation is in visible lines.
	 *
	 * @return <code>true</code> if the annotation is in visible lines and <code>false</code>
	 *         otherwise.
	 */
	boolean isInVisibleLines();
}
