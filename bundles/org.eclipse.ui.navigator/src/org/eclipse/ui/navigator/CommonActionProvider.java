/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionGroup;

/**
 * <p>
 * Provides actions from extensions for menu and
 * {@link org.eclipse.ui.IActionBars} contributions.
 * </p>
 * <p>
 * This abstract class should be subclassed by clients of the
 * <b>org.eclipse.ui.navigator.navigatorContent</b> extension point for
 * top-level and nested &lt;actionProvider &gt; elements.
 * </p>
 * <p> 
 * {@link CommonActionProvider}s are declared via the
 * <b>org.eclipse.ui.navigator.navigatorContent</b> extension point. 
 * {@link CommonActionProvider}s may be declared as top-level elements in the extension point
 * (e.g. an &lt;actionProvider /&gt; element at the root of the extension point.
 * Alternatively, &lt;actionProvider /&gt; elements may be nested under a
 * &lt;navigatorContent /&gt; element, in which case they are considered to be
 * "associated" with that content extension. See the documentation of the
 * <b>org.eclipse.ui.navigator.navigatorContent</b> extension point and
 * {@link CommonActionProvider} for more information on declaring Common Action
 * Providers.
 * </p> 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * <p>
 * Clients may subclass this class.
 * </p>
 * 
 * @since 3.2
 */
public abstract class CommonActionProvider extends ActionGroup implements
		IMementoAware {

	/**
	 * <p>
	 * Initialize the current ICommonActionProvider with the supplied
	 * information.
	 * </p>
	 * <p>
	 * init() is guaranteed to be called before any other method of the
	 * ActionGroup super class.
	 * 
	 * @param aConfig
	 *            The configuration information for the instantiated Common
	 *            Action Provider.
	 */
	public abstract void init(CommonActionProviderConfig aConfig);

	/**
	 * <p>
	 * Restore the previous state of any actions using the flags in aMemento.
	 * This method allows the state of any actions that persist from session to
	 * session to be restored.
	 * </p>
	 * 
	 * <p>
	 * The default behavior is to do nothing.
	 * </p>
	 * 
	 * @param aMemento
	 *            A memento that was given to the view part to restore its
	 *            state.
	 */
	public void restoreState(IMemento aMemento) {
	}

	/**
	 * <p>
	 * Save flags in aMemento to remember the state of any actions that persist
	 * from session to session.
	 * </p>
	 * <p>
	 * Extensions should qualify any keys stored in the memento with their
	 * plugin id
	 * </p>
	 * 
	 * <p>
	 * The default behavior is to do nothing.
	 * </p>
	 * 
	 * @param aMemento
	 *            A memento that was given to the view part to save its state.
	 */
	public void saveState(IMemento aMemento) {
	}

}
