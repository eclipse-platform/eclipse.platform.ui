/*******************************************************************************
 * Copyright (c) 2025 Hannes Wellmann and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hannes Wellmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.model;

import java.util.stream.Stream;
import org.eclipse.e4.emf.xpath.XPathContextFactory;
import org.eclipse.e4.ui.model.application.MApplicationElement;

public class ModelXPathEvaluator {

	@SuppressWarnings({ "deprecation", "removal" })
	public static <T> Stream<T> findMatchingElements(MApplicationElement searchRoot, String xPath, Class<T> clazz) {
		return XPathContextFactory.newInstance().newContext(searchRoot).stream(xPath, clazz);
	}

	// Inline the then still used part of JavaXPathContextFactoryImpl here once the
	// e4.emf.xpath bundle is finally removed
}
