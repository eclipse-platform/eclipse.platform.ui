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

package org.eclipse.core.tests.internal.databinding.conversion;

import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;

/**
 * @since 3.2
 *
 */
public class StringToNumberParserDoubleTest extends
		StringToNumberParserTestHarness {

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserTestHarness#assertValid(java.lang.Number)
	 */
	protected boolean assertValid(Number number) {
		return StringToNumberParser.inDoubleRange(number);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserTestHarness#getValidMax()
	 */
	protected Number getValidMax() {
		return new Double(Double.MAX_VALUE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserTestHarness#getValidMin()
	 */
	protected Number getValidMin() {
		return new Double(-Double.MAX_VALUE);
	}
}
