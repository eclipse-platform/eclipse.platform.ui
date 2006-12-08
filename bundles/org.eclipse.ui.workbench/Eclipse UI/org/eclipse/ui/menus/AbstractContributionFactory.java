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

package org.eclipse.ui.menus;

import java.util.List;

/**
 * ContributionFactories are used by the IMenuService to populate
 * ContributionManagers. In {@link #createContributionItems(IMenuService, List)}
 * you fill in the additions List with IContributionItems to be inserted at this
 * factory's location. For example:
 * <p>
 * 
 * <pre>
 * AbstractContributionFactory contributions = new AbstractContributionFactory(
 * 		&quot;menu:org.eclipse.ui.tests.api.MenuTestHarness?after=additions&quot;) {
 * 	public void createContributionItems(IMenuService menuService, List additions) {
 * 		CommandContributionItem item = new CommandContributionItem(
 * 				&quot;org.eclipse.ui.tests.menus.helloWorld&quot;,
 * 				&quot;org.eclipse.ui.tests.commands.enabledHelloWorld&quot;, null, null,
 * 				&quot;Say Hello&quot;, null);
 * 		additions.add(item);
 * 		item = new CommandContributionItem(
 * 				&quot;org.eclipse.ui.tests.menus.refresh&quot;,
 * 				&quot;org.eclipse.ui.tests.commands.refreshView&quot;, null, null,
 * 				&quot;Refresh&quot;, null);
 * 		menuService.registerVisibleWhen(item, new MyActiveContextExpression(
 * 				&quot;org.eclipse.ui.tests.myview.context&quot;));
 * 		additions.add(item);
 * 	}
 * 
 * 	public void releaseContributionItems(IMenuService menuService, List items) {
 * 		// we have nothing to do
 * 	}
 * };
 * IMenuService service = (IMenuService) PlatformUI.getWorkbench().getService(
 * 		IMenuService.class);
 * service.addContributionFactory(contributions);
 * </pre>
 * 
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * Only the abstract methods may be implemented.
 * </p>
 * 
 * @since 3.3
 * @see org.eclipse.ui.menus.IMenuService
 * @see org.eclipse.jface.action.MenuManager
 * @see org.eclipse.jface.action.ToolBarManager
 */
public abstract class AbstractContributionFactory {
	private String location = null;

	/**
	 * The contribution factories must be instantiated with their location,
	 * which which specifies the contributions insertion location.
	 * 
	 * @param location
	 *            the addition location in Menu API URI format. It must not be
	 *            <code>null</code>.
	 */
	public AbstractContributionFactory(String location) {
		this.location = location;
	}

	/**
	 * Return the location as a String.
	 * 
	 * @return the location - never <code>null</code>.
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * This factory should create the IContributionItems that it wants to
	 * contribute, and add them to the additions list. The menu service will
	 * call this method at the appropriate time. It should always return new
	 * instances of its contributions in the additions list.
	 * 
	 * @param menuService
	 *            the service for callbacks, like accessing
	 *            {@link IMenuService#registerVisibleWhen(org.eclipse.jface.action.IContributionItem, org.eclipse.core.expressions.Expression)}
	 * @param additions
	 *            A List supplied by the framework. It should be filled in with
	 *            new instances of IContributionItems. It will never be
	 *            <code>null</code>.
	 * @see org.eclipse.ui.menus.CommandContributionItem
	 * @see org.eclipse.jface.action.MenuManager
	 */
	public abstract void createContributionItems(IMenuService menuService,
			List additions);

	/**
	 * This method tells the factory that the menu service is finished with the
	 * IContributionItems that were created. If the factory caches them
	 * internally, it is time to remove them.
	 * 
	 * @param menuService
	 *            the service for callbacks
	 * @param items
	 *            a list of IContributionItems created by this factory.
	 */
	public abstract void releaseContributionItems(IMenuService menuService,
			List items);
}
