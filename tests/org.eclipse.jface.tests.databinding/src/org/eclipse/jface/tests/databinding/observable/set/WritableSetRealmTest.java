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

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.set.WritableSet;

/**
 * @since 1.1
 */
public class WritableSetRealmTest extends AbstractObservableSetRealmTestCase {
	protected IObservableSet doCreateSet() {
		return new WritableSetStub();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.databinding.observable.set.AbstractObservableSetRealmTestCase#isMutable()
	 */
	protected boolean isMutable() {
		return true;
	}
	
	private static class WritableSetStub extends WritableSet {
		public void getterCalled() {
			super.getterCalled();
		}
		
		public void fireSetChange(SetDiff diff) {
			super.fireSetChange(diff);
		}
	}
}
