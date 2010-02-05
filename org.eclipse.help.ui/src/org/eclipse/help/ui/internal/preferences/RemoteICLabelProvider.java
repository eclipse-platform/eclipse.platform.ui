/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
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


public class RemoteICLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	private final String PROTOCOL = "://"; //$NON-NLS-1$

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		String result = ""; //$NON-NLS-1$
		RemoteIC remoteic = (RemoteIC) element;
		switch (columnIndex) {
		case 0:
			result = remoteic.getName();
			break;
		case 1:
			
			if(remoteic.getPort().equals("80")) //$NON-NLS-1$
			{
				result = remoteic.getProtocol() + PROTOCOL + remoteic.getHost() + remoteic.getPath();
			}
			else
			{
				result = remoteic.getProtocol() + PROTOCOL + remoteic.getHost() + ":" + remoteic.getPort() //$NON-NLS-1$
				+ remoteic.getPath();
			}
			break;
		case 2:
			result = (remoteic.isEnabled()) ? Messages.RemoteICLabelProvider_4 : Messages.RemoteICLabelProvider_5;
			break;
		default:
			break;
		}
		return result;
	}

}
