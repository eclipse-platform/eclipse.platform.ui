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
import org.eclipse.e4.ui.model.application.MModelComponents;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * This is the item provider adapter for a {@link org.eclipse.e4.ui.model.application.MModelComponents} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class ModelComponentsItemProvider
	extends ItemProviderAdapter
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
	public ModelComponentsItemProvider(AdapterFactory adapterFactory) {
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

		}
		return itemPropertyDescriptors;
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
			childrenFeatures.add(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__COMPONENTS);
			childrenFeatures.add(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS);
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
	 * This returns ModelComponents.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/ModelComponents")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		return getString("_UI_ModelComponents_type"); //$NON-NLS-1$
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

		switch (notification.getFeatureID(MModelComponents.class)) {
			case ApplicationPackageImpl.MODEL_COMPONENTS__COMPONENTS:
			case ApplicationPackageImpl.MODEL_COMPONENTS__IMPORTS:
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
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__COMPONENTS,
				 MApplicationFactory.INSTANCE.createModelComponent()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MApplicationFactory.INSTANCE.createApplication()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MApplicationFactory.INSTANCE.createModelComponent()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MApplicationFactory.INSTANCE.createAddon()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createBindingContext()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createBindingTable()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createCommand()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createCommandParameter()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createHandler()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createKeyBinding()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MCommandsFactory.INSTANCE.createParameter()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createItem()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createHandledItem()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createMenuSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createMenu()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createMenuContribution()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createPopupMenu()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createDirectMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createHandledMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createToolControl()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createHandledToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createDirectToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createToolBarSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createRenderedMenu()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MMenuFactory.INSTANCE.createRenderedToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createPart()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createInputPart()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createPartStack()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createPartSashContainer()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createWindow()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createTrimmedWindow()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createTrimBar()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MBasicFactory.INSTANCE.createStackElement()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MAdvancedFactory.INSTANCE.createPlaceholder()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MAdvancedFactory.INSTANCE.createPerspective()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 MAdvancedFactory.INSTANCE.createPerspectiveStack()));

		newChildDescriptors.add
			(createChildParameter
				(ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS,
				 org.eclipse.e4.ui.model.application.descriptor.basic.MBasicFactory.INSTANCE.createPartDescriptor()));
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
			childFeature == ApplicationPackageImpl.Literals.MODEL_COMPONENTS__COMPONENTS ||
			childFeature == ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS;

		if (qualify) {
			return getString
				("_UI_CreateChild_text2", //$NON-NLS-1$
				 new Object[] { getTypeText(childObject), getFeatureText(childFeature), getTypeText(owner) });
		}
		return super.getCreateChildText(owner, feature, child, selection);
	}

	/**
	 * Return the resource locator for this item provider's resources.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return UIElementsEditPlugin.INSTANCE;
	}

}
