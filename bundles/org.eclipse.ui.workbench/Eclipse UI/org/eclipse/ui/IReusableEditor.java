package org.eclipse.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * Interface for reusable editors. 
 * 
 * An editors may support changing its input so that 
 * the workbench may change its contents instead of 
 * opening a new editor.
 * 
 * Note: For EXPERIMENTAL use only. IT MAY CHANGE IN NEAR FUTURE.
 * 
 */
public interface IReusableEditor extends IEditorPart {
/**
 * @see EditorPart#setInput
 */
public void setInput(IEditorInput newInput);
}

