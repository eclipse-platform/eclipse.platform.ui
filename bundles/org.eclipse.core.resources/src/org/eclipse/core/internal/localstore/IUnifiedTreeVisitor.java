package org.eclipse.core.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;

public interface IUnifiedTreeVisitor {
/**
 * Returns true to visit the members of this node and false otherwise.
 */
public boolean visit(UnifiedTreeNode node) throws CoreException;
}
