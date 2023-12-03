/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.navigator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.services.IDisposable;

/**
 * Provides {@link Saveable} objects to the common navigator, and allows to map
 * between elements in the tree and models.
 * <p>
 * This class is intended to be subclassed by clients.
 * </p>
 * Instances of subclasses will be returned from content extensions that
 * implement {@link IAdaptable}.
 *
 * @since 3.2
 */
public abstract class SaveablesProvider implements IDisposable {

	private ISaveablesLifecycleListener listener;

	/**
	 * Creates a new saveable model provider. May only be called by subclasses.
	 */
	protected SaveablesProvider() {
	}

	/**
	 * Initializes this SaveablesProvider with the given listener, and calls the
	 * hook method doInit().
	 *
	 * @param listener listener for lifecycle events
	 */
	final public void init(ISaveablesLifecycleListener listener) {
		this.listener = listener;
		doInit();
	}

	/**
	 * May be overridden by clients. The default implementation does nothing.
	 */
	protected void doInit() {
	}

	/**
	 * Notifies the listener that the given models were opened in this model
	 * provider. This method must be called on the UI thread.
	 *
	 * @param models the affected saveables
	 */
	final protected void fireSaveablesOpened(Saveable[] models) {
		listener.handleLifecycleEvent(new SaveablesLifecycleEvent(this,
				SaveablesLifecycleEvent.POST_OPEN, models, false));
	}

	/**
	 * Notifies the listener that the given models are about to be closed in
	 * this model provider. This method must be called on the UI thread.
	 *
	 * @param models the affected saveables
	 * @param force
	 *            true if the closing may be canceled by the user
	 * @return true if the listener vetoed the closing (may be ignored if force
	 *         is true)
	 */
	final protected boolean fireSaveablesClosing(Saveable[] models,
			boolean force) {
		SaveablesLifecycleEvent saveablesLifecycleEvent = new SaveablesLifecycleEvent(
				this, SaveablesLifecycleEvent.PRE_CLOSE, models, force);
		listener.handleLifecycleEvent(saveablesLifecycleEvent);
		return saveablesLifecycleEvent.isVeto();
	}

	/**
	 * Notifies the listener that the given models were closed in this model
	 * provider. This method must be called on the UI thread.
	 *
	 * @param models the affected saveables
	 */
	final protected void fireSaveablesClosed(Saveable[] models) {
		listener.handleLifecycleEvent(new SaveablesLifecycleEvent(this,
				SaveablesLifecycleEvent.POST_CLOSE, models, false));
	}

	/**
	 * Notifies the listener that the given models' dirty state has changed.
	 * This method must be called on the UI thread.
	 *
	 * @param models the affected saveables
	 */
	final protected void fireSaveablesDirtyChanged(Saveable[] models) {
		listener.handleLifecycleEvent(new SaveablesLifecycleEvent(this,
				SaveablesLifecycleEvent.DIRTY_CHANGED, models, false));
	}

	/**
	 * Returns the saveables reachable through this provider. Changes to the
	 * list of saveables or to the saveables' dirty state must be announced
	 * using the appropriate fire* methods.
	 *
	 * @return the saveables returned by this saveables provider.
	 */
	public abstract Saveable[] getSaveables();

	/**
	 * Returns the elements representing the given saveable. It is recommended
	 * that a saveable be represented by only one element.
	 *
	 * @param saveable the savable
	 * @return the elements representing the given saveable (array may be empty)
	 */
	public abstract Object[] getElements(Saveable saveable);

	/**
	 * Returns the saveable for the given element, or null if the element does
	 * not represent a saveable.
	 *
	 * @param element the element
	 * @return the saveable for the given element, or null
	 */
	public abstract Saveable getSaveable(Object element);

	/**
	 * Disposes of this saveables provider. Subclasses may extend, but must call
	 * the super implementation.
	 */
	@Override
	public void dispose() {
		listener = null;
	}

}
