/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.ConfigurationElementMemento;
import org.eclipse.ui.statushandlers.StatusManager;

final class ExtensionActivityRegistry extends AbstractActivityRegistry {

	/**
	 * Prefix for all activity preferences
	 */
	private static final String PREFIX = "UIActivities."; //$NON-NLS-1$

	private IExtensionRegistry extensionRegistry;

	ExtensionActivityRegistry(IExtensionRegistry extensionRegistry) {
		if (extensionRegistry == null) {
			throw new NullPointerException();
		}

		this.extensionRegistry = extensionRegistry;

		this.extensionRegistry.addRegistryChangeListener(registryChangeEvent -> {
			IExtensionDelta[] extensionDeltas = registryChangeEvent.getExtensionDeltas(Persistence.PACKAGE_PREFIX,
					Persistence.PACKAGE_BASE);

			if (extensionDeltas.length != 0) {
				load();
			}
		});

		load();
	}

	private String getNamespace(IConfigurationElement configurationElement) {
		String namespace = null;

		if (configurationElement != null) {
			IExtension extension = configurationElement.getDeclaringExtension();

			if (extension != null) {
				namespace = extension.getContributor().getName();
			}
		}

		return namespace;
	}

	/**
	 * Returns the activity definition found at this id.
	 *
	 * @param id <code>ActivityDefinition</code> id.
	 * @return <code>ActivityDefinition</code> with given id or <code>null</code> if
	 *         not found.
	 */
	private ActivityDefinition getActivityDefinitionById(String id) {
		int size = activityDefinitions.size();
		for (int i = 0; i < size; i++) {
			ActivityDefinition activityDef = activityDefinitions.get(i);
			if (activityDef.getId().equals(id)) {
				return activityDef;
			}
		}
		return null;
	}

