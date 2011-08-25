/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - adjustment to EObject
 ******************************************************************************/
package org.eclipse.e4.emf.internal.xpath;

import org.eclipse.e4.emf.xpath.XPathContext;
import org.eclipse.e4.emf.xpath.XPathContextFactory;


/**
 * Factory creating context using JXPath
 * 
 * @param <Type>
 *            the object the XPath is created for
 */
public class JXPathContextFactoryImpl<Type> extends XPathContextFactory<Type> {

	public XPathContext newContext(XPathContext parentContext, Object contextBean) {
		return new JXPathContextImpl(parentContext, contextBean);
	}

	public XPathContext newContext(Type contextBean) {
		return new JXPathContextImpl(contextBean);
	}

}
