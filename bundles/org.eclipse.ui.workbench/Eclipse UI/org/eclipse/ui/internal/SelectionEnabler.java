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
package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.actions.SimpleWildcardTester;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This is a helper class that works with PluginAction
 * to quickly test if the action should be enabled
 * without loading the contributing plugin. An object
 * of this class is created in PluginAction if attribute
 * "enablesFor" is seen in the configuration.
 * 
 */
public class SelectionEnabler {
	public static final int ONE_OR_MORE = -1;
	private static final String ATT_NAME = "name";//$NON-NLS-1$
	private static final String ATT_CLASS = "class";//$NON-NLS-1$
	public static final int UNKNOWN = 0;
	public static final int MULTIPLE = -5;
	public static final int ANY_NUMBER =  -2;
	public static final int NONE_OR_ONE = -3;
	public static final int NONE        = -4;
	private List classes = new ArrayList();;
	private ActionExpression enablementExpression;
	private int mode=UNKNOWN;

	public static class SelectionClass {
		public String className;
		public boolean recursive;
		public String nameFilter;
	}
/**
 * ActionEnabler constructor.
 */
public SelectionEnabler(IConfigurationElement configElement) {
	parseClasses(configElement);
	
}
/**
 * Returns true if given selection matches the
 * conditions specified in the registry for
 * this action.
 */
public boolean isEnabledForSelection(ISelection selection) {
	// Optimize it.
	if (mode == UNKNOWN) return false;

	// Handle undefined selections.	
	if (selection == null) 
		selection = StructuredSelection.EMPTY;
		
	// According to the dictionary, a selection is "one that
	// is selected", or "a collection of selected things".  
	// In reflection of this, we deal with one or a collection.
	if (selection instanceof IStructuredSelection)
		return isEnabledFor((IStructuredSelection)selection);
	else if (selection instanceof ITextSelection)
		return isEnabledFor((ITextSelection)selection);
	else
		return isEnabledFor(selection);
}
/**
 * Compare selection count with requirements.
 */
private boolean verifySelectionCount(int count) {
	if (count > 0 && mode == NONE) return false;
	if (count == 0 && mode == ONE_OR_MORE) return false;
	if (count > 1 && mode == NONE_OR_ONE) return false;
	if (count < 2 && mode == MULTIPLE) return false;
	if (mode > 0 && count != mode) return false;
	return true;
}
	
/**
 * Returns true if given structured selection matches the
 * conditions specified in the registry for
 * this action.
 */
private boolean isEnabledFor(ISelection sel) {
	Object obj = sel;
	int count = sel.isEmpty() ? 0: 1;
	
	if(verifySelectionCount(count) == false)
		return false;

	// Compare selection to enablement expression.
	if (enablementExpression != null)
		return enablementExpression.isEnabledFor(obj);

	// Compare selection to class requirements.
	if (classes.isEmpty()) return true;
	if (obj instanceof IAdaptable) {
		IAdaptable element = (IAdaptable)obj;
		if (verifyElement(element)==false) return false;
	} else {
		return false;
	}
	
	return true;
}
/**
 * Returns true if given text selection matches the
 * conditions specified in the registry for this action.
 */
private boolean isEnabledFor(ITextSelection sel) {
	int count = sel.getLength();
	
	if(verifySelectionCount(count) == false)
		return false;

	// Compare selection to enablement expression.
	if (enablementExpression != null)
		return enablementExpression.isEnabledFor(sel);

	// Compare selection to class requirements.
	return verifyElement(sel);
}

/**
 * Returns true if given structured selection matches the
 * conditions specified in the registry for
 * this action.
 */
private boolean isEnabledFor(IStructuredSelection ssel) {
	int count = ssel.size();
	
	if(verifySelectionCount(count) == false)
		return false;

	// Compare selection to enablement expression.
	if (enablementExpression != null)
		return enablementExpression.isEnabledFor(ssel);

	// Compare selection to class requirements.
	if (classes.isEmpty()) return true;
	for (Iterator elements=ssel.iterator(); elements.hasNext();) {
		Object obj = elements.next();
		if (obj instanceof IAdaptable) {
			IAdaptable element = (IAdaptable)obj;
			if (verifyElement(element)==false) return false;
		} else {
			return false;
		}
	}
	
	return true;
}
/**
 * Parses registry element to extract mode
 * and selection elements that will be used
 * for verification.
 */
private void parseClasses(IConfigurationElement config) {
	// Get enables for.
	String enablesFor = config.getAttribute(PluginActionBuilder.ATT_ENABLES_FOR);
	if (enablesFor == null)
		enablesFor = "*"; //$NON-NLS-1$
	if (enablesFor.equals("*")) //$NON-NLS-1$
		mode = ANY_NUMBER;
	else if (enablesFor.equals("?")) //$NON-NLS-1$
		mode = NONE_OR_ONE;
	else if (enablesFor.equals("!")) //$NON-NLS-1$
		mode = NONE;
	else if (enablesFor.equals("+")) //$NON-NLS-1$
		mode = ONE_OR_MORE;
	else if (
		enablesFor.equals("multiple")	//$NON-NLS-1$
			|| enablesFor.equals("2+")) //$NON-NLS-1$
		mode = MULTIPLE;
	else {
		try {
			mode = Integer.parseInt(enablesFor);
		} catch (NumberFormatException e) {
			mode = UNKNOWN;
		}
	}
	
	// Get enablement block.					
	IConfigurationElement[] children = config.getChildren(PluginActionBuilder.TAG_ENABLEMENT);
	if (children.length > 0) {
		enablementExpression = new ActionExpression(children[0]);
		return;
	}
	
	// Get selection block.
	children = config.getChildren(PluginActionBuilder.TAG_SELECTION);
	if (children.length > 0) {
		classes = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			IConfigurationElement sel = children[i];
			String cname = sel.getAttribute(ATT_CLASS);
			String name = sel.getAttribute(ATT_NAME);
			SelectionClass sclass = new SelectionClass();
			sclass.className = cname;
			sclass.nameFilter = name;
			classes.add(sclass);
		}
	}
}
/**
 * Verifies if the element is an instance of a class
 * with a given class name. If direct match fails,
 * implementing interfaces will be tested,
 * then recursively all superclasses and their
 * interfaces.
 */
