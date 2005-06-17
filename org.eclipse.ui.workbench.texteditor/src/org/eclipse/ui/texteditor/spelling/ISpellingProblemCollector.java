/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor.spelling;

/**
 * A collector of {@link SpellingProblem}s. The {@link SpellingService} service
 * will report its results to such a collector.
 * <p>
 * An implementer may specify if a collector is thread aware, i.e., if problems
 * can be reported by any thread, potentially in parallel, and thus, multiple
 * reporting sessions may be active at the same time. Clients of concrete
 * collectors in turn must evaluate the usage of their collector and chose an
 * appropriate implementation.
 * </p>
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 *
 * @see SpellingService
 * @since 3.1
 */
public interface ISpellingProblemCollector {

	/**
	 * Notification of a spelling problem.
	 *
	 * @param problem the spelling problem
	 */
	public void accept(SpellingProblem problem);

	/**
	 * Notification sent before starting to collect problems. This method
	 * will be called by the spelling infrastructure and is not intended
	 * to be called by clients.
	 */
	public void beginCollecting();

	/**
	 * Notification sent after completing to collect problems. This method
	 * will be called by the spelling infrastructure and is not intended
	 * to be called by clients.
	 */
	public void endCollecting();
}
