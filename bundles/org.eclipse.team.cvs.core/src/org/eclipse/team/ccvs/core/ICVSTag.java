package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * The ICVSTag interface is used to proivde information about
 * a tag that exists on a file in CVS.
 */
public interface ICVSTag {
	
	/**
	 * Get the name of the tag
	 */
	public String getName();
	
	/**
	 * Return true if the tag is a branch tag
	 */
	public boolean isBranch();
	
	/**
	 * Return true if the tag is a version tag
	 */
	public boolean isVersion();

}

