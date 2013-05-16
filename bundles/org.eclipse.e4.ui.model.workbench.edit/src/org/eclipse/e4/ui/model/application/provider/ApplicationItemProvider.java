/**
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.provider;

import java.util.Collection;
import java.util.List;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.descriptor.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.model.application.ui.provider.ElementContainerItemProvider;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ViewerNotification;

/**
 * This is the item provider adapter for a
 * {@link org.eclipse.e4.ui.model.application.MApplication} object. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
public class ApplicationItemProvider extends ElementContainerItemProvider
		implements IEditingDomainItemProvider, IStructuredItemContentProvider,
		ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
	/**
	 * This constructs an instance from a factory and a notifier. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public ApplicationItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	/**
	 * This returns the property descriptors for the adapted class. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
		if (itemPropertyDescriptors == null) {
			super.getPropertyDescriptors(object);

			addContextPropertyDescriptor(object);
			addVariablesPropertyDescriptor(object);
			addBindingContextsPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Context feature. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void addContextPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Context_context_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Context_context_feature", "_UI_Context_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 UiPackageImpl.Literals.CONTEXT__CONTEXT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Variables feature. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void addVariablesPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Context_variables_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Context_variables_feature", "_UI_Context_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 UiPackageImpl.Literals.CONTEXT__VARIABLES,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Binding Contexts feature. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void addBindingContextsPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Bindings_bindingContexts_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Bindings_bindingContexts_feature", "_UI_Bindings_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 CommandsPackageImpl.Literals.BINDINGS__BINDING_CONTEXTS,
				 true,
				 false,
				 false,
				 null,
				 null,
				 null));
	}

	/**
	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Collection<? extends EStructuralFeature> getChildrenFeatures(
			Object object) {
		if (childrenFeatures == null) {
			super.getChildrenFeatures(object);
			childrenFeatures.add(UiPackageImpl.Literals.CONTEXT__PROPERTIES);
			childrenFeatures.add(CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS);
			childrenFeatures.add(CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__BINDING_TABLES);
			childrenFeatures.add(CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__ROOT_CONTEXT);
			childrenFeatures.add(BasicPackageImpl.Literals.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS);
			childrenFeatures.add(MenuPackageImpl.Literals.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS);
			childrenFeatures.add(MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS);
			childrenFeatures.add(MenuPackageImpl.Literals.TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS);
			childrenFeatures.add(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS);
			childrenFeatures.add(ApplicationPackageImpl.Literals.APPLICATION__COMMANDS);
			childrenFeatures.add(ApplicationPackageImpl.Literals.APPLICATION__ADDONS);
			childrenFeatures.add(ApplicationPackageImpl.Literals.APPLICATION__CATEGORIES);
		}
		return childrenFeatures;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EStructuralFeature getChildFeature(Object object, Object child) {
		// Check the type of the specified child object and return the proper feature to use for
		// adding (see {@link AddCommand}) it as a child.

		return super.getChildFeature(object, child);
	}

	/**
	 * This returns Application.gif. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/Application")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	@Override
	public String getText(Object object) {
		String label = null; // ((MApplication)object).getElementId();
		return label == null || label.length() == 0 ? getString("_UI_Application_type") : //$NON-NLS-1$
				getString("_UI_Application_type") + " " + label; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * This handles model notifications by calling {@link #updateChildren} to update any cached
	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 */
	@Override
	public void notifyChanged(Notification notification) {
		updateChildren(notification);

		switch (notification.getFeatureID(MApplication.class)) {
			case ApplicationPackageImpl.APPLICATION__CONTEXT:
			case ApplicationPackageImpl.APPLICATION__VARIABLES:
			case ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case ApplicationPackageImpl.APPLICATION__PROPERTIES:
			case ApplicationPackageImpl.APPLICATION__HANDLERS:
			case ApplicationPackageImpl.APPLICATION__BINDING_TABLES:
			case ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT:
			case ApplicationPackageImpl.APPLICATION__DESCRIPTORS:
			case ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS:
			case ApplicationPackageImpl.APPLICATION__TOOL_BAR_CONTRIBUTIONS:
			case ApplicationPackageImpl.APPLICATION__TRIM_CONTRIBUTIONS:
			case ApplicationPackageImpl.APPLICATION__SNIPPETS:
			case ApplicationPackageImpl.APPLICATION__COMMANDS:
			case ApplicationPackageImpl.APPLICATION__ADDONS:
			case ApplicationPackageImpl.APPLICATION__CATEGORIES:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
				return;
		}
		super.notifyChanged(notification);
	}

	/**
	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s
	 * describing the children that can be created under this object. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected void collectNewChildDescriptors(
			Collection<Object> newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.CONTEXT__PROPERTIES,
				 ((EFactory)MApplicationFactory.INSTANCE).create(ApplicationPackageImpl.Literals.STRING_TO_STRING_MAP)));

		newChildDescriptors.add
			(createChildParameter
				(CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS,
				 MCommandsFactory.INSTANCE.createHandler()));

		newChildDescriptors.add
			(createChildParameter
				(CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__BINDING_TABLES,
				 MCommandsFactory.INSTANCE.createBindingTable()));

		newChildDescriptors.add
			(createChildParameter
				(CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__ROOT_CONTEXT,
				 MCommandsFactory.INSTANCE.createBindingContext()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS,
				 MBasicFactory.INSTANCE.createPartDescriptor()));

		newChildDescriptors.add
			(createChildParameter
				(MenuPackageImpl.Literals.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS,
				 MMenuFactory.INSTANCE.createMenuContribution()));

		newChildDescriptors.add
			(createChildParameter
				(MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS,
				 MMenuFactory.INSTANCE.createToolBarContribution()));

		newChildDescriptors.add
			(createChildParameter
				(MenuPackageImpl.Literals.TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS,
				 MMenuFactory.INSTANCE.createTrimContribution()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MApplicationFactory.INSTANCE.createApplication()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createMenuSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createMenu()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createMenuContribution()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createPopupMenu()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createDirectMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createHandledMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createToolControl()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createHandledToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createDirectToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createToolBarSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createRenderedMenu()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createRenderedToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createToolBarContribution()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createTrimContribution()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createRenderedMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createOpaqueToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createOpaqueMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createOpaqueMenuSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createOpaqueMenu()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createDynamicMenuContribution()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory.INSTANCE.createPart()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory.INSTANCE.createInputPart()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory.INSTANCE.createPartStack()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory.INSTANCE.createPartSashContainer()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory.INSTANCE.createWindow()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory.INSTANCE.createTrimmedWindow()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory.INSTANCE.createTrimBar()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MAdvancedFactory.INSTANCE.createPlaceholder()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MAdvancedFactory.INSTANCE.createPerspective()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MAdvancedFactory.INSTANCE.createPerspectiveStack()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MAdvancedFactory.INSTANCE.createArea()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.APPLICATION__COMMANDS,
				 MCommandsFactory.INSTANCE.createCommand()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.APPLICATION__ADDONS,
				 MApplicationFactory.INSTANCE.createAddon()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.APPLICATION__CATEGORIES,
				 MCommandsFactory.INSTANCE.createCategory()));
	}

	/**
	 * This returns the label text for {@link org.eclipse.emf.edit.command.CreateChildCommand}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCreateChildText(Object owner, Object feature, Object child, Collection<?> selection) {
		Object childFeature = feature;
		Object childObject = child;

		boolean qualify =
			childFeature == ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE ||
			childFeature == UiPackageImpl.Literals.CONTEXT__PROPERTIES ||
			childFeature == UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN ||
			childFeature == UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS ||
			childFeature == MenuPackageImpl.Literals.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS ||
			childFeature == MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS ||
			childFeature == MenuPackageImpl.Literals.TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS;

		if (qualify) {
			return getString
				("_UI_CreateChild_text2", //$NON-NLS-1$
				 new Object[] { getTypeText(childObject), getFeatureText(childFeature), getTypeText(owner) });
		}
		return super.getCreateChildText(owner, feature, child, selection);
	}

	/**
	 * Return the resource locator for this item provider's resources. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return UIElementsEditPlugin.INSTANCE;
	}

}
