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
import org.eclipse.swt.graphics.Image;

/**
 * This class is used for decorating a searchBar. Usually it is used for the
 * strg +f and strg + h search. Changing something here will change the
 * decoration which is used while searching for regex in both these searches.
 *
 *
 */
public class SearchDecoration {
	String message;

	public void decorateA(ControlDecoration decoration, String regex) {
		if (!isValidRegex(regex)) {
			Image decorationImage = FieldDecorationRegistry.getDefault()
					.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
			decoration.setImage(decorationImage);
			decoration.setDescriptionText(message);
			decoration.show();
		} else {
			decoration.hide();

		}
	}

	/**
	 * checks if string is a valid regex
	 *
	 * @param string the string
	 * @return if the string is valid regex
	 */
	private boolean isValidRegex(String string) {
		try {
			Pattern p = Pattern.compile(string);
			p.toString();
			message = ""; //$NON-NLS-1$
			return true;
		} catch (PatternSyntaxException e) {
			message = e.getLocalizedMessage();
			createMessage();
			return false;
		}
	}

	/**
	 * creates the message which will be shown when hovering over the decoration
	 */
	private void createMessage() {
		int i = 0;
		while (i < message.length() && "\n\r".indexOf(message.charAt(i)) == -1) { //$NON-NLS-1$
			i++;
		}
		message = message.substring(0, i);
	}

}