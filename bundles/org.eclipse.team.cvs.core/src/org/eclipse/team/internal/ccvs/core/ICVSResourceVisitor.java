/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core;




/**
 * Interface for an visitor of the IManagedResources.
 */
public interface ICVSResourceVisitor {
	public void visitFile(ICVSFile file) throws CVSException;
	public void visitFolder(ICVSFolder folder) throws CVSException;	
}

