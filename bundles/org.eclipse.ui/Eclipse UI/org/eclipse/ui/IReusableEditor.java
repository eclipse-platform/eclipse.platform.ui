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
 * @see EditorPart#setInput
 */
public void setInput(IEditorInput newInput);
}

