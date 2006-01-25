/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.ui.mapping.ISynchronizationConstants;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class DiffTreeChangesSection extends ForwardingChangesSection implements IDiffChangeListener, IPropertyChangeListener {

	private ISynchronizationContext context;

	public DiffTreeChangesSection(Composite parent, AbstractSynchronizePage page, ISynchronizePageConfiguration configuration) {
		super(parent, page, configuration);
		context = (ISynchronizationContext)configuration.getProperty(ISynchronizationConstants.P_SYNCHRONIZATION_CONTEXT);
		context.getDiffTree().addDiffChangeListener(this);
		getConfiguration().addPropertyChangeListener(this);
	}

	public void dispose() {
		context.getDiffTree().removeDiffChangeListener(this);
		getConfiguration().removePropertyChangeListener(this);
		super.dispose();
	}
	
	protected int getChangesCount() {
		return context.getDiffTree().size();
	}

	protected long getChangesInMode(int candidateMode) {
		long numChanges;
		switch (candidateMode) {
		case ISynchronizePageConfiguration.OUTGOING_MODE:
			numChanges = context.getDiffTree().countFor(IThreeWayDiff.OUTGOING, IThreeWayDiff.DIRECTION_MASK);
			break;
		case ISynchronizePageConfiguration.INCOMING_MODE:
			numChanges = context.getDiffTree().countFor(IThreeWayDiff.INCOMING, IThreeWayDiff.DIRECTION_MASK);
			break;
		case ISynchronizePageConfiguration.BOTH_MODE:
			numChanges = context.getDiffTree().countFor(IThreeWayDiff.INCOMING, IThreeWayDiff.DIRECTION_MASK) 
				+ context.getDiffTree().countFor(IThreeWayDiff.OUTGOING, IThreeWayDiff.DIRECTION_MASK);
			break;
		default:
			numChanges = 0;
			break;
		}
		return numChanges;
	}

	protected long getVisibleChangesCount() {
		int currentMode =  getConfiguration().getMode();
		return getChangesInMode(currentMode);
	}

	protected int getCandidateMode() {
		SynchronizePageConfiguration configuration = (SynchronizePageConfiguration)getConfiguration();
		long outgoingChanges = context.getDiffTree().countFor(IThreeWayDiff.OUTGOING, IThreeWayDiff.DIRECTION_MASK);
		if (outgoingChanges > 0) {
			if (configuration.isModeSupported(ISynchronizePageConfiguration.OUTGOING_MODE)) {
				return ISynchronizePageConfiguration.OUTGOING_MODE;
			}
			if (configuration.isModeSupported(ISynchronizePageConfiguration.BOTH_MODE)) {
				return ISynchronizePageConfiguration.BOTH_MODE;
			}
		}
		long incomingChanges = context.getDiffTree().countFor(IThreeWayDiff.INCOMING, IThreeWayDiff.DIRECTION_MASK);
		if (incomingChanges > 0) {
			if (configuration.isModeSupported(ISynchronizePageConfiguration.INCOMING_MODE)) {
				return ISynchronizePageConfiguration.INCOMING_MODE;
			}
			if (configuration.isModeSupported(ISynchronizePageConfiguration.BOTH_MODE)) {
				return ISynchronizePageConfiguration.BOTH_MODE;
			}
		}
		return configuration.getMode();
	}

	public void diffChanged(IDiffChangeEvent event, IProgressMonitor monitor) {
		calculateDescription();
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(ISynchronizePageConfiguration.P_MODE)) {
			calculateDescription();
		}
	}

}
