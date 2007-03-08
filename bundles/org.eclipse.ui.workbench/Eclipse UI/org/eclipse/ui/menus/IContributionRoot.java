/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.menus;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.ui.ActiveShellExpression;

/**
 * Instances of this interface represent a position in the contribution
 * hierarchy into which {@link AbstractContributionFactory} instances may insert
 * elements. Instances of this interface are provided by the platform and this
 * interface should <b>NOT</b> be implemented by clients.
 * 
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.3
 */
public interface IContributionRoot {
	/**
	 * Adds a given contribution item with provided visibility expression and
	 * kill-switch filtering as a direct child of this container. This should be
	 * called for all top-level elements created in
	 * {@link AbstractContributionFactory#createContributionItems(org.eclipse.ui.services.IServiceLocator, IContributionRoot)}
	 * 
	 * @param item
	 *            the item to add
	 * @param visibleWhen
	 *            the visibility expression. May be <code>null</code>.
	 * @param restrictEvaluation
	 *            an expression that must evaluate to <code>true</code> in
	 *            order for the visibleWhen expression to be evaluated. This is
	 *            most commonly used to restrict visibleWhen evaluation to the
	 *            active workbench window via an {@link ActiveShellExpression}.
	 *            In this way you can prevent visibility changes in background
	 *            window elements who have visibilty tied to global properties
	 *            such as action sets and contexts.
	 */
	public void addContributionItem(IContributionItem item,
			Expression visibleWhen, Expression restrictEvaluation);

	/**
	 * Registers visibilty for arbitrary {@link IContributionItem} instances
	 * that are <b>NOT</b> direct children of this container. Ie: children of a
	 * {@link IContributionManager} that has been previously registered with a
	 * call to {{@link #addContributionItem(IContributionItem, Expression, Expression)}.
	 * 
	 * @param item
	 *            the item for which to register a visibility clause
	 * @param visibleWhen
	 *            the visibility expression. May be <code>null</code> in which
	 *            case this method is a no-op.
	 * @param restrictEvaluation
	 *            an expression that must evaluate to <code>true</code> in
	 *            order for the visibleWhen expression to be evaluated. This is
	 *            most commonly used to restrict visibleWhen evaluation to the
	 *            active workbench window via an {@link ActiveShellExpression}.
	 *            In this way you can prevent visibility changes in background
	 *            window elements who have visibilty tied to global properties
	 *            such as action sets and contexts.
	 */
	public void registerVisibilityForChild(IContributionItem item,
			Expression visibleWhen, Expression restrictEvaluation);
}
