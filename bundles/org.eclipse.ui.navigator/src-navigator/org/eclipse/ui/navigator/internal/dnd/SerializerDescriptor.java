/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.navigator.internal.dnd;
 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.navigator.internal.ActionExpression;
import org.eclipse.ui.navigator.internal.NavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.dnd.SerializerCollectionDescriptor.ExtensionPointElements;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2 
 *
 */
public class SerializerDescriptor {

	public final String id;

	protected final IConfigurationElement serializerElement;

	public final ActionExpression enablement;

	private ISerializer serializer;

	public SerializerDescriptor(String dropHandlerId, IConfigurationElement serializer, ActionExpression dragEnablement) {

		Assert.isNotNull(serializer, NavigatorMessages.getString("SerializerDescriptor.0")); //$NON-NLS-1$

		String localId = serializer.getAttribute(ExtensionPointElements.ATT_ID);
		Assert.isNotNull(localId, NavigatorMessages.getString("SerializerDescriptor.1")); //$NON-NLS-1$

		this.id = dropHandlerId + ":" + localId; //$NON-NLS-1$
		this.serializerElement = serializer;

		IConfigurationElement[] enablementConfigElement = this.serializerElement.getChildren(ExtensionPointElements.ENABLEMENT);

		if (enablementConfigElement.length == 0)
			this.enablement = dragEnablement;
		else if (enablementConfigElement.length == 1)
			this.enablement = new ActionExpression(enablementConfigElement[0]);
		else {
			NavigatorPlugin.log(NavigatorMessages.format("SerializerDescriptor.3", new Object[]{this.id})); //$NON-NLS-1$ 
			this.enablement = dragEnablement;
		}
	}

	public ISerializer getSerializer() {
		if (serializer == null)
			try {
				serializer = (ISerializer) serializerElement.createExecutableExtension(ExtensionPointElements.ATT_CLASS);
			} catch (CoreException e) {
				NavigatorPlugin.log(NavigatorMessages.format("SerializerDescriptor.5", new Object[]{id, e.toString()})); //$NON-NLS-1$  
				serializer = null;
			}
		return serializer;
	}

}