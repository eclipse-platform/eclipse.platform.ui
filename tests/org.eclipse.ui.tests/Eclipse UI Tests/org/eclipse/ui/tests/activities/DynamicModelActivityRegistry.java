/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.activities;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.ui.internal.activities.AbstractActivityRegistry;
import org.eclipse.ui.internal.activities.ActivityDefinition;
import org.eclipse.ui.internal.activities.ActivityPatternBindingDefinition;
import org.eclipse.ui.internal.activities.ActivityRequirementBindingDefinition;
import org.eclipse.ui.internal.activities.CategoryActivityBindingDefinition;
import org.eclipse.ui.internal.activities.CategoryDefinition;

/**
 * 
 * The dynamic activity registry populated activities with dummy definitions for
 * testing purposes.
 */
public class DynamicModelActivityRegistry extends AbstractActivityRegistry {
    final String sourceId = "org.eclipse.ui.tests"; //$NON-NLS-1$

    /**
     * The constructor for the plugin model registry.
     */
    public DynamicModelActivityRegistry() {
        super();
        load();
    }

    /**
     * Populate definitions.
     *  
     */
    private void load() {
        categoryDefinitions = new ArrayList();
        activityDefinitions = new ArrayList();
        categoryActivityBindingDefinitions = new ArrayList();
        activityPatternBindingDefinitions = new ArrayList();
        activityRequirementBindingDefinitions = new ArrayList();
        defaultEnabledActivities = new ArrayList();
        populateCategoryDefinitions();
        populateActivityDefinitions();
        populateCategoryActivityBindingDefinitions();
        populateActivityPatternBindingDefinitions();
        populateActivityRequirementBindingDefinitions();
        populateDefaultEnabledActivities();
    }

    /**
     * Populate the default enabled activties.
     *  
     */
    private void populateDefaultEnabledActivities() {
        defaultEnabledActivities.add(((ActivityDefinition) activityDefinitions
                .toArray()[0]).getId());
        defaultEnabledActivities.add(((ActivityDefinition) activityDefinitions
                .toArray()[2]).getId());
        defaultEnabledActivities.add(((ActivityDefinition) activityDefinitions
                .toArray()[8]).getId());
    }

    /**
     * Populate the activity activity binding definitions.
     *  
     */
    private void populateActivityRequirementBindingDefinitions() {
        activityRequirementBindingDefinitions
                .add(new ActivityRequirementBindingDefinition(
                        ((ActivityDefinition) activityDefinitions.toArray()[0])
                                .getId(),
                        ((ActivityDefinition) activityDefinitions.toArray()[1])
                                .getId(), sourceId)); //$NON-NLS-1$
        activityRequirementBindingDefinitions
                .add(new ActivityRequirementBindingDefinition(
                        ((ActivityDefinition) activityDefinitions.toArray()[2])
                                .getId(),
                        ((ActivityDefinition) activityDefinitions.toArray()[3])
                                .getId(), sourceId)); //$NON-NLS-1$
    }

    /**
     * Populate the activity pattern binding definitions.
     *  
     */
    private void populateActivityPatternBindingDefinitions() {
        for (int index = 0; index < activityDefinitions.size(); index++) {
            activityPatternBindingDefinitions
                    .add(new ActivityPatternBindingDefinition(
                            ((ActivityDefinition) activityDefinitions.toArray()[index])
                                    .getId(), "org.eclipse.pattern" //$NON-NLS-1$
                                    + Integer.toString(index + 1), sourceId));
        }
    }

    /**
     * Populate the category activity binding definitions.
     *  
     */
    private void populateCategoryActivityBindingDefinitions() {
        int counter = 1;
        for (int index = 1; index <= categoryDefinitions.size(); index++) {
            categoryActivityBindingDefinitions
                    .add(new CategoryActivityBindingDefinition(
                            "org.eclipse.activity" + Integer.toString(counter), //$NON-NLS-1$
                            "org.eclipse.category" + Integer.toString(index), //$NON-NLS-1$
                            sourceId));
            counter++;
            categoryActivityBindingDefinitions
                    .add(new CategoryActivityBindingDefinition(
                            "org.eclipse.activity" + Integer.toString(counter), //$NON-NLS-1$
                            "org.eclipse.category" + Integer.toString(index), //$NON-NLS-1$
                            sourceId));
            counter++;
            categoryActivityBindingDefinitions
                    .add(new CategoryActivityBindingDefinition(
                            "org.eclipse.activity" + Integer.toString(counter), //$NON-NLS-1$
                            "org.eclipse.category" + Integer.toString(index), //$NON-NLS-1$
                            sourceId));
            counter++;
        }
    }

