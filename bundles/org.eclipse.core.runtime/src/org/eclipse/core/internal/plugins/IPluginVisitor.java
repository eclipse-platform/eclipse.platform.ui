package org.eclipse.core.internal.plugins;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPluginDescriptor;

public interface IPluginVisitor {
public void visit(IPluginDescriptor descriptor);
}
