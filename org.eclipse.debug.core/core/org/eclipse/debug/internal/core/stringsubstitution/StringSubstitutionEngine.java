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
package org.eclipse.debug.internal.core.stringsubstitution;

import java.text.MessageFormat;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;

/**
 * Performs string substitution for context and value variables.
 */
public class StringSubstitutionEngine {
	
	// delimiters
	private static final String VARIABLE_START = "${"; //$NON-NLS-1$
	private static final char VARIABLE_END = '}'; //$NON-NLS-1$
	private static final char VARIABLE_ARG = ':'; //$NON-NLS-1$
	// parsing states
	private static final int SCAN_FOR_START = 0;
	private static final int SCAN_FOR_END = 1;
	
	/**
	 * Resulting string
	 */
	private StringBuffer fResult;
	
	/**
	 * whether substitutions were performed
	 */
	private boolean fSubs;
	
	/**
	 * Stack of variables to resolve
	 */
	private Stack fStack;
	
	class VariableReference {
		
		// the text inside the variable reference
		private StringBuffer fText;
		
		public VariableReference() {
			fText = new StringBuffer();
		}
		
		public void append(String text) {
			fText.append(text);
		}
		
		public String getText() {
			return fText.toString();
		}
		
	}
	
	/**
	 * Performs recursive string substitution and returns the resulting string.
	 * 
	 * @return the resulting string with all variables recursively
	 *  substituted
	 * @exception CoreException if unable to resolve a referenced variable
	 */
	public String performStringSubstitution(String expression) throws CoreException {
		substitute(expression);
		while (fSubs) {
			substitute(fResult.toString());
		}
		return fResult.toString();
	}

	/**
	 * Makes a substitution pass of the given expression and returns
	 * whether any substitutions were made.
	 *  
	 * @param expression source expression
	 * @exception CoreException if unable to resolve a variable
	 */
	private void substitute(String expression) throws CoreException {
		fResult = new StringBuffer(expression.length());
		fStack = new Stack();
		fSubs = false;
		int pos = 0;
		int state = SCAN_FOR_START;
		while (pos < expression.length()) {
			switch (state) {
				case SCAN_FOR_START:
					int start = expression.indexOf(VARIABLE_START, pos);
					if (start >= 0) {
						int length = start - pos;
						// copy non-variable text to the result
						if (length > 0) {
							fResult.append(expression.substring(pos, start));
						}
						pos = start + 2;
						state = SCAN_FOR_END;
						fStack.push(new VariableReference());						
					} else {
						// done - no more variables
						fResult.append(expression.substring(pos));
						pos = expression.length();
					}
					break;
				case SCAN_FOR_END:
					// be careful of nested variables
					start = expression.indexOf(VARIABLE_START, pos);
					int end = expression.indexOf(VARIABLE_END, pos);
					if (end < 0) {
						// variables are not completed
						VariableReference tos = (VariableReference)fStack.peek();
						tos.append(expression.substring(pos));
						pos = expression.length();
					} else {
						if (start >= 0 && start < end) {
							// start of a nested variable
							int length = start - pos;
							if (length > 0) {
								VariableReference tos = (VariableReference)fStack.peek();
								tos.append(expression.substring(pos, start));
							}
							pos = start + 2;
							fStack.push(new VariableReference());
						} else {
							// end of variable reference
							VariableReference tos = (VariableReference)fStack.pop();
							tos.append(expression.substring(pos, end));
							pos = end + 1;
							String value = resolve(tos);
							if (value == null) {
								value = ""; //$NON-NLS-1$
							}
							if (fStack.isEmpty()) {
								// append to result
								fResult.append(value);
								state = SCAN_FOR_START;
							} else {
								// append to previous variable
								tos = (VariableReference)fStack.peek();
								tos.append(value);
							}
						}
					}
					break;
			}
		}
		// process incomplete variable references
		while (!fStack.isEmpty()) {
			VariableReference tos = (VariableReference)fStack.pop();
			if (fStack.isEmpty()) {
				fResult.append(VARIABLE_START);
				fResult.append(tos.getText());
			} else {
				VariableReference var = (VariableReference)fStack.peek();
				var.append(VARIABLE_START);
				var.append(tos.getText());
			}
		}
	}

	/**
	 * Resolve and return the value of the given variable reference,
	 * possibly <code>null</code>. 
	 * 
	 * @param var
	 * @return variable value, possibly <code>null</code>
	 * @exception CoreException if unable to resolve a value
	 */
	private String resolve(VariableReference var) throws CoreException {
		String text = var.getText();
		int pos = text.indexOf(VARIABLE_ARG);
		String name = null;
		String arg = null;
		if (pos > 0) {
			name = text.substring(0, pos);
			pos++;
			if (pos < text.length()) {
				arg = text.substring(pos);
			} 
		} else {
			name = text;
		}
		IStringVariableManager manager = StringVariableManager.getDefault();
		IValueVariable valueVariable = manager.getValueVariable(name);
		if (valueVariable == null) {
			IContextVariable contextVariable = manager.getContextVariable(name);
			if (contextVariable == null) {
				// no variables with the given name - leave as is
				StringBuffer res = new StringBuffer(var.getText());
				res.insert(0, VARIABLE_START);
				res.append(VARIABLE_END);
				return res.toString();
			} else {
				fSubs = true;
				return contextVariable.getValue(arg);
			}
		} else {
			if (arg == null) {
				fSubs = true;
				return valueVariable.getValue();
			} else {
				// error - an argument specified for a value variable
				throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, MessageFormat.format("Variable {0} does not accept arguments.", new String[]{valueVariable.getName()}), null)); //$NON-NLS-1$
			}
		}
	}

}