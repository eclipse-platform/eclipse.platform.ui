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

import org.eclipse.core.databinding.observable.set.AbstractObservableSet;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;

/**
 * @since 1.1
 */
public class AbstractObservableSetRealmTest extends AbstractObservableSetRealmTestCase {
	/* package */static class ObservableSetStub extends AbstractObservableSet {
		private Set set;

		/* package */ObservableSetStub(Set set) {
			this.set = set;
		}

		protected Set getWrappedSet() {
			return set;
		}

		public Object getElementType() {
			return Object.class;
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
		return new ObservableSetStub(new HashSet());
	}
}
