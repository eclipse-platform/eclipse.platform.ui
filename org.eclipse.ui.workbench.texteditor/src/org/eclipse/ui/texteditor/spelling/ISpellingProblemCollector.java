/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor.spelling;

/**
 * A collector of {@link SpellingProblem}s. The {@link SpellingService} service
 * will report its results to such a collector.
 * <p>
 * This interface is intended to be implemented by clients.
 * </p><p>
 * Not yet for public use. API under construction.
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
	 * Notification sent before starting the problem reporting. This method
	 * will be called by the spelling infrastructure and is not intended
	 * to be called by clients.
	 */
	public void beginReporting();

	/**
	 * Notification sent after completing the problem reporting. This method
	 * will be called by the spelling infrastructure and is not intended
	 * to be called by clients.
	 */
	public void endReporting();
}
