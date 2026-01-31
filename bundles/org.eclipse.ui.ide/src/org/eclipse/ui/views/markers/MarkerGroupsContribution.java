package org.eclipse.ui.views.markers;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.internal.views.markers.GroupsContribution;

/**
 * @since 3.23
 *
 */
public class MarkerGroupsContribution extends CompoundContributionItem {

	@Override
	protected IContributionItem[] getContributionItems() {
		GroupsContribution gc = new GroupsContribution();
		return gc.getContributionItems();
	}

}