	private void load() {
		if (activityRequirementBindingDefinitions == null) {
			activityRequirementBindingDefinitions = new ArrayList<>();
		} else {
			activityRequirementBindingDefinitions.clear();
		}

		if (activityDefinitions == null) {
			activityDefinitions = new ArrayList<>();
		} else {
			activityDefinitions.clear();
		}

		if (activityPatternBindingDefinitions == null) {
			activityPatternBindingDefinitions = new ArrayList<>();
		} else {
			activityPatternBindingDefinitions.clear();
		}

		if (categoryActivityBindingDefinitions == null) {
			categoryActivityBindingDefinitions = new ArrayList<>();
		} else {
			categoryActivityBindingDefinitions.clear();
		}

		if (categoryDefinitions == null) {
			categoryDefinitions = new ArrayList<>();
		} else {
			categoryDefinitions.clear();
		}

		if (defaultEnabledActivities == null) {
			defaultEnabledActivities = new ArrayList<>();
		} else {
			defaultEnabledActivities.clear();
		}

		IConfigurationElement[] configurationElements = extensionRegistry
				.getConfigurationElementsFor(Persistence.PACKAGE_FULL);

		for (IConfigurationElement configurationElement : configurationElements) {
			String name = configurationElement.getName();

			switch (name) {
			case Persistence.TAG_ACTIVITY_REQUIREMENT_BINDING:
				readActivityRequirementBindingDefinition(configurationElement);
				break;
			case Persistence.TAG_ACTIVITY:
				readActivityDefinition(configurationElement);
				break;
			case Persistence.TAG_ACTIVITY_PATTERN_BINDING:
				readActivityPatternBindingDefinition(configurationElement);
				break;
			case Persistence.TAG_CATEGORY_ACTIVITY_BINDING:
				readCategoryActivityBindingDefinition(configurationElement);
				break;
			case Persistence.TAG_CATEGORY:
				readCategoryDefinition(configurationElement);
				break;
			case Persistence.TAG_DEFAULT_ENABLEMENT:
				readDefaultEnablement(configurationElement);
				break;
			default:
				break;
			}
		}

		// merge enablement overrides from plugin_customization.ini
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		for (ActivityDefinition activityDef : activityDefinitions) {
			String id = activityDef.getId();
			String preferenceKey = createPreferenceKey(id);
			if ("".equals(store.getDefaultString(preferenceKey))) //$NON-NLS-1$
				continue;
			if (store.getDefaultBoolean(preferenceKey)) {
				if (!defaultEnabledActivities.contains(id) && activityDef.getEnabledWhen() == null)
					defaultEnabledActivities.add(id);
			} else {
				defaultEnabledActivities.remove(id);
			}
		}

		// Removal of all defaultEnabledActivites which target to expression
		// controlled activities.
		for (int i = 0; i < defaultEnabledActivities.size();) {
			String id = defaultEnabledActivities.get(i);
			ActivityDefinition activityDef = getActivityDefinitionById(id);
			if (activityDef != null && activityDef.getEnabledWhen() != null) {
				defaultEnabledActivities.remove(i);
				StatusManager.getManager().handle(new Status(IStatus.WARNING, PlatformUI.PLUGIN_ID,
						"Default enabled activity declarations will be ignored (id: " + id + ")")); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				i++;
			}
		}

		// remove all requirement bindings that reference expression-bound activities
		for (Iterator<ActivityRequirementBindingDefinition> i = activityRequirementBindingDefinitions.iterator(); i
				.hasNext();) {
			ActivityRequirementBindingDefinition bindingDef = i.next();
			ActivityDefinition activityDef = getActivityDefinitionById(bindingDef.getRequiredActivityId());
			if (activityDef != null && activityDef.getEnabledWhen() != null) {
				i.remove();
				StatusManager.getManager().handle(new Status(IStatus.WARNING, PlatformUI.PLUGIN_ID,
						"Expression activity cannot have requirements (id: " + activityDef.getId() + ")")); //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}

			activityDef = getActivityDefinitionById(bindingDef.getActivityId());
			if (activityDef != null && activityDef.getEnabledWhen() != null) {
				i.remove();
				StatusManager.getManager().handle(new Status(IStatus.WARNING, PlatformUI.PLUGIN_ID,
						"Expression activity cannot be required (id: " + activityDef.getId() + ")")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		boolean activityRegistryChanged = false;

		if (!activityRequirementBindingDefinitions.equals(super.activityRequirementBindingDefinitions)) {
			super.activityRequirementBindingDefinitions = Collections
					.unmodifiableList(new ArrayList<>(activityRequirementBindingDefinitions));
			activityRegistryChanged = true;
		}

		if (!activityDefinitions.equals(super.activityDefinitions)) {
			super.activityDefinitions = Collections.unmodifiableList(new ArrayList<>(activityDefinitions));
			activityRegistryChanged = true;
		}

		if (!activityPatternBindingDefinitions.equals(super.activityPatternBindingDefinitions)) {
			super.activityPatternBindingDefinitions = Collections
					.unmodifiableList(new ArrayList<>(activityPatternBindingDefinitions));
			activityRegistryChanged = true;
		}

		if (!categoryActivityBindingDefinitions.equals(super.categoryActivityBindingDefinitions)) {
			super.categoryActivityBindingDefinitions = Collections
					.unmodifiableList(new ArrayList<>(categoryActivityBindingDefinitions));
			activityRegistryChanged = true;
		}

		if (!categoryDefinitions.equals(super.categoryDefinitions)) {
			super.categoryDefinitions = Collections.unmodifiableList(new ArrayList<>(categoryDefinitions));
			activityRegistryChanged = true;
		}

		if (!defaultEnabledActivities.equals(super.defaultEnabledActivities)) {
			super.defaultEnabledActivities = Collections.unmodifiableList(new ArrayList<>(defaultEnabledActivities));
			activityRegistryChanged = true;
		}

		if (activityRegistryChanged) {
			fireActivityRegistryChanged();
		}
	}

	/**
	 * Create the preference key for the activity.
	 *
	 * @param activityId the activity id.
	 * @return String a preference key representing the activity.
	 */
	private String createPreferenceKey(String activityId) {
		return PREFIX + activityId;
	}

	private void readDefaultEnablement(IConfigurationElement configurationElement) {
		String enabledActivity = Persistence
				.readDefaultEnablement(new ConfigurationElementMemento(configurationElement));

		if (enabledActivity != null) {
			defaultEnabledActivities.add(enabledActivity);
		}

	}

	private void readActivityRequirementBindingDefinition(IConfigurationElement configurationElement) {
		ActivityRequirementBindingDefinition activityRequirementBindingDefinition = Persistence
				.readActivityRequirementBindingDefinition(new ConfigurationElementMemento(configurationElement),
						getNamespace(configurationElement));

		if (activityRequirementBindingDefinition != null) {
			activityRequirementBindingDefinitions.add(activityRequirementBindingDefinition);
		}
	}

	private void readActivityDefinition(IConfigurationElement configurationElement) {
		ActivityDefinition activityDefinition = Persistence.readActivityDefinition(
				new ConfigurationElementMemento(configurationElement), getNamespace(configurationElement));

		if (activityDefinition != null) {
			// this is not ideal, but core expressions takes an
			// IConfigurationElement or a w3c dom Document
			IConfigurationElement[] enabledWhen = configurationElement
					.getChildren(IWorkbenchRegistryConstants.TAG_ENABLED_WHEN);
			if (enabledWhen.length == 1) {
				IConfigurationElement[] expElement = enabledWhen[0].getChildren();
				if (expElement.length == 1) {
					try {
						Expression expression = ExpressionConverter.getDefault().perform(expElement[0]);
						activityDefinition.setEnabledWhen(expression);
					} catch (CoreException e) {
						StatusManager.getManager().handle(e, WorkbenchPlugin.PI_WORKBENCH);
					}
				}
			}
			activityDefinitions.add(activityDefinition);
		}
	}

	private void readActivityPatternBindingDefinition(IConfigurationElement configurationElement) {
		ActivityPatternBindingDefinition activityPatternBindingDefinition = Persistence
				.readActivityPatternBindingDefinition(new ConfigurationElementMemento(configurationElement),
						getNamespace(configurationElement));

		if (activityPatternBindingDefinition != null) {
			activityPatternBindingDefinitions.add(activityPatternBindingDefinition);
		}
	}

	private void readCategoryActivityBindingDefinition(IConfigurationElement configurationElement) {
		CategoryActivityBindingDefinition categoryActivityBindingDefinition = Persistence
				.readCategoryActivityBindingDefinition(new ConfigurationElementMemento(configurationElement),
						getNamespace(configurationElement));

		if (categoryActivityBindingDefinition != null) {
			categoryActivityBindingDefinitions.add(categoryActivityBindingDefinition);
		}
	}

	private void readCategoryDefinition(IConfigurationElement configurationElement) {
		CategoryDefinition categoryDefinition = Persistence.readCategoryDefinition(
				new ConfigurationElementMemento(configurationElement), getNamespace(configurationElement));

		if (categoryDefinition != null) {
			categoryDefinitions.add(categoryDefinition);
		}
	}
}
