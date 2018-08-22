/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 226289
 *******************************************************************************/

package org.eclipse.core.internal.databinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.map.ObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;

/**
 * @since 1.0
 *
 */
public class ValidationStatusMap extends ObservableMap<Binding, IStatus> {

	private boolean isDirty = true;

	private final WritableList<Binding> bindings;

	private List<IObservableValue<IStatus>> dependencies = new ArrayList<>();

	private IChangeListener markDirtyChangeListener = event -> markDirty();

	/**
	 * @param realm
	 * @param bindings
	 */
	public ValidationStatusMap(Realm realm, WritableList<Binding> bindings) {
		super(realm, new HashMap<>());
		this.bindings = bindings;
		bindings.addChangeListener(markDirtyChangeListener);
	}

	@Override
	public Object getKeyType() {
		return Binding.class;
	}

	@Override
	public Object getValueType() {
		return IStatus.class;
	}

	@Override
	protected void getterCalled() {
		recompute();
		super.getterCalled();
	}

	private void markDirty() {
		// since we are dirty, we don't need to listen anymore
		removeElementChangeListener();
		final Map<Binding, IStatus> oldMap = wrappedMap;
		// lazy computation of diff
		MapDiff<Binding, IStatus> mapDiff = new MapDiff<Binding, IStatus>() {
			private MapDiff<Binding, IStatus> cachedDiff = null;

			private void ensureCached() {
				if (cachedDiff == null) {
					recompute();
					cachedDiff = Diffs.computeMapDiff(oldMap, wrappedMap);
				}
			}

			@Override
			public Set<Binding> getAddedKeys() {
				ensureCached();
				return cachedDiff.getAddedKeys();
			}

			@Override
			public Set<Binding> getChangedKeys() {
				ensureCached();
				return cachedDiff.getChangedKeys();
			}

			@Override
			public IStatus getNewValue(Object key) {
				ensureCached();
				return cachedDiff.getNewValue(key);
			}

			@Override
			public IStatus getOldValue(Object key) {
				ensureCached();
				return cachedDiff.getOldValue(key);
			}

			@Override
			public Set<Binding> getRemovedKeys() {
				ensureCached();
				return cachedDiff.getRemovedKeys();
			}
		};
		wrappedMap = new HashMap<>();
		isDirty = true;
		fireMapChange(mapDiff);
	}

	private void recompute() {
		if (isDirty) {
			Map<Binding, IStatus> newContents = new HashMap<>();
			for (Binding binding : bindings) {
				IObservableValue<IStatus> validationError = binding.getValidationStatus();
				dependencies.add(validationError);
				validationError.addChangeListener(markDirtyChangeListener);
				IStatus validationStatusValue = validationError.getValue();
				newContents.put(binding, validationStatusValue);
			}
			wrappedMap.putAll(newContents);
			isDirty = false;
		}
	}

	@Override
	public synchronized void dispose() {
		bindings.removeChangeListener(markDirtyChangeListener);
		removeElementChangeListener();
		super.dispose();
	}

	private void removeElementChangeListener() {
		for (IObservableValue<IStatus> observableValue : dependencies) {
			observableValue.removeChangeListener(markDirtyChangeListener);
		}
	}

	@Override
	public synchronized void addChangeListener(IChangeListener listener) {
		// this ensures that the next change will be seen by the new listener.
		recompute();
		super.addChangeListener(listener);
	}

	@Override
	public synchronized void addMapChangeListener(IMapChangeListener<? super Binding, ? super IStatus> listener) {
		// this ensures that the next change will be seen by the new listener.
		recompute();
		super.addMapChangeListener(listener);
	}

}
