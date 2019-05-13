/*******************************************************************************
 * Copyright (c) 2010, 2015 BestSolution.at and others.
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

import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.eclipse.e4.emf.internal.xpath.EObjectPointerFactory;
import org.eclipse.e4.emf.internal.xpath.JXPathContextFactoryImpl;
import org.eclipse.emf.ecore.EObject;

/**
 * Factory which creates an XPathContextFactory for {@link EObject}s
 *
 * @since 1.0
 */
public class EcoreXPathContextFactory{

	static {
		JXPathContextReferenceImpl.addNodePointerFactory(new EObjectPointerFactory());
	}

	/**
	 * Create a new factory
	 *
	 * @return the factory
	 */
	public static XPathContextFactory<EObject> newInstance() {
		return new JXPathContextFactoryImpl<>();
	}

}
