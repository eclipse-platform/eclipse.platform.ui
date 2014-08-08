/*******************************************************************************
 * Copyright (c) 2014 Obeo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.menus;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.eclipse.equinox.log.LogFilter;
import org.eclipse.equinox.log.SynchronousLogListener;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;

/**
 * @author Maxime Porhel
 */
public class Bug410426Test extends UITestCase {

    public Bug410426Test(String testName) {
        super(testName);
    }

    public void testToolbarContributionFromFactoryVisibility() throws Exception {
        IWorkbenchWindow window = openTestWindow();
        IMenuService menus = window.getService(IMenuService.class);
        ToolBarManager manager = new ToolBarManager();

        try {
            // populate contribution
            populateTestToolbar(menus, manager);

            // check the contributions and their visibility
            IContributionItem[] items = manager.getItems();
            assertEquals(6, items.length);
            checkItem(DeclaredProgrammaticFactoryForToolbarVisibilityTest.TEST_ITEM_WITHOUT_VISIBLE_WHEN, items, true);
            checkItem(DeclaredProgrammaticFactoryForToolbarVisibilityTest.TEST_ITEM_WITH_ALWAYS_TRUE_VISIBLE_WHEN, items, true);
            checkItem(DeclaredProgrammaticFactoryForToolbarVisibilityTest.TEST_ITEM_WITH_ALWAYS_FALSE_VISIBLE_WHEN, items, false);

            checkItem(DeclaredProgrammaticFactoryForToolbarVisibilityTest.TEST_MENU_MANAGER_WITHOUT_VISIBLE_WHEN, items, true);
            checkItem(DeclaredProgrammaticFactoryForToolbarVisibilityTest.TEST_MENU_MANAGER_WITH_ALWAYS_TRUE_VISIBLE_WHEN, items, true);
            checkItem(DeclaredProgrammaticFactoryForToolbarVisibilityTest.TEST_ITEM_WITH_ALWAYS_FALSE_VISIBLE_WHEN, items, false);

            // now get the tool items
            ToolBar toolBar = manager.createControl(window.getShell());
            manager.update(true);
            ToolItem[] toolItems = toolBar.getItems();
            assertEquals("Only four tool items should be created as there are four visible contributions on the six contributions:", 4, toolItems.length); //$NON-NLS-N$
        } finally {
            menus.releaseContributions(manager);
        }
    }

    private void populateTestToolbar(IMenuService menus, ToolBarManager manager) {
        menus.populateContributionManager(manager, "toolbar:org.eclipse.ui.tests.toolbarContributionFromFactoryVisibilityTest"); //$NON-NLS-N$
    }

    private void checkItem(String id, IContributionItem[] items, boolean expectedVisibility) {
        IContributionItem item = getItemWithId(id, items);

        assertNotNull(item);
        assertEquals("The contribution item with id '" + id + "' has not the expected vibility:", expectedVisibility, item.isVisible()); //$NON-NLS-N$
    }

    private IContributionItem getItemWithId(String id, IContributionItem[] items) {
        for (IContributionItem item : items) {
            if (id.equals(item.getId())) {
                return item;
            }
        }
        return null;
    }

    public void testNoClassCastExceptionForMenuManagerToolbarContribution() throws Exception {
        IWorkbenchWindow window = openTestWindow();
        IMenuService menus = window.getService(IMenuService.class);
        ToolBarManager manager = new ToolBarManager();

        //Add a log listener to detect the corrected ClassCastException in bug 410426.
        final List<ClassCastException> cces = new ArrayList<ClassCastException>();
		ExtendedLogReaderService log = window
				.getService(ExtendedLogReaderService.class);
        LogListener logListener = new SynchronousLogListener() {
            @Override
			public void logged(LogEntry entry) {
                if (entry.getLevel() == LogService.LOG_ERROR && entry.getException() instanceof ClassCastException
                        && entry.getException().getMessage().contains("MenuManager cannot be cast to org.eclipse.jface.action.ContributionItem")) { //$NON-NLS-N$
                    cces.add((ClassCastException) entry.getException());
                }
            }
        };
        LogFilter logFilter = new LogFilter() {
            @Override
			public boolean isLoggable(Bundle bundle, String loggerName, int logLevel) {
                return logLevel == LogService.LOG_ERROR && loggerName == null && "org.eclipse.equinox.event".equals(bundle.getSymbolicName()); //$NON-NLS-N$
            }
        };
        log.addLogListener(logListener, logFilter);

        try {
            populateTestToolbar(menus, manager);

            assertTrue("We should not get these 'MenuManager cannot be cast to org.eclipse.jface.action.ContributionItem' ClassCastException.", cces.isEmpty()); //$NON-NLS-N$

            // check the contributions count.
            IContributionItem[] items = manager.getItems();
            assertEquals(6, items.length);
        } finally {
            menus.releaseContributions(manager);
            log.removeLogListener(logListener);
            cces.clear();
        }
    }
}
