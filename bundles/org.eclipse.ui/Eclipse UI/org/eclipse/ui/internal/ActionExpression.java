package org.eclipse.ui.internal;

/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionFilter;

/**
 * An ActionExpression is used to evaluate the enablement / visibility
 * criteria for an action.  
 */
public class ActionExpression {

	private SingleExpression root;
	
	public ActionExpression(IConfigurationElement element) {
		try {
			root = new SingleExpression();
			root.readFrom(element);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			root = null;
		}
	}
	
	public boolean isEnabledFor(Object object) {
		if (root == null)
			return false;
		return root.isEnabledFor(object);
	}
	
	public boolean isEnabledFor(IStructuredSelection ssel) {
		if (root == null)
			return false;
		if (ssel.isEmpty()) {
			// We should pass a safe object in when there is no
			// selection, to avoid NPE's.
			return root.isEnabledFor(this);
		}
		for (Iterator elements=ssel.iterator(); elements.hasNext();) {
			Object obj = elements.next();
			if (!isEnabledFor(obj))
				return false;
		}
		return true;
	}

	protected AbstractExpression createExpression(IConfigurationElement element)
		throws IllegalStateException 
	{
		AbstractExpression childExpr = null;
		String tag = element.getName();
		if (tag.equals("or")) { //$NON-NLS-1$
			childExpr = new OrExpression();
			childExpr.readFrom(element);
		} else if (tag.equals("and")) { //$NON-NLS-1$
			childExpr = new AndExpression();
			childExpr.readFrom(element);
		} else if (tag.equals("not")) { //$NON-NLS-1$
			childExpr = new NotExpression();
			childExpr.readFrom(element);
		} else if (tag.equals("objectState")) { //$NON-NLS-1$
			childExpr = new ObjectStateExpression();
			childExpr.readFrom(element);
		} else if (tag.equals("objectClass")) { //$NON-NLS-1$
			childExpr = new ObjectClassExpression();
			childExpr.readFrom(element);
		} else if (tag.equals("pluginState")) { //$NON-NLS-1$
			childExpr = new PluginStateExpression();
			childExpr.readFrom(element);
		} else if (tag.equals("systemProperty")) { //$NON-NLS-1$
			childExpr = new SystemPropertyExpression();
			childExpr.readFrom(element);
		} else {
			throw new IllegalStateException("Unreconized element: " + tag); //$NON-NLS-1$
		}
		return childExpr;
	}
	
	protected abstract class AbstractExpression {
		public abstract void readFrom(IConfigurationElement element) 
			throws IllegalStateException;
		public abstract boolean isEnabledFor(Object obj);
	}
	
	protected abstract class CompositeExpression extends AbstractExpression {
		protected ArrayList list = new ArrayList();
		public void readFrom(IConfigurationElement element) 
			throws IllegalStateException 
		{
			IConfigurationElement [] children = element.getChildren();
			if (children.length == 0)
				throw new IllegalStateException("Expression must have 1 or more children"); //$NON-NLS-1$
			for (int nX = 0; nX < children.length; nX ++) {
				String tag = children[nX].getName();
				AbstractExpression expr = createExpression(children[nX]);
				if (tag.equals("objectClass")) //$NON-NLS-1$
					prepend(expr);
				else
					append(expr);
			}
		}
		public void append(AbstractExpression expr) {
			list.add(expr);
			list.trimToSize();
		}
		public void prepend(AbstractExpression expr) {
			list.add(0, expr);
			list.trimToSize();
		}
	}
	
	protected class SingleExpression extends AbstractExpression {
		AbstractExpression child;
		public void readFrom(IConfigurationElement element) 
			throws IllegalStateException 
		{
			IConfigurationElement [] children = element.getChildren();
			if (children.length != 1)
				throw new IllegalStateException("Expression must have 1 child"); //$NON-NLS-1$
			child = createExpression(children[0]);
		}
		public boolean isEnabledFor(Object obj) {
			return child.isEnabledFor(obj);
		}
	}
	
