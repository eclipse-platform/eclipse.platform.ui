package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IIdVersionPair {
/**
 * Returns the unique identifier of this Id-Version pair
 * This identifier is a non-empty string and is unique 
 * within the Update Manager.
 *
 * @return the unique identifier of this Id-Version pair
 */
public String getUniqueIdentifier();
/**
 * Returns the version identifier of this Id-Version pair.
 *
 * @return the version identifier of this Id-Version pair
 */
public VersionIdentifier getVersionIdentifier();
/**
 * Returns the version string of this Id-Version pair.
 *
 * @return the version string of this Id-Version pair
 */
public String getVersionStr();
}
