/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.conversion;

import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;

/**
 * @since 1.1
 */
public class StringToNumberParserLongTest extends
		StringToNumberParserTestHarness {

	@Override
	protected boolean assertValid(Number number) {
		return StringToNumberParser.inLongRange(number);
	}

	@Override
	protected Number getValidMax() {
		return Long.valueOf(Long.MAX_VALUE);
	}

	@Override
	protected Number getValidMin() {
		return Long.valueOf(Long.MIN_VALUE);
	}
}
