/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.action;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests for the MenuManager API.
 * 
 * @since 3.1
 */
public class MenuManagerTest extends JFaceActionTest {

    private int groupMarkerCount = 0;
    private int separatorCount = 0;

    /**
     * Constructs a new test with the given name.
     * 
     * @param name
     *            the name of the test
     */
    public MenuManagerTest(String name) {
        super(name);
    }

    /**
     * Tests that a menu with no concrete visible items (that is, ignoring
     * separators and group markers) is hidden.
     * 
     * @see MenuManager#isVisible()
     */
    public void testMenuWithNoConcreteVisibleItemsIsHidden() {
        MenuManager menuBarMgr = createMenuBarManager();

        MenuManager fileMenu = createMenuManager("File", "gsgn");
        menuBarMgr.add(fileMenu);
        menuBarMgr.updateAll(false);
        assertEquals(0, getShell().getMenuBar().getItems().length);
    }

    /**
     * Tests that adding a concrete visible item to a menu with no concrete
     * visible items makes the menu visible. Regression test for bug 54779 [RCP]
     * Problem updating menus, menu not appearing
     * 
     * @see MenuManager#isVisible()
     * @see MenuManager#markDirty()
     */
    public void testAddingConcreteItemToMenuWithNoConcreteVisibleItems() {
        MenuManager menuBarMgr = createMenuBarManager();

        MenuManager fileMenuMgr = createMenuManager("File", "gsgn");
        menuBarMgr.add(fileMenuMgr);
        menuBarMgr.updateAll(false);
        
        Menu menuBar = getShell().getMenuBar();
        assertEquals(0, menuBar.getItems().length);
        
        fileMenuMgr.add(createItem('a'));
        menuBarMgr.updateAll(false);
        assertEquals(1, menuBar.getItems().length);
        
        assertEquals("File", menuBar.getItems()[0].getText());
        
        Menu fileMenu = menuBar.getItems()[0].getMenu();
        assertEquals(1, fileMenu.getItems().length);
    }
    
    /**
     * Tests the following:
     * <ul>
     * <li>an empty submenu (one with no contribution items) 
     * will be disabled.</li>
     * <li>the submenu was not updated while determining its enablement state</li>
     * </ul>
     */
    public void testEmptySubmenuIsDisabled() {
        // This is the empty submenu that should be disabled
        MenuManager someSubmenu = createMenuManager("subMenu", "");
        runDisabledSubmenuTest(someSubmenu);
    }
    
    /**
     * Tests the following:
     * <ul>
     * <li>a nonempty submenu whose contribution items create no widgets
     * will be disabled.</li>
     * <li>the submenu was not updated while determining its enablement state</li>
     * </ul>
     */
    public void testSubmenuWithNoWidgetsIsDisabled() {
        // This is the empty submenu that should be disabled
        MenuManager someSubmenu = createMenuManager("subMenu", "eeeee");
        runDisabledSubmenuTest(someSubmenu);
    }
    
    /**
     * Tests the following:
     * <ul>
     * <li>a nonempty submenu contaning separators, group markers, and 
     * invisible items will be disabled.</li>
     * <li>the submenu was not updated while determining its enablement state</li>
     * </ul>
     */
    public void testNonConcreteSubmenuIsDisabled() {
        // This is the empty submenu that should be disabled
        MenuManager someSubmenu = createMenuManager("subMenu", "gsnegsen");
        runDisabledSubmenuTest(someSubmenu);
    }

    private void runDisabledSubmenuTest(MenuManager someSubmenu) {
        // This is the menu we'll make visible
        MenuManager popupMenuManager = createMenuManager("File", "a");
        
        someSubmenu.markDirty();
        
        popupMenuManager.add(someSubmenu);
        popupMenuManager.markDirty();

        // Now create the menu as a popup
        Menu theMenu = popupMenuManager.createContextMenu(getShell());

        popupMenuManager.update(false);
        
        assertTrue("MenuManager should be dirty: It should not be necessary to update a submenu in order to compute its enablement", 
                someSubmenu.isDirty());
        
        // The top level menu should not be dirty because we triggered an update above
        assertFalse("The popup menu manager should not be dirty (this indicates that the test isn't doing what it is supposed to)", 
                popupMenuManager.isDirty());
        
        // Ensure that no MenuItems were created for the submenu
        assertChildWidgetCount(someSubmenu, 0);
        
        // Ensure that the top-level menu only contains the concrete action (but not the submenu)
        assertTrue("Empty submenus should either be hidden or disabled",
                theMenu.getItemCount() == 1 || !(theMenu.getItems())[1].isEnabled());
    }

