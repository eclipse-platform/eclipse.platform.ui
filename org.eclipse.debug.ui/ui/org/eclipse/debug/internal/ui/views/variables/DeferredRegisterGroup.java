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
package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugElementWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

public class DeferredRegisterGroup extends DebugElementWorkbenchAdapter implements IDeferredWorkbenchAdapter {

    public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
        if(monitor.isCanceled()) {
            return;
        }
        
        IRegisterGroup group = (IRegisterGroup) object;
        try {
            IRegister[] registers = group.getRegisters();
            collector.add(registers, monitor);
        } catch (DebugException e) {
            DebugUIPlugin.log(e);
        }
        collector.done();
    }

    public boolean isContainer() {
        return true;
    }

    public ISchedulingRule getRule(Object object) {
        return null;
    }

}
