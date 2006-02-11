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

package org.eclipse.jface.internal.databinding.api.observable.set;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.internal.databinding.api.observable.mapping.IMappingChangeListener;
import org.eclipse.jface.internal.databinding.api.observable.mapping.IMappingDiff;
import org.eclipse.jface.internal.databinding.api.observable.mapping.IObservableMapping;

/**
 * @since 3.2
 * 
 */
public class MappedSet extends ObservableSet {

	private final IObservableMapping wrappedMapping;

	/*
	 * Map from values (range elements) to Integer ref counts
	 */
	private Map valueCounts = new HashMap();

	private ISetChangeListener domainListener = new ISetChangeListener() {
		public void handleSetChange(IObservableSet source, ISetDiff diff) {
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
			fireSetChange(new SetDiff(additions, removals));
		}
	};

	private IMappingChangeListener mappingChangeListener = new IMappingChangeListener() {
		public void handleMappingValueChange(IObservableMapping source,
				IMappingDiff diff) {
			Set affectedElements = diff.getElements();
			Set additions = new HashSet();
			Set removals = new HashSet();
			for (Iterator it = affectedElements.iterator(); it.hasNext();) {
				Object element = it.next();
				Object oldFunctionValue = diff.getOldMappingValue(element);
				Object newFunctionValue = diff.getNewMappingValue(element);
				if (handleRemoval(oldFunctionValue)) {
					removals.add(oldFunctionValue);
				}
				if (handleAddition(newFunctionValue)) {
					additions.add(newFunctionValue);
				}
			}
			fireSetChange(new SetDiff(additions, removals));
		}
	};

	private IObservableSet input;

	/**
	 * @param wrappedMapping
	 * @param domain
	 */
	public MappedSet(IObservableSet input, IObservableMapping mapping) {
		super(Collections.EMPTY_SET);
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
	 * @return
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
	 * @return
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

}
