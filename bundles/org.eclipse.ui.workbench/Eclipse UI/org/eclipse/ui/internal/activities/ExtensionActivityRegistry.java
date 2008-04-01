/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.ConfigurationElementMemento;
import org.eclipse.ui.statushandlers.StatusManager;

final class ExtensionActivityRegistry extends AbstractActivityRegistry {
    private List activityRequirementBindingDefinitions;

    private List activityDefinitions;

    private List activityPatternBindingDefinitions;

    private List categoryActivityBindingDefinitions;

    private List categoryDefinitions;

    private List defaultEnabledActivities;

    private IExtensionRegistry extensionRegistry;

    ExtensionActivityRegistry(IExtensionRegistry extensionRegistry) {
        if (extensionRegistry == null) {
			throw new NullPointerException();
		}

        this.extensionRegistry = extensionRegistry;

        this.extensionRegistry
                .addRegistryChangeListener(new IRegistryChangeListener() {
                    public void registryChanged(
                            IRegistryChangeEvent registryChangeEvent) {
                        IExtensionDelta[] extensionDeltas = registryChangeEvent
                                .getExtensionDeltas(Persistence.PACKAGE_PREFIX,
                                        Persistence.PACKAGE_BASE);

                        if (extensionDeltas.length != 0) {
							try {
                                load();
                            } catch (IOException eIO) {
                            }
						}
                    }
                });

        try {
            load();
        } catch (IOException eIO) {
        }
    }

    private String getNamespace(IConfigurationElement configurationElement) {
        String namespace = null;

        if (configurationElement != null) {
            IExtension extension = configurationElement.getDeclaringExtension();

            if (extension != null) {
				namespace = extension.getNamespace();
			}
        }

        return namespace;
    }
    
    /**
     * Returns the activity definition found at this id.
     * 
     * @param id <code>ActivityDefinition</code> id.
     * @return <code>ActivityDefinition</code> with given id or <code>null</code> if not found. 
     */
    private ActivityDefinition getActivityDefinitionById(String id) {
		int size = activityDefinitions.size();
		for (int i = 0; i < size; i++) {
			ActivityDefinition activityDef = (ActivityDefinition) activityDefinitions
					.get(i);
			if (activityDef.getId().equals(id)) {
				return activityDef;
			}
		}
		return null;
	}

    private void load() throws IOException {
        if (activityRequirementBindingDefinitions == null) {
			activityRequirementBindingDefinitions = new ArrayList();
		} else {
			activityRequirementBindingDefinitions.clear();
		}

        if (activityDefinitions == null) {
			activityDefinitions = new ArrayList();
		} else {
			activityDefinitions.clear();
		}

        if (activityPatternBindingDefinitions == null) {
			activityPatternBindingDefinitions = new ArrayList();
		} else {
			activityPatternBindingDefinitions.clear();
		}

        if (categoryActivityBindingDefinitions == null) {
			categoryActivityBindingDefinitions = new ArrayList();
		} else {
			categoryActivityBindingDefinitions.clear();
		}

        if (categoryDefinitions == null) {
			categoryDefinitions = new ArrayList();
		} else {
			categoryDefinitions.clear();
		}

        if (defaultEnabledActivities == null) {
			defaultEnabledActivities = new ArrayList();
		} else {
			defaultEnabledActivities.clear();
		}

        IConfigurationElement[] configurationElements = extensionRegistry
                .getConfigurationElementsFor(Persistence.PACKAGE_FULL);

        for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement configurationElement = configurationElements[i];
            String name = configurationElement.getName();

            if (Persistence.TAG_ACTIVITY_REQUIREMENT_BINDING.equals(name)) {
				readActivityRequirementBindingDefinition(configurationElement);
			} else if (Persistence.TAG_ACTIVITY.equals(name)) {
				readActivityDefinition(configurationElement);
			} else if (Persistence.TAG_ACTIVITY_PATTERN_BINDING.equals(name)) {
				readActivityPatternBindingDefinition(configurationElement);
			} else if (Persistence.TAG_CATEGORY_ACTIVITY_BINDING.equals(name)) {
				readCategoryActivityBindingDefinition(configurationElement);
			} else if (Persistence.TAG_CATEGORY.equals(name)) {
				readCategoryDefinition(configurationElement);
			} else if (Persistence.TAG_DEFAULT_ENABLEMENT.equals(name)) {
				readDefaultEnablement(configurationElement);
			}
        }
                
        // Removal of all defaultEnabledActivites which target to expression
        // controlled activities.
		for (int i = 0; i < defaultEnabledActivities.size();) {
			String id = (String) defaultEnabledActivities.get(i);
			ActivityDefinition activityDef = getActivityDefinitionById(id);
			if (activityDef != null && activityDef.getEnabledWhen() != null) {
				defaultEnabledActivities.remove(i);
			} else {
				i++;
			}
		}

