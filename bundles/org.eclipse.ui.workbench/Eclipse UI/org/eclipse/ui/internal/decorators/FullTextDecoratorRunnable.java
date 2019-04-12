/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.decorators;

/**
 * FullTextDecoratorRunnable is the decorator that runs the text decoration.
 */
public class FullTextDecoratorRunnable extends FullDecoratorRunnable {
	String result = null;

	String start;

	@Override
	public void run() throws Exception {
		result = decorator.decorateText(start, element);
	}

	/**
	 * Get the result of the decoration or <code>null</code> if there was a failure.
	 * 
	 * @return the result
	 */
	String getResult() {
		return result;
	}

	/**
	 * Set the values of the initialString and the decorator and object that are
	 * going to be used to determine the result.
	 * 
	 * @param initialString
	 * @param object
	 * @param definition
	 */
	void setValues(String initialString, Object object, FullDecoratorDefinition definition) {
		setValues(object, definition);
		start = initialString;
		result = null;
	}

	/**
	 * Clear decorator references.
	 * 
	 * @since 3.1
	 */
	void clearReferences() {
		decorator = null;
	}
}
