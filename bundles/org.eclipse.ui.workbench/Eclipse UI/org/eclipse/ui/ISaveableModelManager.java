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

package org.eclipse.ui;


/**
 * The model manager maintains a list of open saveable models.
 * 
 * @see ISaveableModel
 * @see ISaveableModelSource
 * 
 * @since 3.2
 */
public interface ISaveableModelManager extends IModelLifecycleListener {

	/**
	 * Returns the list of open models managed by this model manager.
	 * 
	 * @return a list of models
	 */
	public ISaveableModel[] getOpenModels();

	/**
	 * This implementation of handleModelLifecycleEvent must be called by
	 * implementers of ISaveableModelSource whenever the list of models of the
	 * model source changes, or when the dirty state of models changes. The
	 * ISaveableModelSource instance must be passed as the source of the event
	 * object.
	 * <p>
	 * This method may also be called by objects that hold on to models but are
	 * not workbench parts. In this case, the event source must be set to an
	 * object that is not an instanceof IWorkbenchPart.
	 * </p>
	 * <p>
	 * Corresponding open and close events must originate from the same
	 * (identical) event source.
	 * </p>
	 * <p>
	 * This method must be called on the UI thread.
	 * </p>
	 */
	public void handleModelLifecycleEvent(ModelLifecycleEvent event);

	/**
	 * Adds the given listener to the list of listeners. Has no effect if the
	 * same (identical) listener has already been added. The listener will be
	 * notified about changes to the models managed by this model manager. Event
	 * types include: <br>
	 * POST_OPEN when models were added to the list of models <br>
	 * POST_CLOSE when models were removed from the list of models <br>
	 * DIRTY_CHANGED when the dirty state of models changed
	 * <p>
	 * Listeners should ignore all other event types, including PRE_CLOSE. There
	 * is no guarantee that listeners are notified before models are closed.
	 * 
	 * @param listener
	 */
	public void addModelLifecycleListener(IModelLifecycleListener listener);

	/**
	 * Removes the given listener from the list of listeners. Has no effect if
	 * the given listener is not contained in the list.
	 * 
	 * @param listener
	 */
	public void removeModelLifecycleListener(IModelLifecycleListener listener);

}
