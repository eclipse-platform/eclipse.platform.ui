package org.eclipse.core.internal.localstore;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.CoreException;

public interface IUnifiedTreeVisitor {
/**
 * Returns true to visit the members of this node and false otherwise.
 */
public boolean visit(UnifiedTreeNode node) throws CoreException;
}
