/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.intro;

/**
 * An intro content detector is used when starting the Workbench to determine if
 * new intro content is available. Since calling this method is part of the
 * Workbench start sequence, subclasses should be implemented with care as not
 * to introduce noticeable delay at startup. If an intro content detector
 * reports new available content, the view part showing the content will be
 * opened again even if the user had closed it in a previous session. Because of
 * this, the intro view part should draw the user's attention to the new content
 * to avoid confusion about why the intro view part was opened again without the
 * user requesting it.
 *
 * @since 3.3
 */
public abstract class IntroContentDetector {

	/**
	 * Returns <code>true</code> if new intro content is available.
	 *
	 * @return <code>true</code> if new intro content is available
	 */
	public abstract boolean isNewContentAvailable();

}
