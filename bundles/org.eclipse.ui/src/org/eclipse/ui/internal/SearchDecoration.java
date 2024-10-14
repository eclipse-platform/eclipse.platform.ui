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
		String errorMessage = getValidationError(regex, targetDecoration);
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
	private static String getValidationError(String regex, ControlDecoration targetDecoration) {
		GC gc = new GC(targetDecoration.getControl());

		try {
			Pattern.compile(regex);
			return ""; //$NON-NLS-1$
		} catch (PatternSyntaxException e) {
			String description = e.getDescription();
			int errorIndex = e.getIndex();
			String pattern = e.getPattern();

			StringBuilder sBuilder = new StringBuilder();

			sBuilder.append(description);
			if (errorIndex == -1) {
				return sBuilder.toString();
			}

			sBuilder.append(" at index ").append(errorIndex); //$NON-NLS-1$
			sBuilder.append(System.lineSeparator());
			sBuilder.append(pattern);
			sBuilder.append(System.lineSeparator());

			String stringToIndexString = pattern.substring(0, errorIndex);
			String buildString = ""; //$NON-NLS-1$
			String thinSpace = "\u2009"; //$NON-NLS-1$

			while (gc.stringExtent(buildString).x < gc.stringExtent(stringToIndexString).x - 2) {
				buildString += thinSpace; // $NON-NLS-1$
			}
			sBuilder.append(buildString);

			sBuilder.append("^"); //$NON-NLS-1$
			gc.dispose();

			return sBuilder.toString();
		}
	}

}