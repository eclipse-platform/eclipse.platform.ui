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
package org.eclipse.team.internal.ui.jobs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class JobsLabelProvider extends LabelProvider implements IColorProvider {

	public Image getImage(Object element) {
		return super.getImage(element);
	}

	public String getText(Object element) {
		if(element instanceof Job) {
			return ((Job)element).getName();
		}
		if(element instanceof JobStateCategory) {
			return ((JobStateCategory)element).name;
		}
		if(element instanceof JobDoneElement) {
			JobDoneElement done = (JobDoneElement)element;
			return done.job.getName() + " (" + done.status.toString() + ")";
		}
		if(element instanceof String) {
			return (String)element;
		}
		return "unkown element";
	}

	public Color getForeground(Object element) {
		if(element instanceof JobDoneElement) {
			IStatus done = ((JobDoneElement)element).status;
			if(done.getSeverity() == IStatus.CANCEL) {
				return Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);				
			}
			if(done.getSeverity() == IStatus.ERROR) {
				return Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);
			}
		}
		// use default color
		return null;
	}

	public Color getBackground(Object element) {
		// use default color
		return null;
	}
}
