package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/** 
  * A listener which is notified when a tree viewer expands or collapses
  * a node.
  */
public interface ITreeViewerListener {
/**
 * Notifies that a node in the tree has been collapsed.
 *
 * @param event event object describing details
 */
public void treeCollapsed(TreeExpansionEvent event);
/**
 * Notifies that a node in the tree has been expanded.
 *
 * @param event event object describing details
 */
public void treeExpanded(TreeExpansionEvent event);
}
