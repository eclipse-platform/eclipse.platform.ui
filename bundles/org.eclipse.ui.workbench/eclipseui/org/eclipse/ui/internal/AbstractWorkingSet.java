/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
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
 *     Tomasz Zarna <tomasz.zarna@tasktop.com> - Bug 37183
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

/**
 * Abstract baseclass for IWorkingSet implementations.
 *
 * @since 3.2
 */
public abstract class AbstractWorkingSet implements IAdaptable, IWorkingSet, Cloneable {

	protected static final String FACTORY_ID = "org.eclipse.ui.internal.WorkingSetFactory"; //$NON-NLS-1$

	static final String TAG_AGGREGATE = "aggregate"; //$NON-NLS-1$

	private String name;

	protected ArrayList<IAdaptable> elements;

	private IWorkingSetManager manager;

	protected IMemento workingSetMemento;

	private String label;

	// Workspace wide unique id for workingsets
	private String uniqueId;

	private static int counter;

	/**
	 * Whether or not the label value should follow the name value. It should do
	 * this until a call to setLabel() differentiates it from the name.
	 */
	private boolean labelBoundToName;

	/**
	 * Create a new instance of this class
	 *
	 * @param name  the unique name for this working set
	 * @param label the user-friendly name for this working set
	 */
	public AbstractWorkingSet(String name, String label) {
		Assert.isNotNull(name, "name must not be null"); //$NON-NLS-1$
		this.name = name;
		this.label = label;
		labelBoundToName = Objects.equals(name, label);
		uniqueId = System.currentTimeMillis() + "_" + counter++; //$NON-NLS-1$
	}

	/**
	 * Returns the receiver if the requested type is either IWorkingSet or
	 * IPersistableElement.
	 *
	 * @param adapter the requested type
	 * @return the receiver if the requested type is either IWorkingSet or
	 *         IPersistableElement.
	 */
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IWorkingSet.class || adapter == IPersistableElement.class) {
			return adapter.cast(this);
		}
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String newName) {
		Assert.isNotNull(newName, "Working set name must not be null"); //$NON-NLS-1$
		if (manager != null) {
			IWorkingSet wSet = manager.getWorkingSet(newName);
			if (wSet != this) {
				Assert.isTrue(wSet == null, "working set with same name already registered"); //$NON-NLS-1$
			}
		}

		AbstractWorkingSet oldWorkingSet = clone();
		name = newName;

		fireWorkingSetChanged(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE, oldWorkingSet);

		if (labelBoundToName) {
			setLabel(newName);
		}
	}

	/**
	 * Connect this working set to a manger.
	 *
	 * @param manager the manager to connect to
	 */
	public void connect(IWorkingSetManager manager) {
		Assert.isTrue(this.manager == null, "A working set can only be connected to one manager"); //$NON-NLS-1$
		this.manager = manager;
	}

	/**
	 * Disconnect this working set from its manager, if any.
	 */
	public void disconnect() {
		this.manager = null;
	}

	protected void fireWorkingSetChanged(String property, Object oldValue) {
		AbstractWorkingSetManager receiver = manager != null ? (AbstractWorkingSetManager) manager
				: (AbstractWorkingSetManager) WorkbenchPlugin.getDefault().getWorkingSetManager();
		receiver.workingSetChanged(this, property, oldValue);
	}

	/**
	 * Create a copy of the elements to store in the receiver.
	 *
	 * @param newElements the elements to store a copy of in the receiver.
	 */
	protected void internalSetElements(IAdaptable[] newElements) {
		Assert.isNotNull(newElements, "Working set elements array must not be null"); //$NON-NLS-1$

		elements = new ArrayList<>(newElements.length);
		elements.addAll(Arrays.asList(newElements));
	}

	@Override
	public IAdaptable[] getElements() {
		ArrayList<IAdaptable> list = getElementsArray();
		return list.toArray(new IAdaptable[list.size()]);
	}

	/**
	 * Returns the elements array list. Lazily restores the elements from
	 * persistence memento.
	 *
	 * @return the elements array list
	 */
	protected ArrayList<IAdaptable> getElementsArray() {
		if (elements == null) {
			restoreWorkingSet();
			workingSetMemento = null;
		}
		return elements;
	}

	abstract void restoreWorkingSet();

	protected IWorkingSetManager getManager() {
		return manager;
	}

	@Override
	public String getFactoryId() {
		return FACTORY_ID;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		AbstractWorkingSet oldWorkingSet = clone();

		this.label = label == null ? getName() : label;
		labelBoundToName = Objects.equals(label, name); // rebind the label to the name

		fireWorkingSetChanged(IWorkingSetManager.CHANGE_WORKING_SET_LABEL_CHANGE, oldWorkingSet);
	}

	@Override
	public boolean isEmpty() {
		return getElementsArray().isEmpty();
	}

	@Override
	public final ImageDescriptor getImage() {
		return getImageDescriptor();
	}

	/* package */String getUniqueId() {
		return uniqueId;
	}

	/* package */void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	protected AbstractWorkingSet clone() {
		try {
			AbstractWorkingSet clone = (AbstractWorkingSet) super.clone();
			clone.disconnect();
			return clone;
		} catch (CloneNotSupportedException e) {
			// ignore
		}
		return null;
	}
}
