package org.eclipse.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * Interface for reusable editors. 
 * 
 * An editors may support changing its input
 * so that the UI may change its contents when
 * opening a new resource instead of opening
 * a new editor.
 */
public interface IReusableEditor extends IEditorPart {
/**
 * Returns true if this editor can be reused otherwise
 * returns false
 *
 * @return whether or not the editor can be reused.
 */
public boolean getReuseEditor();
/**
 * Set whether of not this editor may be reused
 * to edit another resource.
 * 
 * @param true if the editor may be reused.
 */
public void setReuseEditor(boolean reuse);
/**
 * @see EditorPart#setInput
 */
public void setInput(IEditorInput newInput);
}

