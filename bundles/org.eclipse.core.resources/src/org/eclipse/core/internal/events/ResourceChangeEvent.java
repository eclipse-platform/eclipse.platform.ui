package org.eclipse.core.internal.events;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import java.util.EventObject;

public class ResourceChangeEvent extends EventObject implements IResourceChangeEvent {
	int type;
	IResource resource;
	IResourceDelta delta;
protected ResourceChangeEvent(Object source, int type, IResource resource) {
	super (source);
	this.resource = resource;
	this.type = type;
}
protected ResourceChangeEvent(Object source, int type, IResourceDelta delta) {
	super (source);
	this.delta = delta;
	this.type = type;
}
/**
 * @see IResourceChangeEvent#getDelta
 */
public IResourceDelta getDelta() {
	return delta;
}
/**
 * @see IResourceChangeEvent#getResource
 */
public IResource getResource() {
	return resource;
}
/**
 * @see IResourceChangeEvent#getType
 */
public int getType() {
	return type;
}
public void setDelta(IResourceDelta value) {
	delta = value;
}
}
