/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.texteditor.spelling;

import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;

/**
 * Spelling annotation.
 *
 * @since 3.3
 */
public class SpellingAnnotation extends Annotation implements IQuickFixableAnnotation {

	/** The spelling annotation type. */
	public static final String TYPE= "org.eclipse.ui.workbench.texteditor.spelling"; //$NON-NLS-1$

	/** The spelling problem. */
	private SpellingProblem fSpellingProblem;


	/**
	 * Creates a new spelling annotation.
	 *
	 * @param problem the spelling problem.
	 */
	public SpellingAnnotation(SpellingProblem problem) {
		super(TYPE, false, problem.getMessage());
		fSpellingProblem= problem;
	}

	@Override
	public boolean isQuickFixable() {
		return true;
	}

	@Override
	public boolean isQuickFixableStateSet() {
		return true;
	}

	@Override
	public void setQuickFixable(boolean state) {
		// always true
	}

	/**
	 * Returns the spelling problem.
	 *
	 * @return the spelling problem
	 */
	public SpellingProblem getSpellingProblem() {
		return fSpellingProblem;
	}

}
