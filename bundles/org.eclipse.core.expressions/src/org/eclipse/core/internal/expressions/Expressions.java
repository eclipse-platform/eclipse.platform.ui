/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
		String value= Platform.getDebugOption("org.eclipse.jdt.ui/typeExtension/tracing"); //$NON-NLS-1$
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
				ExpressionMessages.getFormattedString("Expression.attribute.missing", name))); //$NON-NLS-1$
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
			ExpressionMessages.getFormattedString("Expression.attribute.invalid_value", value))); //$NON-NLS-1$
	}
	
	public static void checkCollection(Object var, Expression expression) throws CoreException {
		if (var instanceof Collection)
			return;
		throw new CoreException(new ExpressionStatus(
			ExpressionStatus.VARIABLE_IS_NOT_A_COLLECTION, 
			ExpressionMessages.getFormattedString("Expression.variable.not_a_collection", expression.toString()))); //$NON-NLS-1$
	}
	
	public static void checkList(Object var, Expression expression) throws CoreException {
		if (var instanceof List)
			return;
		throw new CoreException(new ExpressionStatus(
			ExpressionStatus.VARIABLE_IS_NOT_A_LIST, 
			ExpressionMessages.getFormattedString("Expression.variable.not_a_list", expression.toString()))); //$NON-NLS-1$
	}
	
	//---- Argument parsing --------------------------------------------
	
	public static Object[] getArguments(IConfigurationElement element, String attributeName) {
		String args= element.getAttribute(attributeName);
		if (args != null) {
			return parseArguments(args);
		} else {
			return EMPTY_ARGS;
		}
	}
	
	public static Object[] parseArguments(String args) {
		List result= new ArrayList();
		Tokenizer tokenizer= new Tokenizer(args);
		String arg;
		while ((arg= tokenizer.next()) != null) {
			result.add(convertArgument(arg));
		}
		return result.toArray();
	}
		
	private static final Object[] EMPTY_ARGS= new Object[0];
	
	private static class Tokenizer {
		private String fString;
		private int fPosition;
		public Tokenizer(String s) {
			fString= s.trim();
			fPosition= 0;
		}
		public String next() {
			if (fPosition >= fString.length())
				return null;
			char ch= fString.charAt(fPosition);
			while (ch == ' ') {
				fPosition++;
				ch= fString.charAt(fPosition);
			}
			if (ch == '\'') {
				String result= getString();
				fPosition++;
				int comma= fString.indexOf(',', fPosition);
				if (comma != -1)
					fPosition= comma + 1;
				else
					fPosition= fString.length();
				return result;
			} else {
				int nextComma= fString.indexOf(',', fPosition);
				String result;
				if (nextComma == -1) {
					result= fString.substring(fPosition, fString.length());
					fPosition= fString.length();
				} else {
					result= fString.substring(fPosition, nextComma);
					
					fPosition= nextComma + 1;
				}
				return result.trim();
			}
		}
		private String getString() {
			StringBuffer result= new StringBuffer();
			result.append('\'');
			fPosition++;
			loop: for (; fPosition < fString.length(); fPosition++) {
				char ch= fString.charAt(fPosition);
				switch (ch) {
					case '\'':
						if (fPosition == fString.length() - 1) {
							break loop;
						} else {
							char next= fString.charAt(fPosition + 1);
							if (next == '\'') {
								result.append('\'');
								fPosition++;
							} else {
								break loop;
							}
						}
						break;
					default:
						result.append(ch);
				}
			}
			result.append('\'');
			return result.toString();
		}
	}
	
	public static Object convertArgument(String arg) {
		if (arg == null) {
			return null;
		} else if (arg.length() == 0) {
			return arg;
		} else if (arg.charAt(0) == '\'' && arg.charAt(arg.length() - 1) == '\'') {
			return arg.substring(1, arg.length() - 1);
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
}
