/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.dnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.Assert;
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
public class SerializerCollectionDescriptor {

	public interface ExtensionPointElements {

		public static final String ATT_ID = "id"; //$NON-NLS-1$

		public static final String ATT_CLASS = "class"; //$NON-NLS-1$

		public static final String SERIALIZERS = "serializers"; //$NON-NLS-1$

		public static final String SERIALIZER = "serializer"; //$NON-NLS-1$

		public static final String ENABLEMENT = "enablement"; //$NON-NLS-1$
	}

	private IConfigurationElement configurationElement;

	private SerializerDescriptor[] serializerElements;

	private final String dropHandlerId;

	private Map serializerIndex;

	public SerializerCollectionDescriptor(String aDropHandlerId, IConfigurationElement aConfigurationElement, Expression theDefaultEnablement) {
		super();
		dropHandlerId = aDropHandlerId;
		Assert.isNotNull(aConfigurationElement);
		configurationElement = aConfigurationElement;
		init(theDefaultEnablement);
	}

	/**
	 *  
	 */
	private void init(Expression defaultEnablement) {
		IConfigurationElement[] elements = configurationElement.getChildren(ExtensionPointElements.SERIALIZER);
		serializerElements = new SerializerDescriptor[elements.length];
		if (elements != null && elements.length > 0) {
			for (int i = 0; i < elements.length; i++)
				serializerElements[i] = new SerializerDescriptor(dropHandlerId, elements[i], defaultEnablement);
		}
	}

	public SerializerDescriptor[] getSerializersEnabledFor(Object element) {

		List results = new ArrayList();
		SerializerDescriptor[] serializerArray = null;
		for (int i = 0; i < serializerElements.length; i++) {
		
			try {
				 if (serializerElements[i].enablement.evaluate(new EvaluationContext(null, element)) == EvaluationResult.TRUE)
						results.add(serializerElements[i]);
			} catch (CoreException e) {
				NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
			}  
		 
		}
		if (results.size() > 0)
			results.toArray((serializerArray = new SerializerDescriptor[results.size()]));
		else
			serializerArray = new SerializerDescriptor[0];
		return serializerArray;
	}

	public SerializerDescriptor getSerializerById(String id) {
		return (SerializerDescriptor) getSerializerIndex().get(id);
	}

	/**
	 * @return
	 */
	protected Map getSerializerIndex() {
		if (serializerIndex == null) {
			serializerIndex = new HashMap();
			for (int i = 0; i < serializerElements.length; i++)
				serializerIndex.put(serializerElements[i].id, serializerElements[i]);
		}
		return serializerIndex;
	}

}
