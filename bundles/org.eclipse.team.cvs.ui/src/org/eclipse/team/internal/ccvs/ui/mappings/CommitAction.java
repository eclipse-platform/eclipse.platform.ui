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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.wizards.CommitWizard;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.PlatformUI;

public class CommitAction extends CVSModelProviderAction implements IPropertyChangeListener {

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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void execute() {
    	final List resources = new ArrayList();
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						ResourceTraversal[] traversals = getResourceTraversals(getStructuredSelection(), monitor);
						IDiff[] diffs = getSynchronizationContext().getDiffTree().getDiffs(traversals);
						for (int i = 0; i < diffs.length; i++) {
							IDiff diff = diffs[i];
							if (hasLocalChange(diff)) {
								IResource resource = ResourceDiffTree.getResourceFor(diff);
								if (resource != null)
									resources.add(resource);
							}
						}
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}

				private boolean hasLocalChange(IDiff diff) {
					if (diff instanceof IThreeWayDiff) {
						IThreeWayDiff twd = (IThreeWayDiff) diff;
						return twd.getDirection() == IThreeWayDiff.OUTGOING 
							|| twd.getDirection() ==  IThreeWayDiff.CONFLICTING;
					}
					return false;
				}
			});
		} catch (InvocationTargetException e) {
			Utils.handleError(getConfiguration().getSite().getShell(), e, null, null);
		} catch (InterruptedException e) {
			// Ignore
		}
		if (!resources.isEmpty()) {
	        Shell shell= getConfiguration().getSite().getShell();
	        try {
	            CommitWizard.run(getConfiguration().getSite().getPart(), shell, (IResource[]) resources.toArray(new IResource[resources.size()]));
	        } catch (CVSException e) {
	            CVSUIPlugin.log(e);
	        }
		}
	}

}
