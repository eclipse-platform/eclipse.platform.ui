package org.eclipse.toolscript.ui.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.ArrayList;

import org.apache.tools.ant.Target;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Ant script target label provider
 */
public class AntTargetLabelProvider extends LabelProvider {
	private ArrayList selectedTargets = null;
	private String defaultTargetName = null;

	/* (non-Javadoc)
	 * Method declared on ILabelProvider.
	 */
	public String getText(Object model) {
		Target targetToDisplay = (Target) model;
		if (targetToDisplay != null) {
			StringBuffer result = new StringBuffer(targetToDisplay.getName());
			if (targetToDisplay.getName().equals(defaultTargetName)) {
				result.append(" ("); //$NON-NLS-1$;
				result.append(ToolScriptMessages.getString("AntTargetLabelProvider.defaultTarget")); //$NON-NLS-1$;
				result.append(")"); //$NON-NLS-1$;
			}
			if (selectedTargets != null) {
				int targetIndex = selectedTargets.indexOf(targetToDisplay);
				if (targetIndex >= 0) {
					result.append(" ["); //$NON-NLS-1$;
					result.append(targetIndex + 1);
					result.append("]"); //$NON-NLS-1$;
				}
			}
			return result.toString();
		} else {
			return "";
		}
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

}