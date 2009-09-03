/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.dnd;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.internal.navigator.extensions.CommonDragAssistantDescriptor;
import org.eclipse.ui.internal.navigator.extensions.NavigatorViewerDescriptor;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorDnDService;

/**
 * 
 * Provides instances of {@link CommonDragAdapterAssistant} and
 * {@link CommonDropAdapterAssistant} for the associated
 * {@link INavigatorContentService}.
 * 
 * <p>
 * Clients may not extend, instantiate or directly reference this class.
 * </p>
 * 
 * @since 3.2
 * 
 */
public class NavigatorDnDService implements INavigatorDnDService {

	private static final CommonDropAdapterAssistant[] NO_ASSISTANTS = new CommonDropAdapterAssistant[0];

	private INavigatorContentService contentService;

	private CommonDragAdapterAssistant[] dragAssistants;
	
	private CommonDropAdapter dropAdapter;

	private final Map dropAssistants = new HashMap();

	/**
	 * 
	 * @param aContentService
	 *            The associated content service
	 */
	public NavigatorDnDService(INavigatorContentService aContentService) {
		contentService = aContentService;
	}

	/**
	 * @param da
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void setDropAdaptor(CommonDropAdapter da) {
		dropAdapter = da;
	}
	
	public synchronized CommonDragAdapterAssistant[] getCommonDragAssistants() {

		if (dragAssistants == null) 
			initializeDragAssistants(); 
		return dragAssistants;
	}
 
	private void initializeDragAssistants() {
		int i = 0;
		Set dragDescriptors = ((NavigatorViewerDescriptor) contentService
				.getViewerDescriptor()).getDragAssistants();
		dragAssistants = new CommonDragAdapterAssistant[dragDescriptors
				.size()];
		for (Iterator iter = dragDescriptors.iterator(); iter.hasNext();) {
			CommonDragAssistantDescriptor descriptor = (CommonDragAssistantDescriptor) iter
					.next();
			dragAssistants[i++] = descriptor.createDragAssistant();
		}
	}
	

	public synchronized void bindDragAssistant(CommonDragAdapterAssistant anAssistant) {
		if(dragAssistants == null) 
			initializeDragAssistants(); 
		CommonDragAdapterAssistant[] newDragAssistants = new CommonDragAdapterAssistant[dragAssistants.length + 1];
		System.arraycopy(dragAssistants, 0, newDragAssistants, 0, dragAssistants.length);
		newDragAssistants[dragAssistants.length] = anAssistant;
		dragAssistants = newDragAssistants;		
	}

	public CommonDropAdapterAssistant[] findCommonDropAdapterAssistants(
			Object aDropTarget, TransferData aTransferType) {
 
		// TODO Make sure descriptors are sorted by priority 
		CommonDropAdapterDescriptor[] descriptors = CommonDropDescriptorManager
				.getInstance().findCommonDropAdapterAssistants(aDropTarget,
						contentService);

		if (descriptors.length == 0) {
			return NO_ASSISTANTS;
		}

		if (LocalSelectionTransfer.getTransfer().isSupportedType(aTransferType)  
						&& LocalSelectionTransfer.getTransfer().getSelection() instanceof IStructuredSelection) {
			return getAssistantsBySelection(descriptors, (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection());
		} 
		return getAssistantsByTransferData(descriptors, aTransferType);
	}
	

	public CommonDropAdapterAssistant[] findCommonDropAdapterAssistants(
			Object aDropTarget, IStructuredSelection theDragSelection) {
 
		// TODO Make sure descriptors are sorted by priority 
		CommonDropAdapterDescriptor[] descriptors = CommonDropDescriptorManager
				.getInstance().findCommonDropAdapterAssistants(aDropTarget,
						contentService);

		if (descriptors.length == 0) {
			return NO_ASSISTANTS;
		}

		return getAssistantsBySelection(descriptors, theDragSelection);  
	}

	private CommonDropAdapterAssistant[] getAssistantsByTransferData(
			CommonDropAdapterDescriptor[] descriptors,
			TransferData aTransferType) {

		Set assistants = new LinkedHashSet();
		for (int i = 0; i < descriptors.length; i++) {
			CommonDropAdapterAssistant asst = getAssistant(descriptors[i]);
			if (asst.isSupportedType(aTransferType)) {
				assistants.add(asst);
			}
		}
		return sortAssistants((CommonDropAdapterAssistant[]) assistants
				.toArray(new CommonDropAdapterAssistant[assistants.size()]));

	}

	private CommonDropAdapterAssistant[] getAssistantsBySelection(
			CommonDropAdapterDescriptor[] descriptors, IStructuredSelection aSelection) {

		Set assistants = new LinkedHashSet(); 
			
		for (int i = 0; i < descriptors.length; i++) {
			if(descriptors[i].areDragElementsSupported(aSelection)) {
				assistants.add(getAssistant(descriptors[i]));
			}
		}  

		return sortAssistants((CommonDropAdapterAssistant[]) assistants
				.toArray(new CommonDropAdapterAssistant[assistants.size()]));
	}

	private CommonDropAdapterAssistant[] sortAssistants(CommonDropAdapterAssistant[] array) {
		Arrays.sort(array, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				CommonDropAdapterAssistant a = (CommonDropAdapterAssistant) arg0;
				CommonDropAdapterAssistant b = (CommonDropAdapterAssistant) arg1;
				// This is to ensure that the navigator resources drop assistant will
				// always be first on the list of drop assistant, if a conflict ever 
				// occurs.
				String id = "org.eclipse.ui.navigator.resources."; //$NON-NLS-1$
				if (a.getClass().getName().startsWith(id))
					return -1;
				if (b.getClass().getName().startsWith(id))
					return 1;
				return a.getClass().getName().compareTo(b.getClass().getName());
			}
		});
		return array;
	}
	private CommonDropAdapterAssistant getAssistant(
			CommonDropAdapterDescriptor descriptor) {
		CommonDropAdapterAssistant asst = (CommonDropAdapterAssistant) dropAssistants
				.get(descriptor);
		if (asst != null) {
			return asst;
		}
		synchronized (dropAssistants) {
			asst = (CommonDropAdapterAssistant) dropAssistants.get(descriptor);
			if (asst == null) {
				dropAssistants.put(descriptor, (asst = descriptor
						.createDropAssistant()));
				asst.init(contentService);
				asst.setCommonDropAdapter(dropAdapter);
			}
		}
		return asst;
	}

}
