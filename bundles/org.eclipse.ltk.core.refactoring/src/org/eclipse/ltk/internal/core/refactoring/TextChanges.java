/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.jface.text.IDocument;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Helper class for text file changes.
 */
public class TextChanges {

	private TextChanges() {
		// no instance
	}

	public static RefactoringStatus isValid(IDocument document, int length) {
		RefactoringStatus result= new RefactoringStatus();
		if (length != document.getLength()) {
			result.addFatalError(RefactoringCoreMessages.TextChanges_error_document_content_changed);
		}
		return result;
	}
}
