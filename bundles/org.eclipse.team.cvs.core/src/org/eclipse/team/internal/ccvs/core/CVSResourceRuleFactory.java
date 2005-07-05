/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.ResourceRuleFactory;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class CVSResourceRuleFactory extends ResourceRuleFactory {
    public ISchedulingRule validateEditRule(IResource[] resources) {
    	return CVSTeamProvider.internalGetFileModificationValidator().validateEditRule(this, resources);
    }
    
    public ISchedulingRule getParent(IResource resource) {
        return parent(resource);
    }
    
}