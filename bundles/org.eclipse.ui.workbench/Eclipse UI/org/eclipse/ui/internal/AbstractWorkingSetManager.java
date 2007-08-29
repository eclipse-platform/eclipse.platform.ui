/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
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
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;


/**
 * Abstract implementation of <code>IWorkingSetManager</code>.
 */
public abstract class AbstractWorkingSetManager extends EventManager implements
		IWorkingSetManager, BundleListener, IExtensionChangeHandler {
	
    private SortedSet workingSets = new TreeSet(WorkingSetComparator.INSTANCE);
    
    /**
     * Size of the list of most recently used working sets.
     */
    private static final int MRU_SIZE = 5;
    private List recentWorkingSets = new ArrayList();

    private BundleContext bundleContext;
    private Map/*<String, IWorkingSetUpdater>*/ updaters= new HashMap();

	private Map/*<String, IWorkingSetElementAdapter>*/ elementAdapters = new HashMap();
    
    private static final IWorkingSetUpdater NULL_UPDATER= new IWorkingSetUpdater() {
		public void add(IWorkingSet workingSet) {
		}
		public boolean remove(IWorkingSet workingSet) {
			return true;
		}
		public boolean contains(IWorkingSet workingSet) {
			return true;
		}
		public void dispose() {
		}
	};
	
	private static final IWorkingSetElementAdapter IDENTITY_ADAPTER = new IWorkingSetElementAdapter() {

		public IAdaptable[] adaptElements(IWorkingSet ws, IAdaptable[] elements) {
			return elements;
		}

		public void dispose() {
		}
	};
		
    /**
     * Returns the descriptors for the given editable working set ids. If an id
     * refers to a missing descriptor, or one that is non-editable, it is
     * skipped. If <code>null</code> is passed, all editable descriptors are
     * returned.
     * 
     * @param supportedWorkingSetIds
     *            the ids for the working set descriptors, or <code>null</code>
     *            for all editable descriptors
     * @return the descriptors corresponding to the given editable working set
     *         ids
     */
    private static WorkingSetDescriptor[] getSupportedEditableDescriptors(
            String[] supportedWorkingSetIds) {
        WorkingSetRegistry registry = WorkbenchPlugin.getDefault()
                .getWorkingSetRegistry();
        if (supportedWorkingSetIds == null) {
            return registry.getNewPageWorkingSetDescriptors();
        }
        List result = new ArrayList(supportedWorkingSetIds.length);
        for (int i = 0; i < supportedWorkingSetIds.length; i++) {
            WorkingSetDescriptor desc = registry
                    .getWorkingSetDescriptor(supportedWorkingSetIds[i]);
            if (desc != null && desc.isEditable()) {
                result.add(desc);
            }
        }
        return (WorkingSetDescriptor[]) result
                .toArray(new WorkingSetDescriptor[result.size()]);
    }
    
    protected AbstractWorkingSetManager(BundleContext context) {
    	bundleContext= context;
    	bundleContext.addBundleListener(this);
    	PlatformUI.getWorkbench().getExtensionTracker().registerHandler(this, ExtensionTracker
				.createExtensionPointFilter(getExtensionPointFilter()));
	}
    
    /**
	 * 
	 * @return
	 * @since 3.3
	 */
	private IExtensionPoint getExtensionPointFilter() {
		return Platform.getExtensionRegistry().getExtensionPoint(
				PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_WORKINGSETS);
	}
    
    public void dispose() {
    	bundleContext.removeBundleListener(this);
    	for (Iterator iter= updaters.values().iterator(); iter.hasNext();) {
			((IWorkingSetUpdater)iter.next()).dispose();
		}
    	
    	for (Iterator iter= elementAdapters.values().iterator(); iter.hasNext();) {
			((IWorkingSetElementAdapter)iter.next()).dispose();
		}
    }
    
    //---- working set creation -----------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public IWorkingSet createWorkingSet(String name, IAdaptable[] elements) {
        return new WorkingSet(name, name, elements);
    }
    
    public IWorkingSet createAggregateWorkingSet(String name, String label,
			IWorkingSet[] components) {
		return new AggregateWorkingSet(name, label, components);
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager
	 */
    public IWorkingSet createWorkingSet(IMemento memento) {
        return restoreWorkingSet(memento);
    }

    //---- working set management ---------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public void addWorkingSet(IWorkingSet workingSet) {
        Assert.isTrue(!workingSets.contains(workingSet),
                "working set already registered"); //$NON-NLS-1$
        internalAddWorkingSet(workingSet);
    }

    private void internalAddWorkingSet(IWorkingSet workingSet) {
		workingSets.add(workingSet);
        ((AbstractWorkingSet)workingSet).connect(this);
        addToUpdater(workingSet);
        firePropertyChange(CHANGE_WORKING_SET_ADD, null, workingSet);
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    protected boolean internalRemoveWorkingSet(IWorkingSet workingSet) {
        boolean workingSetRemoved = workingSets.remove(workingSet);
        boolean recentWorkingSetRemoved = recentWorkingSets.remove(workingSet);
        
        if (workingSetRemoved) {
        	((AbstractWorkingSet)workingSet).disconnect();
        	removeFromUpdater(workingSet);
            firePropertyChange(CHANGE_WORKING_SET_REMOVE, workingSet, null);
        }
        return workingSetRemoved || recentWorkingSetRemoved;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public IWorkingSet[] getWorkingSets() {
    		SortedSet visibleSubset = new TreeSet(WorkingSetComparator.INSTANCE);
    		for (Iterator i = workingSets.iterator(); i.hasNext();) {
				IWorkingSet workingSet = (IWorkingSet) i.next();
				if (workingSet.isVisible()) {
					visibleSubset.add(workingSet);
				}
			}
        return (IWorkingSet[]) visibleSubset.toArray(new IWorkingSet[visibleSubset.size()]);
    }
    
    public IWorkingSet[] getAllWorkingSets() {
    		return (IWorkingSet[]) workingSets.toArray(new IWorkingSet[workingSets.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public IWorkingSet getWorkingSet(String name) {
        if (name == null || workingSets == null) {
			return null;
		}

        Iterator iter = workingSets.iterator();
        while (iter.hasNext()) {
            IWorkingSet workingSet = (IWorkingSet) iter.next();
            if (name.equals(workingSet.getName())) {
				return workingSet;
			}
        }
        return null;
    }
    
    // ---- recent working set management --------------------------------------
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public IWorkingSet[] getRecentWorkingSets() {
        return (IWorkingSet[]) recentWorkingSets.toArray(new IWorkingSet[recentWorkingSets.size()]);
    }

    /**
     * Adds the specified working set to the list of recently used
     * working sets.
     * 
     * @param workingSet working set to added to the list of recently 
     * 	used working sets.
     */
    protected void internalAddRecentWorkingSet(IWorkingSet workingSet) {
    		if (!workingSet.isVisible()) {
				return;
			}
        recentWorkingSets.remove(workingSet);
        recentWorkingSets.add(0, workingSet);
        if (recentWorkingSets.size() > MRU_SIZE) {
            recentWorkingSets.remove(MRU_SIZE);
        }
    }

    //---- equals and hash code -----------------------------------------------
    
    /**
     * Tests the receiver and the object for equality
     * 
     * @param object object to compare the receiver to
     * @return true=the object equals the receiver, it has the same 
     * 	working sets. false otherwise
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!getClass().getName().equals(object.getClass().getName())) {
			return false;
		}
        AbstractWorkingSetManager other= (AbstractWorkingSetManager)object;
        return other.workingSets.equals(workingSets);
    }

    /**
     * Returns the hash code.
     * 
     * @return the hash code.
     */
    public int hashCode() {
        return workingSets.hashCode();
    }

    //---- property listeners -------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        addListenerObject(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        removeListenerObject(listener);
    }

    /**
     * Notify property change listeners about a change to the list of 
     * working sets.
     * 
     * @param changeId one of 
     * 	IWorkingSetManager#CHANGE_WORKING_SET_ADD 
     * 	IWorkingSetManager#CHANGE_WORKING_SET_REMOVE
     * 	IWorkingSetManager#CHANGE_WORKING_SET_CONTENT_CHANGE 
     * 	IWorkingSetManager#CHANGE_WORKING_SET_NAME_CHANGE
     * @param oldValue the removed working set or null if a working set 
     * 	was added or changed.
     * @param newValue the new or changed working set or null if a working 
     * 	set was removed.
     */
    protected void firePropertyChange(String changeId, Object oldValue,
            Object newValue) {
        final Object[] listeners = getListeners();
		
        if (listeners.length == 0) {
			return;
		}
		
        final PropertyChangeEvent event = new PropertyChangeEvent(this,
                changeId, oldValue, newValue);
		Runnable notifier = new Runnable() {
			public void run() {
				for (int i = 0; i < listeners.length; i++) {
					final IPropertyChangeListener listener = (IPropertyChangeListener) listeners[i];
					ISafeRunnable safetyWrapper = new ISafeRunnable() {

						public void run() throws Exception {
							listener.propertyChange(event);
						}

						public void handleException(Throwable exception) {
							// logged by the runner
						}
					};
					SafeRunner.run(safetyWrapper);
				}
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
	 * @param changedWorkingSet
	 *            the working set that has changed
	 * @param propertyChangeId
	 *            the changed property. one of
	 *            CHANGE_WORKING_SET_CONTENT_CHANGE,
	 *            CHANGE_WORKING_SET_LABEL_CHANGE, and
	 *            CHANGE_WORKING_SET_NAME_CHANGE
	 * @param oldValue
	 *            the old value
	 */
	public void workingSetChanged(IWorkingSet changedWorkingSet,
			String propertyChangeId, Object oldValue) {
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
        Iterator iterator = workingSets.iterator();
        
        // break the sets into aggregates and non aggregates.  The aggregates should be saved after the non-aggregates
        // so that on restoration all necessary aggregate components can be found.
        
        ArrayList standardSets = new ArrayList();
        ArrayList aggregateSets = new ArrayList();
        while (iterator.hasNext()) {
        		IWorkingSet set = (IWorkingSet) iterator.next();
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
	 * @param list the working sets to save
	 * @since 3.2
	 */
	private void saveWorkingSetState(IMemento memento, List list) {
		for (Iterator i = list.iterator(); i.hasNext();) {
            IPersistableElement persistable = (IWorkingSet) i.next();
            IMemento workingSetMemento = memento
                    .createChild(IWorkbenchConstants.TAG_WORKING_SET);
            workingSetMemento.putString(IWorkbenchConstants.TAG_FACTORY_ID,
                    persistable.getFactoryId());
            persistable.saveState(workingSetMemento);
        }
	}
    
    /**
     * Recreates all working sets from the persistence store
     * and adds them to the receiver.
     * 
     * @param memento the persistence store
     */
    protected void restoreWorkingSetState(IMemento memento) {
        IMemento[] children = memento
                .getChildren(IWorkbenchConstants.TAG_WORKING_SET);
        for (int i = 0; i < children.length; i++) {
            IWorkingSet workingSet = restoreWorkingSet(children[i]);
            if (workingSet != null) {
            	internalAddWorkingSet(workingSet);
            }
        }
    }
    
    /**
     * Recreates a working set from the persistence store.
     * 
     * @param memento the persistence store
     * @return the working set created from the memento or null if
     * 	creation failed.
     */
    protected IWorkingSet restoreWorkingSet(IMemento memento) {
        String factoryID = memento
                .getString(IWorkbenchConstants.TAG_FACTORY_ID);

        if (factoryID == null) {
            // if the factory id was not set in the memento
            // then assume that the memento was created using
            // IMemento.saveState, and should be restored using WorkingSetFactory
            factoryID = AbstractWorkingSet.FACTORY_ID;
        }
        IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(
                factoryID);
        if (factory == null) {
            WorkbenchPlugin
                    .log("Unable to restore working set - cannot instantiate factory: " + factoryID); //$NON-NLS-1$
            return null;
        }
        IAdaptable adaptable = factory.createElement(memento);
        if (adaptable == null) {
            WorkbenchPlugin
                    .log("Unable to restore working set - cannot instantiate working set: " + factoryID); //$NON-NLS-1$
            return null;
        }
        if ((adaptable instanceof IWorkingSet) == false) {
            WorkbenchPlugin
                    .log("Unable to restore working set - element is not an IWorkingSet: " + factoryID); //$NON-NLS-1$
            return null;
        }
        return (IWorkingSet) adaptable;
    }

    /**
     * Saves the list of most recently used working sets in the persistence 
     * store.
     * 
     * @param memento the persistence store
     */
    protected void saveMruList(IMemento memento) {
        Iterator iterator = recentWorkingSets.iterator();

        while (iterator.hasNext()) {
            IWorkingSet workingSet = (IWorkingSet) iterator.next();
            IMemento mruMemento = memento
                    .createChild(IWorkbenchConstants.TAG_MRU_LIST);

            mruMemento.putString(IWorkbenchConstants.TAG_NAME, workingSet
                    .getName());
        }
    }

   /**
     * Restores the list of most recently used working sets from the 
     * persistence store.
     * 
     * @param memento the persistence store
     */
    protected void restoreMruList(IMemento memento) {
        IMemento[] mruWorkingSets = memento
                .getChildren(IWorkbenchConstants.TAG_MRU_LIST);

        for (int i = mruWorkingSets.length - 1; i >= 0; i--) {
            String workingSetName = mruWorkingSets[i]
                    .getString(IWorkbenchConstants.TAG_NAME);
            if (workingSetName != null) {
                IWorkingSet workingSet = getWorkingSet(workingSetName);
                if (workingSet != null) {
                    internalAddRecentWorkingSet(workingSet);
                }
            }
        }
    }

    //---- user interface support -----------------------------------------------------
    
    /**
     * @see org.eclipse.ui.IWorkingSetManager#createWorkingSetEditWizard(org.eclipse.ui.IWorkingSet)
     * @since 2.1
     */
    public IWorkingSetEditWizard createWorkingSetEditWizard(
            IWorkingSet workingSet) {
        String editPageId = workingSet.getId();
        WorkingSetRegistry registry = WorkbenchPlugin.getDefault()
                .getWorkingSetRegistry();
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
    public IWorkingSetSelectionDialog createWorkingSetSelectionDialog(
            Shell parent) {
        return createWorkingSetSelectionDialog(parent, true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public IWorkingSetSelectionDialog createWorkingSetSelectionDialog(
            Shell parent, boolean multi) {
        return createWorkingSetSelectionDialog(parent, multi, null);
    }
    
    /**
	 * {@inheritDoc}
	 */
    public IWorkingSetNewWizard createWorkingSetNewWizard(String[] workingSetIds) {
         WorkingSetDescriptor[] descriptors= getSupportedEditableDescriptors(workingSetIds);
         if (descriptors.length == 0) {
			return null;
		}
         return new WorkingSetNewWizard(descriptors);
}

    //---- working set delta handling -------------------------------------------------
    
	public void bundleChanged(BundleEvent event) {
		if (event.getBundle().getSymbolicName() == null)
			return;
		// If the workbench isn't running anymore simply return.
		if (!Workbench.getInstance().isRunning()) {
			return;
		}
		
		if (event.getBundle().getState() == Bundle.ACTIVE) {
			WorkingSetDescriptor[] descriptors = WorkbenchPlugin.getDefault()
					.getWorkingSetRegistry().getUpdaterDescriptorsForNamespace(
							event.getBundle().getSymbolicName());
			synchronized (updaters) {
				for (int i = 0; i < descriptors.length; i++) {
					WorkingSetDescriptor descriptor = descriptors[i];
					List workingSets = getWorkingSetsForId(descriptor.getId());
					if (workingSets.size() == 0) {
						continue;
					}
					IWorkingSetUpdater updater = getUpdater(descriptor);
					for (Iterator iter = workingSets.iterator(); iter.hasNext();) {
						IWorkingSet workingSet = (IWorkingSet) iter.next();
						if (!updater.contains(workingSet)) {
							updater.add(workingSet);
						}
					}
				}
			}
		}
	}

	private List getWorkingSetsForId(String id) {
		List result= new ArrayList();
    	for (Iterator iter= workingSets.iterator(); iter.hasNext();) {
    		IWorkingSet ws= (IWorkingSet)iter.next();
    		if (id.equals(ws.getId())) {
				result.add(ws);
			}
		}
    	return result;
	}
	
    private void addToUpdater(IWorkingSet workingSet) {
    	WorkingSetDescriptor descriptor= WorkbenchPlugin.getDefault()
			.getWorkingSetRegistry().getWorkingSetDescriptor(workingSet.getId());
    	if (descriptor == null || !descriptor.isUpdaterClassLoaded()) {
			return;
		}
		synchronized(updaters) {
	    	IWorkingSetUpdater updater= getUpdater(descriptor);
	    	if (!updater.contains(workingSet)) {
				updater.add(workingSet);
			}
		}
    }
    
    private IWorkingSetUpdater getUpdater(WorkingSetDescriptor descriptor) {
		IWorkingSetUpdater updater= (IWorkingSetUpdater)updaters.get(descriptor.getId());
    	if (updater == null) {
    		updater= descriptor.createWorkingSetUpdater();
    		if (updater == null) {
    			updater= NULL_UPDATER;
    		} else {
    			firePropertyChange(CHANGE_WORKING_SET_UPDATER_INSTALLED, null, updater);
    			PlatformUI.getWorkbench().getExtensionTracker().registerObject(
						descriptor.getConfigurationElement()
								.getDeclaringExtension(), updater,
						IExtensionTracker.REF_WEAK);
    			
    		}
        	updaters.put(descriptor.getId(), updater);
    	}
		return updater;
	}
    
    IWorkingSetElementAdapter getElementAdapter(WorkingSetDescriptor descriptor) {
		IWorkingSetElementAdapter elementAdapter = (IWorkingSetElementAdapter) elementAdapters
				.get(descriptor.getId());
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

	private void removeFromUpdater(IWorkingSet workingSet) {
		synchronized (updaters) {
			IWorkingSetUpdater updater = (IWorkingSetUpdater) updaters
					.get(workingSet.getId());
			if (updater != null) {
				updater.remove(workingSet);
			}
		}
    }	
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager#createWorkingSetSelectionDialog(org.eclipse.swt.widgets.Shell, boolean, java.lang.String[])
     */
    public IWorkingSetSelectionDialog createWorkingSetSelectionDialog(Shell parent, boolean multi, String[] workingsSetIds) {
        return new WorkingSetSelectionDialog(parent, multi, workingsSetIds);
    }

	/**
	 * Save the state to the state file.
	 * 
	 * @param stateFile
	 * @throws IOException
	 */
	public void saveState(File stateFile) throws IOException {
		XMLMemento memento = XMLMemento
				.createWriteRoot(IWorkbenchConstants.TAG_WORKING_SET_MANAGER);
		saveWorkingSetState(memento);
		saveMruList(memento);
	
		FileOutputStream stream = new FileOutputStream(stateFile);
		OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
		memento.save(writer);
		writer.close();
	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamichelpers.IExtensionTracker, org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		// nothing - this is handled lazily.  These items are only created as needed by the getUpdater() and getElementAdapter() methods
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
	 */
	public void removeExtension(IExtension extension, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (object instanceof IWorkingSetUpdater) {
				removeUpdater((IWorkingSetUpdater)object);
				
			}
			if (object instanceof IWorkingSetElementAdapter) {
				removeElementAdapter((IWorkingSetElementAdapter) object);
			}
		} 
	}

	/**
	 * Remove the element adapter from the manager and dispose of it.
	 * 
	 * @param elementAdapter
	 * @since 3.3
	 */
	private void removeElementAdapter(
			final IWorkingSetElementAdapter elementAdapter) {
		SafeRunner.run(new ISafeRunnable() {

			public void handleException(Throwable exception) {
				StatusManager.getManager().handle(
						StatusUtil.newStatus(PlatformUI.PLUGIN_ID, exception));
			}

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
	 * @param updater
	 * @since 3.3
	 */
	private void removeUpdater(final IWorkingSetUpdater updater) {
		SafeRunner.run(new ISafeRunnable() {

			public void handleException(Throwable exception) {
				StatusManager.getManager().handle(
						StatusUtil.newStatus(PlatformUI.PLUGIN_ID, exception));
			}

			public void run() throws Exception {
				updater.dispose();

			}
		});
		synchronized (updaters) {
			updaters.values().remove(updater);			
		}
		firePropertyChange(IWorkingSetManager.CHANGE_WORKING_SET_UPDATER_UNINSTALLED, updater, null);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#addToWorkingSets(org.eclipse.core.runtime.IAdaptable,
	 *      org.eclipse.ui.IWorkingSet[])
	 */
	public void addToWorkingSets(IAdaptable element, IWorkingSet[] workingSets) {
		// ideally this method would be in a static util class of some kind but
		// we dont have any such beast for working sets and making one for one
		// method is overkill.
		for (int i = 0; i < workingSets.length; i++) {
			IWorkingSet workingSet = workingSets[i];
			IAdaptable[] adaptedNewElements = workingSet
					.adaptElements(new IAdaptable[] { element });
			if (adaptedNewElements.length == 1) {
				IAdaptable[] elements = workingSet.getElements();
				IAdaptable[] newElements = new IAdaptable[elements.length + 1];
				System.arraycopy(elements, 0, newElements, 0, elements.length);
				newElements[newElements.length - 1] = adaptedNewElements[0];
				workingSet.setElements(newElements);
			}
		}
	}
}
