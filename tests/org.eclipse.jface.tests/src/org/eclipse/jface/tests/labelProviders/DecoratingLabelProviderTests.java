/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.jface.tests.labelProviders;

import org.junit.runner.JUnitCore;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ CompositeLabelProviderTableTest.class, DecoratingLabelProviderTreePathTest.class,
		DecoratingLabelProviderTreeTest.class, ColorAndFontLabelProviderTest.class,
		ColorAndFontViewerLabelProviderTest.class, DecoratingStyledCellLabelProviderTest.class,
		IDecorationContextTest.class })
public class DecoratingLabelProviderTests {

	public static void main(String[] args) {
		JUnitCore.main(DecoratingLabelProviderTests.class.getName());
	}

}
