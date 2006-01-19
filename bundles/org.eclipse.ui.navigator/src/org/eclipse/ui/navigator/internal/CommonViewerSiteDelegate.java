package org.eclipse.ui.navigator.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.navigator.ICommonViewerSite;

public class CommonViewerSiteDelegate implements ICommonViewerSite {
	
	
	private String id; 
	private ISelectionProvider selectionProvider; 
	private Shell shell;

	public CommonViewerSiteDelegate(String anId,  ISelectionProvider aSelectionProvider, Shell aShell) {
		Assert.isNotNull(anId);
		Assert.isNotNull(aSelectionProvider);
		Assert.isNotNull(aShell);
		id = anId;
		selectionProvider = aSelectionProvider;		
		shell = aShell;
	} 

	public String getId() {
		return id;
	} 

	public Shell getShell() {
		return shell;
	}

	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}  


	public void setSelectionProvider(ISelectionProvider aSelectionProvider) {
		selectionProvider = aSelectionProvider;
	}

	public Object getAdapter(Class adapter) { 
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

}