        boolean activityRegistryChanged = false;

        if (!activityRequirementBindingDefinitions
                .equals(super.activityRequirementBindingDefinitions)) {
            super.activityRequirementBindingDefinitions = Collections
                    .unmodifiableList(activityRequirementBindingDefinitions);
            activityRegistryChanged = true;
        }

        if (!activityDefinitions.equals(super.activityDefinitions)) {
            super.activityDefinitions = Collections
                    .unmodifiableList(activityDefinitions);
            activityRegistryChanged = true;
        }

        if (!activityPatternBindingDefinitions
                .equals(super.activityPatternBindingDefinitions)) {
            super.activityPatternBindingDefinitions = Collections
                    .unmodifiableList(activityPatternBindingDefinitions);
            activityRegistryChanged = true;
        }

        if (!categoryActivityBindingDefinitions
                .equals(super.categoryActivityBindingDefinitions)) {
            super.categoryActivityBindingDefinitions = Collections
                    .unmodifiableList(categoryActivityBindingDefinitions);
            activityRegistryChanged = true;
        }

        if (!categoryDefinitions.equals(super.categoryDefinitions)) {
            super.categoryDefinitions = Collections
                    .unmodifiableList(categoryDefinitions);
            activityRegistryChanged = true;
        }

        if (!defaultEnabledActivities.equals(super.defaultEnabledActivities)) {
            super.defaultEnabledActivities = Collections
                    .unmodifiableList(defaultEnabledActivities);
            activityRegistryChanged = true;
        }

        if (activityRegistryChanged) {
			fireActivityRegistryChanged();
		}
    }

    private void readDefaultEnablement(
            IConfigurationElement configurationElement) {
        String enabledActivity = Persistence
                .readDefaultEnablement(new ConfigurationElementMemento(
                        configurationElement));

        if (enabledActivity != null) {
			defaultEnabledActivities.add(enabledActivity);
		}

    }

    private void readActivityRequirementBindingDefinition(
            IConfigurationElement configurationElement) {
        ActivityRequirementBindingDefinition activityRequirementBindingDefinition = Persistence
                .readActivityRequirementBindingDefinition(
                        new ConfigurationElementMemento(configurationElement),
                        getNamespace(configurationElement));

        if (activityRequirementBindingDefinition != null) {
			activityRequirementBindingDefinitions
                    .add(activityRequirementBindingDefinition);
		}
    }

    private void readActivityDefinition(
            IConfigurationElement configurationElement) {
        ActivityDefinition activityDefinition = Persistence
                .readActivityDefinition(new ConfigurationElementMemento(
                        configurationElement),
                        getNamespace(configurationElement));
        
        if (activityDefinition != null) {
        	// this is not ideal, but core expressions takes an
        	// IConfigurationElement or a w3c dom Document
			IConfigurationElement[] enabledWhen = configurationElement
					.getChildren(IWorkbenchRegistryConstants.TAG_ENABLED_WHEN);
			if (enabledWhen.length == 1) {
				IConfigurationElement[] expElement = enabledWhen[0]
						.getChildren();
				if (expElement.length == 1) {
					try {
						Expression expression = ExpressionConverter
								.getDefault().perform(expElement[0]);
						activityDefinition.setEnabledWhen(expression);
					} catch (CoreException e) {
						StatusManager.getManager().handle(e, WorkbenchPlugin.PI_WORKBENCH);
					}
				}
			}
			activityDefinitions.add(activityDefinition);
		}
    }

    private void readActivityPatternBindingDefinition(
            IConfigurationElement configurationElement) {
        ActivityPatternBindingDefinition activityPatternBindingDefinition = Persistence
                .readActivityPatternBindingDefinition(
                        new ConfigurationElementMemento(configurationElement),
                        getNamespace(configurationElement));

        if (activityPatternBindingDefinition != null) {
			activityPatternBindingDefinitions
                    .add(activityPatternBindingDefinition);
		}
    }

    private void readCategoryActivityBindingDefinition(
            IConfigurationElement configurationElement) {
        CategoryActivityBindingDefinition categoryActivityBindingDefinition = Persistence
                .readCategoryActivityBindingDefinition(
                        new ConfigurationElementMemento(configurationElement),
                        getNamespace(configurationElement));

        if (categoryActivityBindingDefinition != null) {
			categoryActivityBindingDefinitions
                    .add(categoryActivityBindingDefinition);
		}
    }

    private void readCategoryDefinition(
            IConfigurationElement configurationElement) {
        CategoryDefinition categoryDefinition = Persistence
                .readCategoryDefinition(new ConfigurationElementMemento(
                        configurationElement),
                        getNamespace(configurationElement));

        if (categoryDefinition != null) {
			categoryDefinitions.add(categoryDefinition);
		}
    }
}
