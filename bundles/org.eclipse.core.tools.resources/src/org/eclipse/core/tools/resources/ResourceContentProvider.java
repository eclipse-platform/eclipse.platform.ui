/**********************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.resources;

import java.util.*;
import org.eclipse.core.internal.properties.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tools.*;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.Viewer;

/**
 * Constructs a tree made of <code>TreeContentProviderNode</code>, representing
 * several details about resources. 
 * Details include:
 * <ul>
 *   <li>resource's full path;</li>
 *   <li>content ID;</li>
 *   <li>node ID;</li>  
 *   <li>resource type;</li>
 *   <li>local sync info;</li>
 *   <li>resource info flags;</li>
 *   <li>markers' type and attributes;</li>
 *   <li>sync info;</li>
 *   <li>session properties;</li>
 *   <li>persistent properties.</li> 
 * </ul>
 * 
 * @see org.eclipse.core.tools.TreeContentProviderNode
 */
public class ResourceContentProvider extends AbstractTreeContentProvider {

	/**
	 * The maximum number of bytes to be shown for sync info  
	 */
	private final static int MAX_SYNC_INFO_BYTES = 8;

	/**
	 * Collects resource info. Calls all other <code>extract...</code> methods. 
	 *
	 * @param selectedResource the resource object from where to extract information
	 */
	protected void extractInfo(IResource selectedResource) {

		if (!(selectedResource instanceof Resource)) {
			String message = Policy.bind("resource.error_unknown_resource_impl", selectedResource.getClass().getName()); //$NON-NLS-1$
			getRootNode().addChild(message);
			return;
		}

		Resource resource = (Resource) selectedResource;

		ResourceInfo info = resource.getResourceInfo(true, false);
		// Resource#getResourceInfo may return null when the resource 
		// does not exist anymore. In this case, we just ignore it.
		if (info == null)
			return;

		extractBasicInfo(resource, info);
		extractFlags(info);
		extractContentDescription(resource);
		extractMarkersInfo(resource, info);
		extractSyncInfo(info);
		extractPersistentProperties(resource);
		extractSessionProperties(resource);

	}

	/**
	 * Collects persistent properties information.
	 * 
	 * @param resource the resource object from where to extract information
	 */
	protected void extractPersistentProperties(IResource resource) {
		try {

			PropertyStore store = org.eclipse.core.internal.properties.SpySupport.getPropertyStore(resource);
			if (store == null)
				return;

			// retrieves the properties for the selected resource
			IPath path = resource.getProjectRelativePath();
			ResourceName resourceName = new ResourceName("", path); //$NON-NLS-1$
			QueryResults results = store.getAll(resourceName, 1);
			List projectProperties = results.getResults(resourceName);
			int listSize = projectProperties.size();
			if (listSize == 0)
				return;

			// creates a node for persistent properties and populates it
			TreeContentProviderNode propertiesRootNode = createNode(Policy.bind("resource.persistent_properties")); //$NON-NLS-1$
			getRootNode().addChild(propertiesRootNode);

			for (int i = 0; i < listSize; i++) {
				StoredProperty prop = (StoredProperty) projectProperties.get(i);
				propertiesRootNode.addChild(createNode(prop.getName().toString(), prop.getStringValue()));
			}

		} catch (CoreException ce) {
			getRootNode().addChild(createNode(Policy.bind("resources.error_stored_properties", ce.toString()))); //$NON-NLS-1$
		}
	}

	/**
	 * Collects session properties information.
	 * 
	 * @param resource the resource object from where to extract information
	 */
	protected void extractSessionProperties(IResource resource) {

		// retrieves all session properties for the selected resource 
		Map properties = org.eclipse.core.internal.resources.SpySupport.getSessionProperties(resource);
		if (properties == null || properties.size() == 0)
			return;

		// creates a node for session properties and populates it
		TreeContentProviderNode propertiesRootNode = createNode(Policy.bind("resource.session_properties")); //$NON-NLS-1$
		getRootNode().addChild(propertiesRootNode);

		Set set = properties.entrySet();
		for (Iterator propertiesIter = set.iterator(); propertiesIter.hasNext();) {
			Map.Entry entry = (Map.Entry) propertiesIter.next();
			propertiesRootNode.addChild(createNode(entry.getKey().toString(), entry.getValue()));
		}

	}

