/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.IFileContentManager;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.*;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * A wizard to commit the resources whose synchronization state is given in form
 * of a set of <code>SyncInfo</code>.
 */
public class CommitWizard extends Wizard {
    
    private static final String COMMIT_WIZARD_SECTION = "CommitWizard"; //$NON-NLS-1$
    
    /**
     * Persists the size of the wizard dialog.
     */
    private static class SettingsSaver {
        
        private static final String BOUNDS_HEIGHT_KEY = "width"; //$NON-NLS-1$
        private static final String BOUNDS_WIDTH_KEY = "height"; //$NON-NLS-1$
        
        private final IWizard fWizard;
        
        private SettingsSaver(IWizard wizard) {
            fWizard= wizard;
        }
        
        public void saveSize(int width, int height) {
            final IDialogSettings settings= fWizard.getDialogSettings();
            settings.addNewSection(COMMIT_WIZARD_SECTION);
            settings.put(BOUNDS_WIDTH_KEY, width);
            settings.put(BOUNDS_HEIGHT_KEY, height);
        }
        
        public Point getSize() {
            final Point size= new Point(300, 400);
            final IDialogSettings settings= fWizard.getDialogSettings();
            try {
                size.x= settings.getInt(BOUNDS_WIDTH_KEY);
                size.y= settings.getInt(BOUNDS_HEIGHT_KEY);
            } catch (NumberFormatException e) {
            }
            return size;
        }
    }
    
    /**
     * An operation to add and commit resources to a CVS repository.
     */
    private static class AddAndCommitOperation extends CVSOperation {
        
        private final IResource[] fAllResources;
        private final String fComment;
        
        private Map fModesForExtensionsForOneTime;
        private Map fModesForNamesForOneTime;
        
        private IResource[] fNewResources;
        
        private AddAndCommitOperation(IWorkbenchPart part, IResource[] allResources, String comment) {
            super(part);
            fNewResources = new IResource [0];
            fModesForExtensionsForOneTime = Collections.EMPTY_MAP;
            fModesForNamesForOneTime= Collections.EMPTY_MAP;
            fAllResources = allResources;
            fComment = comment;
        }
        
        public void setModesForExtensionsForOneTime(Map modes) {
            fModesForExtensionsForOneTime= modes;
        }
        
        public void setModesForNamesForOneTime(Map modes) {
            fModesForNamesForOneTime= modes;
        }
        
        
        public void setNewResources(IResource [] newResources) {
            this.fNewResources= newResources;
        }
        
        protected void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
            try {
                final AddOperation op= new AddOperation(getPart(), fNewResources);
                op.addModesForExtensions(fModesForExtensionsForOneTime);
                op.addModesForNames(fModesForNamesForOneTime);
                op.run(monitor);
                new CommitOperation(getPart(), fAllResources, new Command.LocalOption[0], fComment).run(monitor);
            } catch (InvocationTargetException e) {
                throw CVSException.wrapException(e);
            }
        }
        
        protected String getJobName() {
            return Policy.bind("CommitWizard.0"); //$NON-NLS-1$
        }
        
