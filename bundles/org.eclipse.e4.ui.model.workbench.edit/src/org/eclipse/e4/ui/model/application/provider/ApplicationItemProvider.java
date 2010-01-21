/**
 * Copyright (c) 2008 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.MApplicationPackage;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

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
 * This is the item provider adapter for a {@link org.eclipse.e4.ui.model.application.MApplication} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class ApplicationItemProvider
	extends ContextItemProvider
	implements
		IEditingDomainItemProvider,
		IStructuredItemContentProvider,
		ITreeItemContentProvider,
		IItemLabelProvider,
		IItemPropertySource {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ApplicationItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	/**
	 * This returns the property descriptors for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
		if (itemPropertyDescriptors == null) {
			super.getPropertyDescriptors(object);

			addIdPropertyDescriptor(object);
			addStylePropertyDescriptor(object);
			addWidgetPropertyDescriptor(object);
			addRendererPropertyDescriptor(object);
			addToBeRenderedPropertyDescriptor(object);
			addOnTopPropertyDescriptor(object);
			addVisiblePropertyDescriptor(object);
			addContainerDataPropertyDescriptor(object);
			addActiveChildPropertyDescriptor(object);
			addCommandsPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Id feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addIdPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ApplicationElement_id_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_ApplicationElement_id_feature", "_UI_ApplicationElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.APPLICATION_ELEMENT__ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Style feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addStylePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ApplicationElement_style_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_ApplicationElement_style_feature", "_UI_ApplicationElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.APPLICATION_ELEMENT__STYLE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Widget feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addWidgetPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_widget_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_widget_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.UI_ELEMENT__WIDGET,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Renderer feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addRendererPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_renderer_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_renderer_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.UI_ELEMENT__RENDERER,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the To Be Rendered feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addToBeRenderedPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_toBeRendered_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_toBeRendered_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.UI_ELEMENT__TO_BE_RENDERED,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the On Top feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOnTopPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_onTop_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_onTop_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.UI_ELEMENT__ON_TOP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Visible feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addVisiblePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_visible_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_visible_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.UI_ELEMENT__VISIBLE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Container Data feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addContainerDataPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_containerData_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_containerData_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.UI_ELEMENT__CONTAINER_DATA,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Active Child feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addActiveChildPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ElementContainer_activeChild_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_ElementContainer_activeChild_feature", "_UI_ElementContainer_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.ELEMENT_CONTAINER__ACTIVE_CHILD,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Commands feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCommandsPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Application_commands_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Application_commands_feature", "_UI_Application_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.APPLICATION__COMMANDS,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
		if (childrenFeatures == null) {
			super.getChildrenFeatures(object);
			childrenFeatures.add(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN);
			childrenFeatures.add(MApplicationPackage.Literals.HANDLER_CONTAINER__HANDLERS);
			childrenFeatures.add(MApplicationPackage.Literals.BINDING_CONTAINER__BINDINGS);
			childrenFeatures.add(MApplicationPackage.Literals.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS);
		}
		return childrenFeatures;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EStructuralFeature getChildFeature(Object object, Object child) {
		// Check the type of the specified child object and return the proper feature to use for
		// adding (see {@link AddCommand}) it as a child.

		return super.getChildFeature(object, child);
	}

	/**
	 * This returns Application.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/Application")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public String getText(Object object) {
		String label = null; // ((MApplication)object).getId();
		return label == null || label.length() == 0 ?
			getString("_UI_Application_type") : //$NON-NLS-1$
			getString("_UI_Application_type") + " " + label; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * This handles model notifications by calling {@link #updateChildren} to update any cached
	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void notifyChanged(Notification notification) {
		updateChildren(notification);

		switch (notification.getFeatureID(MApplication.class)) {
			case MApplicationPackage.APPLICATION__ID:
			case MApplicationPackage.APPLICATION__STYLE:
			case MApplicationPackage.APPLICATION__WIDGET:
			case MApplicationPackage.APPLICATION__RENDERER:
			case MApplicationPackage.APPLICATION__TO_BE_RENDERED:
			case MApplicationPackage.APPLICATION__ON_TOP:
			case MApplicationPackage.APPLICATION__VISIBLE:
			case MApplicationPackage.APPLICATION__CONTAINER_DATA:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case MApplicationPackage.APPLICATION__CHILDREN:
			case MApplicationPackage.APPLICATION__HANDLERS:
			case MApplicationPackage.APPLICATION__BINDINGS:
			case MApplicationPackage.APPLICATION__DESCRIPTORS:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
				return;
		}
		super.notifyChanged(notification);
	}

	/**
	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
	 * that can be created under this object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createMenu()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createDirectMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createDirectToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createApplication()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPart()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPartDescriptor()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createSaveablePart()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPartStack()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPartSashContainer()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createWindow()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createHandledItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createHandledMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createHandledToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createWindowTrim()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createEditor()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPlaceholder()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPerspective()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createPerspectiveStack()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN,
				 MApplicationFactory.eINSTANCE.createTestHarness()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.HANDLER_CONTAINER__HANDLERS,
				 MApplicationFactory.eINSTANCE.createHandler()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.BINDING_CONTAINER__BINDINGS,
				 MApplicationFactory.eINSTANCE.createKeyBinding()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS,
				 MApplicationFactory.eINSTANCE.createPartDescriptor()));
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
			childFeature == MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN ||
			childFeature == MApplicationPackage.Literals.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS;

		if (qualify) {
			return getString
				("_UI_CreateChild_text2", //$NON-NLS-1$
				 new Object[] { getTypeText(childObject), getFeatureText(childFeature), getTypeText(owner) });
		}
		return super.getCreateChildText(owner, feature, child, selection);
	}

}
