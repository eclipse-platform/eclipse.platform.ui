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

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.internal.util.Util;

/**
 * 
 * @since 3.2
 */
public class AggregateWorkingSet extends AbstractWorkingSet implements
		IPropertyChangeListener {

	private IWorkingSet[] components;

	/**
	 * 
	 * @param name
	 * @param label
	 * @param components
	 */
	public AggregateWorkingSet(String name, String label,
			IWorkingSet[] components) {
		super(name, label);

		IWorkingSet[] componentCopy = new IWorkingSet[components.length];
		System.arraycopy(components, 0, componentCopy, 0, components.length);
		internalSetComponents(componentCopy);
		constructElements(false);
	}

	/**
	 * 
	 * @param name
	 * @param label
	 * @param memento
	 */
	public AggregateWorkingSet(String name, String label, IMemento memento) {
		super(name, label);
		workingSetMemento = memento;
	}

	void setComponents(IWorkingSet[] components) {
		internalSetComponents(components);
		constructElements(true);
	}

	private void internalSetComponents(IWorkingSet[] components) {
		this.components = components;
	}

	/**
	 * Takes the elements from all component working sets and sets them to be
	 * the elements of this working set. Any duplicates are trimmed.
	 * 
	 * @param fireEvent whether a working set change event should be fired
	 */
	private void constructElements(boolean fireEvent) {
		Set elements = new HashSet();
		for (int i = 0; i < components.length; i++) {
			IWorkingSet workingSet = components[i];
			elements.addAll(Arrays.asList(workingSet.getElements()));
		}
		internalSetElements((IAdaptable[]) elements
				.toArray(new IAdaptable[elements.size()]));
		if (fireEvent) {
			fireWorkingSetChanged(
				IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE, null);
		}
	}

	public String getId() {
		return null;
	}

	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_WORKING_SETS);
	}

	/**
	 * A no-op for aggregates - their contents should be derived.
	 */
	public void setElements(IAdaptable[] elements) {
	}

	public void setId(String id) {

	}

	/**
	 * Aggregates are not editable.
	 */
	public boolean isEditable() {
		return false;
	}

	/**
	 * Aggregates should not generally be visible in the UI.
	 */
	public boolean isVisible() {
		return false;
	}

	public void saveState(IMemento memento) {
		if (workingSetMemento != null) {
			// just re-save the previous memento if the working set has
			// not been restored
			memento.putMemento(workingSetMemento);
		} else {
			memento.putString(IWorkbenchConstants.TAG_NAME, getName());
			memento.putString(IWorkbenchConstants.TAG_LABEL, getLabel());
			memento.putString(AbstractWorkingSet.TAG_AGGREGATE, Boolean.TRUE
					.toString());

			for (int i = 0; i < components.length; i++) {
				IWorkingSet componentSet = components[i];
				memento.createChild(IWorkbenchConstants.TAG_WORKING_SET,
						componentSet.getName());
			}
		}
	}

	public void connect(IWorkingSetManager manager) {
		manager.addPropertyChangeListener(this);
		super.connect(manager);
	}

	public void disconnect() {
		getManager().removePropertyChangeListener(this);
		super.disconnect();
	}

	/**
	 * Return the component working sets.
	 * 
	 * @return the component working sets
	 */
	public IWorkingSet[] getComponents() {
		if (components == null) {
			restoreWorkingSet();
			workingSetMemento = null;
		}
		return components;
	}

	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
			for (int i = 0; i < getComponents().length; i++) {
				IWorkingSet set = getComponents()[i];
				if (set.equals(event.getOldValue())) {
					IWorkingSet[] newComponents = new IWorkingSet[components.length - 1];
					Util
							.arrayCopyWithRemoval(getComponents(),
									newComponents, i);
					setComponents(newComponents);
				}
			}
		} else if (property
				.equals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {
			for (int i = 0; i < getComponents().length; i++) {
				IWorkingSet set = getComponents()[i];
				if (set.equals(event.getNewValue())) {
					constructElements(true);
					break;
				}
			}
		}
	}

	void restoreWorkingSet() {
		IWorkingSetManager manager = getManager();
		if (manager == null) {
			throw new IllegalStateException();
		}
		IMemento[] workingSetReferences = workingSetMemento
				.getChildren(IWorkbenchConstants.TAG_WORKING_SET);
		ArrayList list = new ArrayList(workingSetReferences.length);

		for (int i = 0; i < workingSetReferences.length; i++) {
			IMemento setReference = workingSetReferences[i];
			String setId = setReference.getID();
			IWorkingSet set = manager.getWorkingSet(setId);
			if (set != null) {
				list.add(set);
			}
		}
		internalSetComponents((IWorkingSet[]) list
				.toArray(new IWorkingSet[list.size()]));
		constructElements(false);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object instanceof AggregateWorkingSet) {
			AggregateWorkingSet workingSet = (AggregateWorkingSet) object;

			return Util.equals(workingSet.getName(), getName())
					&& Util.equals(workingSet.getComponents(), getComponents());
		}
		return false;
	}

	public int hashCode() {
		int hashCode = getName().hashCode() & getComponents().hashCode();
		return hashCode;
	}
	
	public boolean isSelfUpdating() {
		if (components == null || components.length == 0) {
			return false;
		}
		for (int i= 0; i < components.length; i++) {
			if (!components[i].isSelfUpdating()) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isAggregateWorkingSet() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkingSet#adaptElements(org.eclipse.core.runtime.IAdaptable[])
	 */
	public IAdaptable[] adaptElements(IAdaptable[] objects) {
		return new IAdaptable[0];
	}
}
