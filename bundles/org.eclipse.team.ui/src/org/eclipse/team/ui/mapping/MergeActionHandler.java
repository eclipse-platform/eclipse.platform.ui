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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.mapping.ResourceMarkAsMergedHandler;
import org.eclipse.team.internal.ui.mapping.ResourceMergeHandler;
import org.eclipse.team.ui.compare.IModelBuffer;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * An abstract superclass that enables models to create handlers
 * for the basic merge operations (merge, overwrite and mark-as-merged).
 * This class makes use of a {@link SynchronizationOperation} to determine its 
 * enablement state and execute the handler. Enablement is detemermined
 * using {@link SynchronizationOperation#shouldRun()} and the handler will
 * invoke {@link SynchronizationOperation#run()} when executed.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 * @see SynchronizationActionProvider
 */
public abstract class MergeActionHandler extends AbstractHandler {

	private final ISynchronizePageConfiguration configuration;
	private boolean enabled = false;
	private ISelectionChangedListener listener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			updatedEnablement(event);
		}
	};
	
	/**
	 * Return an instance of the default handler for the given merge action id.
	 * @param mergeActionId the merge action id
	 * @param configuration the ynchronization page configuration
	 * @return the default handler for the given nerge action or <code>null</code>
	 */
	public static IHandler getDefaultHandler(String mergeActionId, ISynchronizePageConfiguration configuration) {
		if (mergeActionId == SynchronizationActionProvider.MERGE_ACTION_ID) {
			return new ResourceMergeHandler(configuration, false /* no overwrite */);
		} else if (mergeActionId == SynchronizationActionProvider.OVERWRITE_ACTION_ID) {
			return new ResourceMergeHandler(configuration, true /* overwrite */);
		} else if (mergeActionId == SynchronizationActionProvider.MARK_AS_MERGE_ACTION_ID) {
			return new ResourceMarkAsMergedHandler(configuration);
		}
		return null;
	}

	/**
	 * Create the handler.
	 * @param model the extension state model that contains the state
	 * provided by the synchronize page display the model.
	 */
	public MergeActionHandler(ISynchronizePageConfiguration configuration) {
		this.configuration = configuration;
		getSelectionProvider().addSelectionChangedListener(listener);
		updateEnablement((IStructuredSelection)getSelectionProvider().getSelection());
	}
	
	/**
	 * Deregister this handler from selection change events. 
	 */
	public void dispose() {
		getSelectionProvider().removeSelectionChangedListener(listener);
	}

	/* private */ void updatedEnablement(SelectionChangedEvent event) {
		updateEnablement((IStructuredSelection)event.getSelection());
	}

	/**
	 * Update the enablement of this handler for the new selection.
	 * By default, this method uses the <code>shouldRun</code>
	 * method of the handler's operation to determine the enablement
	 * of this handler. Subclasses may override but should
	 * either still invoke this method or {@link #setEnabled(boolean)}
	 * to set the enablement.
	 * @param selection the selection
	 */
	protected void updateEnablement(IStructuredSelection selection) {
		boolean isEnabled = getOperation().shouldRun();
		setEnabled(isEnabled);
	}

	/**
	 * Return the configuration of the synchronize page that is surfacing
	 * the merge action to which this handler is registered.
	 * @return 
	 */
	protected final ISynchronizePageConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Return the current selection.
	 * @return the current selection.
	 */
	protected final IStructuredSelection getStructuredSelection() {
		return (IStructuredSelection)getSelectionProvider().getSelection();
	}

	private ISelectionProvider getSelectionProvider() {
		return getConfiguration().getSite().getSelectionProvider();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Set the enablement of this handler.
	 * @param isEnabled whether the handelr is enabled
	 */
	protected void setEnabled(boolean isEnabled) {
		if (enabled != isEnabled) {
			enabled = isEnabled;
			fireHandlerChanged(new HandlerEvent(this, true, false));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		try {
			getOperation().run();
		} catch (InvocationTargetException e) {
			Utils.handle(e);
		} catch (InterruptedException e) {
			// Ignore
		}
		return null;
	}

	/**
	 * Return the synchronizatio operation that performs
	 * the merge operaton.
	 * @return a synchronization operation
	 */
	protected abstract SynchronizationOperation getOperation();
	
	/**
	 * Return the buffer that is the target of this handler.
	 * By default, <code>null</code> is returned.
	 * @return the buffer that is the target of this operation
	 */
	public IModelBuffer getTargetBuffer() {
		return getOperation().getTargetBuffer();
	}
}
