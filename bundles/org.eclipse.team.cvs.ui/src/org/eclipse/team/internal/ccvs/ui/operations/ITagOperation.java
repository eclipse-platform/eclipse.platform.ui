/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.ui.tags.TagSource;

public interface ITagOperation {
	public abstract CVSTag getTag();
	public abstract void setTag(CVSTag tag);
	public abstract void run() throws InvocationTargetException, InterruptedException;
	public abstract ICVSResource[] getCVSResources();
	public abstract void moveTag();
	public abstract void recurse();
    public abstract TagSource getTagSource();
}
