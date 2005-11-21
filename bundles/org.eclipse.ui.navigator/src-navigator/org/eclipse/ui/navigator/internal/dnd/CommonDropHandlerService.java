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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonDropActionDelegate;
import org.eclipse.ui.navigator.internal.NavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.extensions.RegistryReader;

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
public class CommonDropHandlerService extends RegistryReader {

	private static final DropHandlerDescriptor[] NO_DROP_HANDLER_DESCRIPTORS = new DropHandlerDescriptor[0];

	private Map descriptors;

	private Map actions;

	private static Map registries;

	private String viewerId;

	protected CommonDropHandlerService(String viewerId) {
		super(NavigatorPlugin.PLUGIN_ID, DropHandlerDescriptor.ExtensionPointElements.DROP_HANDLER);
		this.viewerId = viewerId;
	}

	public DropHandlerDescriptor[] getDropHandlersEnabledFor(Object source, Object target) {
		try {
			List results = new ArrayList();
			DropHandlerDescriptor descr = null;
			for (Iterator descriptorIterator = getDescriptors().values().iterator(); descriptorIterator.hasNext();) {
				descr = (DropHandlerDescriptor) descriptorIterator.next();
				if (descr.isDropEnabledFor(target) && (source == null || descr.isDragEnabledFor(source)))
					results.add(descr);
			}
			if (results.size() > 0)
				return (DropHandlerDescriptor[]) results.toArray(new DropHandlerDescriptor[results.size()]);
			return NO_DROP_HANDLER_DESCRIPTORS;
		} catch (Throwable t) {
			NavigatorPlugin.log(NavigatorMessages.getString("CommonDropHandlerService.0") + t); //$NON-NLS-1$
			return NO_DROP_HANDLER_DESCRIPTORS;
		}
	}

	public DropHandlerDescriptor[] getDropHandlersBySerializerId(String id) {
		try {
			List results = new ArrayList();
			DropHandlerDescriptor descr = null;
			SerializerCollectionDescriptor serializerCollectionDescriptor = null;
			for (Iterator descriptorIterator = getDescriptors().values().iterator(); descriptorIterator.hasNext();) {
				descr = (DropHandlerDescriptor) descriptorIterator.next();
				serializerCollectionDescriptor = descr.getSerializersDescriptor();
				if (serializerCollectionDescriptor != null && serializerCollectionDescriptor.getSerializerById(id) != null)
					results.add(descr);
			}
			if (results.size() > 0)
				return (DropHandlerDescriptor[]) results.toArray(new DropHandlerDescriptor[results.size()]);
			return NO_DROP_HANDLER_DESCRIPTORS;
		} catch (Throwable t) {
			NavigatorPlugin.log(NavigatorMessages.getString("CommonDropHandlerService.0") + t); //$NON-NLS-1$
			return NO_DROP_HANDLER_DESCRIPTORS;
		}
	}


	public SerializerDescriptor getSerializerById(String id) {
		DropHandlerDescriptor descr = null;
		SerializerDescriptor result = null;
		SerializerCollectionDescriptor serializerCollectionDescriptor = null;
		for (Iterator descriptorIterator = getDescriptors().values().iterator(); descriptorIterator.hasNext();) {
			descr = (DropHandlerDescriptor) descriptorIterator.next();
			serializerCollectionDescriptor = descr.getSerializersDescriptor();
			if (serializerCollectionDescriptor != null && (result = serializerCollectionDescriptor.getSerializerById(id)) != null)
				break;
		}
		return result;
	}

	public ICommonDropActionDelegate getActionForSerializerId(String serializerId) {

		DropHandlerDescriptor descr = null;
		SerializerCollectionDescriptor serializerCollectionDescriptor = null;
		ICommonDropActionDelegate action = null;
		for (Iterator descriptorIterator = getDescriptors().values().iterator(); descriptorIterator.hasNext();) {
			descr = (DropHandlerDescriptor) descriptorIterator.next();
			serializerCollectionDescriptor = descr.getSerializersDescriptor();
			if (serializerCollectionDescriptor != null && serializerCollectionDescriptor.getSerializerById(serializerId) != null) {
				action = descr.createAction();
				break;
			}
		}
		return action;
	}

	public SerializerDescriptor[] getSerializersEnabledFor(Object source) {
		SerializerDescriptor[] serializers = null;
		try {
			List results = new ArrayList();
			Iterator descriptorIterator = getDescriptors().values().iterator();
			DropHandlerDescriptor descr = null;
			SerializerCollectionDescriptor serializerCollectionDescriptor = null;
			while (descriptorIterator.hasNext()) {
				descr = (DropHandlerDescriptor) descriptorIterator.next();
				if (descr.isDragEnabledFor(source)) {
					serializerCollectionDescriptor = descr.getSerializersDescriptor();
					if (serializerCollectionDescriptor != null)
						results.addAll(Arrays.asList(serializerCollectionDescriptor.getSerializersEnabledFor(source)));
				}
			}
			if (results.size() > 0) {
				results.toArray((serializers = new SerializerDescriptor[results.size()]));
			} else
				serializers = new SerializerDescriptor[0];

		} catch (Throwable t) {
			NavigatorPlugin.log(NavigatorMessages.getString("CommonDropHandlerService.0") + t); //$NON-NLS-1$
			serializers = new SerializerDescriptor[0];
		}
		return serializers;
	}

	public IDropValidator getDropValidator(CommonViewer aViewer, DropHandlerDescriptor descriptor) {
		if (descriptor == null)
			return null;
		ICommonDropActionDelegate action = null;
		IDropValidator dropValidator = descriptor.getDropValidator();
		if (dropValidator == null && (action = getDropActionDelegate(aViewer, descriptor)) instanceof IDropValidator)
			dropValidator = (IDropValidator) action;
		return dropValidator;
	}

	public ICommonDropActionDelegate getDropActionDelegate(CommonViewer aViewer, DropHandlerDescriptor descriptor) {
		Assert.isTrue(this.viewerId.equals(aViewer.getNavigatorContentService().getViewerId()));
		ICommonDropActionDelegate action = (ICommonDropActionDelegate) getActions().get(descriptor.getId());
		if (action == null) {
			if ((action = descriptor.createAction()) != null) {
				action.init(aViewer);
				getActions().put(descriptor.getId(), action);
			}
		}
		return action;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.registry.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
	 */
	public boolean readElement(IConfigurationElement element) {
		if (DropHandlerDescriptor.ExtensionPointElements.DROP_HANDLER.equals(element.getName())) {
			addDescriptor(new DropHandlerDescriptor(element));
			return true;
		}
		return false;
	}

	protected void addDescriptor(DropHandlerDescriptor descriptor) {
		getDescriptors().put(descriptor.getId(), descriptor);
	}

	/**
	 * @return Returns the descriptors.
	 */
	protected Map getDescriptors() {
		if (descriptors == null)
			descriptors = new HashMap();
		return descriptors;
	}

	/**
	 * @return Returns the actions.
	 */
	protected Map getActions() {
		if (actions == null)
			actions = new HashMap();
		return actions;
	}

	/**
	 * @return Returns the registries.
	 */
	protected static Map getRegistries() {
		if (registries == null)
			registries = new HashMap();
		return registries;
	}

	public static CommonDropHandlerService getInstance(String viewerId) {
		CommonDropHandlerService instance = (CommonDropHandlerService) getRegistries().get(viewerId);
		if (instance == null) {
			getRegistries().put(viewerId, (instance = new CommonDropHandlerService(viewerId)));
			instance.readRegistry();
		}
		return instance;
	}

}
