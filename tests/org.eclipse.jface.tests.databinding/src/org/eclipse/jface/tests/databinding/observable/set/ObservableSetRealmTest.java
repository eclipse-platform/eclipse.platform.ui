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

package org.eclipse.jface.tests.databinding.observable.set;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;

/**
 * @since 1.1
 */
public class ObservableSetRealmTest extends AbstractObservableSetRealmTestCase {	
	/* package */ class ObservableSetStub extends ObservableSet {
		/**
		 * @param wrappedSet
		 * @param elementType
		 */
		protected ObservableSetStub(Set wrappedSet, Object elementType) {
			super(wrappedSet, elementType);
		}
		
		public void getterCalled() {
			super.getterCalled();
		}
		
		public void fireSetChange(SetDiff diff) {
			super.fireSetChange(diff);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.databinding.observable.set.AbstractObservableSetRealmTestCase#doCreateSet()
	 */
	protected IObservableSet doCreateSet() {
		return new ObservableSetStub(new HashSet(), Object.class);
	}
}
