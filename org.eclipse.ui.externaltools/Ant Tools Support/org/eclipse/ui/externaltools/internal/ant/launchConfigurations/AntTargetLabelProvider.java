package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.ant.core.TargetInfo;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * Ant target label provider
 */
public class AntTargetLabelProvider extends LabelProvider {
	private TableViewer viewer= null;

	public AntTargetLabelProvider(TableViewer viewer) {
		this.viewer= viewer;
	}
	
	/* (non-Javadoc)
	 * Method declared on ILabelProvider.
	 */
	public String getText(Object model) {
		TargetInfo target = (TargetInfo) model;
		
		if (target != null) {
			StringBuffer result = new StringBuffer(target.getName());
			if (viewer != null) {
				TableItem[] items= viewer.getTable().getItems();
				for (int i = 0; i < items.length; i++) {
					TableItem item = items[i];
					TargetInfo info = (TargetInfo)item.getData();
					if (info == target) {					
						if (info.isDefault()) {
							item.setForeground(viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_BLUE));
							result.append(" ("); //$NON-NLS-1$;
							result.append(AntLaunchConfigurationMessages.getString("AntTargetLabelProvider.default_target_1")); //$NON-NLS-1$
							result.append(")"); //$NON-NLS-1$;
						}  else {
							item.setForeground(viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
						}
						break;
					} 
				}
			} 
			return result.toString();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (viewer == null || viewer.getControl().isEnabled()) {
			return ExternalToolsImages.getImage(IExternalToolConstants.IMG_TAB_ANT_TARGETS);
		}
		return null;
	}
}