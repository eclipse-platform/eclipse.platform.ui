package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.ArrayList;

import org.eclipse.ant.core.TargetInfo;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * Ant target label provider
 */
public class AntTargetLabelProvider extends LabelProvider {
	private ArrayList selectedTargets = null;
	private String defaultTargetName = null;
	private TableViewer viewer= null;

	public AntTargetLabelProvider(TableViewer viewer) {
		this.viewer= viewer;
	}
	
	public AntTargetLabelProvider() {
	}
	
	/* (non-Javadoc)
	 * Method declared on ILabelProvider.
	 */
	public String getText(Object model) {
		TargetInfo target = (TargetInfo) model;
		
		if (target != null) {
			StringBuffer result = new StringBuffer(target.getName());
			if (target.getName().equals(defaultTargetName)) {
				result.append(" ("); //$NON-NLS-1$;
				result.append(AntLaunchConfigurationMessages.getString("AntTargetLabelProvider.default_target_1")); //$NON-NLS-1$
				result.append(")"); //$NON-NLS-1$;
			} 
			return result.toString();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Sets the targets selected in the viewer.
	 */
	public void setSelectedTargets(ArrayList value) {
		selectedTargets = value;
	}

	/**
	 * Sets the name of the default target
	 */
	public void setDefaultTargetName(String name) {
		defaultTargetName = name;
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