	/**
	 * Extracts basic resource info.
	 * 
	 * @param resource the resource object from where to extract information
	 * @param info the resource info object from where to extract information
	 */
	protected void extractBasicInfo(IResource resource, ResourceInfo info) {

		// extracts information from IResource
		IPath resourcePath = resource.getFullPath();
		long id = info.getContentId();

		String type = null;
		switch (resource.getType()) {
			case IResource.FILE :
				type = Policy.bind("resource.file"); //$NON-NLS-1$
				break;
			case IResource.FOLDER :
				type = Policy.bind("resource.folder"); //$NON-NLS-1$
				break;
			case IResource.PROJECT :
				type = Policy.bind("resource.project"); //$NON-NLS-1$
				break;
			case IResource.ROOT :
				type = Policy.bind("resource.root"); //$NON-NLS-1$
				break;
		}

		// extracts information from ResourceInfo
		long localSyncInfo = info.getLocalSyncInfo();
		long nodeId = info.getNodeId();

		// creates a root node for each basic information
		getRootNode().addChild(createNode(Policy.bind("resource.full_path"), resourcePath)); //$NON-NLS-1$
		getRootNode().addChild(createNode(Policy.bind("resource.content_id"), Long.toString(id))); //$NON-NLS-1$
		getRootNode().addChild(createNode(Policy.bind("resource.type"), type)); //$NON-NLS-1$
		getRootNode().addChild(createNode(Policy.bind("resource.node_id"), Long.toString(nodeId))); //$NON-NLS-1$
		getRootNode().addChild(createNode(Policy.bind("resource.local_sync_info"), Long.toString(localSyncInfo))); //$NON-NLS-1$
	}

	/**
	 * Extracts flags from the resource info object, building a 'Flags' 
	 * subtree in the resource details' tree.
	 * 
	 * @param info the resource info object from where to extract information
	 */

	protected void extractFlags(ResourceInfo info) {
		// extract flags'values for the resource info object 
		boolean isOpen = info.isSet(ICoreConstants.M_OPEN);
		boolean localExists = info.isSet(ICoreConstants.M_LOCAL_EXISTS);
		boolean isPhantom = info.isSet(ICoreConstants.M_PHANTOM);
		boolean isUsed = info.isSet(ICoreConstants.M_USED);
		boolean isDerived = info.isSet(ICoreConstants.M_DERIVED);
		boolean isTeamPrivateMember = info.isSet(ICoreConstants.M_TEAM_PRIVATE_MEMBER);
		boolean isMarkersSnapDirty = info.isSet(ICoreConstants.M_MARKERS_SNAP_DIRTY);
		boolean isSyncInfoSnapDirty = info.isSet(ICoreConstants.M_SYNCINFO_SNAP_DIRTY);
		boolean noContentDescription = info.isSet(ICoreConstants.M_NO_CONTENT_DESCRIPTION);
		boolean defaultContentDescription = info.isSet(ICoreConstants.M_DEFAULT_CONTENT_DESCRIPTION);

		// creates a node for flags
		TreeContentProviderNode flagsParentNode = createNode(Policy.bind("resource.flags")); //$NON-NLS-1$
		getRootNode().addChild(flagsParentNode);

		// creates a child node in "Flags" node for each basic information
		flagsParentNode.addChild(createNode(Policy.bind("resource.open"), Boolean.valueOf(isOpen))); //$NON-NLS-1$
		flagsParentNode.addChild(createNode(Policy.bind("resource.local_exists"), Boolean.valueOf(localExists))); //$NON-NLS-1$
		flagsParentNode.addChild(createNode(Policy.bind("resource.phantom"), Boolean.valueOf(isPhantom))); //$NON-NLS-1$
		flagsParentNode.addChild(createNode(Policy.bind("resource.used"), Boolean.valueOf(isUsed))); //$NON-NLS-1$
		flagsParentNode.addChild(createNode(Policy.bind("resource.derived"), Boolean.valueOf(isDerived))); //$NON-NLS-1$
		flagsParentNode.addChild(createNode(Policy.bind("resource.team_private"), Boolean.valueOf(isTeamPrivateMember))); //$NON-NLS-1$
		flagsParentNode.addChild(createNode(Policy.bind("resource.markers_snap_dirty"), Boolean.valueOf(isMarkersSnapDirty))); //$NON-NLS-1$
		flagsParentNode.addChild(createNode(Policy.bind("resource.sync_info_snap_dirty"), Boolean.valueOf(isSyncInfoSnapDirty))); //$NON-NLS-1$
		flagsParentNode.addChild(createNode(Policy.bind("resource.no_content_description"), Boolean.valueOf(noContentDescription))); //$NON-NLS-1$
		flagsParentNode.addChild(createNode(Policy.bind("resource.default_content_description"), Boolean.valueOf(defaultContentDescription))); //$NON-NLS-1$

	}

