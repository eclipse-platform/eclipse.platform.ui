/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.descriptor.basic.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.ui.model.LocalizationHelper;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.impl.ApplicationElementImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.impl.StringToStringMapImpl;
import org.eclipse.e4.ui.model.application.ui.MLocalizable;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Part Descriptor</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getIconURI <em>Icon URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getTooltip <em>Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getLocalizedLabel <em>Localized Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getLocalizedTooltip <em>Localized Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getHandlers <em>Handlers</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getBindingContexts <em>Binding Contexts</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#isAllowMultiple <em>Allow Multiple</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getCategory <em>Category</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getMenus <em>Menus</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getToolbar <em>Toolbar</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#isCloseable <em>Closeable</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#isDirtyable <em>Dirtyable</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getContributionURI <em>Contribution URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getLocalizedDescription <em>Localized Description</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getVariables <em>Variables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.impl.PartDescriptorImpl#getTrimBars <em>Trim Bars</em>}</li>
 * </ul>
 *
 * @since 1.0
 * @generated
 */
@SuppressWarnings("deprecation")
public class PartDescriptorImpl extends ApplicationElementImpl implements MPartDescriptor {
	/**
	 * The default value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected static final String LABEL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected String label = LABEL_EDEFAULT;

	/**
	 * The default value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected static final String ICON_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected String iconURI = ICON_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected static final String TOOLTIP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected String tooltip = TOOLTIP_EDEFAULT;

	/**
	 * The default value of the '{@link #getLocalizedLabel() <em>Localized Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedLabel()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_LABEL_EDEFAULT = ""; //$NON-NLS-1$

	/**
	 * The default value of the '{@link #getLocalizedTooltip() <em>Localized Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedTooltip()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_TOOLTIP_EDEFAULT = ""; //$NON-NLS-1$

	/**
	 * The cached value of the '{@link #getHandlers() <em>Handlers</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHandlers()
	 * @generated
	 * @ordered
	 */
	protected EList<MHandler> handlers;

	/**
	 * The cached value of the '{@link #getBindingContexts() <em>Binding Contexts</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindingContexts()
	 * @generated
	 * @ordered
	 */
	protected EList<MBindingContext> bindingContexts;

	/**
	 * The default value of the '{@link #isAllowMultiple() <em>Allow Multiple</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAllowMultiple()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ALLOW_MULTIPLE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isAllowMultiple() <em>Allow Multiple</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAllowMultiple()
	 * @generated
	 * @ordered
	 */
	protected boolean allowMultiple = ALLOW_MULTIPLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getCategory() <em>Category</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCategory()
	 * @generated
	 * @ordered
	 */
	protected static final String CATEGORY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCategory() <em>Category</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCategory()
	 * @generated
	 * @ordered
	 */
	protected String category = CATEGORY_EDEFAULT;

	/**
	 * The cached value of the '{@link #getMenus() <em>Menus</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMenus()
	 * @generated
	 * @ordered
	 */
	protected EList<MMenu> menus;

	/**
	 * The cached value of the '{@link #getToolbar() <em>Toolbar</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getToolbar()
	 * @generated
	 * @ordered
	 */
	protected MToolBar toolbar;

	/**
	 * The default value of the '{@link #isCloseable() <em>Closeable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCloseable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CLOSEABLE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isCloseable() <em>Closeable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCloseable()
	 * @generated
	 * @ordered
	 */
	protected boolean closeable = CLOSEABLE_EDEFAULT;

	/**
	 * The default value of the '{@link #isDirtyable() <em>Dirtyable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDirtyable()
	 * @deprecated See {@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable() model documentation} for details.
	 * @noreference See {@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable() model documentation} for details.
	 * @generated
	 * @ordered
	 */
	@Deprecated
	protected static final boolean DIRTYABLE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isDirtyable() <em>Dirtyable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDirtyable()
	 * @deprecated See {@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable() model documentation} for details.
	 * @noreference See {@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable() model documentation} for details.
	 * @generated
	 * @ordered
	 */
	@Deprecated
	protected boolean dirtyable = DIRTYABLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getContributionURI() <em>Contribution URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContributionURI()
	 * @generated
	 * @ordered
	 */
	protected static final String CONTRIBUTION_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getContributionURI() <em>Contribution URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContributionURI()
	 * @generated
	 * @ordered
	 */
	protected String contributionURI = CONTRIBUTION_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The default value of the '{@link #getLocalizedDescription() <em>Localized Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getVariables() <em>Variables</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVariables()
	 * @generated
	 * @ordered
	 */
	protected EList<String> variables;

