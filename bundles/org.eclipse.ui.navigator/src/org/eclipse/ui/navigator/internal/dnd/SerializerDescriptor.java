/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.dnd;
 
import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.Assert;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;
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

	public Expression enablement;

	private ISerializer serializer;

	public SerializerDescriptor(String dropHandlerId, IConfigurationElement serializer, Expression dragEnablement) {

		Assert.isNotNull(serializer, CommonNavigatorMessages.SerializerDescriptor_0);

		String localId = serializer.getAttribute(ExtensionPointElements.ATT_ID);
		Assert.isNotNull(localId, CommonNavigatorMessages.SerializerDescriptor_1);  

		this.id = dropHandlerId + ":" + localId; //$NON-NLS-1$
		this.serializerElement = serializer;

		IConfigurationElement[] enablementConfigElement = this.serializerElement.getChildren(ExtensionPointElements.ENABLEMENT);

		if (enablementConfigElement.length == 0)
			this.enablement = dragEnablement; 
		else if (enablementConfigElement.length == 1) {			
			try {
				enablement = ElementHandler.getDefault().create(
						ExpressionConverter.getDefault(), enablementConfigElement[0]);
			} catch (CoreException e) {
				NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
				enablement = dragEnablement;
			}
		}
		else {
			NavigatorPlugin.log(NLS.bind(CommonNavigatorMessages.SerializerDescriptor_3, new Object[]{this.id}));
			enablement = dragEnablement;
		}
	}

	public ISerializer getSerializer() {
		if (serializer == null)
			try {
				serializer = (ISerializer) serializerElement.createExecutableExtension(ExtensionPointElements.ATT_CLASS);
			} catch (CoreException e) {
				NavigatorPlugin.log(NLS.bind(CommonNavigatorMessages.SerializerDescriptor_5, new Object[]{id, e.toString()}));   
				serializer = null;
			}
		return serializer;
	}

}
