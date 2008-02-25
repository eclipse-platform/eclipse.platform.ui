/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.preferences;

import org.eclipse.help.internal.base.remote.RemoteIC;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


public class RemoteICLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	private final String PROTOCOL = "http://"; //$NON-NLS-1$

	public Image getColumnImage(Object element, int columnIndex) {

		switch (columnIndex) {
		case 0:
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_FILE);
		case 1:
			break;
		case 2:
			break;
		case 3:
			break;
		default:
			break;
		}

		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		String result = ""; //$NON-NLS-1$
		RemoteIC remoteic = (RemoteIC) element;
		switch (columnIndex) {
		case 0:
			break;
		case 1:
			result = remoteic.getName();
			break;
		case 2:
			
			if(remoteic.getPort().equals("80")) //$NON-NLS-1$
			{
				result = PROTOCOL + remoteic.getHost() + remoteic.getPath();
			}
			else
			{
				result = PROTOCOL + remoteic.getHost() + ":" + remoteic.getPort() //$NON-NLS-1$
				+ remoteic.getPath();
			}
			break;
		case 3:
			result = (remoteic.isEnabled()) ? Messages.RemoteICLabelProvider_4 : Messages.RemoteICLabelProvider_5;
			break;
		default:
			break;
		}
		return result;
	}

}
