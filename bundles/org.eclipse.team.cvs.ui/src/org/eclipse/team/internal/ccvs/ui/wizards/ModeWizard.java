/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.wizards;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;

/**
 * A wizard for changing the keyword substitution mode of files.
 * 
 * 1.  Ask the user select to select the desired keyword substitution mode.
 * 2.  Compute the set of possibly affected resources
 * 3.  If the affected resources include existing committed files, warn the user
 *     and provide an option to include them in the operation anyways.
 * 4.  If the affected resources include dirty files, warn the user and provide
 *     an option to include them in the operation anyways.
 * 5.  Perform the operation on Finish.
 */
public class ModeWizard extends ResizableWizard {
    
    public interface ModeChange extends Comparable {
        
        KSubstOption getMode();
        KSubstOption getNewMode();
        void setNewMode(KSubstOption mode);
        
        boolean hasChanged();
    }
    
    public static class FileModeChange implements ModeChange {
            
        private final IFile fFile;
        private final KSubstOption fMode;

        private KSubstOption fNewMode;
        private final boolean fShared;
        
        public FileModeChange(IFile file, KSubstOption mode, boolean shared) {
            fFile = file;
            fMode= mode;
            fNewMode= mode;
            fShared= shared;
        }
        
        public IFile getFile() {
            return fFile;
        }
        
        public KSubstOption getMode() {
            return fMode;
        }
        
        public KSubstOption getNewMode() {
            return fNewMode;
        }
        
        public boolean hasChanged() {
            return !fMode.equals(fNewMode);
        }
        
        public void setNewMode(KSubstOption mode) {
            fNewMode= mode;
        }

        public boolean isShared() {
            return fShared;
        }

        public int compareTo(Object o) {
            return fFile.getName().compareTo(((FileModeChange)o).getFile().getName());
        }
    }

    private List fChanges;

    public static ModeWizard run(Shell shell, IResource [] resources) {
        final ModeWizard wizard= new ModeWizard(resources);
        open(shell, wizard);
        return wizard;
    }

    /**
	 * Creates a wizard to set the keyword substitution mode for the specified resources.
	 * 
	 * @param resources the resources to alter
	 * @param depth the recursion depth
	 * @param defaultOption the keyword substitution option to select by default
	 */
	protected ModeWizard(IResource[] resources) {
		super("ModeWizard", CVSUIPlugin.getPlugin().getDialogSettings(), 500, 400);
		setWindowTitle("Change the CVS file transfer mode");
        fChanges= getModeChanges(resources);
	}

	public void addPages() {
		addPage(new ModeWizardSelectionPage(fChanges));
    }
	
	/* (Non-javadoc)
	 * Method declared on IWizard.
	 */
	public boolean needsProgressMonitor() {
		return true;
	}

    private List getModeChanges(IResource [] resources) {
        
        final ArrayList changes= new ArrayList();
        
        final HashSet visited= new HashSet();
        
        for (int i = 0; i < resources.length; i++) {
            final IResource currentResource = resources[i];
            try {
                currentResource.accept(new IResourceVisitor() {
                    public boolean visit(IResource resource) throws CoreException {
                        try {
                            if (resource.getType() != IResource.FILE || !resource.exists() || visited.contains(resource)) 
                                return true;
                            visited.add(resource);
                            IFile file = (IFile) resource;
                            ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(file);
                            if (!cvsFile.isManaged()) 
                                return true;
                            final ResourceSyncInfo info = cvsFile.getSyncInfo();
                            final KSubstOption mode = info.getKeywordMode();
                            
                            changes.add(new FileModeChange(file, mode, !info.isAdded()));

                        } catch (TeamException e) {
                            throw new CoreException(e.getStatus());
                        }
                        // always return true and let the depth determine if children are visited
                        return true;
                    }
                }, IResource.DEPTH_INFINITE, false); 
            } catch (CoreException e) {
                CVSUIPlugin.openError(getShell(), "An error occurred", null, e);
            }
        }
        return changes;
    }
}
