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

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.internal.expressions.AlwaysEnabledExpression;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

public class DeclaredProgrammaticFactoryForToolbarVisibilityTest extends ExtensionContributionFactory {
    public static final String TEST_ITEM_WITHOUT_VISIBLE_WHEN = "TestItemWithoutVisibleWhen";//$NON-NLS-1$

    public static final String TEST_ITEM_WITH_ALWAYS_FALSE_VISIBLE_WHEN = "TestItemWithAlwaysFalseVisibleWhen"; //$NON-NLS-1$

    public static final String TEST_ITEM_WITH_ALWAYS_TRUE_VISIBLE_WHEN = "TestItemWithAlwaysTrueVisibleWhen"; //$NON-NLS-1$

    public static final String TEST_MENU_MANAGER_WITHOUT_VISIBLE_WHEN = "TestMenuManagerWithoutVisibleWhen"; //$NON-NLS-1$

    public static final String TEST_MENU_MANAGER_WITH_ALWAYS_FALSE_VISIBLE_WHEN = "TestMenuManagerWithAlwaysFalseVisibleWhen"; //$NON-NLS-1$

    public static final String TEST_MENU_MANAGER_WITH_ALWAYS_TRUE_VISIBLE_WHEN = "TestMenuManagerWithAlwaysTrueVisibleWhen"; //$NON-NLS-1$

    public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
        addContribution(additions, new TestAction(TEST_ITEM_WITHOUT_VISIBLE_WHEN), null);
        addContribution(additions, new TestAction(TEST_ITEM_WITH_ALWAYS_TRUE_VISIBLE_WHEN), AlwaysEnabledExpression.INSTANCE);
        addContribution(additions, new TestAction(TEST_ITEM_WITH_ALWAYS_FALSE_VISIBLE_WHEN), new AlwaysDisabledExpression());

        additions.addContributionItem(new TestActionMenuManager(TEST_MENU_MANAGER_WITHOUT_VISIBLE_WHEN), null);
        additions.addContributionItem(new TestActionMenuManager(TEST_MENU_MANAGER_WITH_ALWAYS_TRUE_VISIBLE_WHEN), AlwaysEnabledExpression.INSTANCE);
        additions.addContributionItem(new TestActionMenuManager(TEST_MENU_MANAGER_WITH_ALWAYS_FALSE_VISIBLE_WHEN), new AlwaysDisabledExpression());
    }

    private void addContribution(IContributionRoot additions, Action action, Expression visibleWhen) {
        additions.addContributionItem(new ActionContributionItem(action), visibleWhen);
    }

    private static class TestAction extends Action {
        public TestAction(String id) {
            super(id);
            setId(id);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gmf.runtime.common.ui.action.ActionMenuManager which is
     * an implementation of an <code>IMenuManager</code> that inherits its UI
     * (text + icon + hints) from a given action. When filled in a toolbar, the
     * menu is rendered as a tool item.
     *
     * See Bug 410426 which corrects a ClassCastException in
     * ToolbarManagerRenderer while looking for a IContributionItem parent ToolBarManager.
     */
    private static class TestActionMenuManager extends MenuManager {
        private ActionContributionItem actionContributionItem;

        public TestActionMenuManager(String id) {
            super(id, id);
            actionContributionItem = new ActionContributionItem(new TestAction(id));
            add(new TestAction(TEST_ITEM_WITHOUT_VISIBLE_WHEN));
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets
         * .ToolBar, int)
         */
        public void fill(ToolBar parent, int index) {
            actionContributionItem.fill(parent, index);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.action.MenuManager#dispose()
         */
        @Override
        public void dispose() {
            super.dispose();
            actionContributionItem.dispose();
        }
    }

    private static class AlwaysDisabledExpression extends Expression {
        public EvaluationResult evaluate(IEvaluationContext context) {
            return EvaluationResult.FALSE;
        }
    }
}
