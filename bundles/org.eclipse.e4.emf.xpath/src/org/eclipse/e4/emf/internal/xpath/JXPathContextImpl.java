/*******************************************************************************
 * Copyright (c) 2010, 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - adjustment to EObject
 ******************************************************************************/
package org.eclipse.e4.emf.internal.xpath;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.Pointer;
import org.eclipse.e4.emf.xpath.XPathContext;
import org.eclipse.emf.ecore.EObject;

/**
 * Context which uses JXPath to evaluate XPath expressions
 */
public final class JXPathContextImpl implements XPathContext {

	private JXPathContext context;

	public static class EMFFunctions {
		public static String eClassName(Object o) {
			if( o instanceof Collection<?> ) {
				if( ! ((Collection<?>) o).isEmpty() ) {
					return eClassName(((Collection<?>) o).iterator().next());
				}
			} else if( o instanceof EObject ) {
				return ((EObject) o).eClass().getName();
			} else if( o instanceof NodeSet ) {
				List<?> l = ((NodeSet) o).getValues();
				if( l.size() > 0 && l.get(0) instanceof EObject ) {
					return eClassName((EObject) l.get(0));
				}
			} else if( o instanceof Pointer ) {
				if( ((Pointer) o).getValue() instanceof EObject ) {
					return eClassName((EObject) ((Pointer) o).getValue());
				}
			}
			
			return null;
		}
	}
	
	/**
	 * Create a new context
	 * 
	 * @param contextBean
	 *            the context bean (=root of the xpath expression)
	 */
	JXPathContextImpl(Object contextBean) {
		this.context = JXPathContext.newContext(contextBean);
		this.context.setFunctions(new ClassFunctions(EMFFunctions.class, "ecore"));
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

	public <Type> Iterator<Type> iterate(String xpath) {
		return context.iterate(xpath);
	}

	private JXPathContext getJXPathContext() {
		return context;
	}

}
