package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.properties.PropertyStore;
import org.eclipse.core.runtime.QualifiedName;

public class RootInfo extends ResourceInfo {
	/** The property store for this resource */
	protected PropertyStore propertyStore = null;
/**
 * Returns the property store associated with this info.  The return value may be null.
 */
public PropertyStore getPropertyStore() {
	return propertyStore;
}
/**
 * Override parent's behaviour and do nothing. Sync information
 * cannot be stored on the workspace root so we don't need to
 * update this counter which is used for deltas.
 */
public void incrementSyncInfoGenerationCount() {
}
/**
 * Sets the property store associated with this info.  The value may be null.
 */
public void setPropertyStore(PropertyStore value) {
	propertyStore = value;
}
/**
 * Overrides parent's behaviour since sync information is not
 * stored on the workspace root.
 */
public void setSyncInfo(QualifiedName id, byte[] value) {
}
}
