/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.menus;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.services.IServiceLocator;

/**
 * ContributionFactories are used by the IMenuService to populate
 * ContributionManagers. In {@link #createContributionItems(IServiceLocator, IContributionRoot)}
 * you fill in the additions List with {@link IContributionItem} to be inserted at this
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
	private String namespace;

	/**
	 * The contribution factories must be instantiated with their location,
	 * which which specifies the contributions insertion location.
	 * 
	 * @param location
	 *            the addition location in Menu API URI format. It must not be
	 *            <code>null</code>.
	 * @param namespace
	 *            the namespace for this contribution. May be <code>null</code>.
	 * @see #getNamespace()
	 */
	public AbstractContributionFactory(String location, String namespace) {
		this.location = location;
		this.namespace = namespace;
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
	 * <p>
	 * This method is not meant to be called by clients. It will be called by
	 * the menu service at the appropriate time.
	 * </p>
	 * 
	 * @param serviceLocator
	 *            a service locator that may be used in the construction of
	 *            items created by this factory
	 * @param additions
	 *            A {@link IContributionRoot} supplied by the framework. It will
	 *            never be <code>null</code>.
	 * @see org.eclipse.ui.menus.CommandContributionItem
	 * @see org.eclipse.jface.action.MenuManager
	 */
	public abstract void createContributionItems(IServiceLocator serviceLocator,
			IContributionRoot additions);

	/**
	 * Return the namespace for this cache. This corresponds to the plug-in that
	 * is contributing this factory.
	 * 
	 * @return the namespace the namespace of this factory
	 */
	public String getNamespace() {
		return namespace;
	}
}
