/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.dnd;
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.navigator.ICommonDropActionDelegate;
import org.eclipse.ui.navigator.internal.ActionExpression;
import org.eclipse.ui.navigator.internal.NavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 *  
 */
public class DropHandlerDescriptor {

	public interface ExtensionPointElements {

		public static final String DROP_HANDLER = "dropHandler"; //$NON-NLS-1$

		public static final String ATT_ID = "id"; //$NON-NLS-1$

		public static final String ATT_CLASS = "class"; //$NON-NLS-1$

		public static final String ATT_VALIDATOR_CLASS = "validatorClass"; //$NON-NLS-1$

		public static final String DRAG_ENABLEMENT = "dragEnablement"; //$NON-NLS-1$

		public static final String DROP_ENABLEMENT = "dropEnablement"; //$NON-NLS-1$

		public static final String SERIALIZERS = "serializers"; //$NON-NLS-1$

		public static final String SERIALIZER = "serializer"; //$NON-NLS-1$

		public static final String NAME = "name"; //$NON-NLS-1$

		public static final String DESCRIPTION = "description"; //$NON-NLS-1$
	}

	private String name;

	private String description;

	private String id = null;

	private IConfigurationElement configurationElement;

	private ActionExpression dragEnablement;

	private ActionExpression dropEnablement;

	private SerializerCollectionDescriptor serializers;

	private IConfigurationElement dropEnablementConfigElement;

	private IDropValidator dropValidator;

	/**
	 *  
	 */
	public DropHandlerDescriptor(IConfigurationElement aConfigurationElement) {
		super();
		Assert.isLegal(ExtensionPointElements.DROP_HANDLER.equals(aConfigurationElement.getName()));
		configurationElement = aConfigurationElement;
		init();
	}

	/**
	 *  
	 */
	private void init() {
		id = configurationElement.getAttribute(ExtensionPointElements.ATT_ID);
		name = configurationElement.getAttribute(ExtensionPointElements.NAME);
		description = configurationElement.getAttribute(ExtensionPointElements.DESCRIPTION);

		IConfigurationElement[] dragEnablementChildren = configurationElement.getChildren(ExtensionPointElements.DRAG_ENABLEMENT);
		if (dragEnablementChildren.length > 0 && dragEnablementChildren[0] != null)
			this.dragEnablement = new ActionExpression(dragEnablementChildren[0]);

		IConfigurationElement[] dropEnablementChildren = configurationElement.getChildren(ExtensionPointElements.DROP_ENABLEMENT);
		if (dropEnablementChildren.length > 0 && dropEnablementChildren[0] != null) {
			this.dropEnablementConfigElement = dropEnablementChildren[0];
			this.dropEnablement = new ActionExpression(dropEnablementChildren[0]);
		}

		IConfigurationElement[] serializersChild = configurationElement.getChildren(ExtensionPointElements.SERIALIZERS);
		if (serializersChild.length > 0 && serializersChild[0] != null)
			this.serializers = new SerializerCollectionDescriptor(this.id, serializersChild[0], dragEnablement);
	}

	public boolean isDragEnabledFor(Object element) {
		if (this.dragEnablement != null)
			return this.dragEnablement.isEnabledFor(element);
		return true;
	}

	public boolean isDropEnabledFor(Object element) {
		if (this.dropEnablement != null)
			return this.dropEnablement.isEnabledFor(element);
		return false;
	}

	/**
	 * @return
	 */
	protected IDropValidator getDropValidator() {
		if (dropValidator == null) {
			try {
				String classValue = this.dropEnablementConfigElement.getAttribute(ExtensionPointElements.ATT_VALIDATOR_CLASS);
				if (classValue != null && classValue.length() > 0)
					dropValidator = (IDropValidator) this.dropEnablementConfigElement.createExecutableExtension(ExtensionPointElements.ATT_VALIDATOR_CLASS);
			} catch (CoreException e) {
			}
		}
		return dropValidator;
	}

	public SerializerCollectionDescriptor getSerializersDescriptor() {
		return this.serializers;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	protected ICommonDropActionDelegate createAction() {
		try {
			return (ICommonDropActionDelegate) this.configurationElement.createExecutableExtension(ExtensionPointElements.ATT_CLASS);
		} catch (CoreException e) {
			String msg = NavigatorMessages.format("DropHandlerDescriptor.10", new Object[]{getId()}); //$NON-NLS-1$
			NavigatorPlugin.log(msg, e.getStatus());
		}
		return null;
	}

	public String getDescription() {
		return description != null ? description : ""; //$NON-NLS-1$
	}

	public String getName() {
		return name != null ? name : id;
	}
}