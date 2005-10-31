
/*
 * Created on Feb 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.navigator.resources.internal.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonOpenListener;
import org.eclipse.ui.navigator.NavigatorContentService;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 */
public class ResourceOpenListener implements ICommonOpenListener{
	OpenFileAction openFileAction ;
	private CommonNavigator commonNavigator;
	private NavigatorContentService contentService;
	
	public void initialize(CommonNavigator aCommonNavigator, NavigatorContentService aContentService) {
		commonNavigator = aCommonNavigator;
		contentService = aContentService;
		openFileAction = new OpenFileAction(commonNavigator.getSite().getPage());
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IOpenListener#open(org.eclipse.jface.viewers.OpenEvent)
	 */
	public void open(OpenEvent event) {
		ISelection selection = event.getSelection();
		if (selection != null && selection instanceof IStructuredSelection) {
			if (openFileAction != null ) {
				IStructuredSelection structureSelection = (IStructuredSelection) selection;
				Object element = structureSelection.getFirstElement();
		        if (element instanceof IFile) {
		            openFileAction.selectionChanged(structureSelection);
		            openFileAction.run();
		        }
			}
		}
	}

}
