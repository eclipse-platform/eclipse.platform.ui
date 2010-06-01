/*******************************************************************************
 * Copyright (c) 2009, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 263709)
 ******************************************************************************/

package org.eclipse.core.internal.databinding;

import java.util.List;

import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.property.list.ListProperty;

/**
 * @since 3.3
 * 
 */
public class ValidationStatusProviderTargetsProperty extends ListProperty {
	public Object getElementType() {
		return IObservable.class;
	}

	protected List doGetList(Object source) {
		return ((ValidationStatusProvider) source).getTargets();
	}

	protected void doSetList(Object source, List list) {
		throw new UnsupportedOperationException(toString() + " is unmodifiable"); //$NON-NLS-1$
	}

	protected void doUpdateList(Object source, ListDiff diff) {
		throw new UnsupportedOperationException(toString() + " is unmodifiable"); //$NON-NLS-1$
	}

	public IObservableList observe(Realm realm, Object source) {
		return ((ValidationStatusProvider) source).getTargets();
	}

	public String toString() {
		return "ValidationStatusProvider#targets[] <IObservable>"; //$NON-NLS-1$
	}
}
