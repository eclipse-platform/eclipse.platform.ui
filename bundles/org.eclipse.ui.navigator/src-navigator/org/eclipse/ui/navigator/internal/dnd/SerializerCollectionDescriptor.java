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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.navigator.internal.ActionExpression;

/**
 * @author mdelder
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

	public SerializerCollectionDescriptor(String dropHandlerId, IConfigurationElement configurationElement, ActionExpression defaultEnablement) {
		super();
		this.dropHandlerId = dropHandlerId;
		Assert.isNotNull(configurationElement);
		this.configurationElement = configurationElement;
		init(defaultEnablement);
	}

	/**
	 *  
	 */
	private void init(ActionExpression defaultEnablement) {
		IConfigurationElement[] elements = this.configurationElement.getChildren(ExtensionPointElements.SERIALIZER);
		serializerElements = new SerializerDescriptor[elements.length];
		if (elements != null && elements.length > 0) {
			for (int i = 0; i < elements.length; i++)
				serializerElements[i] = new SerializerDescriptor(this.dropHandlerId, elements[i], defaultEnablement);
		}
	}

	public SerializerDescriptor[] getSerializersEnabledFor(Object element) {

		List results = new ArrayList();
		SerializerDescriptor[] serializerArray = null;
		for (int i = 0; i < serializerElements.length; i++) {
			if (serializerElements[i].enablement.isEnabledFor(element))
				results.add(serializerElements[i]);
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