/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.menus;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;

public class DynamicContributionItemPopup extends CompoundContributionItem {

	public DynamicContributionItemPopup() {
	}
	
	public DynamicContributionItemPopup(String id) {
		super(id);
	}
	
	private int count = 1;
	
	protected IContributionItem[] getContributionItems() {
		// set the labels here, which will be verified in the test case
		ContributionItem contributionItem1 = new ActionContributionItem(new DoNothingAction("something " + (count++)));
		ContributionItem contributionItem2 = new ActionContributionItem(new DoNothingAction("something " + (count++)));
		ContributionItem contributionItem3 = new ActionContributionItem(new DoNothingAction("something " + (count++)));
		return new IContributionItem[] {contributionItem1, contributionItem2, contributionItem3};
	}
	
	class DoNothingAction extends Action{
		
		public DoNothingAction(String text){
			setText(text);
		}
	}
}