	/**
	 * The cached value of the '{@link #getProperties() <em>Properties</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProperties()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, String> properties;

	/**
	 * The cached value of the '{@link #getTrimBars() <em>Trim Bars</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTrimBars()
	 * @since 1.3
	 * @generated
	 * @ordered
	 */
	protected EList<MTrimBar> trimBars;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PartDescriptorImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BasicPackageImpl.Literals.PART_DESCRIPTOR;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public void setLabel(String newLabel) {
		String oldLabel = label;
		label = newLabel;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__LABEL, oldLabel,
					label));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public String getIconURI() {
		return iconURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public void setIconURI(String newIconURI) {
		String oldIconURI = iconURI;
		iconURI = newIconURI;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__ICON_URI,
					oldIconURI, iconURI));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public void setTooltip(String newTooltip) {
		String oldTooltip = tooltip;
		tooltip = newTooltip;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__TOOLTIP, oldTooltip,
					tooltip));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLocalizedLabel() {
		return LocalizationHelper.getLocalizedFeature(UiPackageImpl.Literals.UI_LABEL__LABEL, this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLocalizedTooltip() {
		return LocalizationHelper.getLocalizedFeature(UiPackageImpl.Literals.UI_LABEL__TOOLTIP, this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MHandler> getHandlers() {
		if (handlers == null) {
			handlers = new EObjectContainmentEList<>(MHandler.class, this,
					BasicPackageImpl.PART_DESCRIPTOR__HANDLERS);
		}
		return handlers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MBindingContext> getBindingContexts() {
		if (bindingContexts == null) {
			bindingContexts = new EObjectResolvingEList<>(MBindingContext.class, this,
					BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS);
		}
		return bindingContexts;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isAllowMultiple() {
		return allowMultiple;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAllowMultiple(boolean newAllowMultiple) {
		boolean oldAllowMultiple = allowMultiple;
		allowMultiple = newAllowMultiple;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__ALLOW_MULTIPLE,
					oldAllowMultiple, allowMultiple));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCategory() {
		return category;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCategory(String newCategory) {
		String oldCategory = category;
		category = newCategory;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__CATEGORY,
					oldCategory, category));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MMenu> getMenus() {
		if (menus == null) {
			menus = new EObjectContainmentEList<>(MMenu.class, this, BasicPackageImpl.PART_DESCRIPTOR__MENUS);
		}
		return menus;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MToolBar getToolbar() {
		return toolbar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetToolbar(MToolBar newToolbar, NotificationChain msgs) {
		MToolBar oldToolbar = toolbar;
		toolbar = newToolbar;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR, oldToolbar, newToolbar);
			if (msgs == null) {
				msgs = notification;
			} else {
				msgs.add(notification);
			}
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setToolbar(MToolBar newToolbar) {
		if (newToolbar != toolbar) {
			NotificationChain msgs = null;
			if (toolbar != null) {
				msgs = ((InternalEObject) toolbar).eInverseRemove(this,
						EOPPOSITE_FEATURE_BASE - BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR, null, msgs);
			}
			if (newToolbar != null) {
				msgs = ((InternalEObject) newToolbar).eInverseAdd(this,
						EOPPOSITE_FEATURE_BASE - BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR, null, msgs);
			}
			msgs = basicSetToolbar(newToolbar, msgs);
			if (msgs != null) {
				msgs.dispatch();
			}
		} else if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR, newToolbar,
					newToolbar));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isCloseable() {
		return closeable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCloseable(boolean newCloseable) {
		boolean oldCloseable = closeable;
		closeable = newCloseable;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__CLOSEABLE,
					oldCloseable, closeable));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated See {@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable() model documentation} for details.
	 * @noreference See {@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable() model documentation} for details.
	 * @generated
	 */
	@Deprecated
	@Override
	public boolean isDirtyable() {
		return dirtyable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated See {@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable() model documentation} for details.
	 * @noreference See {@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable() model documentation} for details.
	 * @generated
	 */
	@Deprecated
	@Override
	public void setDirtyable(boolean newDirtyable) {
		boolean oldDirtyable = dirtyable;
		dirtyable = newDirtyable;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__DIRTYABLE,
					oldDirtyable, dirtyable));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getContributionURI() {
		return contributionURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContributionURI(String newContributionURI) {
		String oldContributionURI = contributionURI;
		contributionURI = newContributionURI;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__CONTRIBUTION_URI,
					oldContributionURI, contributionURI));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__DESCRIPTION,
					oldDescription, description));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLocalizedDescription() {
		return LocalizationHelper.getLocalizedFeature(BasicPackageImpl.Literals.PART_DESCRIPTOR__DESCRIPTION, this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<String> getVariables() {
		if (variables == null) {
			variables = new EDataTypeUniqueEList<>(String.class, this,
					BasicPackageImpl.PART_DESCRIPTOR__VARIABLES);
		}
		return variables;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = new EcoreEMap<>(ApplicationPackageImpl.Literals.STRING_TO_STRING_MAP,
					StringToStringMapImpl.class, this, BasicPackageImpl.PART_DESCRIPTOR__PROPERTIES);
		}
		return properties.map();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.3
	 * @generated
	 */
	@Override
	public List<MTrimBar> getTrimBars() {
		if (trimBars == null) {
			trimBars = new EObjectContainmentEList<>(MTrimBar.class, this,
					BasicPackageImpl.PART_DESCRIPTOR__TRIM_BARS);
		}
		return trimBars;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 */
	@Override
	public void updateLocalization() {
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__LOCALIZED_LABEL,
					null, getLocalizedLabel()));
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.PART_DESCRIPTOR__LOCALIZED_TOOLTIP,
					null, getLocalizedTooltip()));
			eNotify(new ENotificationImpl(this, Notification.SET,
					BasicPackageImpl.PART_DESCRIPTOR__LOCALIZED_DESCRIPTION, null, getLocalizedDescription()));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BasicPackageImpl.PART_DESCRIPTOR__HANDLERS:
			return ((InternalEList<?>) getHandlers()).basicRemove(otherEnd, msgs);
		case BasicPackageImpl.PART_DESCRIPTOR__MENUS:
			return ((InternalEList<?>) getMenus()).basicRemove(otherEnd, msgs);
		case BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR:
			return basicSetToolbar(null, msgs);
		case BasicPackageImpl.PART_DESCRIPTOR__PROPERTIES:
			return ((InternalEList<?>) ((EMap.InternalMapView<String, String>) getProperties()).eMap())
					.basicRemove(otherEnd, msgs);
		case BasicPackageImpl.PART_DESCRIPTOR__TRIM_BARS:
			return ((InternalEList<?>) getTrimBars()).basicRemove(otherEnd, msgs);
		default:
			return super.eInverseRemove(otherEnd, featureID, msgs);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case BasicPackageImpl.PART_DESCRIPTOR__LABEL:
			return getLabel();
		case BasicPackageImpl.PART_DESCRIPTOR__ICON_URI:
			return getIconURI();
		case BasicPackageImpl.PART_DESCRIPTOR__TOOLTIP:
			return getTooltip();
		case BasicPackageImpl.PART_DESCRIPTOR__LOCALIZED_LABEL:
			return getLocalizedLabel();
		case BasicPackageImpl.PART_DESCRIPTOR__LOCALIZED_TOOLTIP:
			return getLocalizedTooltip();
		case BasicPackageImpl.PART_DESCRIPTOR__HANDLERS:
			return getHandlers();
		case BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS:
			return getBindingContexts();
		case BasicPackageImpl.PART_DESCRIPTOR__ALLOW_MULTIPLE:
			return isAllowMultiple();
		case BasicPackageImpl.PART_DESCRIPTOR__CATEGORY:
			return getCategory();
		case BasicPackageImpl.PART_DESCRIPTOR__MENUS:
			return getMenus();
		case BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR:
			return getToolbar();
		case BasicPackageImpl.PART_DESCRIPTOR__CLOSEABLE:
			return isCloseable();
		case BasicPackageImpl.PART_DESCRIPTOR__DIRTYABLE:
			return isDirtyable();
		case BasicPackageImpl.PART_DESCRIPTOR__CONTRIBUTION_URI:
			return getContributionURI();
		case BasicPackageImpl.PART_DESCRIPTOR__DESCRIPTION:
			return getDescription();
		case BasicPackageImpl.PART_DESCRIPTOR__LOCALIZED_DESCRIPTION:
			return getLocalizedDescription();
		case BasicPackageImpl.PART_DESCRIPTOR__VARIABLES:
			return getVariables();
		case BasicPackageImpl.PART_DESCRIPTOR__PROPERTIES:
			if (coreType) {
				return ((EMap.InternalMapView<String, String>) getProperties()).eMap();
			} else {
				return getProperties();
			}
		case BasicPackageImpl.PART_DESCRIPTOR__TRIM_BARS:
			return getTrimBars();
		default:
			return super.eGet(featureID, resolve, coreType);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case BasicPackageImpl.PART_DESCRIPTOR__LABEL:
			setLabel((String) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__ICON_URI:
			setIconURI((String) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__TOOLTIP:
			setTooltip((String) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__HANDLERS:
			getHandlers().clear();
			getHandlers().addAll((Collection<? extends MHandler>) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS:
			getBindingContexts().clear();
			getBindingContexts().addAll((Collection<? extends MBindingContext>) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__ALLOW_MULTIPLE:
			setAllowMultiple((Boolean) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__CATEGORY:
			setCategory((String) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__MENUS:
			getMenus().clear();
			getMenus().addAll((Collection<? extends MMenu>) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR:
			setToolbar((MToolBar) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__CLOSEABLE:
			setCloseable((Boolean) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__DIRTYABLE:
			setDirtyable((Boolean) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__CONTRIBUTION_URI:
			setContributionURI((String) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__DESCRIPTION:
			setDescription((String) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__VARIABLES:
			getVariables().clear();
			getVariables().addAll((Collection<? extends String>) newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__PROPERTIES:
			((EStructuralFeature.Setting) ((EMap.InternalMapView<String, String>) getProperties()).eMap())
					.set(newValue);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__TRIM_BARS:
			getTrimBars().clear();
			getTrimBars().addAll((Collection<? extends MTrimBar>) newValue);
			return;
		default:
			super.eSet(featureID, newValue);
			return;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case BasicPackageImpl.PART_DESCRIPTOR__LABEL:
			setLabel(LABEL_EDEFAULT);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__ICON_URI:
			setIconURI(ICON_URI_EDEFAULT);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__TOOLTIP:
			setTooltip(TOOLTIP_EDEFAULT);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__HANDLERS:
			getHandlers().clear();
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS:
			getBindingContexts().clear();
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__ALLOW_MULTIPLE:
			setAllowMultiple(ALLOW_MULTIPLE_EDEFAULT);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__CATEGORY:
			setCategory(CATEGORY_EDEFAULT);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__MENUS:
			getMenus().clear();
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR:
			setToolbar((MToolBar) null);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__CLOSEABLE:
			setCloseable(CLOSEABLE_EDEFAULT);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__DIRTYABLE:
			setDirtyable(DIRTYABLE_EDEFAULT);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__CONTRIBUTION_URI:
			setContributionURI(CONTRIBUTION_URI_EDEFAULT);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__DESCRIPTION:
			setDescription(DESCRIPTION_EDEFAULT);
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__VARIABLES:
			getVariables().clear();
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__PROPERTIES:
			getProperties().clear();
			return;
		case BasicPackageImpl.PART_DESCRIPTOR__TRIM_BARS:
			getTrimBars().clear();
			return;
		default:
			super.eUnset(featureID);
			return;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case BasicPackageImpl.PART_DESCRIPTOR__LABEL:
			return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
		case BasicPackageImpl.PART_DESCRIPTOR__ICON_URI:
			return ICON_URI_EDEFAULT == null ? iconURI != null : !ICON_URI_EDEFAULT.equals(iconURI);
		case BasicPackageImpl.PART_DESCRIPTOR__TOOLTIP:
			return TOOLTIP_EDEFAULT == null ? tooltip != null : !TOOLTIP_EDEFAULT.equals(tooltip);
		case BasicPackageImpl.PART_DESCRIPTOR__LOCALIZED_LABEL:
			return LOCALIZED_LABEL_EDEFAULT == null ? getLocalizedLabel() != null
					: !LOCALIZED_LABEL_EDEFAULT.equals(getLocalizedLabel());
		case BasicPackageImpl.PART_DESCRIPTOR__LOCALIZED_TOOLTIP:
			return LOCALIZED_TOOLTIP_EDEFAULT == null ? getLocalizedTooltip() != null
					: !LOCALIZED_TOOLTIP_EDEFAULT.equals(getLocalizedTooltip());
		case BasicPackageImpl.PART_DESCRIPTOR__HANDLERS:
			return handlers != null && !handlers.isEmpty();
		case BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS:
			return bindingContexts != null && !bindingContexts.isEmpty();
		case BasicPackageImpl.PART_DESCRIPTOR__ALLOW_MULTIPLE:
			return allowMultiple != ALLOW_MULTIPLE_EDEFAULT;
		case BasicPackageImpl.PART_DESCRIPTOR__CATEGORY:
			return CATEGORY_EDEFAULT == null ? category != null : !CATEGORY_EDEFAULT.equals(category);
		case BasicPackageImpl.PART_DESCRIPTOR__MENUS:
			return menus != null && !menus.isEmpty();
		case BasicPackageImpl.PART_DESCRIPTOR__TOOLBAR:
			return toolbar != null;
		case BasicPackageImpl.PART_DESCRIPTOR__CLOSEABLE:
			return closeable != CLOSEABLE_EDEFAULT;
		case BasicPackageImpl.PART_DESCRIPTOR__DIRTYABLE:
			return dirtyable != DIRTYABLE_EDEFAULT;
		case BasicPackageImpl.PART_DESCRIPTOR__CONTRIBUTION_URI:
			return CONTRIBUTION_URI_EDEFAULT == null ? contributionURI != null
					: !CONTRIBUTION_URI_EDEFAULT.equals(contributionURI);
		case BasicPackageImpl.PART_DESCRIPTOR__DESCRIPTION:
			return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
		case BasicPackageImpl.PART_DESCRIPTOR__LOCALIZED_DESCRIPTION:
			return LOCALIZED_DESCRIPTION_EDEFAULT == null ? getLocalizedDescription() != null
					: !LOCALIZED_DESCRIPTION_EDEFAULT.equals(getLocalizedDescription());
		case BasicPackageImpl.PART_DESCRIPTOR__VARIABLES:
			return variables != null && !variables.isEmpty();
		case BasicPackageImpl.PART_DESCRIPTOR__PROPERTIES:
			return properties != null && !properties.isEmpty();
		case BasicPackageImpl.PART_DESCRIPTOR__TRIM_BARS:
			return trimBars != null && !trimBars.isEmpty();
		default:
			return super.eIsSet(featureID);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == MLocalizable.class) {
			switch (derivedFeatureID) {
			default:
				return -1;
			}
		}
		if (baseClass == MUILabel.class) {
			switch (derivedFeatureID) {
			case BasicPackageImpl.PART_DESCRIPTOR__LABEL:
				return UiPackageImpl.UI_LABEL__LABEL;
			case BasicPackageImpl.PART_DESCRIPTOR__ICON_URI:
				return UiPackageImpl.UI_LABEL__ICON_URI;
			case BasicPackageImpl.PART_DESCRIPTOR__TOOLTIP:
				return UiPackageImpl.UI_LABEL__TOOLTIP;
			case BasicPackageImpl.PART_DESCRIPTOR__LOCALIZED_LABEL:
				return UiPackageImpl.UI_LABEL__LOCALIZED_LABEL;
			case BasicPackageImpl.PART_DESCRIPTOR__LOCALIZED_TOOLTIP:
				return UiPackageImpl.UI_LABEL__LOCALIZED_TOOLTIP;
			default:
				return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (derivedFeatureID) {
			case BasicPackageImpl.PART_DESCRIPTOR__HANDLERS:
				return CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS;
			default:
				return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (derivedFeatureID) {
			case BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS:
				return CommandsPackageImpl.BINDINGS__BINDING_CONTEXTS;
			default:
				return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == MLocalizable.class) {
			switch (baseFeatureID) {
			default:
				return -1;
			}
		}
		if (baseClass == MUILabel.class) {
			switch (baseFeatureID) {
			case UiPackageImpl.UI_LABEL__LABEL:
				return BasicPackageImpl.PART_DESCRIPTOR__LABEL;
			case UiPackageImpl.UI_LABEL__ICON_URI:
				return BasicPackageImpl.PART_DESCRIPTOR__ICON_URI;
			case UiPackageImpl.UI_LABEL__TOOLTIP:
				return BasicPackageImpl.PART_DESCRIPTOR__TOOLTIP;
			case UiPackageImpl.UI_LABEL__LOCALIZED_LABEL:
				return BasicPackageImpl.PART_DESCRIPTOR__LOCALIZED_LABEL;
			case UiPackageImpl.UI_LABEL__LOCALIZED_TOOLTIP:
				return BasicPackageImpl.PART_DESCRIPTOR__LOCALIZED_TOOLTIP;
			default:
				return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (baseFeatureID) {
			case CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS:
				return BasicPackageImpl.PART_DESCRIPTOR__HANDLERS;
			default:
				return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (baseFeatureID) {
			case CommandsPackageImpl.BINDINGS__BINDING_CONTEXTS:
				return BasicPackageImpl.PART_DESCRIPTOR__BINDING_CONTEXTS;
			default:
				return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedOperationID(int baseOperationID, Class<?> baseClass) {
		if (baseClass == MLocalizable.class) {
			switch (baseOperationID) {
			case UiPackageImpl.LOCALIZABLE___UPDATE_LOCALIZATION:
				return BasicPackageImpl.PART_DESCRIPTOR___UPDATE_LOCALIZATION;
			default:
				return -1;
			}
		}
		if (baseClass == MUILabel.class) {
			switch (baseOperationID) {
			default:
				return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (baseOperationID) {
			default:
				return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (baseOperationID) {
			default:
				return -1;
			}
		}
		return super.eDerivedOperationID(baseOperationID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
		case BasicPackageImpl.PART_DESCRIPTOR___UPDATE_LOCALIZATION:
			updateLocalization();
			return null;
		default:
			return super.eInvoke(operationID, arguments);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) {
			return super.toString();
		}

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (label: "); //$NON-NLS-1$
		result.append(label);
		result.append(", iconURI: "); //$NON-NLS-1$
		result.append(iconURI);
		result.append(", tooltip: "); //$NON-NLS-1$
		result.append(tooltip);
		result.append(", allowMultiple: "); //$NON-NLS-1$
		result.append(allowMultiple);
		result.append(", category: "); //$NON-NLS-1$
		result.append(category);
		result.append(", closeable: "); //$NON-NLS-1$
		result.append(closeable);
		result.append(", dirtyable: "); //$NON-NLS-1$
		result.append(dirtyable);
		result.append(", contributionURI: "); //$NON-NLS-1$
		result.append(contributionURI);
		result.append(", description: "); //$NON-NLS-1$
		result.append(description);
		result.append(", variables: "); //$NON-NLS-1$
		result.append(variables);
		result.append(')');
		return result.toString();
	}

} //PartDescriptorImpl
