package org.eclipse.update.internal.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.update.internal.ui.model.DiscoveryFolder;
import org.eclipse.update.internal.ui.model.SiteBookmark;

/**
 * @see IWorkbenchWindowActionDelegate
 */
public class UpdateWebSitesAction implements IWorkbenchWindowPulldownDelegate2 {
	private Menu fMenu;
	private boolean fRecreateMenu = true;

	/**
	 *
	 */
	public UpdateWebSitesAction() {
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action)  {
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection)  {
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose()  {
		if (fMenu != null) {
			fMenu.dispose();
		}
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window)  {
	}

	public Menu getMenu(Control parent) {
		setMenu(new Menu(parent));
		fillMenu(fMenu);
		initMenu();
		return fMenu;
	}
	
	public Menu getMenu(Menu parent) {
		setMenu(new Menu(parent));
		fillMenu(fMenu);
		initMenu();
		return fMenu;
	}
	
	private void setMenu(Menu menu) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		fMenu = menu;
	}
	
	protected void fillMenu(Menu menu) {
		addActionToMenu(menu, new WebBookmarksAction());
		addSeparator(menu);
		DiscoveryFolder folder = new DiscoveryFolder();
		Object[] children = folder.getChildren(folder);
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof SiteBookmark) {
				SiteBookmark bookmark = (SiteBookmark)children[i];
				if (bookmark.isWebBookmark())
					addActionToMenu(menu, new GoToWebsiteAction(bookmark));
			}
		}
	}
	
	private void initMenu() {
		fMenu.addMenuListener(new MenuAdapter() {

			public void menuShown(MenuEvent e) {
				if (fRecreateMenu) {
					Menu m = (Menu)e.widget;
					MenuItem[] items = m.getItems();
					for (int i=0; i < items.length; i++) {
						items[i].dispose();
					}
					fillMenu(m);
					fRecreateMenu= false;
				}
			}
		});
	}
	
	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}
	
	private void addSeparator(Menu parent) {
		new MenuItem(parent, SWT.SEPARATOR);
	}


}
