/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;

public class Utilities {
	
	/**
	 * Initialize the given Action from a ResourceBundle.
	 */
	public static void initAction(IAction a, String prefix, ResourceBundle bundle) {
		
		String labelKey= "label"; //$NON-NLS-1$
		String tooltipKey= "tooltip"; //$NON-NLS-1$
		String imageKey= "image"; //$NON-NLS-1$
		String descriptionKey= "description"; //$NON-NLS-1$
		
		if (prefix != null && prefix.length() > 0) {
			labelKey= prefix + labelKey;
			tooltipKey= prefix + tooltipKey;
			imageKey= prefix + imageKey;
			descriptionKey= prefix + descriptionKey;
		}
	
		String s = Policy.bind(labelKey, bundle);
		if(s != null)
			a.setText(s);
		s = Policy.bind(tooltipKey, bundle);
		if(s != null)
			a.setToolTipText(s);
		s = Policy.bind(descriptionKey, bundle);
		if(s != null)
			a.setDescription(s);
	
		String relPath= Policy.bind(imageKey, bundle);
		if (relPath != null && ! relPath.equals(imageKey) && relPath.trim().length() > 0) {
		
			String cPath;
			String dPath;
			String ePath;
		
			if (relPath.indexOf("/") >= 0) { //$NON-NLS-1$
				String path= relPath.substring(1);
				cPath= 'c' + path;
				dPath= 'd' + path;
				ePath= 'e' + path;
			} else {
				cPath= "clcl16/" + relPath; //$NON-NLS-1$
				dPath= "dlcl16/" + relPath; //$NON-NLS-1$
				ePath= "elcl16/" + relPath; //$NON-NLS-1$
			}
		
			ImageDescriptor id= TeamImages.getImageDescriptor(dPath);	// we set the disabled image first (see PR 1GDDE87)
			if (id != null)
				a.setDisabledImageDescriptor(id);
			id= TeamUIPlugin.getImageDescriptor(cPath);
			if (id != null)
				a.setHoverImageDescriptor(id);
			id= TeamUIPlugin.getImageDescriptor(ePath);
			if (id != null)
				a.setImageDescriptor(id);
		}
	}
}
