/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;

public class StandardElementHandler extends ElementHandler {
	
	public Expression create(ExpressionConverter converter, IConfigurationElement element) throws CoreException {
		String name= element.getName();
		if (ExpressionTagNames.INSTANCEOF.equals(name)) {
			return new InstanceofExpression(element);
		} else if (ExpressionTagNames.TEST.equals(name)) {
			return new TestExpression(element);
		} else if (ExpressionTagNames.OR.equals(name)) {
			OrExpression result= new OrExpression();
			processChildren(converter, element, result);
			return result;
		} else if (ExpressionTagNames.AND.equals(name)) {
			AndExpression result= new AndExpression();
			processChildren(converter, element, result);
			return result;
		} else if (ExpressionTagNames.NOT.equals(name)) {
			return new NotExpression(converter.perform(element.getChildren()[0]));
		} else if (ExpressionTagNames.WITH.equals(name)) {
			WithExpression result= new WithExpression(element);
			processChildren(converter, element, result);
			return result;
		} else if (ExpressionTagNames.ADAPT.equals(name)) {
			AdaptExpression result= new AdaptExpression(element);
			processChildren(converter, element, result);
			return result;
		} else if (ExpressionTagNames.ITERATE.equals(name)) {
			IterateExpression result= new IterateExpression(element);
			processChildren(converter, element, result);
			return result;
		} else if (ExpressionTagNames.COUNT.equals(name)) {
			return new CountExpression(element);
		} else if (ExpressionTagNames.SYSTEM_TEST.equals(name)) {
			return new SystemTestExpression(element);
		} else if (ExpressionTagNames.RESOLVE.equals(name)) {
			ResolveExpression result= new ResolveExpression(element);
			processChildren(converter, element, result);
			return result;
		} else if (ExpressionTagNames.ENABLEMENT.equals(name)) {
			EnablementExpression result= new EnablementExpression(element);
			processChildren(converter, element, result);
			return result;
		} else if (ExpressionTagNames.EQUALS.equals(name)) {
			return new EqualsExpression(element);
		}
		return null;
	}
}
