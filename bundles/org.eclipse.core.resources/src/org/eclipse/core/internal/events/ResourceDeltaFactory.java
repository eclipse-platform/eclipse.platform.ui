package org.eclipse.core.internal.events;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.dtree.*;
import org.eclipse.core.internal.events.ResourceComparator;
import org.eclipse.core.internal.events.ResourceDelta;
import org.eclipse.core.internal.events.ResourceDeltaInfo;
import org.eclipse.core.internal.resources.ResourceInfo;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.watson.ElementTree;
import java.util.*;

public class ResourceDeltaFactory {
public static ResourceDelta computeDelta(Workspace workspace, ElementTree oldTree, ElementTree newTree, IPath root, boolean notification) {
	ResourceComparator comparator = ResourceComparator.getComparator(notification);
	newTree.immutable();
	DeltaDataTree delta = null;
	if (Path.ROOT.equals(root))
		delta = oldTree.getDataTree().compareWith(newTree.getDataTree(), comparator);
	else
		delta = oldTree.getDataTree().compareWith(newTree.getDataTree(), comparator, root);

	IPath pathInTree = root;
	IPath pathInDelta = Path.ROOT;

	Hashtable oldNodeIDMap = new Hashtable(11);
	Hashtable newNodeIDMap = new Hashtable(11);
	computeNodeIDMaps(delta, oldNodeIDMap, newNodeIDMap, pathInTree, pathInDelta);

	// get the marker deltas for the delta info object....if needed
	Map allMarkerDeltas = null;
	if (notification)
		allMarkerDeltas = workspace.getMarkerManager().getMarkerDeltas();
	ResourceDeltaInfo deltaInfo = new ResourceDeltaInfo(workspace, allMarkerDeltas, comparator);
	deltaInfo.setNodeMaps(oldNodeIDMap, newNodeIDMap);

	ResourceDelta result = createDelta(workspace, delta, deltaInfo, pathInTree, pathInDelta);
	return result;
}
/**
 * Creates the maps from node id to element id for the old and new states.
 * Used for recognizing moves.
 */
protected static void computeNodeIDMaps(DeltaDataTree delta, Hashtable oldNodeIDMap, Hashtable newNodeIDMap, IPath pathInTree, IPath pathInDelta) {
	long id = 0;
	NodeComparison nodeComparison = (NodeComparison) delta.getData(pathInDelta);
	switch (nodeComparison.getUserComparison() & ResourceDelta.KIND_MASK) {
		case IResourceDelta.ADDED :
			id = ((ResourceInfo) nodeComparison.getNewData()).getNodeId();
			newNodeIDMap.put(new Long(id), pathInTree);
			break;
		case IResourceDelta.REMOVED :
			id = ((ResourceInfo) nodeComparison.getOldData()).getNodeId();
			oldNodeIDMap.put(new Long(id), pathInTree);
			break;
		case IResourceDelta.CHANGED :
			id = ((ResourceInfo) nodeComparison.getOldData()).getNodeId();
			oldNodeIDMap.put(new Long(id), pathInTree);
			id = ((ResourceInfo) nodeComparison.getNewData()).getNodeId();
			newNodeIDMap.put(new Long(id), pathInTree);
			break;
	}

	// XXX: look at using one of the visitors to improve performance
	// recurse
	IPath[] children = delta.getChildren(pathInDelta);
	for (int i = 0; i < children.length; ++i) {
		computeNodeIDMaps(delta, oldNodeIDMap, newNodeIDMap, pathInTree.append(children[i].lastSegment()), children[i]);
	}
}
protected static ResourceDelta createDelta(Workspace workspace, DeltaDataTree delta, ResourceDeltaInfo deltaInfo, IPath pathInTree, IPath pathInDelta) {
	// create the delta and fill it with information
	ResourceDelta result = new ResourceDelta(pathInTree, deltaInfo);

	// fill the result with information
	NodeComparison compare = (NodeComparison) delta.getData(pathInDelta);
	result.setStatus(compare.getUserComparison());
	if (Path.ROOT.equals(pathInTree)) {
		ResourceInfo info = workspace.getResourceInfo(Path.ROOT, false, false);
		result.setOldInfo(info);
		result.setNewInfo(info);
	} else {
		result.setOldInfo((ResourceInfo) compare.getOldData());
		result.setNewInfo((ResourceInfo) compare.getNewData());
	}

	result.checkForMove();
	result.checkForMarkerDeltas();

	// recurse over the children
	IPath[] childKeys = delta.getChildren(pathInDelta);
	IResourceDelta[] children = new IResourceDelta[childKeys.length];
	for (int i = 0; i < childKeys.length; i++)
		children[i] = createDelta(workspace, delta, deltaInfo, pathInTree.append(childKeys[i].lastSegment()), childKeys[i]);
	result.setChildren(children);

	// if this delta has children but no other changes, mark it as changed
	int status = result.status;
	if ((status & IResourceDelta.ALL_WITH_PHANTOMS) == 0 && children.length != 0)
		result.setStatus(status |= IResourceDelta.CHANGED);

	// return the delta
	return result;
}
}
