/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.dynamicplugins;

import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.KeywordRegistry;

/**
 * @since 3.1
 */
public class KeywordTests extends DynamicTestCase {

	private static final String ID_KEYWORD = "dynamicKeyword1";
	public KeywordTests(String testName) {
		super(testName);
	}

	public void testKeywords() {
		KeywordRegistry registry = KeywordRegistry.getInstance();
		assertNull(registry.getKeywordLabel(ID_KEYWORD));
		getBundle();
		String label = registry.getKeywordLabel(ID_KEYWORD);
		assertNotNull(label);
		assertEquals(ID_KEYWORD, label);
		removeBundle();
		assertNull(registry.getKeywordLabel(ID_KEYWORD));
	}

	@Override
	protected String getExtensionId() {
		return "newKeyword1.testDynamicKeywordAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_KEYWORDS;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newKeyword1";
	}

}
