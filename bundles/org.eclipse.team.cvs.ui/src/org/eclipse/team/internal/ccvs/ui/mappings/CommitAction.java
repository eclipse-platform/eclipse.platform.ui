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

import java.util.*;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.wizards.CommitWizard;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;

public class CommitAction extends CVSModelProviderAction implements IPropertyChangeListener {

	public CommitAction(final ISynchronizePageConfiguration configuration) {
		super(configuration);
		configuration.addPropertyChangeListener(this);
		configuration.addActionContribution(new SynchronizePageActionGroup() {
			public void dispose() {
				configuration.removePropertyChangeListener(CommitAction.this);
				super.dispose();
			}
		});
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

	private ResourceMapping[] getResourceMappings(IStructuredSelection selection) {
		List mappings = new ArrayList();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			ResourceMapping mapping = Utils.getResourceMapping(element);
			if (mapping != null)
				mappings.add(mapping);
		}
		return (ResourceMapping[]) mappings.toArray(new ResourceMapping[mappings.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() == getConfiguration() && event.getProperty() == ISynchronizePageConfiguration.P_MODE) {
			setEnabled(internalIsEnabled(getStructuredSelection()));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		ResourceMapping[] mappings = getResourceMappings(getStructuredSelection());
        Shell shell= getConfiguration().getSite().getShell();
        try {
        	// Include the subscriber operation as a job listener so that the busy feedback for the 
        	// commit will appear in the synchronize view
            CommitWizard.run(getConfiguration().getSite().getPart(), shell, mappings);
        } catch (CVSException e) {
            CVSUIPlugin.log(e);
        }
	}

}
