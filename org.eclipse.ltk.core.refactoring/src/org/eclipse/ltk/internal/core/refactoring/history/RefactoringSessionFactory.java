/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

/**
 * Helper class to create tools for refactoring session histories.
 * 
 * @since 3.2
 */
public final class RefactoringSessionFactory {

	/**
	 * Creates a default refactoring session reader object.
	 * 
	 * @return a default reader
	 */
	public static IRefactoringSessionReader createDefaultReader() {
		return new XmlRefactoringSessionReader();
	}

	/**
	 * Creates a default refactoring session transformer object.
	 * 
	 * @return a default transformer
	 */
	public static IRefactoringSessionTransformer createDefaultTransformer() {
		return new XmlRefactoringSessionTransformer();
	}

	/**
	 * Creates a new refactoring session factory.
	 */
	private RefactoringSessionFactory() {
		// Not for instantiation
	}
}