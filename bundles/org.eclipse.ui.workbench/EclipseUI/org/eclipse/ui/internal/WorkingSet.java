/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.IWorkingSetUpdater;
import org.eclipse.ui.IWorkingSetUpdater2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;

/**
 * A working set holds a number of IAdaptable elements. A working set is
 * intended to group elements for presentation to the user or for operations on
 * a set of elements.
 *
 * @see org.eclipse.ui.IWorkingSet
 * @since 2.0
 */
public class WorkingSet extends AbstractWorkingSet {
	private static final String DEFAULT_ID = "org.eclipse.ui.resourceWorkingSetPage"; //$NON-NLS-1$

	private String editPageId;

	/**
	 * Creates a new working set.
	 *
	 * @param name     the name of the new working set. Should not have leading or
	 *                 trailing whitespace.
	 * @param label    the label of the new working set
	 * @param elements the content of the new working set. May be empty but not
	 *                 <code>null</code>.
	 */
	public WorkingSet(String name, String label, IAdaptable[] elements) {
		super(name, label);
		internalSetElements(elements);
	}

	/**
	 * Creates a new working set from a memento.
	 *
	 * @param name    the name of the new working set. Should not have leading or
	 *                trailing whitespace.
	 * @param memento persistence memento containing the elements of the working
	 *                set.
	 */
	protected WorkingSet(String name, String label, IMemento memento) {
		super(name, label);
		workingSetMemento = memento;
		if (workingSetMemento != null) {
			String uniqueId = workingSetMemento.getString(IWorkbenchConstants.TAG_ID);
			if (uniqueId != null) {
				setUniqueId(uniqueId);
			}
		}
	}

	/**
	 * Tests the receiver and the object for equality
	 *
	 * @param object object to compare the receiver to
	 * @return true=the object equals the receiver, the name is the same. false
	 *         otherwise
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object instanceof WorkingSet) {
			WorkingSet workingSet = (WorkingSet) object;
			return Objects.equals(workingSet.getName(), getName())
					&& Objects.equals(workingSet.getId(), getId())
					&& Objects.equals(workingSet.getElementsArray(), getElementsArray());
		}
		return false;
	}

	@Override
	public String toString() {
		return "WS [name=" + getName() + ", elements=" + getElementsArray() + ", id=" + getId() + "]"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEditable() {
		WorkingSetDescriptor descriptor = getDescriptor(null);
		return descriptor != null && descriptor.isEditable();
	}

	@Override
	public String getId() {
		return editPageId;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		WorkingSetDescriptor descriptor = getDescriptor(DEFAULT_ID);
		if (descriptor == null) {
			return null;
		}
		return descriptor.getIcon();
	}

	/**
	 * Returns the hash code.
	 *
	 * @return the hash code.
	 */
	@Override
	public int hashCode() {
		int hashCode = getName().hashCode();

		if (editPageId != null) {
			hashCode &= editPageId.hashCode();
		}
		return hashCode;
	}

	/**
	 * Recreates the working set elements from the persistence memento on first working set access.
	 */
	@Override
	void restoreWorkingSet() {
		IWorkingSetUpdater2 updater = getUpdater();
		IAdaptable[] itemsArray;
		if (updater != null && updater.isManagingPersistenceOf(this)) {
			itemsArray = updater.restore(this);
		} else {
			itemsArray = restoreFromMemento();
		}
		internalSetElements(itemsArray);
	}

