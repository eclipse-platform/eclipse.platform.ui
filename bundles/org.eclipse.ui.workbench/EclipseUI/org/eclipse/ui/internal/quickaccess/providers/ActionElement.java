/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess.providers;

import java.util.Objects;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * @since 3.3
 */
public class ActionElement extends QuickAccessElement {

	private ActionContributionItem item;

	/* package */ ActionElement(ActionContributionItem item) {
		this.item = item;
	}

	@Override
	public void execute() {
		item.getAction().run();
	}

	@Override
	public String getId() {
		return item.getId();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return item.getAction().getImageDescriptor();
	}

	@Override
	public String getLabel() {
		IAction action = item.getAction();
		if (action.getToolTipText() != null && action.getToolTipText().length() != 0) {
			return LegacyActionTools.removeMnemonics(action.getText() + separator + action.getToolTipText());
		}
		return LegacyActionTools.removeMnemonics(action.getText());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(item);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ActionElement other = (ActionElement) obj;
		return Objects.equals(item, other.item);
	}
}
