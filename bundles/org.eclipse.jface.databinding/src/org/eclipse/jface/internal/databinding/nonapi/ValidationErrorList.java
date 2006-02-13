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

package org.eclipse.jface.internal.databinding.nonapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.internal.databinding.api.IBinding;
import org.eclipse.jface.internal.databinding.api.observable.Diffs;
import org.eclipse.jface.internal.databinding.api.observable.IChangeListener;
import org.eclipse.jface.internal.databinding.api.observable.IObservable;
import org.eclipse.jface.internal.databinding.api.observable.list.IListDiff;
import org.eclipse.jface.internal.databinding.api.observable.list.IListDiffEntry;
import org.eclipse.jface.internal.databinding.api.observable.list.ObservableList;
import org.eclipse.jface.internal.databinding.api.observable.list.WritableList;
import org.eclipse.jface.internal.databinding.api.observable.value.IObservableValue;

/**
 * @since 3.2
 * 
 */
public class ValidationErrorList extends ObservableList {

	private boolean isDirty = true;

	private final WritableList bindings;

	private final boolean usePartialErrors;

	private List dependencies = new ArrayList();

	private IChangeListener markDirtyChangeListener = new IChangeListener() {
		public void handleChange(IObservable source) {
			markDirty();
		}
	};

	protected ValidationErrorList(WritableList bindings,
			boolean usePartialErrors) {
		super(new ArrayList());
		this.bindings = bindings;
		this.usePartialErrors = usePartialErrors;
		bindings.addChangeListener(markDirtyChangeListener);
	}

	protected void getterCalled() {
		recompute();
		super.getterCalled();
	}

	private void markDirty() {
		// since we are dirty, we don't need to listen anymore
		removeElementChangeListener();
		final List oldList = wrappedList;
		// lazy computation of diff
		IListDiff listDiff = new IListDiff() {
			IListDiffEntry[] cachedDifferences = null;
			public IListDiffEntry[] getDifferences() {
				if(cachedDifferences==null) {
					recompute();
					cachedDifferences = Diffs.computeDiff(oldList, wrappedList).getDifferences();
				}
				return cachedDifferences;
			}
		};
		wrappedList = new ArrayList();
		isDirty = true;
		fireListChange(listDiff);
	}

	private void recompute() {
		if (isDirty) {
			List newContents = new ArrayList();
			for (Iterator it = bindings.iterator(); it.hasNext();) {
				IBinding binding = (IBinding) it.next();
				IObservableValue validationError = usePartialErrors ? binding
						.getPartialValidationError() : binding
						.getValidationError();
				dependencies.add(validationError);
				validationError.addChangeListener(markDirtyChangeListener);
				Object validationErrorValue = validationError.getValue();
				if (validationErrorValue != null) {
					newContents.add(validationErrorValue);
				}
			}
			wrappedList.addAll(newContents);
			isDirty = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.internal.databinding.api.observable.list.ObservableList#dispose()
	 */
	public void dispose() {
		bindings.removeChangeListener(markDirtyChangeListener);
		removeElementChangeListener();
		super.dispose();
	}

	private void removeElementChangeListener() {
		for (Iterator it = dependencies.iterator(); it.hasNext();) {
			IObservableValue observableValue = (IObservableValue) it.next();
			observableValue.removeChangeListener(markDirtyChangeListener);
		}
	}

}
