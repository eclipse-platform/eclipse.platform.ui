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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.ICommonDropActionDelegate;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.part.IDropActionDelegate;
import org.eclipse.ui.part.PluginTransfer;

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
public class PluginDropAction implements IDropActionDelegate {

	private ISerializer serializer;

	/**
	 *  
	 */
	public PluginDropAction() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.IDropActionDelegate#run(java.lang.Object, java.lang.Object)
	 */
	public boolean run(Object sourceData, Object target) {
		NavigatorSelectionSerializer.NavigatorSerializedSelection navSelection = (NavigatorSelectionSerializer.NavigatorSerializedSelection) getSerializer().fromByteArray((byte[]) sourceData);
		CommonDropHandlerService registry = CommonDropHandlerService.getInstance(navSelection.viewerId);

		TransferData transferData = PluginTransfer.getInstance().getSupportedTypes()[0];
		String serializerId = null;
		ICommonDropActionDelegate action = null;
		List validatedList = new ArrayList();
		Hashtable descriptorToSerizliazerIDHash = new Hashtable();
		for (Iterator keyItr = navSelection.selectionMap.keySet().iterator(); keyItr.hasNext();) {
			serializerId = keyItr.next().toString();
			DropHandlerDescriptor[] descriptors = registry.getDropHandlersBySerializerId(serializerId);
			for (int i = 0; i < descriptors.length; i++) {
				if (!validatedList.contains(descriptors[i])) {
					IDropValidator validator = descriptors[i].getDropValidator();
					if (null == validator || validator.validateDrop(null, target, -1, transferData)) {
						validatedList.add(descriptors[i]);
						descriptorToSerizliazerIDHash.put(descriptors[i], serializerId);
					}
				}
			}
		}
		IDialogSettings settings = NavigatorPlugin.getDefault().getDialogSettings();
		if (null == settings.get(NavigatorDropSelectionDialog.SKIP_ON_SINGLE_SELECTION)) {
			settings.put(NavigatorDropSelectionDialog.SKIP_ON_SINGLE_SELECTION, true);
		}

		boolean skipOnSingle = settings.getBoolean(NavigatorDropSelectionDialog.SKIP_ON_SINGLE_SELECTION);

		if (validatedList.size() > 0) {
			DropHandlerDescriptor[] validatedDescriptors = new DropHandlerDescriptor[validatedList.size()];
			validatedList.toArray(validatedDescriptors);
			DropHandlerDescriptor descriptorToUse = validatedDescriptors[0];

			if (validatedDescriptors.length != 1 || !skipOnSingle) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				NavigatorDropSelectionDialog dialog = new NavigatorDropSelectionDialog(shell, validatedDescriptors);
				if (Window.OK == dialog.open()) {
					descriptorToUse = dialog.getSelectedDescriptor();
				} else {
					descriptorToUse = null;
				}
			}
			if (null != descriptorToUse) {
				serializerId = (String) descriptorToSerizliazerIDHash.get(descriptorToUse);
				List objects = (List) navSelection.selectionMap.get(serializerId);
				IStructuredSelection selection = new StructuredSelection(objects.toArray());
				action = CommonDropHandlerService.getInstance(navSelection.viewerId).getActionForSerializerId(serializerId);
				action.run(selection, target);
				return true;
			}
		}

		return false;
	}

	/**
	 * @return
	 */
	protected ISerializer getSerializer() {
		if (serializer == null)
			serializer = new NavigatorSelectionSerializer();
		return serializer;
	}

}
