package org.eclipse.team.internal.ui.history.actions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.team.internal.ui.history.FileRevisionEditorInput;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;

public class OpenRevisionAction extends TeamAction {

	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					IStructuredSelection structSel = getSelection();
			
					Object[] objArray = structSel.toArray();
					
					for (int i = 0; i < objArray.length; i++) {
						IFileRevision revision = (IFileRevision) objArray[i];
						if (!revision.exists()) {
							MessageDialog.openError(getShell(), TeamUIMessages.OpenRevisionAction_DeletedRevisionTitle, TeamUIMessages.OpenRevisionAction_DeletedRevisionMessage);  
						} else {
							IStorage file =revision.getStorage(monitor);
			
							String id = getEditorID(file.getName(), file.getContents());
							getTargetPage().openEditor(new FileRevisionEditorInput(revision), id);
						}
					
					}
					
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			}
		}, TeamUIMessages.ConfigureProjectAction_configureProject, PROGRESS_BUSYCURSOR); 
	}
	
	protected boolean isEnabled() throws TeamException {
		return true;
	}
	
	private String getEditorID(String fileName, InputStream contents){
		IWorkbench workbench = TeamUIPlugin.getPlugin().getWorkbench();
        IEditorRegistry registry = workbench.getEditorRegistry();
        IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
     
      
        IContentType type = null;
        if (contents != null) {
            try {
                type = Platform.getContentTypeManager().findContentTypeFor(contents, fileName);
            } catch (IOException e) {
            
            }
        }
        if (type == null) {
            type = Platform.getContentTypeManager().findContentTypeFor(fileName);
        }
        IEditorDescriptor descriptor = registry.getDefaultEditor(fileName, type);
        String id;
        if (descriptor == null) {
            id = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
        } else {
            id = descriptor.getId();
        }		
        
        return id;
	}

}
