package org.eclipse.ui;

public interface IReusableEditor extends IEditorPart {
	public boolean getReuseEditor();
	public void setReuseEditor(boolean reuse);
	public void setInput(IEditorInput newInput);
}

