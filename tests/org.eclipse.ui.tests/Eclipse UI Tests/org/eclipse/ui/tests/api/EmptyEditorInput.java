package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class EmptyEditorInput implements IEditorInput {
	private ImageDescriptor input;
	private static final String name = "Empty Editor Input";
	private static final String tooltip = "Empty Editor Input Tooltip";
	
	public EmptyEditorInput()
	{
		input = null;
	}
	
	public boolean exists()
	{
		return false;
	}
   
	public ImageDescriptor getImageDescriptor()
	{
		return input;
	}
 
 	public String getName()
 	{
    	return name;
 	}
 	
 	public IPersistableElement getPersistable()
 	{
 		return null;
 	}
            
	public String getToolTipText()
	{
		return tooltip;
	}
	
	public Object getAdapter( Class adapter )
	{
		return null;
	}

}


