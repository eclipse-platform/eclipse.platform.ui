/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.expressions.Expression;

public class Expressions {
	
	/* debugging flag to enable tracing */
	public static final boolean TRACING;
	static {
		String value= Platform.getDebugOption("org.eclipse.core.expressions/tracePropertyResolving"); //$NON-NLS-1$
		TRACING= value != null && value.equalsIgnoreCase("true"); //$NON-NLS-1$
	}
	
	private Expressions() {
		// no instance
	}
	
	public static boolean isInstanceOf(Object element, String type) {
		// null isn't an instanceof of anything.
		if (element == null)
			return false;
		return isSubtype(element.getClass(), type); 
	}
	
	private static boolean isSubtype(Class clazz, String type) {
		if (clazz.getName().equals(type))
			return true;
		Class superClass= clazz.getSuperclass();
		if (superClass != null && isSubtype(superClass, type))
			return true;
		Class[] interfaces= clazz.getInterfaces();
		for (int i= 0; i < interfaces.length; i++) {
			if (isSubtype(interfaces[i], type))
				return true;
		} 
		return false;
	}
	
	public static void checkAttribute(String name, String value) throws CoreException {
		if (value == null) {
			throw new CoreException(new ExpressionStatus(
				ExpressionStatus.MISSING_ATTRIBUTE, 
				Messages.format(ExpressionMessages.Expression_attribute_missing, name))); 
		}
	}
	
	public static void checkAttribute(String name, String value, String[] validValues) throws CoreException {
		checkAttribute(name, value);
		for (int i= 0; i < validValues.length; i++) {
			if (value.equals(validValues[i]))
				return;
		}
		throw new CoreException(new ExpressionStatus(
			ExpressionStatus.WRONG_ATTRIBUTE_VALUE, 
			Messages.format(ExpressionMessages.Expression_attribute_invalid_value, value))); 
	}
	
	public static void checkCollection(Object var, Expression expression) throws CoreException {
		if (var instanceof Collection)
			return;
		throw new CoreException(new ExpressionStatus(
			ExpressionStatus.VARIABLE_IS_NOT_A_COLLECTION, 
			Messages.format(ExpressionMessages.Expression_variable_not_a_collection, expression.toString()))); 
	}
	
	public static void checkList(Object var, Expression expression) throws CoreException {
		if (var instanceof List)
			return;
		throw new CoreException(new ExpressionStatus(
			ExpressionStatus.VARIABLE_IS_NOT_A_LIST, 
			Messages.format(ExpressionMessages.Expression_variable_not_a_list, expression.toString()))); 
	}
	
	//---- Argument parsing --------------------------------------------
	
	private static final Object[] EMPTY_ARGS= new Object[0];
	
	public static Object[] getArguments(IConfigurationElement element, String attributeName) throws CoreException {
		String args= element.getAttribute(attributeName);
		if (args != null) {
			return parseArguments(args);
		} else {
			return EMPTY_ARGS;
		}
	}
	
	public static Object[] parseArguments(String args) throws CoreException {
		List result= new ArrayList();
		int start= 0;
		int comma;
		while ((comma= findNextComma(args, start)) != -1) {
			result.add(convertArgument(args.substring(start, comma).trim()));
			start= comma + 1;
		}
		result.add(convertArgument(args.substring(start).trim()));
		return result.toArray();
	}
	
	private static int findNextComma(String str, int start) throws CoreException {
		boolean inString= false;
		for (int i= start; i < str.length(); i++) {
			char ch= str.charAt(i);
			if (ch == ',' && ! inString) 
				return i;
			if (ch == '\'') {
				if (!inString) {
					inString= true;
				} else {
					if (i + 1 < str.length() && str.charAt(i + 1) == '\'') {
						i++;
					} else {
						inString= false;
					}
				}
			} else if (ch == ',' && !inString) {
				return i;
			}
		}
		if (inString)
			throw new CoreException(new ExpressionStatus(
				ExpressionStatus.STRING_NOT_TERMINATED, 
				Messages.format(ExpressionMessages.Expression_string_not_terminated, str))); 
			
		return -1;
	}
		
	public static Object convertArgument(String arg) throws CoreException {
		if (arg == null) {
			return null;
		} else if (arg.length() == 0) {
			return arg;
		} else if (arg.charAt(0) == '\'' && arg.charAt(arg.length() - 1) == '\'') {
			return unEscapeString(arg.substring(1, arg.length() - 1));
		} else if ("true".equals(arg)) { //$NON-NLS-1$
			return Boolean.TRUE;
		} else if ("false".equals(arg)) { //$NON-NLS-1$
			return Boolean.FALSE;
		} else if (arg.indexOf('.') != -1) {
			try {
				return Float.valueOf(arg);
			} catch (NumberFormatException e) {
				return arg;
			}
		} else {
			try {
				return Integer.valueOf(arg);
			} catch (NumberFormatException e) {
				return arg;
			}
		}
	}

	public static String unEscapeString(String str) throws CoreException {
		StringBuffer result= new StringBuffer();
		for (int i= 0; i < str.length(); i++) {
			char ch= str.charAt(i);
			if (ch == '\'') {
				if (i == str.length() - 1 || str.charAt(i + 1) != '\'')
					throw new CoreException(new ExpressionStatus(
						ExpressionStatus.STRING_NOT_CORRECT_ESCAPED, 
						Messages.format(ExpressionMessages.Expression_string_not_correctly_escaped, str))); 
				result.append('\'');
				i++;
			} else {
				result.append(ch);
			}
		}
		return result.toString();
	}
}
