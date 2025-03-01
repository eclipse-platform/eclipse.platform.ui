/*******************************************************************************
 * Copyright (c) 2010, 2025 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.emf.xpath;

import org.eclipse.e4.emf.xpath.internal.java.JavaXPathContextFactoryImpl;
import org.eclipse.emf.ecore.EObject;

/**
 * Factory which creates an XPathContextFactory for {@link EObject}s
 *
 * @since 1.0
 */
@Deprecated(forRemoval = true, since = "2025-03 (removal in 2027-03 or later)")
public class EcoreXPathContextFactory {

	/**
	 * Create a new factory
	 *
	 * @return the factory
	 */
	public static XPathContextFactory<EObject> newInstance() {
		return new JavaXPathContextFactoryImpl<>();
	}

}
