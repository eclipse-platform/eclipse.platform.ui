package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class IdVersionPair extends IdVersionPairModel implements IIdVersionPair {
/**
 * IdVersionPair constructor comment.
 */
public IdVersionPair() {
	super();
}
/**
 * 
 * @param strIdAndVersion java.lang.String
 */
public IdVersionPair(String strIdAndVersion) {

	// Separate identifier and version
	//--------------------------------
	int iIndex = strIdAndVersion.lastIndexOf("_");

	if (iIndex > 0) {
		this._setId(strIdAndVersion.substring(0, iIndex));
		this._setVersion(strIdAndVersion.substring(iIndex + 1));
	}
	else {
		this._setId(strIdAndVersion);
	}
}
/**
 * IdVersionPair constructor comment.
 */
public IdVersionPair(String id, String version) {
	super();
	_setId(id);
	_setVersion(version);
}
/**
 * Returns the unique identifier of this Id-Version pair
 * This identifier is a non-empty string and is unique 
 * within the Update Manager.
 *
 * @return the unique identifier of this Id-Version pair 
 */
public String getUniqueIdentifier() {
	return _getId();
}
/**
 * Returns the version identifier of this Id-Version pair.
 *
 * @return the version identifier of this Id-Version pair
 */
public VersionIdentifier getVersionIdentifier() {
	try {
		return new VersionIdentifier(_getVersion());
	} catch (Throwable e) {
		return new VersionIdentifier(0,0,0);
	}
}
/**
 * Returns the version string of this Id-Version pair.
 *
 * @return the version string of this Id-Version pair
 */
public String getVersionStr() {
	return _getVersion();
}
}
