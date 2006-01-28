/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.beans;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IReadableSet;
import org.eclipse.jface.databinding.IUpdatableCellProvider;
import org.eclipse.jface.databinding.Updatable;
import org.eclipse.jface.util.Policy;

/**
 * @since 3.2
 *
 */
public class JavaBeansUpdatableCellProvider extends Updatable implements IUpdatableCellProvider {
	
	private final IReadableSet readableSet;
	private final String[] propertyNames;

	public JavaBeansUpdatableCellProvider(IReadableSet readableSet, String[] propertyNames) {
		this.readableSet = readableSet;
		this.propertyNames = propertyNames;
		readableSet.addChangeListener(listener);
	}
	
	private IChangeListener listener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			if (changeEvent.getChangeType() == ChangeEvent.ADD_MANY) {
				Collection added = (Collection)changeEvent.getNewValue();
				
				for (Iterator iter = added.iterator(); iter.hasNext();) {
					Object element = (Object) iter.next();
					
					addBeansListenerTo(element);
				}
			} else if (changeEvent.getChangeType() == ChangeEvent.REMOVE_MANY) {
				Collection removed = (Collection)changeEvent.getNewValue();
				
				for (Iterator iter = removed.iterator(); iter.hasNext();) {
					Object element = (Object) iter.next();
					
					removeBeansListenerFrom(element);
				}				
			}
		}	
	};

	/**
	 * @param element
	 */
	protected void addBeansListenerTo(Object element) {
		listenerSupport.hookListener(element);
	}

	/**
	 * @param element
	 */
	protected void removeBeansListenerFrom(Object element) {
		listenerSupport.unhookListener(element);
	}


	private boolean updating = false;
	
	private PropertyChangeListener elementListener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating) {
				if (Arrays.asList(propertyNames).indexOf(event.getPropertyName()) != -1) {
					fireChangeEvent(ChangeEvent.FUNCTION_CHANGED, null,
							Collections.singletonList(event.getSource()));
				}
			}
		}
	};
	
	private ListenerSupport listenerSupport = new ListenerSupport(elementListener);
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableTable#getReadableSet()
	 */
	public IReadableSet getReadableSet() {
		return readableSet;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.ICellProvider#getCellValue(java.lang.Object, int)
	 */
	public Object getCellValue(Object element, int index) {
		String prop = propertyNames[index];
		Method getter;
		try {
			getter = element.getClass().getMethod(
					"get"+ prop.substring(0, 1).toUpperCase(Locale.ENGLISH) + prop.substring(1), new Class[0]); //$NON-NLS-1$
			return getter.invoke(element, new Object[0]);						
		} catch (Exception e) {
			Policy.getLog().log(new Status(IStatus.ERROR, Policy.JFACE, IStatus.ERROR, "cannot get value", e)); //$NON-NLS-1$
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.ICellProvider#setCellValue(java.lang.Object, int, java.lang.Object)
	 */
	public void setCellValue(Object element, int index, Object value) {
		// TODO Auto-generated method stub

	}

}
