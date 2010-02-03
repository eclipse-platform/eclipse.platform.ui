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

import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MPerspective;

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
 * This is the item provider adapter for a {@link org.eclipse.e4.ui.model.application.MPerspective} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class PerspectiveItemProvider
	extends UILabelItemProvider
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
	public PerspectiveItemProvider(AdapterFactory adapterFactory) {
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
			addTagsPropertyDescriptor(object);
			addWidgetPropertyDescriptor(object);
			addRendererPropertyDescriptor(object);
			addToBeRenderedPropertyDescriptor(object);
			addOnTopPropertyDescriptor(object);
			addVisiblePropertyDescriptor(object);
			addContainerDataPropertyDescriptor(object);
			addSelectedElementPropertyDescriptor(object);
			addContextPropertyDescriptor(object);
			addVariablesPropertyDescriptor(object);
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
	 * This adds a property descriptor for the Tags feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addTagsPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ApplicationElement_tags_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_ApplicationElement_tags_feature", "_UI_ApplicationElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.APPLICATION_ELEMENT__TAGS,
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
	 * This adds a property descriptor for the Selected Element feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSelectedElementPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ElementContainer_selectedElement_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_ElementContainer_selectedElement_feature", "_UI_ElementContainer_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.ELEMENT_CONTAINER__SELECTED_ELEMENT,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Context feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addContextPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Context_context_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Context_context_feature", "_UI_Context_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.CONTEXT__CONTEXT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Variables feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addVariablesPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Context_variables_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Context_variables_feature", "_UI_Context_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.CONTEXT__VARIABLES,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
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
			childrenFeatures.add(MApplicationPackage.Literals.CONTEXT__PROPERTIES);
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
	 * This returns Perspective.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/Perspective")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		String label = ((MPerspective)object).getLabel();
		return label == null || label.length() == 0 ?
			getString("_UI_Perspective_type") : //$NON-NLS-1$
			getString("_UI_Perspective_type") + " " + label; //$NON-NLS-1$ //$NON-NLS-2$
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

		switch (notification.getFeatureID(MPerspective.class)) {
			case MApplicationPackage.PERSPECTIVE__ID:
			case MApplicationPackage.PERSPECTIVE__TAGS:
			case MApplicationPackage.PERSPECTIVE__WIDGET:
			case MApplicationPackage.PERSPECTIVE__RENDERER:
			case MApplicationPackage.PERSPECTIVE__TO_BE_RENDERED:
			case MApplicationPackage.PERSPECTIVE__ON_TOP:
			case MApplicationPackage.PERSPECTIVE__VISIBLE:
			case MApplicationPackage.PERSPECTIVE__CONTAINER_DATA:
			case MApplicationPackage.PERSPECTIVE__CONTEXT:
			case MApplicationPackage.PERSPECTIVE__VARIABLES:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case MApplicationPackage.PERSPECTIVE__CHILDREN:
			case MApplicationPackage.PERSPECTIVE__PROPERTIES:
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
				(MApplicationPackage.Literals.CONTEXT__PROPERTIES,
				 MApplicationFactory.eINSTANCE.create(MApplicationPackage.Literals.STRING_TO_STRING_MAP)));
	}

}
