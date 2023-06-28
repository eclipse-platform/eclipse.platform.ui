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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.ITextViewer;

/**
 * A code mining provider adds minings {@link ICodeMining} to source text. The mining will be shown
 * as dedicated horizontal lines in between the source text.
 *
 * @since 3.13
 */
public interface ICodeMiningProvider {

	/**
	 * Compute a list of code minings {@link ICodeMining}. This call should return as fast as
	 * possible and if computing the content of {@link ICodeMining} is expensive implementors should
	 * only return code mining objects with the position and implement resolve
	 * {@link ICodeMining#resolve(ITextViewer, IProgressMonitor)}.
	 *
	 * @param viewer the viewer in which the command was invoked.
	 * @param monitor A progress monitor.
	 * @return An array of future of code minings that resolves to such. The lack of a result can be
	 *         signaled by returning null, or an empty array.
	 */
	CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor);

	/**
	 * Dispose code mining provider.
	 */
	void dispose();
}
