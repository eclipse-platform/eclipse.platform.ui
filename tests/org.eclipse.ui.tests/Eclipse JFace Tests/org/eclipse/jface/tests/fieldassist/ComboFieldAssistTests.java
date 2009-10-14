/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.fieldassist;

/**
 * @since 3.6
 *
 */
public class ComboFieldAssistTests extends FieldAssistTestCase {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.fieldassist.AbstractFieldAssistTestCase#createFieldAssistWindow()
	 */
	protected AbstractFieldAssistWindow createFieldAssistWindow() {
		return new ComboFieldAssistWindow();
	}

}