        protected String getTaskName() {
            return Policy.bind("CommitWizard.1"); //$NON-NLS-1$
        }
    }
    
    private final SyncInfoSet fOutOfSyncInfos;
    private final SyncInfoSet fUnaddedInfos;
    
    private CommitWizardFileTypePage fFileTypePage;
    private CommitWizardCommitPage fCommitPage;
    
    private IResource[] fResources;
    private SettingsSaver fSettingsSaver;
    

    public CommitWizard(IResource [] resources) throws CVSException {
        this(resources, getOutOfSyncInfos(resources));
    }
    
    public CommitWizard(SyncInfoSet infos) throws CVSException {
        this(infos.getResources(), infos);
    }
    
    private CommitWizard(IResource [] resources, SyncInfoSet syncInfos) throws CVSException {
        super();
        setWindowTitle(Policy.bind("CommitWizard.2")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_NEW_LOCATION));
        
        final IDialogSettings cvsSettings = CVSUIPlugin.getPlugin().getDialogSettings();
        IDialogSettings section = cvsSettings.getSection(COMMIT_WIZARD_SECTION);
        if (section == null) {
            section = cvsSettings.addNewSection(COMMIT_WIZARD_SECTION);
        }
        setDialogSettings(section);

        fSettingsSaver= new SettingsSaver(this);

        fResources= resources;
        fOutOfSyncInfos= syncInfos;
        fUnaddedInfos= getUnaddedInfos(syncInfos);

    }
    
    public boolean hasOutgoingChanges() {
        return fOutOfSyncInfos.size() > 0;
    }
    
    private static SyncInfoSet getOutOfSyncInfos(IResource [] resources) {
        final CVSWorkspaceSubscriber subscriber= CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber();
        final SyncInfoSet infos= new SyncInfoSet();
        subscriber.collectOutOfSync(resources, IResource.DEPTH_INFINITE, infos, new NullProgressMonitor());
        
        return new SyncInfoSet(infos.getNodes(new FastSyncInfoFilter.SyncInfoDirectionFilter(new int [] { SyncInfo.OUTGOING, SyncInfo.CONFLICTING })));
    }
    
    private static SyncInfoSet getUnaddedInfos(SyncInfoSet infos) throws CVSException {
        final SyncInfoSet unadded= new SyncInfoSet();        
        for (final Iterator iter = infos.iterator(); iter.hasNext();) {
            final SyncInfo info = (SyncInfo) iter.next();
            final IResource file= info.getLocal();
            if (!((file.getType() & IResource.FILE) == 0 || isAdded(file)))
                unadded.add(info);
        }
        return unadded;
    }
    
    private static void getUnknownNamesAndExtension(SyncInfoSet infos, Collection names, Collection extensions) {
        
        final IFileContentManager manager= Team.getFileContentManager();
        
        for (final Iterator iter = infos.iterator(); iter.hasNext();) {
            
            final SyncInfo info = (SyncInfo)iter.next();
            
            final String extension= info.getLocal().getFileExtension();
            if (extension != null && !manager.isKnownExtension(extension)) {
                extensions.add(extension);
            }
            
            final String name= info.getLocal().getName();
            if (extension == null && name != null && !manager.isKnownFilename(name))
                names.add(name);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    public void addPages() {
        
        final Collection names= new ArrayList();
        final Collection extensions= new ArrayList();
        getUnknownNamesAndExtension(fUnaddedInfos, names, extensions);
        
        if (names.size() + extensions.size() > 0) {
            fFileTypePage= new CommitWizardFileTypePage(extensions, names); 
            addPage(fFileTypePage);
        }
        
        fCommitPage= new CommitWizardCommitPage(fResources, fFileTypePage);
        addPage(fCommitPage);
        
        super.addPages();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish() {
        
        final String comment= getComment();
        if (comment == null)
            return false;
        
        final SyncInfoSet infos= fCommitPage.getInfosToCommit();
        if (infos.size() == 0)
            return true;

        final SyncInfoSet unadded;
        try {
            unadded = getUnaddedInfos(infos);
        } catch (CVSException e1) {
            return false;
        }
                    
        final AddAndCommitOperation operation= new AddAndCommitOperation(getPart(), infos.getResources(), comment);
        
        if (fFileTypePage != null) {
            final Map extensionsToSave= new HashMap();
            final Map extensionsNotToSave= new HashMap();
            
            fFileTypePage.getModesForExtensions(extensionsToSave, extensionsNotToSave);
            saveExtensionMappings(extensionsToSave);
            operation.setModesForExtensionsForOneTime(extensionsNotToSave);
            
            final Map namesToSave= new HashMap();
            final Map namesNotToSave= new HashMap();

            fFileTypePage.getModesForNames(namesToSave, namesNotToSave);
            saveNameMappings(namesToSave);
            operation.setModesForNamesForOneTime(namesNotToSave);
        }
        
        if (unadded.size() > 0) {
            operation.setNewResources(unadded.getResources());
        }
        
        try {
            operation.run();
        } catch (InvocationTargetException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }

        final Rectangle bounds= getContainer().getCurrentPage().getControl().getParent().getClientArea();
        fSettingsSaver.saveSize(bounds.width, bounds.height);
        
        return true;
    }
    
    private String getComment() {
        final String comment= fCommitPage.getComment();
        if (comment.length() == 0) {
            
            final IPreferenceStore store= CVSUIPlugin.getPlugin().getPreferenceStore();
            final String value= store.getString(ICVSUIConstants.PREF_ALLOW_EMPTY_COMMIT_COMMENTS);
            
            if (MessageDialogWithToggle.NEVER.equals(value))
                return null;
            
            if (MessageDialogWithToggle.PROMPT.equals(value)) {
                
                final String title= Policy.bind("CommitWizard.3"); //$NON-NLS-1$
                final String message= Policy.bind("CommitWizard.4"); //$NON-NLS-1$
                final String toggleMessage= Policy.bind("CommitWizard.5"); //$NON-NLS-1$
                
                final MessageDialogWithToggle dialog= MessageDialogWithToggle.openYesNoQuestion(getShell(), title, message, toggleMessage, false, store, ICVSUIConstants.PREF_ALLOW_EMPTY_COMMIT_COMMENTS);
                if (dialog.getReturnCode() == IDialogConstants.NO_ID) {
                    fCommitPage.setFocus();
                    return null;
                }
            }
        }
        return comment;
    }
    
    public static void run(Shell shell, IResource [] resources) throws CVSException {
        run(shell, new CommitWizard(resources));
    }
    
    public static void run(Shell shell, SyncInfoSet infos) throws CVSException {
        run(shell, new CommitWizard(infos));
    }
    
    private static void run(Shell shell, CommitWizard wizard) {
        if (!wizard.hasOutgoingChanges()) {
            MessageDialog.openInformation(shell, Policy.bind("CommitWizard.6"), Policy.bind("CommitWizard.7")); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            final WizardDialog dialog= new WizardDialog(shell, wizard);
            dialog.setMinimumPageSize(wizard.loadSize());
            dialog.open();
        }
    }

    
    /**
     * @param modesToPersist
     */
    private static void saveExtensionMappings(Map modesToPersist) {
        
        final String [] extensions= new String [modesToPersist.size()];
        final int [] modes= new int[modesToPersist.size()];
        
        int index= 0;
        for (Iterator iter= modesToPersist.keySet().iterator(); iter.hasNext();) {
            extensions[index]= (String) iter.next();
            modes[index]= ((Integer)modesToPersist.get(extensions[index])).intValue();
            ++index;
        }
        Team.getFileContentManager().addExtensionMappings(extensions, modes);
    }
    
    private static void saveNameMappings(Map modesToPersist) {
        
        final String [] names= new String [modesToPersist.size()];
        final int [] modes= new int[modesToPersist.size()];
        
        int index= 0;
        for (Iterator iter= modesToPersist.keySet().iterator(); iter.hasNext();) {
            names[index]= (String) iter.next();
            modes[index]= ((Integer)modesToPersist.get(names[index])).intValue();
            ++index;
        }
        Team.getFileContentManager().addNameMappings(names, modes);
    }
    
    /**
     * Get the current workbench part.
     * @return The workbench part.
     */
    private IWorkbenchPart getPart() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
    }
    
    private static boolean isAdded(IResource resource) throws CVSException {
        final ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
        if (cvsResource.isFolder()) {
            return ((ICVSFolder)cvsResource).isCVSFolder();
        }
        return cvsResource.isManaged();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#canFinish()
     */
    public boolean canFinish() {
        final IWizardPage current= getContainer().getCurrentPage();
        if (current == fFileTypePage && fCommitPage != null)
            return false;
        return super.canFinish();
    }
    
    public Point loadSize() {
        return fSettingsSaver.getSize();
    }
}    

