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

import java.util.Iterator;

import org.apache.commons.jxpath.JXPathContext;
import org.eclipse.e4.emf.xpath.XPathContext;

/**
 * Context which uses JXPath to evaluate XPath expressions
 */
public final class JXPathContextImpl implements XPathContext {

	private JXPathContext context;

	/**
	 * Create a new context
	 * 
	 * @param contextBean
	 *            the context bean (=root of the xpath expression)
	 */
	JXPathContextImpl(Object contextBean) {
		this.context = JXPathContext.newContext(contextBean);
	}

	/**
	 * Create a new child context
	 * 
	 * @param parentContext
	 *            the parent
	 * @param contextBean
	 *            the context bean (=root of the xpath expression)
	 */
	JXPathContextImpl(XPathContext parentContext, Object contextBean) {
		JXPathContext jContext = ((JXPathContextImpl) parentContext).getJXPathContext();
		this.context = JXPathContext.newContext(jContext, contextBean);
	}

	public Object getValue(String xpath) {
		return context.getValue(xpath);
	}

	public Object getValue(String xpath, Class<?> requiredType) {
		return context.getValue(xpath, requiredType);
	}

	@SuppressWarnings("unchecked")
	public <Type> Iterator<Type> iterate(String xpath) {
		return context.iterate(xpath);
	}

	private JXPathContext getJXPathContext() {
		return context;
	}

}
