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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.team.internal.ccvs.ui.CVSLightweightDecorator;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ui.synchronize.sets.*;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.IActionDelegate;

public class CVSSynchronizeViewPage extends TeamSubscriberParticipantPage implements ISyncSetChangedListener {

	private List delegates = new ArrayList(2);

	protected class CVSActionDelegate extends Action {
		private IActionDelegate delegate;

		public CVSActionDelegate(IActionDelegate delegate) {
			this.delegate = delegate;
			addDelegate(this);
		}

		public void run() {
			IStructuredContentProvider cp = (IStructuredContentProvider) getViewer().getContentProvider();
			StructuredSelection selection = new StructuredSelection(cp.getElements(getInput()));
			if (!selection.isEmpty()) {
				delegate.selectionChanged(this, selection);
				delegate.run(this);
			}
		}

		public IActionDelegate getDelegate() {
			return delegate;
		}
	}

	public CVSSynchronizeViewPage(TeamSubscriberParticipant page, ISynchronizeView view, SubscriberInput input) {
		super(page, view, input);
		getInput().getFilteredSyncSet().addSyncSetChangedListener(this);
		CVSUIPlugin.addPropertyChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.sync.AbstractSynchronizeParticipant#dispose()
	 */
	public void dispose() {
		super.dispose();
		getInput().getFilteredSyncSet().removeSyncSetChangedListener(this);
		CVSUIPlugin.removePropertyChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.internal.ui.sync.sets.ISyncSetChangedListener#syncSetChanged(org.eclipse.team.internal.ui.sync.sets.SyncSetChangedEvent)
	 */
	public void syncSetChanged(SyncSetChangedEvent event) {
		StructuredViewer viewer = getViewer();
		if (viewer != null) {
			IStructuredContentProvider cp = (IStructuredContentProvider) viewer.getContentProvider();
			StructuredSelection selection = new StructuredSelection(cp.getElements(getInput()));
			for (Iterator it = delegates.iterator(); it.hasNext(); ) {
				CVSActionDelegate delegate = (CVSActionDelegate) it.next();
				delegate.getDelegate().selectionChanged(delegate, selection);
			}
		}
	}

	private void addDelegate(CVSActionDelegate delagate) {
		delegates.add(delagate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.synchronize.TeamSubscriberParticipantPage#getLabelProvider()
	 */
	protected ILabelProvider getLabelProvider() {
		return new TeamSubscriberParticipantLabelProvider() {
			protected String decorateText(String input, Object resource) {
				if (resource instanceof IResource) {
					CVSLightweightDecorator.Decoration decoration = new CVSLightweightDecorator.Decoration();
					CVSLightweightDecorator.decorateTextLabel((IResource) resource, decoration, false, true);
					StringBuffer output = new StringBuffer(25);
					if(decoration.prefix != null) {
						output.append(decoration.prefix);
					}
					output.append(input);
					if(decoration.suffix != null) {
						output.append(decoration.suffix);
					}
					return output.toString();
				} else {
					return input;
				}
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {		
		super.propertyChange(event);
		String prop = event.getProperty();
		if(prop.equals(CVSUIPlugin.P_DECORATORS_CHANGED)) {
			getViewer().refresh(true /* update labels */);
		}
	}
}