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

package org.eclipse.jface.internal.databinding.provisional.observable.set;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.internal.databinding.provisional.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IMappingChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IObservableMapping;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.MappingDiff;

/**
 * @since 1.0
 * 
 */
public class MappedSet extends ObservableSet {

	private final IObservableMapping wrappedMapping;

	/*
	 * Map from values (range elements) to Integer ref counts
	 */
	private Map valueCounts = new HashMap();

	private ISetChangeListener domainListener = new ISetChangeListener() {
		public void handleSetChange(IObservableSet source, SetDiff diff) {
			Set additions = new HashSet();
			for (Iterator it = diff.getAdditions().iterator(); it.hasNext();) {
				Object added = it.next();
				Object mappingValue = wrappedMapping.getMappingValue(added);
				if (handleAddition(mappingValue)) {
					additions.add(mappingValue);
				}
			}
			Set removals = new HashSet();
			for (Iterator it = diff.getRemovals().iterator(); it.hasNext();) {
				Object removed = it.next();
				Object mappingValue = wrappedMapping.getMappingValue(removed);
				if (handleRemoval(mappingValue)) {
					removals.add(mappingValue);
				}
			}
			fireSetChange(Diffs.createSetDiff(additions, removals));
		}
	};

	private IMappingChangeListener mappingChangeListener = new IMappingChangeListener() {
		public void handleMappingValueChange(IObservable source,
				MappingDiff diff) {
			Set affectedElements = diff.getElements();
			Set additions = new HashSet();
			Set removals = new HashSet();
			for (Iterator it = affectedElements.iterator(); it.hasNext();) {
				Object element = it.next();
				Object oldFunctionValue = diff.getOldMappingValues(element,
						new int[] { 0 })[0];
				Object newFunctionValue = diff.getNewMappingValues(element,
						new int[] { 0 });
				if (handleRemoval(oldFunctionValue)) {
					removals.add(oldFunctionValue);
				}
				if (handleAddition(newFunctionValue)) {
					additions.add(newFunctionValue);
				}
			}
			fireSetChange(Diffs.createSetDiff(additions, removals));
		}
	};

	private IObservableSet input;

	/**
	 * @param input
	 * @param mapping
	 */
	public MappedSet(IObservableSet input, IObservableMapping mapping) {
		super(Collections.EMPTY_SET, mapping.getValueType());
		setWrappedSet(valueCounts.keySet());
		this.wrappedMapping = mapping;
		this.input = input;
		for (Iterator it = input.iterator(); it.hasNext();) {
			Object element = it.next();
			Object functionValue = wrappedMapping.getMappingValue(element);
			handleAddition(functionValue);
		}
		input.addSetChangeListener(domainListener);
		mapping.addMappingChangeListener(mappingChangeListener);
	}

	/**
	 * @param mappingValue
	 * @return true if the given mappingValue was an addition
	 */
	protected boolean handleAddition(Object mappingValue) {
		Integer count = (Integer) valueCounts.get(mappingValue);
		if (count == null) {
			valueCounts.put(mappingValue, new Integer(1));
			return true;
		}
		valueCounts.put(mappingValue, new Integer(count.intValue() + 1));
		return false;
	}

	/**
	 * @param mappingValue
	 * @return true if the given mappingValue has been removed
	 */
	protected boolean handleRemoval(Object mappingValue) {
		Integer count = (Integer) valueCounts.get(mappingValue);
		if (count.intValue() <= 1) {
			valueCounts.remove(mappingValue);
			return true;
		}
		valueCounts.put(mappingValue, new Integer(count.intValue() - 1));
		return false;
	}

	public void dispose() {
		wrappedMapping.removeMappingChangeListener(mappingChangeListener);
		input.removeSetChangeListener(domainListener);
	}

	public Object getElementType() {
		return wrappedMapping.getValueType();
	}

}
