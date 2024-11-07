/* Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IAggregateWorkingSet;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.internal.util.Util;

/**
 *
 * @since 3.2
 */
public class AggregateWorkingSet extends AbstractWorkingSet implements IAggregateWorkingSet, IPropertyChangeListener {

	private IWorkingSet[] components;

	/**
	 * Prevents stack overflow on cyclic element inclusions.
	 */
	private boolean inElementConstruction = false;

	public AggregateWorkingSet(String name, String label, IWorkingSet[] components) {
		super(name, label);

		IWorkingSet[] componentCopy = new IWorkingSet[components.length];
		System.arraycopy(components, 0, componentCopy, 0, components.length);
		internalSetComponents(componentCopy);
		constructElements(false);
	}

	public AggregateWorkingSet(String name, String label, IMemento memento) {
		super(name, label);
		workingSetMemento = memento;
		if (workingSetMemento != null) {
			String uniqueId = workingSetMemento.getString(IWorkbenchConstants.TAG_ID);
			if (uniqueId != null) {
				setUniqueId(uniqueId);
			}
		}
	}

	void setComponents(IWorkingSet[] components) {
		internalSetComponents(components);
		constructElements(true);
	}

	private void internalSetComponents(IWorkingSet[] components) {
		this.components = components;
	}

	/**
	 * Takes the elements from all component working sets and sets them to be the
	 * elements of this working set. Any duplicates are trimmed.
	 *
	 * @param fireEvent whether a working set change event should be fired
	 */
	private void constructElements(boolean fireEvent) {
		if (inElementConstruction) {
			String msg = NLS.bind(WorkbenchMessages.ProblemCyclicDependency, getName());
			WorkbenchPlugin.log(msg);
			throw new IllegalStateException(msg);
		}
		inElementConstruction = true;
		try {
			// use *linked* set to maintain predictable elements order
			Set<IAdaptable> elements = new LinkedHashSet<>();
			IWorkingSet[] localComponents = getComponentsInternal();
			for (int i = 0; i < localComponents.length; i++) {
				IWorkingSet workingSet = localComponents[i];
				try {
					IAdaptable[] componentElements = workingSet.getElements();
					elements.addAll(Arrays.asList(componentElements));
				} catch (IllegalStateException e) { // an invalid component; remove it
					IWorkingSet[] tmp = new IWorkingSet[components.length - 1];
					if (i > 0)
						System.arraycopy(components, 0, tmp, 0, i);
					if (components.length - i - 1 > 0)
						System.arraycopy(components, i + 1, tmp, i, components.length - i - 1);
					components = tmp;
					workingSetMemento = null; // toss cached info
					fireWorkingSetChanged(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE, null);
					continue;
				}
			}
			internalSetElements(elements.toArray(new IAdaptable[elements.size()]));
			if (fireEvent) {
				fireWorkingSetChanged(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE, null);
			}
		} finally {
			inElementConstruction = false;
		}
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_WORKING_SETS);
	}

	/**
	 * A no-op for aggregates - their contents should be derived.
	 */
	@Override
	public void setElements(IAdaptable[] elements) {
	}

	@Override
	public void setId(String id) {

	}

	/**
	 * Aggregates are not editable.
	 */
	@Override
	public boolean isEditable() {
		return false;
	}

	/**
	 * Aggregates should not generally be visible in the UI.
	 */
	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public void saveState(IMemento memento) {
		if (workingSetMemento != null) {
			// just re-save the previous memento if the working set has
			// not been restored
			memento.putMemento(workingSetMemento);
		} else {
			memento.putString(IWorkbenchConstants.TAG_NAME, getName());
			memento.putString(IWorkbenchConstants.TAG_LABEL, getLabel());
			memento.putString(IWorkbenchConstants.TAG_ID, getUniqueId());
			memento.putString(AbstractWorkingSet.TAG_AGGREGATE, Boolean.TRUE.toString());

			for (IWorkingSet workingSet : getComponentsInternal()) {
				memento.createChild(IWorkbenchConstants.TAG_WORKING_SET, workingSet.getName());
			}
		}
	}

	@Override
	public void connect(IWorkingSetManager manager) {
		manager.addPropertyChangeListener(this);
		super.connect(manager);
	}

	@Override
	public void disconnect() {
		IWorkingSetManager connectedManager = getManager();
		if (connectedManager != null)
			connectedManager.removePropertyChangeListener(this);
		super.disconnect();
	}

	/**
	 * Return the component working sets.
	 *
	 * @return the component working sets
	 */
	@Override
	public IWorkingSet[] getComponents() {
		IWorkingSet[] localComponents = getComponentsInternal();
		IWorkingSet[] copiedArray = new IWorkingSet[localComponents.length];
		System.arraycopy(localComponents, 0, copiedArray, 0, localComponents.length);
		return copiedArray;
	}

	private IWorkingSet[] getComponentsInternal() {
		if (components == null) {
			restoreWorkingSet();
			workingSetMemento = null;
		}
		return components;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
			IWorkingSet[] localComponents = getComponentsInternal();
			for (int i = 0; i < localComponents.length; i++) {
				IWorkingSet set = localComponents[i];
				if (set.equals(event.getOldValue())) {
					IWorkingSet[] newComponents = new IWorkingSet[localComponents.length - 1];
					Util.arrayCopyWithRemoval(localComponents, newComponents, i);
					setComponents(newComponents);
				}
			}
		} else if (property.equals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {
			IWorkingSet[] localComponents = getComponentsInternal();
			for (IWorkingSet set : localComponents) {
				if (set.equals(event.getNewValue())) {
					constructElements(true);
					break;
				}
			}
		}
	}

	@Override
	void restoreWorkingSet() {
		IWorkingSetManager manager = getManager();
		if (manager == null) {
			throw new IllegalStateException();
		}
		IMemento[] workingSetReferences = workingSetMemento.getChildren(IWorkbenchConstants.TAG_WORKING_SET);
		ArrayList<IWorkingSet> list = new ArrayList<>(workingSetReferences.length);

		for (IMemento memento : workingSetReferences) {
			String setId = memento.getID();
			IWorkingSet set = manager.getWorkingSet(setId);
			if (set != null) {
				list.add(set);
			}
		}
		internalSetComponents(list.toArray(new IWorkingSet[list.size()]));
		constructElements(false);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object instanceof AggregateWorkingSet) {
			AggregateWorkingSet workingSet = (AggregateWorkingSet) object;

			return Objects.equals(workingSet.getName(), getName())
					&& Arrays.equals(workingSet.getComponentsInternal(), getComponentsInternal());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getName().hashCode() & java.util.Arrays.hashCode(getComponentsInternal());
	}

	@Override
	public boolean isSelfUpdating() {
		IWorkingSet[] localComponents = getComponentsInternal();
		if (localComponents == null || localComponents.length == 0) {
			return false;
		}
		for (IWorkingSet localComponent : localComponents) {
			if (!localComponent.isSelfUpdating()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isAggregateWorkingSet() {
		return true;
	}

	@Override
	public IAdaptable[] adaptElements(IAdaptable[] objects) {
		return new IAdaptable[0];
	}

	@Override
	public String toString() {
		return "AWS [name=" + getName() + ", components=" + Arrays.toString(getComponentsInternal()) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}
