/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.core.expressions.PropertyTester;

/**
 * ContentGeneratorPropertyTester is the property tester for 
 * what content generator is being shown.
 * 
 * @since 3.4
 * 
 */
public class ContentGeneratorPropertyTester extends PropertyTester {

	private static final String ATTRIBUTE_CONTENT_GENERATOR = "contentGenerator"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 */
	public ContentGeneratorPropertyTester() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (property.equals(ATTRIBUTE_CONTENT_GENERATOR)) {
			ExtendedMarkersView view = (ExtendedMarkersView) receiver;
			String currentGenerator = view.getContentGenerator().getId();
			for (int i = 0; i < args.length; i++) {
				if(args[i].equals(currentGenerator))
					return true;
			}
		}
		return false;
	}
}
