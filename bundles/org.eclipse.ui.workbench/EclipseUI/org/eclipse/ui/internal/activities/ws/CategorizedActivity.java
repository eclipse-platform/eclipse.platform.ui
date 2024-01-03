/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
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
package org.eclipse.ui.internal.activities.ws;

import java.util.Set;
import org.eclipse.core.expressions.Expression;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityListener;
import org.eclipse.ui.activities.IActivityPatternBinding;
import org.eclipse.ui.activities.IActivityRequirementBinding;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.NotDefinedException;

/**
 * IActivity proxy that is used by the
 * <code>IActivityCategoryContentProvider</code>. Each proxy keeps a pointer to
 * the <code>ICategory</code> under which it is being provided.
 *
 * @since 3.0
 */
public class CategorizedActivity implements IActivity {

	/**
	 * The real <code>IActivity</code>.
	 */
	private IActivity activity;

	/**
	 * The <code>ICategory</code> under which this proxy will be rendered.
	 */
	private ICategory category;

	/**
	 * Create a new instance.
	 *
	 * @param category the <code>ICategory</code> under which this proxy will be
	 *                 rendered.
	 * @param activity the real <code>IActivity</code>.
	 */
	public CategorizedActivity(ICategory category, IActivity activity) {
		this.activity = activity;
		this.category = category;
	}

	@Override
	public void addActivityListener(IActivityListener activityListener) {
		activity.addActivityListener(activityListener);
	}

	@Override
	public int compareTo(IActivity o) {
		return activity.compareTo(o);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CategorizedActivity) {
			if (((CategorizedActivity) o).getCategory().equals(getCategory())) {
				return ((CategorizedActivity) o).getActivity().equals(getActivity());
			}
		}
		return false;
	}

	/**
	 * @return returns the <code>IActivity</code>.
	 */
	public IActivity getActivity() {
		return activity;
	}

	@Override
	public Set<IActivityRequirementBinding> getActivityRequirementBindings() {
		return activity.getActivityRequirementBindings();
	}

	@Override
	public Set<IActivityPatternBinding> getActivityPatternBindings() {
		return activity.getActivityPatternBindings();
	}

	/**
	 * @return returns the <code>ICategory</code>.
	 */
	public ICategory getCategory() {
		return category;
	}

	@Override
	public String getId() {
		return activity.getId();
	}

	@Override
	public String getName() throws NotDefinedException {
		return activity.getName();
	}

	@Override
	public int hashCode() {
		return activity.hashCode();
	}

	@Override
	public boolean isDefined() {
		return activity.isDefined();
	}

	@Override
	public boolean isEnabled() {
		return activity.isEnabled();
	}

	@Override
	public void removeActivityListener(IActivityListener activityListener) {
		activity.removeActivityListener(activityListener);
	}

	@Override
	public String toString() {
		return category.getId() + " -> " + activity.getId(); //$NON-NLS-1$
	}

	@Override
	public String getDescription() throws NotDefinedException {
		return activity.getDescription();
	}

	@Override
	public boolean isDefaultEnabled() throws NotDefinedException {
		return activity.isDefaultEnabled();
	}

	@Override
	public Expression getExpression() {
		return activity.getExpression();
	}
}
