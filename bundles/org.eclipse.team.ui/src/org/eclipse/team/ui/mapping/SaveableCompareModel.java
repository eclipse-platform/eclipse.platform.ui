/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.mapping;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.IPropertyListener;

/**
 * A model buffer is used to buffer changes made when comparing
 * or merging a model. A buffer can be shared between multiple
 * typed elements within a comparison. The buffer is used by the comparison
 * container in order to determine when a save is required.
 * <p>
 * Clients may subclass this class.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public abstract class SaveableCompareModel implements ISaveableCompareModel {

	private boolean dirty;
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.compare.IModelBuffer#isDirty()
	 */
	public boolean isDirty() {
		return dirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveableModel#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		if (!isDirty())
			return;
		try {
			performSave(monitor);
		} catch (CoreException e) {
			handleException(e);
			monitor.setCanceled(true);
		}
		setDirty(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.compare.ISaveableCompareModel#doRevert(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doRevert(IProgressMonitor monitor) {
		if (!isDirty())
			return;
		performRevert(monitor);
		setDirty(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.compare.IModelBuffer#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyListener(IPropertyListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.compare.IModelBuffer#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyListener(IPropertyListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Set the dirty state of this buffer. If the state
	 * has changed, a property change event will be fired.
	 * @param dirty the dirty state
	 */
	protected void setDirty(boolean dirty) {
		if (this.dirty == dirty) {
			return;
		}
		this.dirty = dirty;
		firePropertyChange(PROP_DIRTY);
	}

	/**
	 * Fire a property change event for this buffer.
	 * @param property the property that changed
	 */
	protected void firePropertyChange(final int property) {
		Object[] allListeners = listeners.getListeners();
		for (int i = 0; i < allListeners.length; i++) {
			final Object object = allListeners[i];
			Platform.run(new ISafeRunnable() {
				public void run() throws Exception {
					((IPropertyListener)object).propertyChanged(SaveableCompareModel.this, property);
				}
				public void handleException(Throwable exception) {
					// handled by platform
				}
			});
		}
	}
	
	/**
	 * Method invoked from {@link #doSave(IProgressMonitor)} to write
	 * out the buffer. By default, this method invokes <code>doSave</code>
	 * on the buffers savable model.
	 * @param monitor a progress monitor
	 * @throws CoreException if errors occur
	 */
	protected abstract void performSave(IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Method invoked from {@link #doRevert(IProgressMonitor)} to discard the 
	 * changes in the buffer.
	 * @param monitor a progress monitor
	 */
	protected abstract void performRevert(IProgressMonitor monitor);

	/** 
	 * Handle an exception that occurred during a save.
	 * @param exception the exception
	 */
	protected void handleException(CoreException exception) {
		// TODO Auto-generated method stub
		
	}
}
