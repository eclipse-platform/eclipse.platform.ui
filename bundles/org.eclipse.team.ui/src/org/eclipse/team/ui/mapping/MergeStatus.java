package org.eclipse.team.ui.mapping;

import org.eclipse.compare.IStreamMerger;
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
 * @see org.eclipse.team.ui.mapping.IResourceMappingMerger
 * 
 * @since 3.2
 */
public final class MergeStatus extends Status {
    
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

	/**
     * Indicates that a change conflict prevented some or all of the resource
     * mappings to be merged (value <code>1</code>). When this code is
     * returned, the status must be of type
     * <code>MergeStatus</code> and must contain the list of all
     * resource mappings for which a manual merge is required.
     */
    public static final int CONFLICTS = IStreamMerger.CONFLICT;
    
    /**
     * Status code describing an internal error (value <code>2</code>).
     * The status return is not required to be of type <code>MergeStatus</code>
     * for internal errors.
     */
    public static final int INTERNAL_ERROR= IStreamMerger.INTERNAL_ERROR;
    
    /**
     * Returns the set of resource mappings for which an auto-merge was
     * not performed. The client should present the mappings to the user
     * in a manner that will allow the user to perform a manual merges.
     * @return the set of resource mappings for which an auto-merge was
     * not performed.
     */
    public ResourceMapping[] getConflictingMappings() {
        return conflictingMappings;
    }
    
    public IFile[] getConflictingFiles() {
    	return conflictingFiles;
    }
}
