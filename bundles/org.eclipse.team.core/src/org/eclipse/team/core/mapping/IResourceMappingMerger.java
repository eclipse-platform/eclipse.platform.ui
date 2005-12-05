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
package org.eclipse.team.core.mapping;

import org.eclipse.core.runtime.*;

/**
 * The purpose of this interface is to provide support for model level
 * auto-merging. It is helpful in the cases where a file may contain multiple
 * model elements or a model element consists of multiple files. It can also be
 * used for cases where there is a one-to-one mapping between model elements and
 * files, although <code>IStreamMerger</code> can also be used in that case.
 * 
 * Clients should group resource mappings by model provider and then attempt to
 * obtain a merger from the model provider using the adaptable mechanism as
 * follows:
 * 
 * <pre>
 *      Object o = mapping.getModelProvider().getAdapter(IResourceMappingMerger.class);
 *      if (o instanceof IResourceMappingMerger.class) {
 *         IResourceMappingMerger merger = (IResourceMappingMerger)o;
 *         ...
 *      }
 * </pre>
 * 
 * <p>
 * Clients are not expected to implement this interface but should subclass
 * {@link ResourceMappingMerger} instead.
 * 
 * @see ResourceMappingMerger
 * @see org.eclipse.compare.IStreamMerger
 * @see org.eclipse.core.resources.mapping.ResourceMapping
 * @see org.eclipse.core.resources.mapping.ModelProvider
 * @see org.eclipse.team.core.mapping.IMergeContext
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public interface IResourceMappingMerger {

    /**
	 * Attempt to automatically merge the mappings of the merge context(<code>MergeContext#getMappings()</code>).
	 * The merge context provides access to the out-of-sync resources (<code>MergeContext#getSyncInfoTree()</code>)
	 * associated with the mappings to be merged. The set of provided mappings
	 * may come from multiple model providers. A particular implementation of
	 * this interface should only merge the mappings associated with their model
	 * provider. Also, the set of resources may contain additional resources
	 * that are not part of the mappings being merged. Implementors of this
	 * interface should use the mappings to determine which resources to merge
	 * and what additional semantics can be used to attempt the merge.
	 * <p>
	 * The type of merge to be performed depends on what is returned by the
	 * <code>MergeContext#getType()</code> method. If the type is
	 * <code>MergeContext.TWO_WAY</code> the merge will replace the local
	 * contents with the remote contents, ignoring any local changes. For
	 * <code>THREE_WAY</code>, the base is used to attempt to merge remote
	 * changes with local changes.
	 * <p>
	 * If merging was not possible for one or more of the mappings to which this
	 * merge applies, these mappings should be returned in an
	 * <code>MergeStatus</code> whose code is
	 * <code>MergeStatus.CONFLICTS</code> and which provides access to the
	 * mappings which could not be merged. Note that it is up to the model to
	 * decide whether it wants to break one of the provided resource mappings
	 * into several sub-mappings and attempt auto-merging at that level.
	 * 
	 * @param mappings the set of resource mappings being merged
	 * @param mergeContext a context that provides access to the resources
	 *            involved in the merge. The context must not be
	 *            <code>null</code>.
	 * @param monitor a progress monitor
	 * @return a status indicating the results of the operation. A code of
	 *         <code>MergeStatus.CONFLICTS</code> indicates that some or all
	 *         of the resource mappings could not be merged. The mappings that
	 *         were not merged are available using
	 *         <code>MergeStatus#getConflictingMappings()</code>
	 * @throws CoreException if errors occurred
	 */
    public IStatus merge(IMergeContext mergeContext,
            IProgressMonitor monitor) throws CoreException;

}
