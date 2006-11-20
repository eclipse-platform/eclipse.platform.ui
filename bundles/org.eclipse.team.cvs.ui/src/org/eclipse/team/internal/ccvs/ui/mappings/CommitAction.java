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
package org.eclipse.team.internal.ccvs.ui.mappings;

import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class CommitAction extends AbstractCommitAction implements IPropertyChangeListener {

	public CommitAction(final ISynchronizePageConfiguration configuration) {
		super(configuration);
		configuration.addPropertyChangeListener(this);
		setId(ICVSUIConstants.CMD_COMMIT);
		setActionDefinitionId(ICVSUIConstants.CMD_COMMIT);
	}
	
	protected String getBundleKeyPrefix() {
		return "WorkspaceCommitAction."; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.ModelProviderAction#isEnabledForSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean isEnabledForSelection(IStructuredSelection selection) {
		return internalIsEnabled(selection);
	}

	private boolean internalIsEnabled(IStructuredSelection selection) {
		// Only enable commit in outgoing or both modes
		int mode = getConfiguration().getMode();
		if (mode == ISynchronizePageConfiguration.OUTGOING_MODE || mode == ISynchronizePageConfiguration.BOTH_MODE) {
			return getResourceMappings(selection).length > 0;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() == getConfiguration() && event.getProperty() == ISynchronizePageConfiguration.P_MODE) {
			setEnabled(internalIsEnabled(getStructuredSelection()));
		}
	}

	protected ResourceTraversal[] getResourceTraversals(IProgressMonitor monitor)
			throws CoreException {
		return getResourceTraversals(getStructuredSelection(), monitor);
	}

}
