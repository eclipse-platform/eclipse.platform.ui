package org.eclipse.core.internal.events;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.watson.*;
import java.util.Map;

public class ResourceDeltaInfo {
	protected Workspace workspace;
	protected Map oldNodeIDMap;
	protected Map newNodeIDMap;
	protected Map allMarkerDeltas;
	protected ResourceComparator comparator;

public ResourceDeltaInfo(Workspace workspace, Map markerDeltas, ResourceComparator comparator) {
	super();
	this.workspace = workspace;
	this.allMarkerDeltas = markerDeltas;
	this.comparator = comparator;	
}
public void destroy() {
	workspace = null;
	oldNodeIDMap = null;
	newNodeIDMap = null;
	allMarkerDeltas = null;
	comparator = null;
}
public ResourceComparator getComparator() {
	return comparator;
}
public Map getMarkerDeltas() {
	return allMarkerDeltas;
}
public Map getNewNodeIDMap() {
	return newNodeIDMap;
}
public Map getOldNodeIDMap() {
	return oldNodeIDMap;
}
public Workspace getWorkspace() {
	return workspace;
}
public void setMarkerDeltas(Map value) {
	allMarkerDeltas = value;
}
public void setNodeMaps(Map oldMap, Map newMap) {
	oldNodeIDMap = oldMap;
	newNodeIDMap = newMap;
}
}
