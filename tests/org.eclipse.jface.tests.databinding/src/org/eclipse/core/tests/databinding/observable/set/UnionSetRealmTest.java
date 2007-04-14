/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.set;

import java.lang.reflect.Method;

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.set.UnionSet;
import org.eclipse.core.databinding.observable.set.WritableSet;

/**
 * @since 1.1
 */
public class UnionSetRealmTest extends AbstractObservableSetRealmTestCase {
	private ISetChangeListener listener = new ISetChangeListener() {
		public void handleSetChange(SetChangeEvent event) {
			//do nothing
		}
	};
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.tests.databinding.observable.set.AbstractObservableSetRealmTestCase#doCreateSet()
	 */
	protected IObservableSet doCreateSet() {
		UnionSet set = new UnionSet(new IObservableSet[] { new WritableSet(),
				new WritableSet()});
		
		//Add a listener so that calculations will be eager.
		set.addSetChangeListener(listener);
		
		return set;
	}

	/**
	 * Overriding because method is not visible.
	 */
	protected Method getMethodGetterCalled() {
		return null;
	}

	/**
	 * Overriding because the method is not accessible.
	 */
	protected Method getMethodFireSetChange() {
		return null;
	}
}
