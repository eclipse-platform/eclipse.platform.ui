/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * Base class for all new-style menu additions
 * 
 * @since 3.3
 * 
 */
public abstract class AdditionBase {

	protected IConfigurationElement element;
	private Expression visibleWhenExpression;

	public AdditionBase(IConfigurationElement element) {
		this.element = element;
		try {
			IConfigurationElement[] visibleConfig = element
					.getChildren(IWorkbenchRegistryConstants.TAG_VISIBLE_WHEN);
			if (visibleConfig.length > 0 && visibleConfig.length < 2) {
				IConfigurationElement[] visibleChild = visibleConfig[0]
						.getChildren();
				if (visibleChild.length > 0) {
					visibleWhenExpression = ExpressionConverter.getDefault()
							.perform(visibleChild[0]);
				}
			}
		} catch (InvalidRegistryObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getId() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
	}

	public Expression getVisibleWhen() {
		return visibleWhenExpression;
	}

	public abstract IContributionItem getContributionItem();
}