	protected void extractContentDescription(Resource resource) {
		if (resource.getType() != IResource.FILE)
			return;
		File file = (File) resource;
		// creates a node for flags
		try {
			boolean cached = org.eclipse.core.internal.resources.SpySupport.isContentDescriptionCached(file);
			String description = file.getContentDescription() + " (" + Policy.bind("resource.content_description_from_cache", Boolean.toString(cached)) + ")"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			TreeContentProviderNode contentDescriptionNode = createNode(Policy.bind("resource.content_description"), description); //$NON-NLS-1$
			getRootNode().addChild(contentDescriptionNode);
		} catch (CoreException ce) {
			// in the case of a CoreException, just present it as node
			getRootNode().addChild(createNode(Policy.bind("resource.error_content_description", ce.toString()))); //$NON-NLS-1$
		}

	}

	/**
	 * Extracts from resource info object all information regarding markers,
	 * building a 'Markers' subtree in the resource details tree.
	 * 
	 * @param resource the resource object from where to extract information
	 * @param info the resource info object from where to extract information
	 */
	protected void extractMarkersInfo(IResource resource, ResourceInfo info) {
		MarkerSet markerSet = info.getMarkers();
		if (markerSet == null)
			return;

		IMarkerSetElement[] markerSetElements = markerSet.elements();
		if (markerSetElements.length == 0)
			return;

		// creates a root node for all markers
		TreeContentProviderNode markersParentNode = createNode(Policy.bind("resource.markers")); //$NON-NLS-1$
		getRootNode().addChild(markersParentNode);

		int markerSetSize = markerSetElements.length;
		for (int i = 0; i < markerSetSize; i++) {
			try {
				long markerID = markerSetElements[i].getId();
				IMarker marker = resource.getMarker(markerID);

				// creates a marker node 
				String id = " [id = " + marker.getId() + "]"; //$NON-NLS-1$//$NON-NLS-2$
				TreeContentProviderNode markerNode = createNode(marker.getType() + id);
				markersParentNode.addChild(markerNode);
				extractMarkerAttributes(markerNode, marker);

			} catch (CoreException ce) {
				// in the case of a CoreException, just present it as node
				markersParentNode.addChild(createNode(Policy.bind("resource.error_marker", ce.toString()))); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Retrieves all attributes from a marker, adding them as sub-nodes in the 
	 * marker's node. 
	 * 
	 * @param markerNode the marker's node
	 * @param marker the marker object
	 * @throws CoreException 
	 */
	protected void extractMarkerAttributes(TreeContentProviderNode markerNode, IMarker marker) throws CoreException {

		Map attributes = marker.getAttributes();
		if (attributes == null || attributes.size() == 0)
			return;

		// create a node (under markerNode) for each attribute found
		Set entrySet = attributes.entrySet();
		for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
			Map.Entry mapEntry = (Map.Entry) iterator.next();
			String attributeName = (String) mapEntry.getKey();
			Object attributeValue = mapEntry.getValue();
			// adds each attribute as a subnode under 
			// this marker's node
			markerNode.addChild(createNode(attributeName, attributeValue));
		}
		// attributes will be ordered by attribute name
		markerNode.sort();
	}

	/**
	 * Extracts from the resource info object all information related to VCM,
	 * building a 'Sync Info' subtree in the resource details tree.
	 * 
	 * @param info the resource info object from where to extract information
	 */
	protected void extractSyncInfo(ResourceInfo info) {
		Map syncInfo = info.getSyncInfo(true);
		if (syncInfo == null || syncInfo.size() == 0)
			return;

		// creates a root node for all sync info		
		TreeContentProviderNode syncInfoParentNode = createNode(Policy.bind("resource.sync_info")); //$NON-NLS-1$
		getRootNode().addChild(syncInfoParentNode);

		Set entrySet = syncInfo.entrySet();
		for (Iterator syncInfoIterator = entrySet.iterator(); syncInfoIterator.hasNext();) {
			Map.Entry entry = (Map.Entry) syncInfoIterator.next();
			// creates a sync info node	
			String name = entry.getKey().toString();
			String value = ByteUtil.byteArrayToString((byte[]) entry.getValue(), MAX_SYNC_INFO_BYTES);
			syncInfoParentNode.addChild(createNode(name, value));
		}

	}

	/**
	 * Reconstructs this content provider data model upon the provided input object.
	 *  
	 * @param input the new input object - must not be null
	 */
	protected void rebuild(Viewer viewer, final Object input) {
		Platform.run(new SafeRunnable() {
			public void run() throws Exception {
				extractInfo((IResource) input);
			}
		});
	}

	/**
	 * Returns true if the input is a resource.
	 * 
	 * @param input an input object
	 * @see org.eclipse.core.tools.AbstractTreeContentProvider#acceptInput(java.lang.Object)
	 */
	protected boolean acceptInput(Object input) {
		return input instanceof IResource;
	}

}