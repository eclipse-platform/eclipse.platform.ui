package org.eclipse.ui.internal.misc;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
/**
 * Defines the events fired by an <code>ActivatorItem</code>.
 */
public interface ItemListener {
/**
 * Notifies the receiver that the close box has been pressed for an item.
 */
public void itemClosePressed(ActivatorItem item);
/**
 * Notifies the receiver that an item has been selected.
 */
public void itemSelected(ActivatorItem item);
}