    /**
     * Populate the activity definitions.
     *  
     */
    private void populateActivityDefinitions() {
        String stringToAppend = null;
        for (int index = 1; index <= categoryDefinitions.size() * 3; index++) {
            stringToAppend = Integer.toString(index);
            activityDefinitions.add(new ActivityDefinition(
                    "org.eclipse.activity" + stringToAppend, "Activity " //$NON-NLS-1$ //$NON-NLS-2$
                            + stringToAppend, sourceId, "description")); //$NON-NLS-1$
        }
    }

    /**
     * Populate the category definitions.
     *  
     */
    private void populateCategoryDefinitions() {
        String stringToAppend = null;
        for (int index = 1; index <= 6; index++) {
            stringToAppend = Integer.toString(index);
            categoryDefinitions.add(new CategoryDefinition(
                    "org.eclipse.category" + stringToAppend, "Category " //$NON-NLS-1$ //$NON-NLS-2$
                            + stringToAppend, sourceId, "description")); //$NON-NLS-1$
        }
    }

    /**
     * Add an activity.
     * 
     * @param activityId
     *            The activity's id.
     * @param activityName
     *            The activity's name
     */
    public void addActivity(String activityId, String activityName) {
        activityDefinitions.add(new ActivityDefinition(activityId,
                activityName, sourceId, "description")); //$NON-NLS-1$
        fireActivityRegistryChanged();
    }

    /**
     * Remove adn activity.
     * 
     * @param activityId
     *            The activity's id.
     * @param activityName
     *            The activity's name.
     */
    public void removeActivity(String activityId, String activityName) {
        activityDefinitions.remove(new ActivityDefinition(activityId,
                activityName, sourceId, "description")); //$NON-NLS-1$
        fireActivityRegistryChanged();
    }

    /**
     * Add a category.
     * 
     * @param categoryId
     *            The category's id.
     * @param categoryName
     *            The category's name.
     */
    public void addCategory(String categoryId, String categoryName) {
        categoryDefinitions.add(new CategoryDefinition(categoryId,
                categoryName, sourceId, "description")); //$NON-NLS-1$
        fireActivityRegistryChanged();
    }

    /**
     * Remove a category.
     * 
     * @param categoryId
     *            The category's id.
     * @param categoryName
     *            The category's name.
     */
    public void removeCategory(String categoryId, String categoryName) {
        categoryDefinitions.remove(new CategoryDefinition(categoryId,
                categoryName, sourceId, "description")); //$NON-NLS-1$
        fireActivityRegistryChanged();
    }

    /**
     * Add an activity activity binding.
     * 
     * @param parentId
     *            The parent id.
     * @param childId
     *            The child id.
     */
    public void addActivityRequirementBinding(String childId, String parentId) {
        activityRequirementBindingDefinitions
                .add(new ActivityRequirementBindingDefinition(childId,
                        parentId, sourceId));
        fireActivityRegistryChanged();
    }

    /**
     * Reomve an activity activity binding.
     * 
     * @param parentId
     *            The parent id.
     * @param childId
     *            The child id.
     */
    public void removeActivityRequirementBinding(String childId, String parentId) {
        activityRequirementBindingDefinitions
                .remove(new ActivityRequirementBindingDefinition(childId,
                        parentId, sourceId));
        fireActivityRegistryChanged();
    }

    /**
     * Add a category activity binding.
     * 
     * @param activityId
     *            The activity id.
     * @param categoryId
     *            The category id.
     */
    public void addCategoryActivityBinding(String activityId, String categoryId) {
        categoryActivityBindingDefinitions
                .add(new CategoryActivityBindingDefinition(activityId,
                        categoryId, sourceId));
        fireActivityRegistryChanged();
    }

    /**
     * Remove a category activity binding.
     * 
     * @param activityId
     *            The activity id.
     * @param categoryId
     *            The category id.
     */
    public void removeCategoryActivityBinding(String activityId,
            String categoryId) {
        categoryActivityBindingDefinitions
                .remove(new CategoryActivityBindingDefinition(activityId,
                        categoryId, sourceId));
        fireActivityRegistryChanged();
    }

