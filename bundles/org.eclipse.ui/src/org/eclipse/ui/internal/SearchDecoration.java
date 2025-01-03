/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

/**
 * This class contains methods to validate and decorate search fields.
 */
public class SearchDecoration {

	private SearchDecoration() {
		// avoid instantiation
	}

	/**
	 * Validate the given regular expression and change the control decoration
	 * accordingly. If the expression is invalid then the decoration will show an
	 * error icon and a message and if the expression is valid then the decoration
	 * will be hidden.
	 *
	 * @param regex            The regular expression to be validated.
	 * @param targetDecoration The control decoration that will show the result of
	 *                         the validation.
	 */
	public static boolean validateRegex(String regex, ControlDecoration targetDecoration) {
		String errorMessage = getValidationError(regex, targetDecoration.getControl());
		if (errorMessage.isEmpty()) {
			targetDecoration.hide();
			return true;
		}

		Image decorationImage = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
		targetDecoration.setImage(decorationImage);
		targetDecoration.setDescriptionText(errorMessage);
		targetDecoration.show();
		return false;
	}

	/**
	 * Validate a regular expression.
	 *
	 * @return The appropriate error message if the regex is invalid or an empty
	 *         string if the regex is valid.
	 */
	private static String getValidationError(String regex, Control targetControl) {
		try {
			Pattern.compile(regex);
			return ""; //$NON-NLS-1$
		} catch (PatternSyntaxException e) {
			return buildValidationErrorString(e, targetControl);
		}
	}

	private static String buildValidationErrorString(PatternSyntaxException e, Control targetControl) {
		String description = e.getDescription();
		int errorIndex = e.getIndex();

		if (errorIndex == -1) {
			return description;
		}

		GC gc = new GC(targetControl);
		String pattern = e.getPattern();

		// This happens when the error is in the last (still unwritten) character e.g.
		// for an "Unescaped trailing backslash"
		if (errorIndex >= pattern.length()) {
			pattern += " "; //$NON-NLS-1$
		}

		StringBuilder validationErrorMessage = new StringBuilder();

		validationErrorMessage.append(description);
		validationErrorMessage.append(" at index ").append(errorIndex).append(System.lineSeparator()); //$NON-NLS-1$
		validationErrorMessage.append(pattern).append(System.lineSeparator());

		String stringToIndexString = pattern.substring(0, errorIndex + 1);
		String hairSpace = "\u200A"; //$NON-NLS-1$
		int hairSpaceWidth = gc.stringExtent(hairSpace).x;

		int stringToIndex = gc.stringExtent(stringToIndexString).x;
		String lastCharacter = stringToIndexString.substring(stringToIndexString.length() - 1);

		int widthLastChar = gc.stringExtent(lastCharacter).x;
		int upWidth = gc.stringExtent("^").x; //$NON-NLS-1$

		double howFar = stringToIndex - widthLastChar / 2 - upWidth / 2;
		int currentWidth = 0;

		while (currentWidth < howFar) {
			currentWidth += hairSpaceWidth;
			validationErrorMessage.append(hairSpace);
		}

		validationErrorMessage.append("^"); //$NON-NLS-1$
		gc.dispose();

		return validationErrorMessage.toString();
	}

}