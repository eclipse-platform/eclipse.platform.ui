/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.action;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;

/**
 * Tests for the [I]ContributionItem API.
 * 
 * @since 3.1
 */
public class ContributionItemTest extends JFaceActionTest {

    /**
     * Constructs a new test with the given name.
     * 
     * @param name
     *            the name of the test
     */
    public ContributionItemTest(String name) {
        super(name);
    }

    /**
     * Tests that a contribution item's parent link is set when added to a
     * contribution manager, and cleared when the item is removed.
     * This is a regression test for:
     * Bug 80569 [Contributions] Parent of contribution item not cleared when item removed from manager
     */
    public void testParentLink() {
        IContributionManager mgr = new DummyContributionManager();
        ContributionItem item = new ActionContributionItem(new DummyAction());
        assertNull(item.getParent());
        mgr.add(item);
        assertEquals(mgr, item.getParent());
        mgr.remove(item);
        assertNull(item.getParent());
    }
}
