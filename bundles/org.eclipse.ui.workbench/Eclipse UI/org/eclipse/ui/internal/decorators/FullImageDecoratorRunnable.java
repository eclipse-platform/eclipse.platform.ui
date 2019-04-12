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

import org.eclipse.swt.graphics.Image;

/**
 * The FullImageDecoratorRunnable is the runnable for decorating images
 */
class FullImageDecoratorRunnable extends FullDecoratorRunnable {
	Image result = null;

	Image start;

	@Override
	public void run() throws Exception {
		result = decorator.decorateImage(start, element);
	}

	/**
	 * Get the result of the decoration or <code>null</code> if there was a failure.
	 * 
	 * @return Image
	 */
	Image getResult() {
		return result;
	}

	/**
	 * Set the values of the initialString and the decorator and object that are
	 * going to be used to determine the result.
	 * 
	 * @param initialImage
	 * @param object
	 * @param definition
	 */
	void setValues(Image initialImage, Object object, FullDecoratorDefinition definition) {
		setValues(object, definition);
		start = initialImage;
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
