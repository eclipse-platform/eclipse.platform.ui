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
package org.eclipse.ui.internal.progress;

import java.util.ArrayList;


/**
 * The CondensedProgressContentProvider is the content provider
 * that shows less info.
 */
public class CondensedProgressContentProvider extends ProgressContentProvider {
	
	/**
	 * Create a new instance of the receiver.
	 * @param mainViewer
	 */
	public CondensedProgressContentProvider(ProgressTreeViewer mainViewer) {
		super(mainViewer);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.ProgressContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		//Only get the TaskInfos
		JobInfo[] infos =  ProgressManager.getInstance().getJobInfos(false);
		ArrayList taskInfos = new ArrayList();
		for (int i = 0; i < infos.length; i++) {
			JobInfo info = infos[i];
			if(info.hasTaskInfo())
				taskInfos.add(info.getTaskInfo());
		}
		return taskInfos.toArray();
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.ProgressContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		return null;
	}

}
