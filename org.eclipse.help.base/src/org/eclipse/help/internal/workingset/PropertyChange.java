/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.workingset;

import java.util.*;

/**
 * Utility classes copied from the org.eclipse.core.runtime.Preferences class.
 */
public class PropertyChange {

	/**
	 * Listener for property changes.
	 * <p>
	 * Usage:
	 * 
	 * <pre>
	 * 
	 *  
	 *   
	 *    
	 *     
	 *      Preferences.IPropertyChangeListener listener =
	 *        new Preferences.IPropertyChangeListener() {
	 *           public void propertyChange(Preferences.PropertyChangeEvent event) {
	 *              ... // code to deal with occurrence of property change
	 *           }
	 *        };
	 *      emitter.addPropertyChangeListener(listener);
	 *      ...
	 *      emitter.removePropertyChangeListener(listener);
	 *      
	 *     
	 *    
	 *   
	 *  
	 * </pre>
	 * 
	 * </p>
	 */
	public interface IPropertyChangeListener extends EventListener {

		/**
		 * Notification that a property has changed.
		 * <p>
		 * This method gets called when the observed object fires a property
		 * change event.
		 * </p>
		 * 
		 * @param event
		 *            the property change event object describing which property
		 *            changed and how
		 */
		public void propertyChange(PropertyChangeEvent event);
	}

	/**
	 * An event object describing a change to a named property.
	 * <p>
	 * The preferences object reports property change events for internal state
	 * changes that may be of interest to external parties. A special listener
	 * interface (<code>Preferences.IPropertyChangeListener</code>) is
	 * defined for this purpose. Listeners are registered via the
	 * <code>Preferences.addPropertyChangeListener</code> method.
	 * </p>
	 * <p>
	 * Clients cannot instantiate or subclass this class.
	 * </p>
	 * 
	 * @see WorkingSetManager#addPropertyChangeListener
	 * @see PropertyChange.IPropertyChangeListener
	 */
	public static class PropertyChangeEvent extends EventObject {
		private static final long serialVersionUID = 1L;

		/**
		 * The name of the changed property.
		 */
		private String propertyName;

		/**
		 * The old value of the changed property, or <code>null</code> if not
		 * known or not relevant.
		 */
		private Object oldValue;

		/**
		 * The new value of the changed property, or <code>null</code> if not
		 * known or not relevant.
		 */
		private Object newValue;

		/**
		 * Creates a new property change event.
		 * 
		 * @param source
		 *            the object whose property has changed
		 * @param property
		 *            the property that has changed (must not be
		 *            <code>null</code>)
		 * @param oldValue
		 *            the old value of the property, or <code>null</code> if
		 *            none
		 * @param newValue
		 *            the new value of the property, or <code>null</code> if
		 *            none
		 */
		PropertyChangeEvent(Object source, String property, Object oldValue,
				Object newValue) {

			super(source);
			if (property == null) {
				throw new IllegalArgumentException();
			}
			this.propertyName = property;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		/**
		 * Returns the name of the property that changed.
		 * <p>
		 * Warning: there is no guarantee that the property name returned is a
		 * constant string. Callers must compare property names using
		 * <code>equals</code>, not ==.
		 * </p>
		 * 
		 * @return the name of the property that changed
		 */
		public String getProperty() {
			return propertyName;
		}

		/**
		 * Returns the new value of the property.
		 * 
		 * @return the new value, or <code>null</code> if not known or not
		 *         relevant
		 */
		public Object getNewValue() {
			return newValue;
		}

		/**
		 * Returns the old value of the property.
		 * 
		 * @return the old value, or <code>null</code> if not known or not
		 *         relevant
		 */
		public Object getOldValue() {
			return oldValue;
		}
	}

