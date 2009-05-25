/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 226289
 *******************************************************************************/

package org.eclipse.core.internal.databinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.observable.ChangeEvent;
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
public class ValidationStatusMap extends ObservableMap {

	private boolean isDirty = true;

	private final WritableList bindings;

	private List dependencies = new ArrayList();

	private IChangeListener markDirtyChangeListener = new IChangeListener() {
		public void handleChange(ChangeEvent event) {
			markDirty();
		}
	};

	/**
	 * @param realm
	 * @param bindings
	 */
	public ValidationStatusMap(Realm realm, WritableList bindings) {
		super(realm, new HashMap());
		this.bindings = bindings;
		bindings.addChangeListener(markDirtyChangeListener);
	}

	public Object getKeyType() {
		return Binding.class;
	}

	public Object getValueType() {
		return IStatus.class;
	}

	protected void getterCalled() {
		recompute();
		super.getterCalled();
	}

	private void markDirty() {
		// since we are dirty, we don't need to listen anymore
		removeElementChangeListener();
		final Map oldMap = wrappedMap;
		// lazy computation of diff
		MapDiff mapDiff = new MapDiff() {
			private MapDiff cachedDiff = null;

			private void ensureCached() {
				if (cachedDiff == null) {
					recompute();
					cachedDiff = Diffs.computeMapDiff(oldMap, wrappedMap);
				}
			}

			public Set getAddedKeys() {
				ensureCached();
				return cachedDiff.getAddedKeys();
			}

			public Set getChangedKeys() {
				ensureCached();
				return cachedDiff.getChangedKeys();
			}

			public Object getNewValue(Object key) {
				ensureCached();
				return cachedDiff.getNewValue(key);
			}

			public Object getOldValue(Object key) {
				ensureCached();
				return cachedDiff.getOldValue(key);
			}

			public Set getRemovedKeys() {
				ensureCached();
				return cachedDiff.getRemovedKeys();
			}
		};
		wrappedMap = new HashMap();
		isDirty = true;
		fireMapChange(mapDiff);
	}

	private void recompute() {
		if (isDirty) {
			Map newContents = new HashMap();
			for (Iterator it = bindings.iterator(); it.hasNext();) {
				Binding binding = (Binding) it.next();
				IObservableValue validationError = binding
						.getValidationStatus();
				dependencies.add(validationError);
				validationError.addChangeListener(markDirtyChangeListener);
				IStatus validationStatusValue = (IStatus) validationError
						.getValue();
				newContents.put(binding, validationStatusValue);
			}
			wrappedMap.putAll(newContents);
			isDirty = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.databinding.observable.list.ObservableList#dispose()
	 */
	public synchronized void dispose() {
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
	
	public synchronized void addChangeListener(IChangeListener listener) {
		// this ensures that the next change will be seen by the new listener.
		recompute();
		super.addChangeListener(listener);
	}
	
	public synchronized void addMapChangeListener(IMapChangeListener listener) {
		// this ensures that the next change will be seen by the new listener.
		recompute();
		super.addMapChangeListener(listener);
	}

}
