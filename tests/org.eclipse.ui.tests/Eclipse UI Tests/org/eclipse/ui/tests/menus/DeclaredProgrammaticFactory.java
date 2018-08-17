/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.menus;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

public class DeclaredProgrammaticFactory extends ExtensionContributionFactory {

	public DeclaredProgrammaticFactory() {
		super();
	}

	static class MyItem extends ActionContributionItem {
		/**
		 *
		 */
		public MyItem() {
			super(new Action("MyItem") {
				@Override
				public String getId() {
					return "myitem";
				}
			});

		}
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator,
			IContributionRoot additions) {
		additions.addContributionItem(new MyItem(), null);
	}
}
