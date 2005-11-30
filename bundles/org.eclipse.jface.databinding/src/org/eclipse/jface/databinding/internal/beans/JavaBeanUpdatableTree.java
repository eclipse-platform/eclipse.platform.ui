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

import org.eclipse.jface.databinding.IChangeEvent;
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
	

	
	private PropertyChangeListener treeListener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating) {
				updating=true; 
				try {
					ITree.ChangeEvent e = (ITree.ChangeEvent)event;
					fireChangeEvent(e.getChangeType(), e.getOldValue(), e.getNewValue(), e.getParent(), e.getPosition());
				} 
				finally{
					updating=false;
				}
			}
		}
	};
	
	/**
	 * @param tree
	 */
	public JavaBeanUpdatableTree (ITree tree)  {
		this.tree = tree;	
		this.classTypes = tree.getTypes();
		tree.addPropertyChangeListener(treeListener);	
	}
		
	private Object[] primRemoveElement(Object[] source, int index) {
		Object[] newArray = new Object[source.length-1];		
		System.arraycopy(source, 0, newArray, 0, index);		
		System.arraycopy(source, index+1, newArray, index, source.length-index);
		return newArray;
	}
	
	private Object[] primAddElement(Object[] source, Object element, int index) {
		Object[] newArray = new Object[source.length+1];		
		System.arraycopy(source, 0, newArray, 0, index);
		newArray[index]=element;
		System.arraycopy(source, index+1, newArray, index+1, source.length-index);
		return newArray;
	}

	public int addElement(Object parentElement, int index, Object value) {
		updating=true;
		try {
			Object[] list = tree.getChildren(parentElement);
			if (index <= 0 || index > list.length)
				index = list.length;
			Object[] newList = primAddElement(list, value, index);	
			tree.setChildren(parentElement, newList);
			fireChangeEvent(IChangeEvent.ADD, null, value, parentElement, index);
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
			Object[] newList=primRemoveElement(list,index);
			tree.setChildren(parentElement, newList);
			fireChangeEvent(IChangeEvent.REMOVE, o, null, parentElement, index);
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
			fireChangeEvent(IChangeEvent.CHANGE, oldValue, value, parentElement, index);
		} finally {
			updating = false;
		}
		
	}

	public void setElements(Object parentElement, Object[] values) {
		updating = true;
		try {
			Object[] list = tree.getChildren(parentElement);			
			tree.setChildren(parentElement, values);			
			fireChangeEvent(IChangeEvent.CHANGE, list, values, parentElement, -1);
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
		tree.removePropertyChangeListener(treeListener);
	}

	public Class[] getTypes() {
		return classTypes;
	}





}
