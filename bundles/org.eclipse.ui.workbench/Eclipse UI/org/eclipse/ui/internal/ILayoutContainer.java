package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
public interface ILayoutContainer {
/**
 * Add a child to the container.
 */
public void add(LayoutPart newPart);
/**
 * Return true if the container allows its
 * parts to show a border if they choose to,
 * else false if the container does not want
 * its parts to show a border.
 */
public boolean allowsBorder();
/**
 * Returns a list of layout children.
 */
public LayoutPart [] getChildren();
/**
 * Remove a child from the container.
 */
public void remove(LayoutPart part);
/**
 * Replace one child with another
 */
public void replace(LayoutPart oldPart, LayoutPart newPart);
/**
 * Gets the parent for this container.
 */
public ILayoutContainer getContainer();
/**
 * Gets root container for this part.
 */
public RootLayoutContainer getRootContainer();
}