    /**
     * Update the category's description.
     * 
     * @param categoryId
     *            The category Id.
     * @param categoryDescription
     *            The category description.
     */
    public void updateCategoryDescription(String categoryId,
            String categoryDescription) {
        CategoryDefinition currentCategory = null;
        for (Iterator i = categoryDefinitions.iterator(); i.hasNext();) {
            currentCategory = (CategoryDefinition) i.next();
            if (currentCategory.getId().equals(categoryId)) {
                categoryDefinitions.remove(currentCategory);
                categoryDefinitions.add(new CategoryDefinition(categoryId,
                        currentCategory.getName(), currentCategory
                                .getSourceId(), categoryDescription));
                fireActivityRegistryChanged();
                return;
            }
        }
    }

    /**
     * Update the activity's description.
     * 
     * @param activityId
     *            The activity id.
     * @param activityDescription
     *            The activity description.
     */
    public void updateActivityDescription(String activityId,
            String activityDescription) {
        ActivityDefinition currentActivity = null;
        for (Iterator i = activityDefinitions.iterator(); i.hasNext();) {
            currentActivity = (ActivityDefinition) i.next();
            if (currentActivity.getId().equals(activityId)) {
                activityDefinitions.remove(currentActivity);
                activityDefinitions.add(new ActivityDefinition(activityId,
                        currentActivity.getName(), currentActivity
                                .getSourceId(), activityDescription));
                fireActivityRegistryChanged();
                return;
            }
        }
    }

    /**
     * Update the activity's name.
     * 
     * @param activityId
     *            The activity id.
     * @param activityName
     *            The activity's name.
     */
    public void updateActivityName(String activityId, String activityName) {
        ActivityDefinition currentActivity = null;
        for (Iterator i = activityDefinitions.iterator(); i.hasNext();) {
            currentActivity = (ActivityDefinition) i.next();
            if (currentActivity.getId().equals(activityId)) {
                activityDefinitions.remove(currentActivity);
                activityDefinitions.add(new ActivityDefinition(activityId,
                        activityName, currentActivity.getSourceId(),
                        currentActivity.getDescription()));
                fireActivityRegistryChanged();
                return;
            }
        }
    }

    /**
     * Update the category's name.
     * 
     * @param categoryId
     *            The category id.
     * @param categoryName
     *            The category name.
     */
    public void updateCategoryName(String categoryId, String categoryName) {
        CategoryDefinition currentCategory = null;
        for (Iterator i = categoryDefinitions.iterator(); i.hasNext();) {
            currentCategory = (CategoryDefinition) i.next();
            if (currentCategory.getId().equals(categoryId)) {
                categoryDefinitions.remove(currentCategory);
                categoryDefinitions.add(new CategoryDefinition(categoryId,
                        categoryName, currentCategory.getSourceId(),
                        currentCategory.getDescription()));
                fireActivityRegistryChanged();
                return;
            }
        }
    }

    /**
     * Remove an activity pattern binding.
     * 
     * @param pattern
     *            The pattern binding.
     */
    public void removeActivityPatternBinding(String pattern) {
        ActivityPatternBindingDefinition currentDefinition = null;
        for (Iterator i = activityPatternBindingDefinitions.iterator(); i
                .hasNext();) {
            currentDefinition = (ActivityPatternBindingDefinition) i.next();
            if (currentDefinition.getPattern().equals(pattern)) {
                activityPatternBindingDefinitions.remove(currentDefinition);
                fireActivityRegistryChanged();
                return;
            }
        }
    }

    /**
     * Add an activity pattern binding.
     * 
     * @param activityId
     *            The actvity Id.
     * @param pattern
     *            The pattern.
     */
    public void addActivityPatternBinding(String activityId, String pattern) {
        if (activityPatternBindingDefinitions
                .add(new ActivityPatternBindingDefinition(activityId, pattern,
                        sourceId))) {
            fireActivityRegistryChanged();
            return;
        }
    }
    
    /**
     * Add default enablement to the provided activity
     * @param activityId the activity id
     */
    public void addDefaultEnabledActivity(String activityId) {
        if (defaultEnabledActivities.add(activityId)) {
            fireActivityRegistryChanged();
        }
    }
    
    
    /**
     * Remove default enablement to the provided activity
     * 
     * @param activityId the activity id.
     */
    public void removeDefaultEnabledActivity(String activityId) {
        if (defaultEnabledActivities.remove(activityId)) {
            fireActivityRegistryChanged();
        }
    }
}