	private IAdaptable[] restoreFromMemento() {
		IMemento[] itemMementos = workingSetMemento.getChildren(IWorkbenchConstants.TAG_ITEM);
		Set<IAdaptable> items = new LinkedHashSet<>();
		for (final IMemento itemMemento : itemMementos) {
			final String factoryID = itemMemento.getString(IWorkbenchConstants.TAG_FACTORY_ID);

			if (factoryID == null) {
				WorkbenchPlugin.log("Unable to restore working set item - no factory ID."); //$NON-NLS-1$
				continue;
			}
			final IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(factoryID);
			if (factory == null) {
				WorkbenchPlugin.log("Unable to restore working set item - cannot instantiate factory: " + factoryID); //$NON-NLS-1$
				continue;
			}
			SafeRunner.run(new SafeRunnable(
					"Unable to restore working set item - exception while invoking factory: " + factoryID) { //$NON-NLS-1$

				@Override
				public void run() throws Exception {
					IAdaptable item = factory.createElement(itemMemento);
					if (item == null) {
						if (Policy.DEBUG_WORKING_SETS) {
							WorkbenchPlugin
									.log("Unable to restore working set item - cannot instantiate item: " + factoryID); //$NON-NLS-1$
						}
					} else {
						items.add(item);
					}
				}
			});
		}
		return items.toArray(new IAdaptable[items.size()]);
	}

	/**
	 * Implements IPersistableElement. Persist the working set name and working set
	 * contents. The contents has to be either IPersistableElements or provide
	 * adapters for it to be persistent.
	 *
	 * @see org.eclipse.ui.IPersistableElement#saveState(IMemento)
	 */
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
			memento.putString(IWorkbenchConstants.TAG_EDIT_PAGE_ID, editPageId);
			IWorkingSetUpdater2 updater = getUpdater();
			if (updater != null && updater.isManagingPersistenceOf(this)) {
				return;
			}
			for (IAdaptable adaptable : elements) {
				final IPersistableElement persistable = Adapters.adapt(adaptable, IPersistableElement.class);
				if (persistable != null) {
					final IMemento itemMemento = memento.createChild(IWorkbenchConstants.TAG_ITEM);

					itemMemento.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
					SafeRunner.run(new SafeRunnable("Problems occurred while saving persistable item state") { //$NON-NLS-1$

						@Override
						public void run() throws Exception {
							persistable.saveState(itemMemento);
						}
					});
				}
			}
		}
	}

	@Override
	public void setElements(IAdaptable[] newElements) {
		AbstractWorkingSet oldWorkingSet = clone();
		internalSetElements(newElements);
		fireWorkingSetChanged(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE, oldWorkingSet);
	}

	@Override
	public void setId(String pageId) {
		editPageId = pageId;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isSelfUpdating() {
		WorkingSetDescriptor descriptor = getDescriptor(null);
		return descriptor != null && descriptor.getUpdaterClassName() != null;
	}

	@Override
	public boolean isAggregateWorkingSet() {
		return false;
	}

	/**
	 * Return the working set descriptor for this working set.
	 *
	 * @param defaultId the default working set type ID to use if this set has no
	 *                  defined type
	 * @return the descriptor for this working set or <code>null</code> if it cannot
	 *         be determined
	 * @since 3.3
	 */
	private WorkingSetDescriptor getDescriptor(String defaultId) {
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		String id = getId();
		if (id == null)
			id = defaultId;
		if (id == null)
			return null;

		return registry.getWorkingSetDescriptor(id);
	}

	@Override
	public IAdaptable[] adaptElements(IAdaptable[] objects) {
		WorkingSetManager manager = getWorkingSetManager();
		if (manager != null) {
			WorkingSetDescriptor descriptor = getDescriptor();
			if (descriptor == null || !descriptor.isElementAdapterClassLoaded()) {
				return objects;
			}
			return manager.getElementAdapter(descriptor).adaptElements(this, objects);
		}
		return objects;
	}

	private WorkingSetManager getWorkingSetManager() {
		IWorkingSetManager manager = getManager();
		if (manager instanceof WorkingSetManager) {
			return (WorkingSetManager) manager;
		}
		return null;
	}

	private WorkingSetDescriptor getDescriptor() {
		return getDescriptor(null);
	}

	private IWorkingSetUpdater2 getUpdater() {
		WorkingSetManager manager = getWorkingSetManager();
		if (manager != null) {
			WorkingSetDescriptor descriptor = getDescriptor();
			if (descriptor != null) {
				IWorkingSetUpdater updater = manager.getUpdater(descriptor);
				if (updater instanceof IWorkingSetUpdater2) {
					return (IWorkingSetUpdater2) updater;
				}
			}
		}
		return null;
	}
}
