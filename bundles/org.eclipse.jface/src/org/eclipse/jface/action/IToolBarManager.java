package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * The <code>IToolBarManager</code> interface provides protocol for managing
 * contributions to a tool bar. It extends <code>IContributionManager</code>
 * but does not declare any new members; it exists only to increase the
 * readability of code using tool bars.
 * <p>
 * This package also provides a concrete tool bar manager implementation,
 * {@link ToolBarManager <code>ToolBarManager</code>}.
 * </p>
 */
public interface IToolBarManager extends IContributionManager {
}
