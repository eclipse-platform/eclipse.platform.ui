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
package org.eclipse.ui.examples.javaeditor.util;


import org.eclipse.jface.text.rules.IWordDetector;

/**
 * A Java aware word detector.
 */
public class JavaWordDetector implements IWordDetector {

	@Override
	public boolean isWordPart(char character) {
		return Character.isJavaIdentifierPart(character);
	}

	@Override
	public boolean isWordStart(char character) {
		return Character.isJavaIdentifierStart(character);
	}
}
