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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import java.util.*;

import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.jface.action.*;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.team.ui.synchronize.subscriber.*;
import org.eclipse.team.ui.synchronize.viewers.ISynchronizeModelChangeListener;
import org.eclipse.team.ui.synchronize.viewers.SynchronizeModelElement;
import org.eclipse.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class CVSSynchronizeViewPage extends SubscriberParticipantPage implements ISynchronizeModelChangeListener {
	
	private List delegates = new ArrayList(2);
	private CVSSynchronizeViewerAdvisor config;
	private Action groupByComment;

	protected class CVSActionDelegate extends Action {
		private IActionDelegate delegate;

		public CVSActionDelegate(IActionDelegate delegate) {
			this.delegate = delegate;
			// Associate delegate with the synchronize view, this will allow
			if(delegate instanceof IViewActionDelegate) {
				((IViewActionDelegate)delegate).init(getSynchronizeView());
			}
			addDelegate(this);
		}

		public void run() {
			StructuredViewer viewer = (StructuredViewer)getViewer();
			if (viewer != null) {
				ISelection selection = new StructuredSelection(viewer.getInput());		
				if (!selection.isEmpty()) {
					delegate.selectionChanged(this, selection);
					delegate.run(this);
				}
			}
		}

		public IActionDelegate getDelegate() {
			return delegate;
		}
	}

	public CVSSynchronizeViewPage(SubscriberParticipant participant, ISynchronizeView view) {
		super(participant, view);
		groupByComment = new Action(Policy.bind("CVSSynchronizeViewPage.0"), Action.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				config.setGroupIncomingByComment(!config.isGroupIncomingByComment());
				setChecked(config.isGroupIncomingByComment());
			}
		};
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.sync.AbstractSynchronizeParticipant#dispose()
	 */
	public void dispose() {
		super.dispose();
		getViewerAdviser().removeInputChangedListener(this);
		CVSUIPlugin.removePropertyChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SubscriberParticipantPage#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void setActionBars(IActionBars actionBars) {
		super.setActionBars(actionBars);
		IMenuManager mgr = actionBars.getMenuManager();
		mgr.add(new Separator());
		//mgr.add(groupByComment);
	}

	/*
	 * Update the enablement of any action delegates 
	 */
	private void updateActionEnablement(DiffNode input) {
		ISelection selection = new StructuredSelection(input);
		for (Iterator it = delegates.iterator(); it.hasNext(); ) {
			CVSActionDelegate delegate = (CVSActionDelegate) it.next();
			delegate.getDelegate().selectionChanged(delegate, selection);
		}
	}

	private void addDelegate(CVSActionDelegate delagate) {
		delegates.add(delagate);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {		
		super.propertyChange(event);
		String prop = event.getProperty();
		if(prop.equals(CVSUIPlugin.P_DECORATORS_CHANGED) && getViewer() != null && getSyncInfoSet() != null) {
			((StructuredViewer)getViewer()).refresh(true /* update labels */);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		// Sync changes are used to update the action state for the update/commit buttons.
		getViewerAdviser().addInputChangedListener(this);
		
		// Listen for decorator changed to refresh the viewer's labels.
		CVSUIPlugin.addPropertyChangeListener(this);
		
		updateActionEnablement((DiffNode)getViewer().getInput());
	}
	
	private SyncInfoTree getSyncInfoSet() {
		return getParticipant().getSubscriberSyncInfoCollector().getSyncInfoTree();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SubscriberParticipantPage#createSyncInfoSetCompareConfiguration()
	 */
	protected SynchronizeViewerAdvisor createSynchronizeViewerAdvisor() {
		if(config == null) {
			config = new CVSSynchronizeViewerAdvisor(getSynchronizeView(), getParticipant());
		}
		return config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.presentation.ISynchronizeModelChangeListener#inputChanged(org.eclipse.team.ui.synchronize.presentation.SynchronizeModelProvider)
	 */
	public void modelChanged(SynchronizeModelElement root) {
		updateActionEnablement(root);
	}
}