package org.eclipse.jface.tests.action;

import org.eclipse.jface.action.ContributionItem;

/**
 * A contribution item that creates no widgets
 * 
 * @since 3.1
 */
public class EmptyContributionItem extends ContributionItem {

    /**
     * 
     */
    public EmptyContributionItem() {
        super();
        setVisible(false);
    }

}
