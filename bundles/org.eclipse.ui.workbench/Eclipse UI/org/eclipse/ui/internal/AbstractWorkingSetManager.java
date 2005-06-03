/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.IWorkingSetUpdater;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.dialogs.IWorkingSetNewWizard;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.internal.dialogs.WorkingSetEditWizard;
import org.eclipse.ui.internal.dialogs.WorkingSetNewWizard;
import org.eclipse.ui.internal.dialogs.WorkingSetSelectionDialog;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;


/**
 * Abstract implementation of <code>IWorkingSetManager</code>.
 */
public abstract class AbstractWorkingSetManager implements IWorkingSetManager, BundleListener  {
	
    private SortedSet workingSets = new TreeSet(new WorkingSetComparator());
    private ListenerList propertyChangeListeners = new ListenerList();
    
    /**
     * Size of the list of most recently used working sets.
     */
    private static final int MRU_SIZE = 5;
    private List recentWorkingSets = new ArrayList();

    private BundleContext bundleContext;
    private Map/*<String, IWorkingSetUpdater>*/ updaters= new HashMap();
    
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
	}
    
    public void dispose() {
    	bundleContext.removeBundleListener(this);
    	for (Iterator iter= updaters.values().iterator(); iter.hasNext();) {
			((IWorkingSetUpdater)iter.next()).dispose();
		}
    }
    
    //---- working set creation -----------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public IWorkingSet createWorkingSet(String name, IAdaptable[] elements) {
        return new WorkingSet(name, elements);
    }

    /* (non-Javadoc)
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
        ((WorkingSet)workingSet).connect(this);
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
        	((WorkingSet)workingSet).disconnect();
        	removeFromUpdater(workingSet);
            firePropertyChange(CHANGE_WORKING_SET_REMOVE, workingSet, null);
        }
        return workingSetRemoved || recentWorkingSetRemoved;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public IWorkingSet[] getWorkingSets() {
        return (IWorkingSet[]) workingSets.toArray(new IWorkingSet[workingSets.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public IWorkingSet getWorkingSet(String name) {
        if (name == null || workingSets == null)
            return null;

        Iterator iter = workingSets.iterator();
        while (iter.hasNext()) {
            IWorkingSet workingSet = (IWorkingSet) iter.next();
            if (name.equals(workingSet.getName()))
                return workingSet;
        }
        return null;
    }
    
    //---- recent working set management --------------------------------------
    
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
        if (!getClass().getName().equals(object.getClass().getName()))
        	return false;
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
        propertyChangeListeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        propertyChangeListeners.remove(listener);
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
        final PropertyChangeEvent event = new PropertyChangeEvent(this,
                changeId, oldValue, newValue);

        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                Object[] listeners = propertyChangeListeners.getListeners();
                for (int i = 0; i < listeners.length; i++) {
                    ((IPropertyChangeListener) listeners[i])
                            .propertyChange(event);
                }
            }
        });
    }
    
    /**
     * Fires a property change event for the changed working set.
     * Should only be called by org.eclipse.ui.internal.WorkingSet.
     * @param changedWorkingSet the working set that has changed
     * @param propertyChangeId the changed property. one of 
     * 	CHANGE_WORKING_SET_CONTENT_CHANGE and CHANGE_WORKING_SET_NAME_CHANGE
     * @param oldValue the old value
     */
    public void workingSetChanged(IWorkingSet changedWorkingSet, String propertyChangeId, Object oldValue) {
        firePropertyChange(propertyChangeId, oldValue, changedWorkingSet);
    }
    
    //---- Persistence ----------------------------------------------------------------
    
    /**
     * Saves all persistable working sets in the persistence store.
     * 
     * @param memento the persistence store
     * @see IPersistableElement
     */
    protected void saveWorkingSetState(IMemento memento) {
        Iterator iterator = workingSets.iterator();

        while (iterator.hasNext()) {
            IPersistableElement persistable = (IWorkingSet) iterator.next();
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
            factoryID = WorkingSet.FACTORY_ID;
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
         if (descriptors.length == 0)
                 return null;
         return new WorkingSetNewWizard(descriptors);
}

    //---- working set delta handling -------------------------------------------------
    
	public void bundleChanged(BundleEvent event) {
		if (event.getBundle().getState() != Bundle.ACTIVE)
				return;		
		// If the workbench isn't running anymore simply return.
		if (!Workbench.getInstance().isRunning())
			return;
		WorkingSetDescriptor[] descriptors= WorkbenchPlugin.getDefault()
        	.getWorkingSetRegistry().getDescriptorsForNamespace(event.getBundle().getSymbolicName());
		synchronized(this) {
			for (int i= 0; i < descriptors.length; i++) {
				WorkingSetDescriptor descriptor= descriptors[i];
				List workingSets= getWorkingSetsForId(descriptor.getId());
				if (workingSets.size() == 0)
					continue;
				IWorkingSetUpdater updater= getUpdater(descriptor);
				for (Iterator iter= workingSets.iterator(); iter.hasNext();) {
					IWorkingSet workingSet= (IWorkingSet)iter.next();
					if (!updater.contains(workingSet))
						updater.add(workingSet);
				}
			}
		}
	}

	private List getWorkingSetsForId(String id) {
		List result= new ArrayList();
    	for (Iterator iter= workingSets.iterator(); iter.hasNext();) {
    		IWorkingSet ws= (IWorkingSet)iter.next();
    		if (id.equals(ws.getId()))
    			result.add(ws);
		}
    	return result;
	}
	
    private void addToUpdater(IWorkingSet workingSet) {
    	WorkingSetDescriptor descriptor= WorkbenchPlugin.getDefault()
			.getWorkingSetRegistry().getWorkingSetDescriptor(workingSet.getId());
    	if (descriptor == null || !descriptor.isDeclaringPluginActive())
    		return;
		synchronized(this) {
	    	IWorkingSetUpdater updater= getUpdater(descriptor);
	    	if (!updater.contains(workingSet))
	    		updater.add(workingSet);
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
    		}
        	updaters.put(descriptor.getId(), updater);
    	}
		return updater;
	}

	private synchronized void removeFromUpdater(IWorkingSet workingSet) {
    	IWorkingSetUpdater updater= (IWorkingSetUpdater)updaters.get(workingSet.getId());
    	if (updater != null) {
    		updater.remove(workingSet);
    	}
    }	
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkingSetManager#createWorkingSetSelectionDialog(org.eclipse.swt.widgets.Shell, boolean, java.lang.String[])
     */
    public IWorkingSetSelectionDialog createWorkingSetSelectionDialog(Shell parent, boolean multi, String[] workingsSetIds) {
        return new WorkingSetSelectionDialog(parent, multi, workingsSetIds);
    }
}
