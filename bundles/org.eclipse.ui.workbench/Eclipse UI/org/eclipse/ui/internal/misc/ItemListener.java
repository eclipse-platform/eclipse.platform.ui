package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
