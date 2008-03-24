/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * Simple factory implementation that will fail on command. This is used to test
 * that working set restoration does not die if one of the factories dies.
 * 
 * @since 3.4
 * 
 */
public class BadElementFactory implements IElementFactory {

	/**
	 * Set to cause the factory to fail.
	 */
	public static boolean fail = false;
	

	/**
	 * Set to true when {@link #createElement(IMemento)} is called while fail is true.
	 */
	public static boolean failAttempted = false;

	public static class BadElementInstance implements IAdaptable,
			IPersistableElement {

		/**
		 * Set to cause save to fail.
		 */
		public static boolean fail = false;
		

		/**
		 * Set to true when {@link #saveState(IMemento)} is called while fail is true.
		 */
		public static boolean failAttempted = false;


		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class adapter) {
			if (adapter.equals(IPersistableElement.class))
				return this;
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
		 */
		public String getFactoryId() {
			return "org.eclipse.ui.tests.badFactory";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
		 */
		public void saveState(IMemento memento) {
			if (fail) {
				failAttempted = true;
				throw new RuntimeException();
			}

		}

	}
;

	public IAdaptable createElement(IMemento memento) {
		if (fail) {
			failAttempted = true;
			throw new RuntimeException();
		}
		return new BadElementInstance();
	}

}
