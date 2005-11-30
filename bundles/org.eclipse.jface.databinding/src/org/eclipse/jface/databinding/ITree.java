/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;


/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * A domain model has to implement this interface in order to establish a tree
 * binding. It is possible that the domain model itself is not organized as a tree, and hence provides 
 * the ability to provide a tree facade.
 * 
 * @see TreeModelDescription for a simpler way to bind a tree. 
 * 
 * @since 3.2
 * 
 */
public interface ITree  {
		
	/**
	 * @since 3.2
	 *
	 */
	public static class ChangeEvent extends EventObject implements IChangeEvent {
		/**
		 * Events to use on changes to the tree
		 */
		private static final long serialVersionUID = 1L;
		private final Object   parent;
		private final Object   oldValue;
		private final Object   newValue;
		private final int	   position;
		private final int	   changeType;
		
		/**
		 * @param source ITree that has changed
		 * @param changeType 
		 * @param parent parent node element
		 * @param oldValue
		 * @param newValue
		 * @param index 
		 */
		public ChangeEvent(ITree source, int changeType, Object parent, 
						   Object oldValue, Object newValue, int index) {
			super(source);
			this.oldValue=oldValue;
			this.newValue=newValue;
			this.parent=parent;
			this.position=index;
			this.changeType=changeType;
		}
		
		/**
		 * @return parent element
		 */
		public Object getParent() {
			return parent;
		}
		
		/**
		 * @return position of changed element 
		 */
		public int getPosition() {
			return position;
		}

		/**
		 * Returns the change type (CHANGE, ADD, or REMOVE).
		 * 
		 * @return the change type
		 */
		public int getChangeType() {
			return changeType;
		}

		public Object getNewValue() {
			return newValue;
		}

		public Object getOldValue() {
			return oldValue;
		}
	}
	
	/**
	 * @since 3.2
	 *
	 */
	public interface ChangeListener  {
		/**
		 * @param evt
		 */
		void treeChange(ChangeEvent evt);
	}
	
	/**
	 * @since 3.2
	 *
	 */
	public static class ChangeSupport {
		private ITree source;
		private List listeners = null;
		
		/**
		 * @param source
		 */
		public ChangeSupport(ITree source){
			this.source=source;
		}
		
		/**
		 * @param listener
		 */
		public void addTreeChangeListener(ChangeListener listener) {
			if (listener!=null) {
				if (listeners==null)
					listeners=new ArrayList();
				listeners.add(listener);
			}
		}
		
		/**
		 * @param listener
		 */
		public void removeTreeChangeListener(ChangeListener listener) {
			if (listener==null || listeners==null)
				return;
			listeners.remove(listener);
		}
		
		/**
		 * @param changeType
		 * @param oldValue
		 * @param newValue
		 * @param parent
		 * @param index
		 */
		public void fireTreeChange(int changeType, Object oldValue, Object newValue, Object parent, int index) {
			ChangeEvent evt = new ChangeEvent(source, changeType, parent, oldValue, newValue, index);
			fireTreeChange(evt);
		}
		
		/**
		 * @param evt
		 */
		public void fireTreeChange(ChangeEvent evt) {
			Object oval = evt.getOldValue();
			Object nval = evt.getNewValue();
			
			if (listeners==null ||
				(oval != null && nval != null && oval.equals(nval)))
				return;
			
			ChangeListener[] list = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
			for (int i = 0; i < list.length; i++) {
				list[i].treeChange(evt);
			}
		}
	}
	
	
	//public static final String 
		
	/**
	 * Returns the child elements of the given parent element.
	 *
	 * @param parentElement the parent element, <code>null</code> for root elements
	 * @return an array of child elements
	 */

	public Object[] getChildren(Object parentElement);
	
	/**
	 * @param parentElement or <code>null</code> for root elements
	 * @param children
	 */
	public void setChildren(Object parentElement, Object[] children);

	/**
	 * Returns whether the given element has children.
	 *
	 * @param element the element
	 * @return <code>true</code> if the given element has children,
	 *  and <code>false</code> if it has no children
	 */
	public boolean hasChildren(Object element);
	
    /**
     * The implementor of an ITree is responsible to provide the event
     * model for changes on the tree's shape and content.  It should be using 
     * <code>ITree.ChangeEvent</code> to notify the listener of any changes to the tree.
     * 
     * @param listener
     */
    public void addTreeChangeListener(ChangeListener listener);
    
    /**
     * @param listener
     */
    public void removeTreeChangeListener(ChangeListener listener);

	
	/**
	 * @return types of all tree nodes
	 */
	public Class[] getTypes();

}
