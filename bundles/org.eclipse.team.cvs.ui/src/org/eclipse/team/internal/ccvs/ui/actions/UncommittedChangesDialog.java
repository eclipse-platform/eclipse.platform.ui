/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.SyncInfoDirectionFilter;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.dialogs.*;

public abstract class UncommittedChangesDialog extends MappingSelectionDialog {
    
    public static final class UncommittedFilter implements IResourceMappingResourceFilter {
        public boolean select(IResource resource,
                ResourceMapping mapping, ResourceTraversal traversal)
                throws CoreException {
            SyncInfo info = CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().getSyncInfo(resource);
            return (info != null && getResourceFilter().select(info));
        }
    }
    
    public static FastSyncInfoFilter getResourceFilter() {
        // Return a filter that selects outgoing changes
        return new SyncInfoDirectionFilter(new int[] { SyncInfo.OUTGOING, SyncInfo.CONFLICTING });
    }
    
    private final ResourceMapping[] allMappings;
    
    
    public UncommittedChangesDialog(Shell parentShell, String dialogTitle, ResourceMapping[] mappings) {
        super(parentShell, dialogTitle, getMatchingMappings(mappings, CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber(), getResourceFilter()), new UncommittedFilter());
        allMappings = mappings;
    }


	protected String getResourceListMessage(ResourceMapping mapping) {
        if (mapping == null) {
            return Policy.bind("UncommittedChangesDialog.2"); //$NON-NLS-1$
        } else {
            String label = ResourceMappingResourceDisplayArea.getLabel(mapping);
            if (mapping.getModelObject() instanceof IFile) {
                return Policy.bind("UncommittedChangesDialog.4", label); //$NON-NLS-1$
            }
            return Policy.bind("UncommittedChangesDialog.3", label); //$NON-NLS-1$
        }
    }

    /**
     * Prompt for any mappings that match the given filter in order to allow the
     * user to explicitly include/exclude those mappings.
     * @return the mappings that either didn't match the filter or were selected by the user
     */
    public ResourceMapping[] promptToSelectMappings() {
        ResourceMapping[] matchingMappings = getMappings();
        if (matchingMappings.length > 0) {
            int code = open();
            if (code == OK) {
                Set result = new HashSet();
                result.addAll(Arrays.asList(allMappings));
                result.removeAll(Arrays.asList(matchingMappings));
                result.addAll(Arrays.asList(getCheckedMappings()));
                return (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);
            }
            return new ResourceMapping[0];
        } else {
            // No mappings match the filter so return them all
            return allMappings;
        }
    }

    private static ResourceMapping[] getMatchingMappings(ResourceMapping[] mappings, final Subscriber subscriber, final FastSyncInfoFilter resourceFilter) {
        Set result = new HashSet();
        for (int i = 0; i < mappings.length; i++) {
            ResourceMapping mapping = mappings[i];
            if (matchesFilter(mapping, subscriber, resourceFilter)) {
                result.add(mapping);
            }
        }
        return (ResourceMapping[]) result.toArray(new ResourceMapping[result.size()]);
    }

    private static boolean matchesFilter(ResourceMapping mapping, final Subscriber subscriber, final FastSyncInfoFilter resourceFilter) {
        try {
            mapping.accept(null, new IResourceVisitor() {
                public boolean visit(IResource resource) throws CoreException {
                    SyncInfo info = subscriber.getSyncInfo(resource);
                    if (info != null && resourceFilter.select(info)) {
                        throw new CoreException(Status.OK_STATUS);
                    }
                    return true;
                }
            }, null);
        } catch (CoreException e) {
            if (e.getStatus().isOK()) {
                return true;
            }
            CVSUIPlugin.log(e);
        }
        return false;
    }


    public ResourceMapping[] getAllMappings() {
        return allMappings;
    }
    
    protected boolean includeCancelButton() {
        if (super.includeCancelButton()) {
            return getAllMappings().length > 1;
        }
        return false;
    }
    
}
