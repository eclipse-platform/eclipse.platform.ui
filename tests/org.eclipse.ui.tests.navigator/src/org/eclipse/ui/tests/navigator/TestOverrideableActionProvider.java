package org.eclipse.ui.tests.navigator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

public class TestOverrideableActionProvider extends CommonActionProvider {
	
	private IAction action;

	public TestOverrideableActionProvider() { 
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	public void init(ICommonActionExtensionSite site) { 

		action = new Action() {
			
		}; 
		action.setText(site.getExtensionId());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) { 		 
		menu.add(action);
	}

}
