/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import org.eclipse.compare.structuremergeviewer.IStructureCreator;

/**
 * A factory object for creating <code>IStructureCreator</code>s from a descriptor.
 * <p>
 * It is used when registering <code>IStructureCreator</code> for types
 * in <code>CompareUIPlugin.registerStructureCreator</code>.
 * </p>
 *
 * @see IStructureCreator
 * @see CompareUIPlugin
 */
public interface IStructureCreatorDescriptor {

	/**
	 * Creates a new structure creator.
	 *
	 * @return a newly created structure creator
	 */
	IStructureCreator createStructureCreator(); 
}
