/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.navigator.extensions.CommonActionExtensionSite;

/**
 * <p>
 * Provides actions from extensions for menu and
 * {@link org.eclipse.ui.IActionBars} contributions.
 * </p>
 * <p>
 * This abstract class should be extended by clients of the
 * <b>org.eclipse.ui.navigator.navigatorContent</b> extension point for
 * top-level and nested <b>actionProvider</b> elements.
 * </p>
 * <p>
 * {@link CommonActionProvider}s may be declared as top-level elements in the
 * extension point (e.g. an <b>actionProvider</b> element at the root of the
 * extension point). Alternatively, <b>actionProvider</b> elements may be
 * nested under a <b>navigatorContent</b> element, in which case they are
 * considered to be "associated" with that content extension. For more
 * information, see the <b>org.eclipse.ui.navigator.navigatorContent</b>
 * extension point.
 * </p>
 * <p>
 * Each action provider will have the opportunity to contribute to the context
 * menu when a user right clicks and also contribute to the {@link IActionBars}
 * each time the selection changes. Clients should re-configure the
 * {@link IActionBars} each time that {@link #fillActionBars(IActionBars)} is
 * called (which is once per selection changes). {@link #updateActionBars()}
 * will never be called from the {@link NavigatorActionService}. This behavior
 * is required since each selection could determine a different set of
 * retargetable actions. For instance, the "Delete" operation for a custom model
 * element is likely to be different than for a resource.
 * </p>
 * <p>
 * Therefore, each extension will have an opportunity to contribute to the
 * IActionBars based on the <b>possibleChildren</b> expression of the enclosing
 * <b>navigatorContent</b> extension or the <b>enablement</b> expression of
 * the <b>actionProvider</b> (for both top-level <b>actionProvider</b>s and
 * nested <b>actionProvider</b>s which only support a subset of the enclosing
 * content extensions <b>possibleChildren</b> expression).
 * <p>
 * Clients may extend this class.
 * </p>
 * 
 * @since 3.2
 */
public abstract class CommonActionProvider extends ActionGroup implements
		IMementoAware {

	private CommonActionExtensionSite actionSite;

	/**
	 * <p>
	 * Initialize the current ICommonActionProvider with the supplied
	 * information.
	 * </p>
	 * <p>
	 * init() is guaranteed to be called before any other method of the
	 * ActionGroup super class.
	 * 
	 * @param aSite
	 *            The configuration information for the instantiated Common
	 *            Action Provider.
	 */
	public void init(ICommonActionExtensionSite aSite) {
		actionSite = (CommonActionExtensionSite) aSite;
	}

	/**
	 * Filters the specified action through the {@link WorkbenchActivityHelper}.
	 * 
	 * This is used to determine if the {@link IAction} should be included based
	 * on the currently enabled activities.
	 * 
	 * @return true, if the action is to be filtered (suppressed)
	 * 
	 * @since 3.4
	 */
	protected boolean filterAction(final IAction action) {
		if (actionSite == null) {
			String message = "init() method was not called on CommonActionProvider: " + this + " make sure your init() method calls the superclass"; //$NON-NLS-1$ //$NON-NLS-2$
			throw new IllegalStateException(message);
		}
		
		IPluginContribution piCont = new IPluginContribution() {
			public String getLocalId() {
				return action.getId();
			}

			public String getPluginId() {
				return actionSite.getPluginId();
			}
		};

		return WorkbenchActivityHelper.filterItem(piCont);
	}
	
	
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

	/**
	 * 
	 * @return The cached reference to the action site. Will only be non-null if
	 *         the subclass calls super.init() first.
	 */
	protected final ICommonActionExtensionSite getActionSite() {
		return actionSite;
	}

}
