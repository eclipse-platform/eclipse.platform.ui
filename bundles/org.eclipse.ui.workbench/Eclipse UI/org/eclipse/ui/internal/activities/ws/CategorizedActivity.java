/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import java.util.Set;

import org.eclipse.core.expressions.Expression;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityListener;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.NotDefinedException;

/**
 * IActivity proxy that is used by the 
 * <code>IActivityCategoryContentProvider</code>.  Each 
 * proxy keeps a pointer to the <code>ICategory</code> under which it is being 
 * provided.
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
     * rendered.
     * @param activity the real <code>IActivity</code>.
     */
    public CategorizedActivity(ICategory category, IActivity activity) {
        this.activity = activity;
        this.category = category;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.IActivity#addActivityListener(org.eclipse.ui.activities.IActivityListener)
     */
    public void addActivityListener(IActivityListener activityListener) {
        activity.addActivityListener(activityListener);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        return activity.compareTo(o);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (o instanceof CategorizedActivity) {
            if (((CategorizedActivity) o).getCategory().equals(getCategory())) {
				return ((CategorizedActivity) o).getActivity().equals(
                        getActivity());
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.IActivity#getActivityRequirementBindings()
     */
    public Set getActivityRequirementBindings() {
        return activity.getActivityRequirementBindings();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.IActivity#getActivityPatternBindings()
     */
    public Set getActivityPatternBindings() {
        return activity.getActivityPatternBindings();
    }

    /**
     * @return returns the <code>ICategory</code>.
     */
    public ICategory getCategory() {
        return category;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.IActivity#getId()
     */
    public String getId() {
        return activity.getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.IActivity#getName()
     */
    public String getName() throws NotDefinedException {
        return activity.getName();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return activity.hashCode();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.IActivity#isDefined()
     */
    public boolean isDefined() {
        return activity.isDefined();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.IActivity#isEnabled()
     */
    public boolean isEnabled() {
        return activity.isEnabled();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.IActivity#removeActivityListener(org.eclipse.ui.activities.IActivityListener)
     */
    public void removeActivityListener(IActivityListener activityListener) {
        activity.removeActivityListener(activityListener);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return category.getId() + " -> " + activity.getId(); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.IActivity#getDescription()
     */
    public String getDescription() throws NotDefinedException {
        return activity.getDescription();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.activities.IActivity#isDefaultEnabled()
     */
    public boolean isDefaultEnabled() throws NotDefinedException {
        return activity.isDefaultEnabled();
    }
    
    public Expression getExpression() {
    	return activity.getExpression();
    }
}
