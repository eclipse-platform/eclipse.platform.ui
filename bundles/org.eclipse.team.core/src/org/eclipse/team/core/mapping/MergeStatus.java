package org.eclipse.team.core.mapping;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A special status that is returned when the return code 
 * of the <code>merge</code> method is <code>CONFLICTS</code>.
 * It is possible that there were problems that caused the 
 * auto-merge to fail. In that case, the implementor of
 * <code>IResourceMappingMerger</code> can return a multi-status
 * in which one of the children is a <code>MergeStatus</code> and
 * the others describe other problems that were encountered.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see org.eclipse.team.core.mapping.IResourceMappingMerger
 * 
 * @since 3.2
 */
public class MergeStatus extends Status implements IMergeStatus {
    
    private ResourceMapping[] conflictingMappings;
	private IFile[] conflictingFiles;

    /**
     * Create a merge status for reporting that some of the resource mappings
     * for which a merge was attempted were not auto-mergable.
     * @param pluginId the plugin id
     * @param message the message for the status
     * @param conflictingMappings the mappings which were not auto-mergable
     */
    public MergeStatus(String pluginId, String message, ResourceMapping[] conflictingMappings) {
        super(IStatus.ERROR, pluginId, CONFLICTS, message, null);
        this.conflictingMappings = conflictingMappings;
    }

    /**
     * Create a merge status for reporting that some of the files
     * for which a merge was attempted were not auto-mergable.
     * @param pluginId the plugin id
     * @param message the message for the status
     * @param files the files which were not auto-mergable
     */
    public MergeStatus(String pluginId, String message, IFile[] files) {
        super(IStatus.ERROR, pluginId, CONFLICTS, message, null);
        this.conflictingFiles = files;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IMergeStatus#getConflictingMappings()
	 */
    public ResourceMapping[] getConflictingMappings() {
        return conflictingMappings;
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IMergeStatus#getConflictingFiles()
	 */
    public IFile[] getConflictingFiles() {
    	return conflictingFiles;
    }
}
