/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
