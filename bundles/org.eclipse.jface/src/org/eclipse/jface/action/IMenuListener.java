package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * A menu listener that gets informed when a menu is about to show.
 *
 * @see MenuManager#addMenuListener
 */
public interface IMenuListener {
/**
 * Notifies this listener that the menu is about to be shown by
 * the given menu manager.
 *
 * @param manager the menu manager
 */
public void menuAboutToShow(IMenuManager manager);
}
