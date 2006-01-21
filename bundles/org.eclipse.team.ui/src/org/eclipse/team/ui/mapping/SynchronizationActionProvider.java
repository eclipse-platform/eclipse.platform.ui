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

import java.util.*;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.mapping.CommonMenuManager;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.*;

/**
 * An action group that can be used by models to contribute actions
 * to a team synchronization viewer. Subclases should override the
 * {@link #initialize()} method in order to register handlers for the
 * following merge actions if they want cutom merge behavior:
 * <ul>
 * <li>{@link #MERGE_ACTION_ID}
 * <li>{@link #OVERWRITE_ACTION_ID}
 * <li>{@link #OVERWRITE_ACTION_ID}
 * </ul>
 * They may also add other actions to the context menu or register action handlers
 * in the {@link #fillActionBars(IActionBars)} method.
 * <p>
 * This class may be subclasses by clients
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see MergeActionHandler
 * @since 3.2
 */
public class SynchronizationActionProvider extends CommonActionProvider {
	
	/**
	 * Action id constant for the merge action.
	 * @see #registerHandler(String, IHandler)
	 */
	public static final String MERGE_ACTION_ID = "org.eclipse.team.ui.mergeAction"; //$NON-NLS-1$
	
	/**
	 * Action id constant for the merge action.
	 * @see #registerHandler(String, IHandler)
	 */
	public static final String OVERWRITE_ACTION_ID = "org.eclipse.team.ui.overwriteAction"; //$NON-NLS-1$
	
	/**
	 * Action id constant for the mark-as-merge action.
	 * @see #registerHandler(String, IHandler)
	 */
	public static final String MARK_AS_MERGE_ACTION_ID = "org.eclipse.team.ui.markAsMergeAction"; //$NON-NLS-1$

	private CommonActionProviderConfig config;
	private Map handlers = new HashMap();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.CommonActionProviderConfig)
	 */
	public final void init(CommonActionProviderConfig aConfig) {
		config = aConfig;
		initialize();
	}

	/**
	 * Method called during action provider initialization.
	 * It is invoked from the {@link #init(CommonActionProviderConfig)}
	 * after after the configuration has been recorded. Subclasses
	 * may override. Subclasses that want to provide there own merge actions
	 * handlers can register them in this method.
	 * @see #registerHandler(String, IHandler)
	 * @see MergeActionHandler
	 */
	protected void initialize() {
		// By deault, do nothing
	}

	/**
	 * Return the configuration for the common viewer.
	 * @return the configuration from the common viewer
	 */
	public final CommonActionProviderConfig getCommonConfiguration() {
		return config;
	}
	
	/**
	 * Return the configuration from the synchronize page that contains
	 * the common viewer.
	 * @return the configuration from the synchronize page that contains
	 * the common viewer
	 */
	protected final ISynchronizePageConfiguration getSynchronizePageConfiguration() {
		return (ISynchronizePageConfiguration)getExtensionStateModel().getProperty(ISynchronizationConstants.P_SYNCHRONIZATION_PAGE_CONFIGURATION);
	}

	/**
	 * Return the extension state model for the content provider associated with
	 * action provider.
	 * @return the extension state model for the content provider associated with
	 * action provider
	 */
	protected final IExtensionStateModel getExtensionStateModel() {
		return config.getExtensionStateModel();
	}
	
	/**
	 * Return the synchronization context to which the actions of this provider
	 * apply.
	 * @return the synchronization context to which the actions of this provider
	 * apply
	 */
	protected final ISynchronizationContext getSynchronizationContext() {
		return (ISynchronizationContext)getExtensionStateModel().getProperty(ISynchronizationConstants.P_SYNCHRONIZATION_CONTEXT);
	}
	
	/**
	 * Register the handler as the handler for the given action id when
	 * a merge action is performed on elements that match this groups 
	 * enablement.
	 * @param actionId the id of the merge action
	 * @param handler the handler for elements of the model that provided this group
	 */
	protected void registerHandler(String actionId, IHandler handler) {
		handlers.put(actionId, handler);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		if (menu instanceof CommonMenuManager) {
			CommonMenuManager manager = (CommonMenuManager) menu;
			for (Iterator iter = handlers.keySet().iterator(); iter.hasNext();) {
				String actionId = (String) iter.next();
				manager.registerHandler(actionId, (IHandler)handlers.get(actionId));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		// TODO: Register the handlers
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	public void dispose() {
		for (Iterator iter = handlers.values().iterator(); iter.hasNext();) {
			IHandler handler = (IHandler) iter.next();
			if (handler instanceof MergeActionHandler) {
				MergeActionHandler mah = (MergeActionHandler) handler;
				mah.dispose();
			}
		}
		super.dispose();
	}

}