	/**
	 * Internal class is used to maintain a list of listeners. It is a fairly
	 * lightweight object, occupying minimal space when no listeners are
	 * registered.
	 * <p>
	 * Note that the <code>add</code> method checks for and eliminates
	 * duplicates based on identity (not equality). Likewise, the
	 * <code>remove</code> method compares based on identity.
	 * </p>
	 * <p>
	 * Use the <code>getListeners</code> method when notifying listeners. Note
	 * that no garbage is created if no listeners are registered. The
	 * recommended code sequence for notifying all registered listeners of say,
	 * <code>FooListener.eventHappened</code>, is:
	 * 
	 * <pre>
	 * Object[] listeners = myListenerList.getListeners();
	 * for (int i = 0; i &lt; listeners.length; ++i) {
	 * 	((FooListener) listeners[i]).eventHappened(event);
	 * }
	 * </pre>
	 * 
	 * </p>
	 */
	public static class ListenerList {
		/**
		 * The initial capacity of the list. Always >= 1.
		 */
		private int capacity;

		/**
		 * The current number of listeners. Maintains invariant: 0 <= size <=
		 * listeners.length.
		 */
		private int size;

		/**
		 * The list of listeners. Initially <code>null</code> but initialized
		 * to an array of size capacity the first time a listener is added.
		 * Maintains invariant: listeners != null IFF size != 0
		 */
		private Object[] listeners = null;

		/**
		 * The empty array singleton instance, returned by getListeners() when
		 * size == 0.
		 */
		private static final Object[] EmptyArray = new Object[0];

		/**
		 * Creates a listener list with an initial capacity of 3.
		 */
		public ListenerList() {
			this(3);
		}

		/**
		 * Creates a listener list with the given initial capacity.
		 * 
		 * @param capacity
		 *            the number of listeners which this list can initially
		 *            accept without growing its internal representation; must
		 *            be at least 1
		 */
		public ListenerList(int capacity) {
			if (capacity < 1) {
				throw new IllegalArgumentException();
			}
			this.capacity = capacity;
		}

		/**
		 * Adds the given listener to this list. Has no effect if an identical
		 * listener is already registered.
		 * 
		 * @param listener
		 *            the listener
		 */
		public void add(Object listener) {
			if (listener == null) {
				throw new IllegalArgumentException();
			}
			if (size == 0) {
				listeners = new Object[capacity];
			} else {
				// check for duplicates using identity
				for (int i = 0; i < size; ++i) {
					if (listeners[i] == listener) {
						return;
					}
				}
				// grow array if necessary
				if (size == listeners.length) {
					System.arraycopy(listeners, 0,
							listeners = new Object[size * 2 + 1], 0, size);
				}
			}
			listeners[size++] = listener;
		}

		/**
		 * Returns an array containing all the registered listeners. The
		 * resulting array is unaffected by subsequent adds or removes. If there
		 * are no listeners registered, the result is an empty array singleton
		 * instance (no garbage is created). Use this method when notifying
		 * listeners, so that any modifications to the listener list during the
		 * notification will have no effect on the notification itself.
		 * 
		 * @return the list of registered listeners
		 */
		public Object[] getListeners() {
			if (size == 0)
				return EmptyArray;
			Object[] result = new Object[size];
			System.arraycopy(listeners, 0, result, 0, size);
			return result;
		}

		/**
		 * Returns whether this listener list is empty.
		 * 
		 * @return <code>true</code> if there are no registered listeners, and
		 *         <code>false</code> otherwise
		 */
		public boolean isEmpty() {
			return size == 0;
		}

		/**
		 * Removes the given listener from this list. Has no effect if an
		 * identical listener was not already registered.
		 * 
		 * @param listener
		 *            the listener
		 */
		public void remove(Object listener) {
			if (listener == null) {
				throw new IllegalArgumentException();
			}
			for (int i = 0; i < size; ++i) {
				if (listeners[i] == listener) {
					if (size == 1) {
						listeners = null;
						size = 0;
					} else {
						System.arraycopy(listeners, i + 1, listeners, i, --size
								- i);
						listeners[size] = null;
					}
					return;
				}
			}
		}

		/**
		 * Returns the number of registered listeners.
		 * 
		 * @return the number of registered listeners
		 */
		public int size() {
			return size;
		}
	}
}
