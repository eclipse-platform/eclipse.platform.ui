/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.events;

import java.util.Map;
import org.eclipse.core.internal.resources.Workspace;

public class ResourceDeltaInfo {
	protected Workspace workspace;
	protected Map allMarkerDeltas;
	protected NodeIDMap nodeIDMap;
	protected ResourceComparator comparator;

public ResourceDeltaInfo(Workspace workspace, Map markerDeltas, ResourceComparator comparator) {
	super();
	this.workspace = workspace;
	this.allMarkerDeltas = markerDeltas;
	this.comparator = comparator;	
}
public void destroy() {
	workspace = null;
	allMarkerDeltas = null;
	comparator = null;
}
public ResourceComparator getComparator() {
	return comparator;
}
/**
 * Table of all marker deltas, IPath -> MarkerSet
 */
public Map getMarkerDeltas() {
	return allMarkerDeltas;
}
public NodeIDMap getNodeIDMap() {
	return nodeIDMap;
}
public Workspace getWorkspace() {
	return workspace;
}
public void setMarkerDeltas(Map value) {
	allMarkerDeltas = value;
}
public void setNodeIDMap(NodeIDMap map) {
	nodeIDMap = map;
}
}
