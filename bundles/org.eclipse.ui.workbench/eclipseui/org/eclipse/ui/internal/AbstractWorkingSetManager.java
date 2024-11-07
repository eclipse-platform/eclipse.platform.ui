/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetElementAdapter;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.IWorkingSetUpdater;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.dialogs.IWorkingSetNewWizard;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.internal.dialogs.WorkingSetEditWizard;
import org.eclipse.ui.internal.dialogs.WorkingSetNewWizard;
import org.eclipse.ui.internal.dialogs.WorkingSetSelectionDialog;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * Abstract implementation of <code>IWorkingSetManager</code>.
 */
public abstract class AbstractWorkingSetManager extends EventManager
		implements IWorkingSetManager, BundleListener, IExtensionChangeHandler {

	static abstract class WorkingSetRunnable implements ISafeRunnable {

		@Override
		public void handleException(Throwable exception) {
			StatusManager.getManager().handle(StatusUtil.newStatus(PlatformUI.PLUGIN_ID, exception));
		}
	}

	private SortedSet<AbstractWorkingSet> workingSets = new TreeSet<>(
			(o1, o2) -> o1.getUniqueId().compareTo(o2.getUniqueId()));

	private List<IWorkingSet> recentWorkingSets = new ArrayList<>();

	private BundleContext bundleContext;
	private Map<String, IWorkingSetUpdater> updaters = new HashMap<>();

	private Map<String, IWorkingSetElementAdapter> elementAdapters = new HashMap<>();

	private static final IWorkingSetUpdater NULL_UPDATER = new IWorkingSetUpdater() {
		@Override
		public void add(IWorkingSet workingSet) {
		}

		@Override
		public boolean remove(IWorkingSet workingSet) {
			return true;
		}

		@Override
		public boolean contains(IWorkingSet workingSet) {
			return true;
		}

		@Override
		public void dispose() {
		}
	};

	private static final IWorkingSetElementAdapter IDENTITY_ADAPTER = new IWorkingSetElementAdapter() {

		@Override
		public IAdaptable[] adaptElements(IWorkingSet ws, IAdaptable[] elements) {
			return elements;
		}

		@Override
		public void dispose() {
		}
	};

	/**
	 * Returns the descriptors for the given editable working set ids. If an id
	 * refers to a missing descriptor, or one that is non-editable, it is skipped.
	 * If <code>null</code> is passed, all editable descriptors are returned.
	 *
	 * @param supportedWorkingSetIds the ids for the working set descriptors, or
	 *                               <code>null</code> for all editable descriptors
	 * @return the descriptors corresponding to the given editable working set ids
	 */
	private static WorkingSetDescriptor[] getSupportedEditableDescriptors(String[] supportedWorkingSetIds) {
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		if (supportedWorkingSetIds == null) {
			return registry.getNewPageWorkingSetDescriptors();
		}
		List<WorkingSetDescriptor> result = new ArrayList<>(supportedWorkingSetIds.length);
		for (String supportedWorkingSetId : supportedWorkingSetIds) {
			WorkingSetDescriptor desc = registry.getWorkingSetDescriptor(supportedWorkingSetId);
			if (desc != null && desc.isEditable()) {
				result.add(desc);
			}
		}
		return result.toArray(new WorkingSetDescriptor[result.size()]);
	}

	protected AbstractWorkingSetManager(BundleContext context) {
		bundleContext = context;
		bundleContext.addBundleListener(this);
		PlatformUI.getWorkbench().getExtensionTracker().registerHandler(this,
				ExtensionTracker.createExtensionPointFilter(getExtensionPointFilter()));
	}

	/**
	 * Returns the working sets extension point.
	 *
	 * @return the working sets extension point
	 * @since 3.3
	 */
	private IExtensionPoint getExtensionPointFilter() {
		return Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_WORKINGSETS);
	}

	@Override
	public void dispose() {
		bundleContext.removeBundleListener(this);
		for (IWorkingSetUpdater next : updaters.values()) {
			SafeRunner.run(new WorkingSetRunnable() {
				@Override
				public void run() throws Exception {
					next.dispose();
				}
			});
		}

		for (IWorkingSetElementAdapter next : elementAdapters.values()) {
			SafeRunner.run(new WorkingSetRunnable() {
				@Override
				public void run() throws Exception {
					next.dispose();
				}
			});
		}
	}

	// ---- working set creation
	// -----------------------------------------------------

	@Override
	public IWorkingSet createWorkingSet(String name, IAdaptable[] elements) {
		return new WorkingSet(name, name, elements);
	}

	@Override
	public IWorkingSet createAggregateWorkingSet(String name, String label, IWorkingSet[] components) {
		return new AggregateWorkingSet(name, label, components);
	}

	@Override
	public IWorkingSet createWorkingSet(IMemento memento) {
		return restoreWorkingSet(memento);
	}

	// ---- working set management
	// ---------------------------------------------------

	@Override
	public void addWorkingSet(IWorkingSet workingSet) {
		IWorkingSet wSet = getWorkingSet(workingSet.getName());
		Assert.isTrue(wSet == null, "working set with same name already registered"); //$NON-NLS-1$
		internalAddWorkingSet(workingSet);
	}

	private void internalAddWorkingSet(IWorkingSet workingSet) {
		AbstractWorkingSet abstractWorkingSet = (AbstractWorkingSet) workingSet;
		workingSets.add(abstractWorkingSet);
		abstractWorkingSet.connect(this);
		addToUpdater(workingSet);
		firePropertyChange(CHANGE_WORKING_SET_ADD, null, workingSet);
	}

	protected boolean internalRemoveWorkingSet(IWorkingSet workingSet) {
		boolean workingSetRemoved = workingSets.remove(workingSet);
		boolean recentWorkingSetRemoved = recentWorkingSets.remove(workingSet);

		if (workingSetRemoved) {
			((AbstractWorkingSet) workingSet).disconnect();
			removeFromUpdater(workingSet);
			firePropertyChange(CHANGE_WORKING_SET_REMOVE, workingSet, null);
		}
		return workingSetRemoved || recentWorkingSetRemoved;
	}

	@Override
	public IWorkingSet[] getWorkingSets() {
		SortedSet<IWorkingSet> visibleSubset = new TreeSet<>(WorkingSetComparator.getInstance());
		for (IWorkingSet workingSet : workingSets) {
			if (workingSet.isVisible()) {
				visibleSubset.add(workingSet);
			}
		}
		return visibleSubset.toArray(new IWorkingSet[visibleSubset.size()]);
	}

	@Override
	public IWorkingSet[] getAllWorkingSets() {
		IWorkingSet[] sets = workingSets.toArray(new IWorkingSet[workingSets.size()]);
		Arrays.sort(sets, WorkingSetComparator.getInstance());
		return sets;
	}

	@Override
	public IWorkingSet getWorkingSet(String name) {
		if (name == null || workingSets == null) {
			return null;
		}

		for (IWorkingSet workingSet : workingSets) {
			if (name.equals(workingSet.getName())) {
				return workingSet;
			}
		}

		return null;
	}

	// ---- recent working set management --------------------------------------

	@Override
	public IWorkingSet[] getRecentWorkingSets() {
		return recentWorkingSets.toArray(new IWorkingSet[recentWorkingSets.size()]);
	}

	/**
	 * Adds the specified working set to the list of recently used working sets.
	 *
	 * @param workingSet working set to added to the list of recently used working
	 *                   sets.
	 */
	protected void internalAddRecentWorkingSet(IWorkingSet workingSet) {
		if (!workingSet.isVisible()) {
			return;
		}
		recentWorkingSets.remove(workingSet);
		recentWorkingSets.add(0, workingSet);
		sizeRecentWorkingSets();
	}

	// ---- equals and hash code -----------------------------------------------

	/**
	 * Tests the receiver and the object for equality
	 *
	 * @param object object to compare the receiver to
	 * @return true=the object equals the receiver, it has the same working sets.
	 *         false otherwise
	 */
	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (this == object) {
			return true;
		}
		if (!getClass().getName().equals(object.getClass().getName())) {
			return false;
		}
		AbstractWorkingSetManager other = (AbstractWorkingSetManager) object;
		return other.workingSets.equals(workingSets);
	}

	/**
	 * Returns the hash code.
	 *
	 * @return the hash code.
	 */
	@Override
	public int hashCode() {
		return workingSets.hashCode();
	}

	// ---- property listeners -------------------------------------------------

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		addListenerObject(listener);
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		removeListenerObject(listener);
	}

	/**
	 * Notify property change listeners about a change to the list of working sets.
	 *
	 * @param changeId one of IWorkingSetManager#CHANGE_WORKING_SET_ADD
	 *                 IWorkingSetManager#CHANGE_WORKING_SET_REMOVE
	 *                 IWorkingSetManager#CHANGE_WORKING_SET_CONTENT_CHANGE
	 *                 IWorkingSetManager#CHANGE_WORKING_SET_NAME_CHANGE
	 * @param oldValue the removed working set or null if a working set was added or
	 *                 changed.
	 * @param newValue the new or changed working set or null if a working set was
	 *                 removed.
	 */
	protected void firePropertyChange(String changeId, Object oldValue, Object newValue) {
		final Object[] listeners = getListeners();

		if (listeners.length == 0) {
			return;
		}

		final PropertyChangeEvent event = new PropertyChangeEvent(this, changeId, oldValue, newValue);
		Runnable notifier = () -> {
			for (Object listener : listeners) {
				final IPropertyChangeListener propertyChangeListener = (IPropertyChangeListener) listener;
				ISafeRunnable safetyWrapper = new ISafeRunnable() {

					@Override
					public void run() throws Exception {
						propertyChangeListener.propertyChange(event);
					}

					@Override
					public void handleException(Throwable exception) {
						// logged by the runner
					}
				};
				SafeRunner.run(safetyWrapper);
			}
		};
		// Notifications are sent on the UI thread.
		if (Display.getCurrent() != null) {
			notifier.run();
		} else {
			// Use an asyncExec to avoid deadlocks.
			Display.getDefault().asyncExec(notifier);
		}
	}

	/**
	 * Fires a property change event for the changed working set. Should only be
	 * called by org.eclipse.ui.internal.WorkingSet.
	 *
	 * @param changedWorkingSet the working set that has changed
	 * @param propertyChangeId  the changed property. one of
	 *                          CHANGE_WORKING_SET_CONTENT_CHANGE,
	 *                          CHANGE_WORKING_SET_LABEL_CHANGE, and
	 *                          CHANGE_WORKING_SET_NAME_CHANGE
	 * @param oldValue          the old value
	 */
	public void workingSetChanged(IWorkingSet changedWorkingSet, String propertyChangeId, Object oldValue) {
		firePropertyChange(propertyChangeId, oldValue, changedWorkingSet);
	}

	// ---- Persistence
	// ----------------------------------------------------------------

	/**
	 * Saves all persistable working sets in the persistence store.
	 *
	 * @param memento the persistence store
	 * @see IPersistableElement
	 */
	public void saveWorkingSetState(IMemento memento) {
		Iterator<AbstractWorkingSet> iterator = workingSets.iterator();

		// break the sets into aggregates and non aggregates. The aggregates should be
		// saved after the non-aggregates
		// so that on restoration all necessary aggregate components can be found.

		ArrayList<IWorkingSet> standardSets = new ArrayList<>();
		ArrayList<IWorkingSet> aggregateSets = new ArrayList<>();
		while (iterator.hasNext()) {
			IWorkingSet set = iterator.next();
			if (set instanceof AggregateWorkingSet) {
				aggregateSets.add(set);
			} else {
				standardSets.add(set);
			}
		}

		saveWorkingSetState(memento, standardSets);
		saveWorkingSetState(memento, aggregateSets);
	}

	/**
	 * @param memento the memento to save to
	 * @param list    the working sets to save
	 * @since 3.2
	 */
	private void saveWorkingSetState(final IMemento memento, List<IWorkingSet> list) {
		for (Iterator<IWorkingSet> i = list.iterator(); i.hasNext();) {
			final IPersistableElement persistable = i.next();
			SafeRunner.run(new WorkingSetRunnable() {

				@Override
				public void run() throws Exception {
					// create a dummy node to write too - the write could fail so we
					// shouldn't soil the final memento until we're sure it succeeds.
					XMLMemento dummy = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WORKING_SET);
					dummy.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
					persistable.saveState(dummy);

					// if the dummy was created successfully copy it to the real output
					IMemento workingSetMemento = memento.createChild(IWorkbenchConstants.TAG_WORKING_SET);
					workingSetMemento.putMemento(dummy);
				}
			});

		}
	}

	/**
	 * Recreates all working sets from the persistence store and adds them to the
	 * receiver.
	 *
	 * @param memento the persistence store
	 */
	protected void restoreWorkingSetState(IMemento memento) {
		for (IMemento child : memento.getChildren(IWorkbenchConstants.TAG_WORKING_SET)) {
			AbstractWorkingSet workingSet = (AbstractWorkingSet) restoreWorkingSet(child);
			if (workingSet != null) {
				internalAddWorkingSet(workingSet);
			}
		}
	}

	/**
	 * Recreates a working set from the persistence store.
	 *
	 * @param memento the persistence store
	 * @return the working set created from the memento or null if creation failed.
	 */
	protected IWorkingSet restoreWorkingSet(final IMemento memento) {
		String factoryID = memento.getString(IWorkbenchConstants.TAG_FACTORY_ID);

		if (factoryID == null) {
			// if the factory id was not set in the memento
			// then assume that the memento was created using
			// IMemento.saveState, and should be restored using WorkingSetFactory
			factoryID = AbstractWorkingSet.FACTORY_ID;
		}
		final IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(factoryID);
		if (factory == null) {
			WorkbenchPlugin.log("Unable to restore working set - cannot instantiate factory: " + factoryID); //$NON-NLS-1$
			return null;
		}
		final IAdaptable[] adaptable = new IAdaptable[1];
		SafeRunner.run(new WorkingSetRunnable() {

			@Override
			public void run() throws Exception {
				adaptable[0] = factory.createElement(memento);
			}
		});
		if (adaptable[0] == null) {
			WorkbenchPlugin.log("Unable to restore working set - cannot instantiate working set: " + factoryID); //$NON-NLS-1$
			return null;
		}
		if (!(adaptable[0] instanceof IWorkingSet)) {
			WorkbenchPlugin.log("Unable to restore working set - element is not an IWorkingSet: " + factoryID); //$NON-NLS-1$
			return null;
		}
		return (IWorkingSet) adaptable[0];
	}

	/**
	 * Saves the list of most recently used working sets in the persistence store.
	 *
	 * @param memento the persistence store
	 */
	protected void saveMruList(IMemento memento) {
		for (IWorkingSet workingSet : recentWorkingSets) {
			IMemento mruMemento = memento.createChild(IWorkbenchConstants.TAG_MRU_LIST);
			mruMemento.putString(IWorkbenchConstants.TAG_NAME, workingSet.getName());
		}
	}

	/**
	 * Restores the list of most recently used working sets from the persistence
	 * store.
	 *
	 * @param memento the persistence store
	 */
	protected void restoreMruList(IMemento memento) {
		IMemento[] mruWorkingSets = memento.getChildren(IWorkbenchConstants.TAG_MRU_LIST);

		for (int i = mruWorkingSets.length - 1; i >= 0; i--) {
			String workingSetName = mruWorkingSets[i].getString(IWorkbenchConstants.TAG_NAME);
			if (workingSetName != null) {
				IWorkingSet workingSet = getWorkingSet(workingSetName);
				if (workingSet != null) {
					internalAddRecentWorkingSet(workingSet);
				}
			}
		}
	}

	// ---- user interface support
	// -----------------------------------------------------

	/**
	 * @see org.eclipse.ui.IWorkingSetManager#createWorkingSetEditWizard(org.eclipse.ui.IWorkingSet)
	 * @since 2.1
	 */
	@Override
	public IWorkingSetEditWizard createWorkingSetEditWizard(IWorkingSet workingSet) {
		String editPageId = workingSet.getId();
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		IWorkingSetPage editPage = null;

		if (editPageId != null) {
			editPage = registry.getWorkingSetPage(editPageId);
		}

		// the following block kind of defeats IWorkingSet.isEditable() and it
		// doesn't make sense for there to be a default page in such a case.

		if (editPage == null) {
			editPage = registry.getDefaultWorkingSetPage();
			if (editPage == null) {
				return null;
			}
		}

		WorkingSetEditWizard editWizard = new WorkingSetEditWizard(editPage);
		editWizard.setSelection(workingSet);
		return editWizard;
	}

	/**
	 * @deprecated use createWorkingSetSelectionDialog(parent, true) instead
	 */
	@Deprecated
	@Override
	public IWorkingSetSelectionDialog createWorkingSetSelectionDialog(Shell parent) {
		return createWorkingSetSelectionDialog(parent, true);
	}

	@Override
	public IWorkingSetSelectionDialog createWorkingSetSelectionDialog(Shell parent, boolean multi) {
		return createWorkingSetSelectionDialog(parent, multi, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWorkingSetNewWizard createWorkingSetNewWizard(String[] workingSetIds) {
		WorkingSetDescriptor[] descriptors = getSupportedEditableDescriptors(workingSetIds);
		if (descriptors.length == 0) {
			return null;
		}
		return new WorkingSetNewWizard(descriptors);
	}

	// ---- working set delta handling
	// -------------------------------------------------

	@Override
	public void bundleChanged(BundleEvent event) {
		String symbolicName = event.getBundle().getSymbolicName();
		if (symbolicName == null) {
			return;
		}
		// If the workbench isn't running anymore simply return.
		if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}
		if (event.getBundle().getState() != Bundle.ACTIVE) {
			return;
		}

		final List<WorkingSetDescriptor> descriptors = getUniqueDescriptors(symbolicName);
		final Map<WorkingSetDescriptor, List<IWorkingSet>> nonEmptyDescriptors = getNonEmpty(descriptors);
		if (nonEmptyDescriptors.isEmpty()) {
			return;
		}
		Job job = new WorkbenchJob(
				NLS.bind(WorkbenchMessages.AbstractWorkingSetManager_updatersActivating, symbolicName)) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				Set<Entry<WorkingSetDescriptor, List<IWorkingSet>>> entrySet = nonEmptyDescriptors.entrySet();
				synchronized (updaters) {
					for (Entry<WorkingSetDescriptor, List<IWorkingSet>> e : entrySet) {
						final IWorkingSetUpdater updater = getUpdater(e.getKey());
						if (updater == NULL_UPDATER) {
							continue;
						}
						for (IWorkingSet workingSet : e.getValue()) {
							SafeRunner.run(new WorkingSetRunnable() {

								@Override
								public void run() throws Exception {
									if (!updater.contains(workingSet)) {
										updater.add(workingSet);
									}
								}
							});
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	private Map<WorkingSetDescriptor, List<IWorkingSet>> getNonEmpty(final List<WorkingSetDescriptor> descriptors) {
		Map<WorkingSetDescriptor, List<IWorkingSet>> map = descriptors.stream()
				.collect(Collectors.toMap(d -> d, d -> getWorkingSetsForId(d.getId())));
		Iterator<Entry<WorkingSetDescriptor, List<IWorkingSet>>> iterator = map.entrySet().iterator();
		iterator.forEachRemaining(e -> {
			if (e.getValue().isEmpty()) {
				iterator.remove();
			}
		});
		return map;
	}

	private static List<WorkingSetDescriptor> getUniqueDescriptors(String symbolicName) {

		// Note: WorkingSetRegistry never contains descriptors with same id
		return WorkbenchPlugin.getDefault().getWorkingSetRegistry()
				.getUpdaterDescriptorsForNamespace(symbolicName);
	}

	private List<IWorkingSet> getWorkingSetsForId(String id) {
		List<IWorkingSet> result = new ArrayList<>();
		for (IWorkingSet ws : workingSets) {
			if (id.equals(ws.getId())) {
				result.add(ws);
			}
		}
		return result;
	}

	private void addToUpdater(final IWorkingSet workingSet) {
		WorkingSetDescriptor descriptor = WorkbenchPlugin.getDefault().getWorkingSetRegistry()
				.getWorkingSetDescriptor(workingSet.getId());
		if (descriptor == null || !descriptor.isUpdaterClassLoaded()) {
			return;
		}
		synchronized (updaters) {
			final IWorkingSetUpdater updater = getUpdater(descriptor);
			SafeRunner.run(new WorkingSetRunnable() {

				@Override
				public void run() throws Exception {
					if (!updater.contains(workingSet)) {
						updater.add(workingSet);
					}
				}
			});
		}
	}

	IWorkingSetUpdater getUpdater(WorkingSetDescriptor descriptor) {
		String updaterId = descriptor.getId();
		IWorkingSetUpdater updater = updaters.get(updaterId);
		if (updater == null) {
			updater = descriptor.createWorkingSetUpdater();
			boolean fireChange = true;
			if (updater == null) {
				updater = NULL_UPDATER;
				fireChange = false;
			}
			synchronized (updaters) {
				IWorkingSetUpdater old = updaters.putIfAbsent(updaterId, updater);
				if (fireChange) {
					// don't fire again if already installed
					fireChange = old == null;
				}
			}
			if (fireChange) {
				firePropertyChange(CHANGE_WORKING_SET_UPDATER_INSTALLED, null, updater);
				PlatformUI.getWorkbench().getExtensionTracker().registerObject(
						descriptor.getConfigurationElement().getDeclaringExtension(), updater,
						IExtensionTracker.REF_WEAK);

			}
		}
		return updater;
	}

	IWorkingSetElementAdapter getElementAdapter(WorkingSetDescriptor descriptor) {
		IWorkingSetElementAdapter elementAdapter = elementAdapters.get(descriptor.getId());
		if (elementAdapter == null) {
			elementAdapter = descriptor.createWorkingSetElementAdapter();
			if (elementAdapter == null) {
				elementAdapter = IDENTITY_ADAPTER;
			} else {
				elementAdapters.put(descriptor.getId(), elementAdapter);
			}
		}
		return elementAdapter;
	}

	private void removeFromUpdater(final IWorkingSet workingSet) {
		synchronized (updaters) {
			final IWorkingSetUpdater updater = updaters.get(workingSet.getId());
			if (updater != null) {
				SafeRunner.run(new WorkingSetRunnable() {

					@Override
					public void run() throws Exception {
						updater.remove(workingSet);
					}
				});
			}
		}
	}

	@Override
	public IWorkingSetSelectionDialog createWorkingSetSelectionDialog(Shell parent, boolean multi,
			String[] workingsSetIds) {
		return new WorkingSetSelectionDialog(parent, multi, workingsSetIds);
	}

	/**
	 * Save the state to the state file.
	 */
	public void saveState(File stateFile) throws IOException {
		XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WORKING_SET_MANAGER);
		saveWorkingSetState(memento);
		saveMruList(memento);

		FileOutputStream stream = new FileOutputStream(stateFile);
		try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
		memento.save(writer);
		}

	}

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		// nothing - this is handled lazily. These items are only created as needed by
		// the getUpdater() and getElementAdapter() methods

	}

	@Override
	public void removeExtension(IExtension extension, Object[] objects) {
		for (Object object : objects) {
			if (object instanceof IWorkingSetUpdater) {
				removeUpdater((IWorkingSetUpdater) object);

			}
			if (object instanceof IWorkingSetElementAdapter) {
				removeElementAdapter((IWorkingSetElementAdapter) object);
			}
		}
	}

	/**
	 * Remove the element adapter from the manager and dispose of it.
	 *
	 * @since 3.3
	 */
	private void removeElementAdapter(final IWorkingSetElementAdapter elementAdapter) {
		SafeRunner.run(new WorkingSetRunnable() {

			@Override
			public void run() throws Exception {
				elementAdapter.dispose();

			}
		});
		synchronized (elementAdapters) {
			elementAdapters.values().remove(elementAdapter);
		}
	}

	/**
	 * Remove the updater from the manager and dispose of it.
	 *
	 * @since 3.3
	 */
	private void removeUpdater(final IWorkingSetUpdater updater) {
		SafeRunner.run(new WorkingSetRunnable() {

			@Override
			public void run() throws Exception {
				updater.dispose();

			}
		});
		synchronized (updaters) {
			updaters.values().remove(updater);
		}
		firePropertyChange(IWorkingSetManager.CHANGE_WORKING_SET_UPDATER_UNINSTALLED, updater, null);
	}

	@Override
	public void addToWorkingSets(final IAdaptable element, IWorkingSet[] workingSets) {
		// ideally this method would be in a static util class of some kind but
		// we dont have any such beast for working sets and making one for one
		// method is overkill.
		for (final IWorkingSet workingSet : workingSets) {
			SafeRunner.run(new WorkingSetRunnable() {

				@Override
				public void run() throws Exception {
					IAdaptable[] adaptedNewElements = workingSet.adaptElements(new IAdaptable[] { element });
					if (adaptedNewElements.length == 1) {
						IAdaptable[] elements = workingSet.getElements();
						IAdaptable[] newElements = new IAdaptable[elements.length + 1];
						System.arraycopy(elements, 0, newElements, 0, elements.length);
						newElements[newElements.length - 1] = adaptedNewElements[0];
						workingSet.setElements(newElements);
					}
				}
			});
		}
	}

	@Override
	public void setRecentWorkingSetsLength(int length) {
		if (length < 1 || length > 99)
			throw new IllegalArgumentException("Invalid recent working sets length: " + length); //$NON-NLS-1$
		IPreferenceStore store = PrefUtil.getAPIPreferenceStore();
		store.setValue(IWorkbenchPreferenceConstants.RECENTLY_USED_WORKINGSETS_SIZE, length);
		// adjust length
		sizeRecentWorkingSets();
	}

	private void sizeRecentWorkingSets() {
		int maxLength = getRecentWorkingSetsLength();
		while (recentWorkingSets.size() > maxLength) {
			int lastPosition = recentWorkingSets.size() - 1;
			recentWorkingSets.remove(lastPosition);
		}
	}

	@Override
	public int getRecentWorkingSetsLength() {
		IPreferenceStore store = PrefUtil.getAPIPreferenceStore();
		return store.getInt(IWorkbenchPreferenceConstants.RECENTLY_USED_WORKINGSETS_SIZE);
	}
}