private boolean verifyClass(Object element, String className) {
	Class eclass = element.getClass();
	Class clazz = eclass;
	boolean match = false;
	while (clazz != null) {
		// test the class itself
		if (clazz.getName().equals(className)) {
			match = true;
			break;
		}
		// test all the interfaces it implements
		Class[] interfaces = clazz.getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			if (interfaces[i].getName().equals(className)) {
				match = true;
				break;
			}
		}
		if (match == true)
			break;
		// get the superclass
		clazz = clazz.getSuperclass();
	}
	return match;
}
/**
 * Verifies if the given element matches one of the
 * selection requirements. Element must at pass the 
 * type test. Filters are ignored in case of 
 * ITextSelection.
 */
private boolean verifyElement(ITextSelection element) {
	if (classes.isEmpty()) return true;
	for (int i = 0; i < classes.size(); i++) {
		SelectionClass sc = (SelectionClass) classes.get(i);
		if (verifyClass(element, sc.className))
			return true;
	}
	return false;
}
/**
 * Verifies if the given element matches one of the
 * selection requirements. Element must at least pass
 * the type test, and optionally wildcard name match.
 */
private boolean verifyElement(IAdaptable element) {
	if (classes.isEmpty()) return true;
	for (int i = 0; i < classes.size(); i++) {
		SelectionClass sc = (SelectionClass) classes.get(i);
		if (verifyClass(element, sc.className) == false)
			continue;
		if (sc.nameFilter == null)
			return true;
		IWorkbenchAdapter de = (IWorkbenchAdapter)element.getAdapter(IWorkbenchAdapter.class);
		if ((de != null) && verifyNameMatch(de.getLabel(element), 
			sc.nameFilter))
			return true;
	}
	return false;
}
/**
 * Verifies that the given name matches the given
 * wildcard filter. Returns true if it does.
 */
public static boolean verifyNameMatch(String name, String filter) {
	return SimpleWildcardTester.testWildcardIgnoreCase(filter, name);
}
}