    /** 
     * Tests that the following:
     * <ul>
     * <li>making a menu visible will cause it to update if dirty</li>
     * <li>making a menu visible will not cause dirty submenus to update</li>
     * </ul>
     */
    public void testLazyMenuCreation() {
        // This is the menu we'll make visible
        MenuManager popupMenuManager = createMenuManager("File", "asaasa");
        
        // This is the submenu. Making the top-level menu visible should not
        // cause this menu's contents to be created yet (since they aren't visible).
        MenuManager someSubmenu = createMenuManager("subMenu", "aaaaa");
        
        popupMenuManager.add(someSubmenu);
        popupMenuManager.markDirty();
        
        // Do the same test on another submenu nested inside someSubmenu, to ensure
        // that recursion is working properly. This is the nested submenu.

        MenuManager submenu2 = createMenuManager("submenu2", "aaaa");
        someSubmenu.add(submenu2);
        someSubmenu.markDirty();
        
        // Now create the menu as a popup
        Menu theMenu = popupMenuManager.createContextMenu(getShell());

        // The popup menu is not visible yet. Ensure that no widgets have been created yet.
        assertChildWidgetCount(popupMenuManager, 0);
        assertChildWidgetCount(someSubmenu, 0);
        assertChildWidgetCount(submenu2, 0);
        
        // Make the top-level menu visible. This should create all top-level widgets but
        // not the menu items inside the submenus.
        showAndHidePopup(theMenu);
        assertChildWidgetCount(popupMenuManager, 7);
        assertChildWidgetCount(someSubmenu, 0);
        assertChildWidgetCount(submenu2, 0);
        
        // Update the first submenu. This should create the top level and second-level
        // widgets but not the ones in the innermost menu.
        someSubmenu.update(false);
        assertChildWidgetCount(popupMenuManager, 7);
        assertChildWidgetCount(someSubmenu, 6);
        assertChildWidgetCount(submenu2, 0);
        
        // Update the second submenu visible. Now all the wigdets should have been created.
        submenu2.update(false);
        assertChildWidgetCount(popupMenuManager, 7);
        assertChildWidgetCount(someSubmenu, 6);
        assertChildWidgetCount(submenu2, 4);
    }
    
    private void step() {
        Display display;
        Shell loopShell = getShell();
        
        if (loopShell == null)
            display = Display.getCurrent();
        else
            display = loopShell.getDisplay();

        while (loopShell != null && !loopShell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }

    private void showAndHidePopup(final Menu toShow) {
        
        toShow.setVisible(true);
        final Display display = Display.getCurrent();
        
        display.asyncExec(new Runnable() {
            public void run() {
                toShow.setVisible(false);
            } 
        });
        
        while (display.readAndDispatch());
    }
    
    private void assertChildWidgetCount(MenuManager toTest, int count) {
        if (toTest.getMenu() == null) {
            assertTrue("Menu is supposed to have " + count + " MenuItems, but the menu has not been created yet",
                    count == 0);
            return;
        }
        
        assertEquals("Menu has wrong number of MenuItems", count, 
                toTest.getMenu().getItemCount());
    }
    
    
    /**
     * Creates a menu manager with the given name, adding items based on the given template.
     * 
     * @param name the name
     * @param template the template (g = group marker, s = separator, 
     * n = invisible contribution item, e = item with no widgets, a = visible action contribution item)
     * 
     * @return a menu with no concrete visible items
     */
    private MenuManager createMenuManager(String name, String template) {
        MenuManager menuMgr = new MenuManager(name);
        addItems(menuMgr, template);
        return menuMgr;
    }

    private void addItems(IContributionManager manager, String template) {
        for (int i = 0; i < template.length(); ++i) {
            manager.add(createItem(template.charAt(i)));
        }
    }
    
    private IContributionItem createItem(char template) {
        switch (template) {
        	case 'g':
        	    return new GroupMarker("testGroup" + groupMarkerCount++);
        	case 's':
        	    return new Separator("testSeparator" + separatorCount++);
        	case 'a': {
        	    IAction action = new DummyAction();
        	    return new ActionContributionItem(action);
        	}
        	case 'n': {
        	    IAction action = new DummyAction();
        	    ActionContributionItem item = new ActionContributionItem(action);
        	    item.setVisible(false);
        	    return item;
        	}
            case 'e': {
                return new EmptyContributionItem();
            }
        	default:
        	    throw new IllegalArgumentException("Unknown template char: " + template);
        }
    }

    protected MenuManager createMenuBarManager() {
        Shell shell = getShell();
        MenuManager menuMgr = new MenuManager();
        Menu menuBar = menuMgr.createMenuBar(shell);
        shell.setMenuBar(menuBar);
        return menuMgr;
    }
}
