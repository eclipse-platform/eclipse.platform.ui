/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.dynamic.FilterResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * This class provides the ability to filter out user assistance model elements
 * that support filtering (e.g. <code>IToc</code>, <code>ITopic</code>, ...).
 * Implementations that display such elements should consult this class before
 * attempting to display them.
 * </p>
 * 
 * @since 3.2
 */
public class UAContentFilter {
	
	private static final String ELEMENT_FILTER = "filter"; //$NON-NLS-1$
	private static final String ATTRIBUTE_FILTER = "filter"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	private static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$
	private static final String VARIABLE_PLATFORM = "platform"; //$NON-NLS-1$

	private static FilterResolver filterResolver;
	private static EvaluationContext defaultContext;
	private static Document document;

	/**
	 * <p>
	 * Returns whether or not the given object should be filtered out. This
	 * applies to any user assistance component's elements where filters apply
	 * (e.g. help tocs, topics, intro elements, context help topics). If the
	 * element is <code>null</code> or is not filterable, this method returns
	 * <code>false</code>. 
	 * </p>
	 * <p>
	 * This method is for use in non-UI environments, when serving help outside
	 * the workbench. If filtering from the UI, use the <code>isFiltered</code>
	 * method that accepts the evaluation context as well.
	 * </p>
	 * 
	 * @param element the element to check
	 * @return whether or not the element should be filtered out
	 */
	public static boolean isFiltered(Object element) {
		if (defaultContext == null) {
			defaultContext = new EvaluationContext(null, Platform.class) {
				public Object getVariable(String name) {
					if (VARIABLE_PLATFORM.equals(name)) {
						return Platform.class;
					}
					return null;
				}
			};
		}
		return isFiltered(element, defaultContext);
	}

	/**
	 * <p>
	 * Returns whether or not the given object should be filtered out. This
	 * applies to any user assistance component's elements where filters apply
	 * (e.g. help tocs, topics, intro elements, context help topics). If the
	 * element is <code>null</code> or is not filterable, this method returns
	 * <code>false</code>. The evaluation context provides the default object
	 * to test on and a set of variables that can be accessed.
	 * </p>
	 * 
	 * @param element the element to check
	 * @param context the evaluation context for evaluating expressions
	 * @return whether or not the element should be filtered out
	 */
	public static boolean isFiltered(Object element, EvaluationContext context) {
		if (element instanceof Node) {
			Node node = (Node)element;
			if (node.getAttribute(ATTRIBUTE_FILTER) != null) {
				return handleFilterAttribute(node);
			}
			Node[] children = node.getChildNodes();
			for (int i=0;i<children.length;++i) {
				if (ELEMENT_FILTER.equals(children[i].getNodeName())) {
					return handleFilterNodes(node);
				}
				if (ExpressionTagNames.ENABLEMENT.equals(children[i].getNodeName())) {
					return handleEnablementNode(children[i], context);
				}
			}
		}
		return false;
	}

	/*
	 * Handle the filter attribute case.
	 */
	private static boolean handleFilterAttribute(Node node) {
		String expression = node.getAttribute(ATTRIBUTE_FILTER);
		if (filterResolver == null) {
			filterResolver = new FilterResolver();
		}
		return filterResolver.isFiltered(expression);
	}
	
	/*
	 * Handle the child filter node case.
	 */
	private static boolean handleFilterNodes(Node node) {
		boolean hasFilteredYet = false;
		Node[] children = node.getChildNodes();
		for (int i=0;i<children.length;++i) {
			Node child = children[i];
			// is it a filter node?
			if (ELEMENT_FILTER.equals(child.getNodeName())) {
				// if it already filtered, don't bother evaluating the rest
				if (!hasFilteredYet) {
					String name = child.getAttribute(ATTRIBUTE_NAME);
					String value = child.getAttribute(ATTRIBUTE_VALUE);
					if (name != null && value != null && name.length() > 0 && value.length() > 0) {
						boolean not = (value.charAt(0) == '!');
						if (not) {
							value = value.substring(1);
						}
						if (filterResolver == null) {
							filterResolver = new FilterResolver();
						}
						hasFilteredYet = filterResolver.isFiltered(name, value, not);
					}
				}
			}
		}
		return hasFilteredYet;
	}
	
	/*
	 * Handle the child enablement node case.
	 */
	private static boolean handleEnablementNode(Node node, EvaluationContext context) {
		if (document == null) {
			try {
				document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			}
			catch (ParserConfigurationException e) {
				String msg = "Error while evaluating UA document element enablement"; //$NON-NLS-1$
				HelpPlugin.logError(msg, e);
			}
		}
		Element element = convertNode(node);
		try {
			Expression expression = ExpressionConverter.getDefault().perform(element);
			return expression.evaluate(context) == EvaluationResult.FALSE;
		}
		catch (CoreException e) {
			/*
			 * This can happen when attempting to resolve a UI variable (e.g. "workbench")
			 * in a non-UI environment (infocenter mode). Fail silently.
			 */
			return false;
		}
	}

	/*
	 * Converts the given element node to a DOM element node.
	 */
	private static Element convertNode(Node node) {
		Element element = document.createElement(node.getNodeName());
		Iterator iter = node.getAttributes().iterator();
		while (iter.hasNext()) {
			String name = (String)iter.next();
			String value = node.getAttribute(name);
			element.setAttribute(name, value);
		}
		Node[] children = node.getChildNodes();
		for (int i=0;i<children.length;++i) {
			boolean isElement = (children[i].getNodeName() != null && children[i].getNodeValue() == null);
			if (isElement) {
				element.appendChild(convertNode(children[i]));
			}
		}
		return element;
	}
}
