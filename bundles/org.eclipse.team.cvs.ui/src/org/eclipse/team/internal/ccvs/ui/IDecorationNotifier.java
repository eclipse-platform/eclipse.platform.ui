package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;

public interface IDecorationNotifier {
	
	/**
	 * Answers the next resource that needs decorating.
	 */
	public IResource next();
	
	/**
	 * Called to associate a decoration to a resource.
	 */
	public void decorated(IResource[] resource, CVSDecoration[] decoration);
	
	/**
	 * Number of resources remaining to be decorated
	 */
	public int remaining();
}
