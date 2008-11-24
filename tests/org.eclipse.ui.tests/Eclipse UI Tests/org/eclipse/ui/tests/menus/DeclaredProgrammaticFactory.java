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
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.action.Action#getId()
				 */
				public String getId() {
					return "myitem";
				}
			});

		}
	}

	public void createContributionItems(IServiceLocator serviceLocator,
			IContributionRoot additions) {
		additions.addContributionItem(new MyItem(), null);
	}
}
