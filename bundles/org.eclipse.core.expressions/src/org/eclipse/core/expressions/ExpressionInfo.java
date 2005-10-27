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
package org.eclipse.core.expressions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A status object describing information about an expression tree. 
 * This information can for example be used to decide whether a
 * expression tree as to be reevaluated if the value of some
 * variables changes.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.2
 */
public class ExpressionInfo {

	private boolean fHasDefaultVariableAccess;
	private boolean fHasSystemPropertyAccess;
	
	// Although we are using this as sets we use lists since
	// they are faster for smaller numbers of elements
	private List fAccessedVariableNames;
	private List fMisbehavingExpressionTypes;
	
	/**
	 * Returns <code>true</code> if the default variable is accessed
	 * by the expression tree.
	 * 
	 * @return whether the default variable is accessed or not
	 */
	public boolean hasDefaultVariableAccess() {
		return fHasDefaultVariableAccess;
	}
	
	/**
	 * Marks the default variable as accessed.
	 */
	public void markDefaultVariableAccessed() {
		fHasDefaultVariableAccess= true;
	}

	/**
	 * Returns <code>true</code> if the system property is accessed
	 * by the expression tree.
	 * 
	 * @return whether the system property is accessed or not
	 */
	public boolean hasSystemPropertyAccess() {
		return fHasSystemPropertyAccess;
	}
	
	/**
	 * Marks the system property as accessed.
	 */
	public void markSystemPropertyAccessed() {
		fHasSystemPropertyAccess= true;
	}

	/**
	 * Returns the set off accessed variables.
	 * 
	 * @return the set off accessed variables
	 */
	public String[] getAccessedVariableNames() {
		if (fAccessedVariableNames == null)
			return new String[0];
		return (String[])fAccessedVariableNames.toArray(new String[fAccessedVariableNames.size()]);
	}
	
	/**
	 * Marks the given variable as accessed.
	 * 
	 * @param name the accessed variable
	 */
	public void addVariableNameAccess(String name) {
		if (fAccessedVariableNames == null) {
			fAccessedVariableNames= new ArrayList(5);
			fAccessedVariableNames.add(name);
		} else {
			if (!fAccessedVariableNames.contains(name))
				fAccessedVariableNames.add(name);
		}
	}

	/**
	 * Returns the set of expression types which don't implement the
	 * new (@link Expression#computeReevaluationInfo(IEvaluationContext)}
	 * method. If one expression didn't implement the method the expression
	 * tree no optimizations can be done. Returns <code>null</code> if 
	 * all expressions implement the method.
	 * 
	 * @return the set of expression types which don't implement the
	 *  <code>computeReevaluationInfo</code> method.
	 */
	public Class[] getMisbehavingExpressionTypes() {
		if (fMisbehavingExpressionTypes == null)
			return null;
		return (Class[])fMisbehavingExpressionTypes.toArray(new Class[fMisbehavingExpressionTypes.size()]);
	}
	
	/**
	 * Adds the given class to the list of misbehaving classes.
	 * 
	 * @param clazz the class to add.
	 */
	public void addMisBehavingExpressionType(Class clazz) {
		if (fMisbehavingExpressionTypes == null) {
			fMisbehavingExpressionTypes= new ArrayList();
			fMisbehavingExpressionTypes.add(clazz);
		} else {
			if (!fMisbehavingExpressionTypes.contains(clazz))
				fMisbehavingExpressionTypes.add(clazz);
		}
	}
	
	/**
	 * Merges this reevaluation information with the given info.
	 * 
	 * @param other the information to merge with
	 */
	public void merge(ExpressionInfo other) {
		mergeDefaultVariableAccess(other);
		mergeSystemPropertyAccess(other);
		
		mergeAccessedVariableNames(other);
		mergeMisbehavingExpressionTypes(other);
	}

	/**
	 * Merges this reevaluation information with the given info
	 * ignoring the default variable access.
	 * 
	 * @param other the information to merge with
	 */
	public void mergeExceptDefaultVariable(ExpressionInfo other) {
		mergeSystemPropertyAccess(other);
		
		mergeAccessedVariableNames(other);
		mergeMisbehavingExpressionTypes(other);
	}
	
	/**
	 * Merges only the default variable access.
	 * 
	 * @param other the information to merge with
	 */
	private void mergeDefaultVariableAccess(ExpressionInfo other) {
		fHasDefaultVariableAccess= fHasDefaultVariableAccess || other.fHasDefaultVariableAccess;
	}
	
	/**
	 * Merges only the system property access.
	 * 
	 * @param other the information to merge with
	 */
	private void mergeSystemPropertyAccess(ExpressionInfo other) {
		fHasSystemPropertyAccess= fHasSystemPropertyAccess || other.fHasSystemPropertyAccess;
	}

	/**
	 * Merges only the accessed variable names.
	 * 
	 * @param other the information to merge with
	 */
	private void mergeAccessedVariableNames(ExpressionInfo other) {
		if (fAccessedVariableNames == null) {
			fAccessedVariableNames= other.fAccessedVariableNames;
		} else {
			if (other.fAccessedVariableNames != null) {
				for (Iterator iter= other.fAccessedVariableNames.iterator(); iter.hasNext();) {
					Object variableName= iter.next();
					if (!fAccessedVariableNames.contains(variableName))
						fAccessedVariableNames.add(variableName);
				}
			}
		}
	}

	/**
	 * Merges only the misbehaving expression types.
	 * 
	 * @param other the information to merge with
	 */
	private void mergeMisbehavingExpressionTypes(ExpressionInfo other) {
		if (fMisbehavingExpressionTypes == null) {
			fMisbehavingExpressionTypes= other.fMisbehavingExpressionTypes;
		} else  {
			if (other.fMisbehavingExpressionTypes != null) {
				for (Iterator iter= other.fMisbehavingExpressionTypes.iterator(); iter.hasNext();) {
					Object clazz= iter.next();
					if (!fMisbehavingExpressionTypes.contains(clazz))
						fMisbehavingExpressionTypes.add(clazz);
				}
			}
		}
	}
}