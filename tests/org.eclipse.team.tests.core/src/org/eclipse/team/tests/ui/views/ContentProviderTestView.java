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
package org.eclipse.team.tests.ui.views;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.SyncInfoSet;
import org.eclipse.team.internal.ui.synchronize.views.CompressedFolderContentProvider;
import org.eclipse.team.ui.synchronize.SyncInfoDiffNode;
import org.eclipse.team.ui.synchronize.TeamSubscriberParticipantLabelProvider;
import org.eclipse.ui.part.ViewPart;

public class ContentProviderTestView extends ViewPart {
	
	public static final String ID = "org.eclipse.team.tests.ui.views.ContentProviderTestView";
	
	private TestTreeViewer viewer;

	public ContentProviderTestView() {
	}

	public void createPartControl(Composite parent) {
		viewer = new TestTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new CompressedFolderContentProvider());
		viewer.setLabelProvider(new TeamSubscriberParticipantLabelProvider());
		setInput(new SyncInfoSet(new SyncInfo[0]));
	}

	public void setInput(SyncInfoSet set) {
		viewer.setInput(new SyncInfoDiffNode(set, ResourcesPlugin.getWorkspace().getRoot()));
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}
}