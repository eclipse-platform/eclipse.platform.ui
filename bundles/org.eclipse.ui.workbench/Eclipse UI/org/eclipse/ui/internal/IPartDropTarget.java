package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Specifies the target layout part that accepts
 * the source part during drag and drop.
 *
 * @see PartDragDrop
 */
public interface IPartDropTarget {
/**
 * Return the layout part that would accept
 * the drag source part, or null if not applicable.
 *
 * @return org.eclipse.ui.internal.LayoutPart
 * @param dragSource org.eclipse.ui.internal.LayoutPart
 */
LayoutPart targetPartFor(LayoutPart dragSource);
}
