package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.misc.UIHackFinder;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.actions.SimpleWildcardTester;
import org.eclipse.jface.viewers.*;
import java.util.*;

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
	private static final String ATT_NAME = "name";
	private static final String ATT_CLASS = "class";
	private static final String TAG_SELECTION = "selection";
	public static final int UNKNOWN = 0;
	public static final int MULTIPLE = -5;
	public static final int ANY_NUMBER =  -2;
	public static final int NONE_OR_ONE = -3;
	public static final int NONE        = -4;
	private List classes;
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
	if (mode == UNKNOWN) return false;
	if (!(selection instanceof IStructuredSelection))
	return false;
	IStructuredSelection ssel = (IStructuredSelection)selection;
	int count = ssel.size();
	
	// a few quick checks
	if (count > 0 && mode == NONE) return false;
	if (count == 0 && mode == ONE_OR_MORE) return false;
	if (count > 1 && mode == NONE_OR_ONE) return false;
	// check multiple selection
	if (count < 2 && mode == MULTIPLE) return false;
	// if exact number is needed, check if it matches
	if (mode > 0 && count != mode) return false;
	// number matches. Check class types.
	if (classes==null) return true;
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
	String enablesFor = config.getAttribute(PluginActionBuilder.ATT_ENABLES_FOR);
	if (enablesFor == null)
		enablesFor = "*";
	if (enablesFor.equals("*"))
		mode = ANY_NUMBER;
	else
		if (enablesFor.equals("?"))
			mode = NONE_OR_ONE;
		else
			if (enablesFor.equals("!"))
				mode = NONE;
			else
				if (enablesFor.equals("+"))
					mode = ONE_OR_MORE;
				else
					if (enablesFor.equals("multiple") || enablesFor.equals("2+"))
						mode = MULTIPLE;
					else {
						try {
							mode = Integer.parseInt(enablesFor);
						} catch (NumberFormatException e) {
							mode = UNKNOWN;
						}
					}
	IConfigurationElement[] children = config.getChildren(TAG_SELECTION);
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
private boolean verifyClass(IAdaptable element, String className) {
	Class eclass = element.getClass();
	String eclassName = eclass.getName();
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
 * selection requirements. Element must at least pass
 * the type test, and optionally wildcard name match.
 */
private boolean verifyElement(IAdaptable element) {
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
