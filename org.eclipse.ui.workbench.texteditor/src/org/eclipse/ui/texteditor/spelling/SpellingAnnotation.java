/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#isQuickFixable()
	 */
	public boolean isQuickFixable() {
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#isQuickFixableStateSet()
	 */
	public boolean isQuickFixableStateSet() {
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#setQuickFixable(boolean)
	 */
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
