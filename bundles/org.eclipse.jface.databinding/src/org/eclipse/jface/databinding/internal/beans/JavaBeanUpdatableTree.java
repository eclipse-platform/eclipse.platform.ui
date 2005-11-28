/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.internal.beans;

import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.ITree;
import org.eclipse.jface.databinding.IUpdatableTree;
import org.eclipse.jface.databinding.Updatable;



/**
 * @since 3.2
 *
 */
public class JavaBeanUpdatableTree extends Updatable implements IUpdatableTree {
	
	private ITree tree;
	private Class[] classTypes;
	
	private boolean updating=false;
	
	private Set elementsListenedTo = new HashSet();
	
	private static class IdentityWrapper {
		private final Object o;
		IdentityWrapper(Object o) {
			this.o = o;
		}
		public boolean equals(Object obj) {
			if(obj.getClass()!=IdentityWrapper.class) {
				return false;
			}
			return o==((IdentityWrapper)obj).o;
		}
		public int hashCode() {
			return System.identityHashCode(o);
		}
	}
	
	private PropertyChangeListener listener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating) {
				// TODO a tree is likely a large structure, we only
				//      need to track/register to elements that have been
				//      virtually propagated across

			}
		}
	};
	
	/**
	 * @param tree
	 */
	public JavaBeanUpdatableTree (ITree tree)  {
		this (tree, tree.getTypes());
	}
	
	/**
	 * @param tree
	 * @param classTypes 
	 */
	public JavaBeanUpdatableTree (ITree tree, Class[] classTypes)  {
		this.tree = tree;	
		this.classTypes = classTypes;
		hookListener(tree);
		
	}
	
	private void hookListener(Object target) {		
		Method addPropertyChangeListenerMethod = null;
		try {
			addPropertyChangeListenerMethod = target.getClass().getMethod(
					"addPropertyChangeListener", //$NON-NLS-1$
					new Class[] { PropertyChangeListener.class });
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}
		if (addPropertyChangeListenerMethod != null) {
			try {
				addPropertyChangeListenerMethod.invoke(target,
						new Object[] { listener });				
			} catch (IllegalArgumentException e) {
				// ignore
			} catch (IllegalAccessException e) {
				// ignore
			} catch (InvocationTargetException e) {
				// ignore
			}
		}		
	}

	private void unhookListener(Object target) {
		Method removePropertyChangeListenerMethod = null;
		try {
			removePropertyChangeListenerMethod = target.getClass().getMethod(
					"removePropertyChangeListener", //$NON-NLS-1$
					new Class[] { PropertyChangeListener.class });
		} catch (SecurityException e) {
			// ignore
		} catch (NoSuchMethodException e) {
			// ignore
		}
		if (removePropertyChangeListenerMethod != null) {
			try {
				removePropertyChangeListenerMethod.invoke(target,
						new Object[] { listener });
				return;
			} catch (IllegalArgumentException e) {
				// ignore
			} catch (IllegalAccessException e) {
				// ignore
			} catch (InvocationTargetException e) {
				// ignore
			}
		}
	}
	
	private void primRemoveElement(Object[] source, int index) {
		Object[] newArray = new Object[source.length-1];		
		System.arraycopy(source, 0, newArray, 0, index);		
		System.arraycopy(source, index+1, newArray, index, source.length-index);
	}
	
	private void primAddElement(Object[] source, Object element, int index) {
		Object[] newArray = new Object[source.length+1];		
		System.arraycopy(source, 0, newArray, 0, index);
		newArray[index]=element;
		System.arraycopy(source, index+1, newArray, index+1, source.length-index);
	}

	public int addElement(Object parentElement, int index, Object value) {
		updating=true;
		try {
			Object[] list = tree.getChildren(parentElement);
			if (index <= 0 || index > list.length)
				index = list.length;
			primAddElement(list, value, index);
			hookListener(value);
			fireChangeEvent(ChangeEvent.ADD, null, value, parentElement, index);
			return index;
		} finally {
			updating=false;
		}
	}

	public void removeElement(Object parentElement, int index) {
		updating=true;
		try {
			Object[] list = tree.getChildren(parentElement);
			if (list == null || index < 0 || index >= list.length)
				return;

			Object o = list[index];
			primRemoveElement(list,index);
			fireChangeEvent(ChangeEvent.REMOVE, o, null, parentElement, index);
		} finally {
			updating=false;
		}
	}

	public void setElement(Object parentElement, int index, Object value) {
		updating = true;
		try {
			Object[] list = tree.getChildren(parentElement);
			if (list==null || index<0 || index>=list.length) return;
			
			Object oldValue = list[index];
			list[index]= value;
			fireChangeEvent(ChangeEvent.CHANGE, oldValue, value, parentElement, index);
		} finally {
			updating = false;
		}
		
	}

	public void setElements(Object parentElement, Object[] values) {
		updating = true;
		try {
			Object[] list = tree.getChildren(parentElement);			
			tree.setChildren(parentElement, values);			
			fireChangeEvent(ChangeEvent.CHANGE, list, values, parentElement, -1);
		} finally {
			updating = false;
		}
		
	}

	public Object getElement(Object parentElement, int index) {
		Object[] list = tree.getChildren(parentElement);
		if (list==null || index<0 || index>=list.length) return null;
		
		return list[index];
	}

	public Object[] getElements(Object parentElement) {
		return tree.getChildren(parentElement);		
	}	

	public void dispose() {
		super.dispose();
		for (Iterator it = elementsListenedTo.iterator(); it.hasNext();) {
			unhookListener(it.next());
		}
		unhookListener(tree);
	}

	public Class[] getTypes() {
		return classTypes;
	}





}
