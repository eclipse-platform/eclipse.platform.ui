/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.observable.masterdetail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.databinding.observable.Diffs;
import org.eclipse.jface.databinding.observable.list.IListChangeListener;
import org.eclipse.jface.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.observable.list.ListDiff;
import org.eclipse.jface.databinding.observable.list.ObservableList;
import org.eclipse.jface.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.observable.value.IValueChangeListener;
import org.eclipse.jface.databinding.observable.value.ValueDiff;

/**
 * @since 3.2
 * 
 */

/* package */class DetailObservableList extends ObservableList {

	private boolean updating = false;

	private IListChangeListener innerChangeListener = new IListChangeListener() {
		public void handleListChange(IObservableList source, ListDiff diff) {
			if (!updating) {
				fireListChange(diff);
			}
		}
	};

	private Object currentOuterValue;

	private IObservableList innerObservableList;

	private IObservableFactory factory;

	private IObservableValue outerObservableValue;

	/**
	 * @param factory
	 * @param outerObservableValue
	 * @param feature
	 * @param detailType
	 */
	public DetailObservableList(IObservableFactory factory,
			IObservableValue outerObservableValue, Object detailType) {
		super(new ArrayList(), detailType);
		this.factory = factory;
		this.outerObservableValue = outerObservableValue;
		updateInnerObservableValue(outerObservableValue);

		outerObservableValue.addValueChangeListener(outerChangeListener);
	}

	IValueChangeListener outerChangeListener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source, ValueDiff diff) {
			List oldList = new ArrayList(wrappedList);
			updateInnerObservableValue(outerObservableValue);
			fireListChange(Diffs.computeListDiff(oldList, wrappedList));
		}
	};

	private void updateInnerObservableValue(
			IObservableValue outerObservableValue) {
		currentOuterValue = outerObservableValue.getValue();
		if (innerObservableList != null) {
			innerObservableList.removeListChangeListener(innerChangeListener);
			innerObservableList.dispose();
		}
		if (currentOuterValue == null) {
			innerObservableList = null;
			wrappedList = new ArrayList();
		} else {
			this.innerObservableList = (IObservableList) factory
					.createObservable(currentOuterValue);
			wrappedList = innerObservableList;
			Object innerValueType = innerObservableList.getElementType();
			Assert.isTrue(getElementType().equals(innerValueType),
					"Cannot change value type in a nested updatable value"); //$NON-NLS-1$
			innerObservableList.addListChangeListener(innerChangeListener);
		}
	}

	public void dispose() {
		super.dispose();

		if (outerObservableValue != null) {
			outerObservableValue.removeValueChangeListener(outerChangeListener);
			outerObservableValue.dispose();
		}
		if (innerObservableList != null) {
			innerObservableList.removeListChangeListener(innerChangeListener);
			innerObservableList.dispose();
		}
		currentOuterValue = null;
		factory = null;
		innerObservableList = null;
		innerChangeListener = null;
	}

}