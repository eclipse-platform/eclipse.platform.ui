package org.eclipse.core.internal.plugins;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.IPluginDescriptor;

public interface IPluginVisitor {
public void visit(IPluginDescriptor descriptor);
}
