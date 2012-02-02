/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.renderers.swt.ContributionRecord;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IMenuService;

/**
 * @since 3.102.0
 * 
 */
public class MenuFactoryGenerator {
	private MApplication application;
	private IConfigurationElement configElement;
	private MenuLocationURI location;

	public MenuFactoryGenerator(MApplication application, IEclipseContext appContext,
			IConfigurationElement configElement, String attribute) {
		this.application = application;
		// this.appContext = appContext;
		assert appContext.equals(this.application.getContext());
		this.configElement = configElement;
		this.location = new MenuLocationURI(attribute);
	}

	public void mergeIntoModel(ArrayList<MMenuContribution> menuContributions,
			ArrayList<MToolBarContribution> toolBarContributions,
			ArrayList<MTrimContribution> trimContributions) {
		MMenuContribution menuContribution = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		String idContrib = MenuHelper.getId(configElement);
		if (idContrib != null && idContrib.length() > 0) {
			menuContribution.setElementId(idContrib);
		}
		if ("org.eclipse.ui.popup.any".equals(location.getPath())) { //$NON-NLS-1$
			menuContribution.setParentId("popup"); //$NON-NLS-1$
		} else {
			menuContribution.setParentId(location.getPath());
		}
		String query = location.getQuery();
		if (query == null || query.length() == 0) {
			query = "after=additions"; //$NON-NLS-1$
		}
		menuContribution.setPositionInParent(query);
		menuContribution.getTags().add("scheme:" + location.getScheme()); //$NON-NLS-1$
		String filter = ContributionsAnalyzer.MC_MENU;
		if ("popup".equals(location.getScheme())) { //$NON-NLS-1$
			filter = ContributionsAnalyzer.MC_POPUP;
		}
		menuContribution.getTags().add(filter);
		menuContribution.setVisibleWhen(MenuHelper.getVisibleWhen(configElement));
		ContextFunction generator = new ContextFunction() {

			@Override
			public Object compute(IEclipseContext context) {
				AbstractContributionFactory factory;
				try {
					factory = (AbstractContributionFactory) configElement
							.createExecutableExtension("class"); //$NON-NLS-1$
				} catch (CoreException e) {
					WorkbenchPlugin.log(e);
					return null;
				}
				final IMenuService menuService = context.get(IMenuService.class);
				final ContributionRoot root = new ContributionRoot(menuService,
						new HashSet<Object>(), null, factory);
				ServiceLocator sl = new ServiceLocator();
				sl.setContext(context);
				factory.createContributionItems(sl, root);
				final List contributionItems = root.getItems();
				final Map<IContributionItem, Expression> itemsToExpression = root.getVisibleWhen();
				List<MMenuElement> menuElements = new ArrayList<MMenuElement>();
				for (Object obj : contributionItems) {
					if (obj instanceof IContributionItem) {
						IContributionItem ici = (IContributionItem) obj;
						MRenderedMenuItem renderedItem = MenuFactoryImpl.eINSTANCE
								.createRenderedMenuItem();
						renderedItem.setElementId(ici.getId());
						renderedItem.setContributionItem(ici);
						if (itemsToExpression.containsKey(ici)) {
							final Expression ex = itemsToExpression.get(ici);
							MCoreExpression exp = UiFactoryImpl.eINSTANCE.createCoreExpression();
							exp.setCoreExpressionId("programmatic." + ici.getId()); //$NON-NLS-1$
							exp.setCoreExpression(ex);
							renderedItem.setVisibleWhen(exp);
						}
						menuElements.add(renderedItem);
					}
				}
				context.set(List.class, menuElements);

				// return something disposable
				return new Runnable() {
					public void run() {
						root.release();
					}
				};
			}
		};
		menuContribution.getTransientData().put(ContributionRecord.FACTORY, generator);
		menuContributions.add(menuContribution);
	}
}
