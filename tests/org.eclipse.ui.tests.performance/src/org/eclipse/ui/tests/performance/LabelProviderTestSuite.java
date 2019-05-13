/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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

package org.eclipse.ui.tests.performance;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.5
 */
public class LabelProviderTestSuite extends TestSuite {

	public static Test suite() {
		return new LabelProviderTestSuite();
	}

	public LabelProviderTestSuite() {
		addTest(new LabelProviderTest("DecoratingStyledCellLabelProvider with Colors", true, true));
		addTest(new LabelProviderTest("DecoratingStyledCellLabelProvider", true, false));
		addTest(new LabelProviderTest("DecoratingLabelProvider with Colors", false, true));
		addTest(new LabelProviderTest("DecoratingLabelProvider", false, false));
	}
}