	protected class NotExpression extends SingleExpression {
		public boolean isEnabledFor(Object obj) {
			return !super.isEnabledFor(obj);
		}
	}
		
	protected class OrExpression extends CompositeExpression {
		public boolean isEnabledFor(Object obj) {
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				AbstractExpression expr = (AbstractExpression)iter.next();
				if (expr.isEnabledFor(obj))
					return true;
			}
			return false;
		}
	}
	
	protected class AndExpression extends CompositeExpression {
		public boolean isEnabledFor(Object obj) {
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				AbstractExpression expr = (AbstractExpression)iter.next();
				if (!expr.isEnabledFor(obj))
					return false;
			}
			return true;
		}
	}

	protected class ObjectStateExpression extends AbstractExpression {
		private String name, value;
		public void readFrom(IConfigurationElement element) 
			throws IllegalStateException
		{
			name = element.getAttribute("name");//$NON-NLS-1$
			value = element.getAttribute("value");//$NON-NLS-1$
			if (name == null || value == null)
				throw new IllegalStateException();
		}
		public boolean isEnabledFor(Object object) {
			// Try out the object.
			if (this.preciselyMatches(object))
				return true;
				
			// If not adaptable, or the object is a resource, just return.
			if (object instanceof IResource)
				return false;
				
			// Try out the underlying resource.
			IResource res = null;
			if (object instanceof IAdaptable)
				res = (IResource)((IAdaptable)object).getAdapter(IResource.class);
			if (res == null)
				return false;
			return this.preciselyMatches(res);
		}
		private boolean preciselyMatches(Object object) {
			// Get the action filter.
			IActionFilter filter = getActionFilter(object);
			if (filter == null)
				return false;
				
			// Run the action filter.
			return filter.testAttribute(object, name, value);
		}
		private IActionFilter getActionFilter(Object object) {
			IActionFilter filter = null;
			if (object instanceof IActionFilter)
				filter = (IActionFilter)object;
			else if (object instanceof IAdaptable)
				filter = (IActionFilter)((IAdaptable)object).getAdapter(IActionFilter.class);
			return filter;
		}
	}

	protected class ObjectClassExpression extends AbstractExpression {
		private String className;
		public void readFrom(IConfigurationElement element) 
			throws IllegalStateException
		{
			className = element.getAttribute("name");//$NON-NLS-1$
			if (className == null)
				throw new IllegalStateException();
		}
		public boolean isEnabledFor(Object element) {
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
	}

	protected class PluginStateExpression extends AbstractExpression {
		private String id, value;
		public void readFrom(IConfigurationElement element) 
			throws IllegalStateException
		{
			id = element.getAttribute("id");//$NON-NLS-1$
			value = element.getAttribute("value");//$NON-NLS-1$
			if (id == null || value == null)
				throw new IllegalStateException();
		}
		public boolean isEnabledFor(Object object) {
			IPluginRegistry reg = Platform.getPluginRegistry();
			IPluginDescriptor desc = reg.getPluginDescriptor(id);
			if (desc == null)
				return false;
			if (value.equals("installed")) //$NON-NLS-1$
				return true;
			else if (value.equals("activated")) //$NON-NLS-1$
				return desc.isPluginActivated();
			else 
				return false;
		}
	}

	protected class SystemPropertyExpression extends AbstractExpression {
		private String name, value;
		public void readFrom(IConfigurationElement element) 
			throws IllegalStateException
		{
			name = element.getAttribute("name");//$NON-NLS-1$
			value = element.getAttribute("value");//$NON-NLS-1$
			if (name == null || value == null)
				throw new IllegalStateException();
		}
		public boolean isEnabledFor(Object object) {
			String str = System.getProperty(name);
			if (str == null)
				return false;
			boolean b = value.equals(str);
			return b;
		}
	}

}

