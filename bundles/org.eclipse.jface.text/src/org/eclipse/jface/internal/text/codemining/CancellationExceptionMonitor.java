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
package org.eclipse.jface.internal.text.codemining;

import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * {@link IProgressMonitor} which throws a {@link CancellationException} when
 * {@link IProgressMonitor#isCanceled()} returns true.
 *
 * @since 3.13
 */
class CancellationExceptionMonitor extends NullProgressMonitor {

	@Override
	public boolean isCanceled() {
		boolean canceled= super.isCanceled();
		if (canceled) {
			throw new CancellationException();
		}
		return canceled;
	}
}
