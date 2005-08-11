/*******************************************************************************
 * Copyright (c) 2004, 2005 Richard Hoefter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Richard Hoefter (richard.hoefter@web.de) - initial API and implementation, bug 95296
 *     IBM Corporation - adapted to wizard export page
 *******************************************************************************/

package org.eclipse.ant.internal.ui.datatransfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class AntBuildfileExportPage extends WizardPage {
    
    private CheckboxTableViewer fTableViewer;
    private List fSelectedJavaProjects = new ArrayList();
    
    public AntBuildfileExportPage()
    {
        super("AntBuildfileExportWizardPage"); //$NON-NLS-1$
        setPageComplete(false);
        setTitle(DataTransferMessages.AntBuildfileExportPage_0);
        setDescription(DataTransferMessages.AntBuildfileExportPage_1);
    }
    
    /*
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);
        
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(data);
            
        initializeDialogUnits(composite);

        //TODO set F1 help
        
        Label label= new Label(composite, SWT.LEFT);
        label.setText(DataTransferMessages.AntBuildfileExportPage_2);

        Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        fTableViewer = new CheckboxTableViewer(table);
        table.setLayout(new TableLayout());
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = 300;
        table.setLayoutData(data);
        fTableViewer.setContentProvider(new WorkbenchContentProvider() {
            public Object[] getElements(Object element) {
                if (element instanceof IJavaProject[]) {
                    return (IJavaProject[]) element;
                }
                return null;
            }
        });
        fTableViewer.setLabelProvider(new WorkbenchLabelProvider());
        fTableViewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (event.getChecked()) {
                    fSelectedJavaProjects.add(event.getElement());
                } else {
                    fSelectedJavaProjects.remove(event.getElement());
                }
                updateEnablement();
            }
        });
       
        initializeProjects();
        setControl(composite);
        updateEnablement();
        Dialog.applyDialogFont(parent);
    }
    
    private void initializeProjects() {
        IWorkspaceRoot rootWorkspace = ResourcesPlugin.getWorkspace().getRoot();
        IJavaModel javaModel = JavaCore.create(rootWorkspace);
        IJavaProject[] javaProjects;
        try {
            javaProjects = javaModel.getJavaProjects();
        }
        catch (JavaModelException e) {
            javaProjects= new IJavaProject[0];
        }
        fTableViewer.setInput(javaProjects);
        // Check any necessary projects
        if (fSelectedJavaProjects != null) {
            fTableViewer.setCheckedElements(fSelectedJavaProjects.toArray(new IJavaProject[fSelectedJavaProjects.size()]));
        }
    }
    
    private void updateEnablement() {
        boolean complete= true;
        if (fSelectedJavaProjects.size() == 0) {
            setMessage(null);
            complete = false;
        } 
        if (complete) {
            setMessage(null);
        }
        setPageComplete(complete);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            fTableViewer.getTable().setFocus();
        }
    }
    
    protected void setSelectedProjects(List selectedJavaProjects) {
        fSelectedJavaProjects.addAll(selectedJavaProjects);
    }
    
    /**
     * Convert Eclipse Java projects to Ant build files. Displays error dialogs.
     */
    public boolean generateBuildfiles() 
    {
        setErrorMessage(null);
        // collect all projects to create build files for
        Set projects = new TreeSet(ExportUtil.getJavaProjectComparator());
        Iterator javaProjects = fSelectedJavaProjects.iterator();
        while (javaProjects.hasNext()) {
            IJavaProject javaProject = (IJavaProject) javaProjects.next();
            try {
                projects.addAll(ExportUtil.getClasspathProjectsRecursive(javaProject));
            } catch (JavaModelException e) {
                AntUIPlugin.log(e);
                return false;
            }
            projects.add(javaProject);
        }
        
        // confirm overwrite
        List confirmOverwrite = getConfirmOverwriteSet(projects);
        if (confirmOverwrite.size() > 0)
        {
            String message = DataTransferMessages.AntBuildfileExportPage_3 + ExportUtil.NEWLINE +
                EclipseClasspath.toString(confirmOverwrite, ExportUtil.NEWLINE);
            if (! MessageDialog.openConfirm(getShell(), DataTransferMessages.AntBuildfileExportPage_4, message))
            {
                return true;
            }
        }
        
        List cyclicProjects = new ArrayList();
        
        // create build files for all projects
        javaProjects = projects.iterator();
        Exception problem= null;
        while (javaProjects.hasNext()) {
            IJavaProject javaProject = (IJavaProject) javaProjects.next();       
            try {
                BuildFileCreator.create(javaProject);
            } catch (JavaModelException e) {
                problem= e;
            } catch (TransformerConfigurationException e) {
                problem= e;
            } catch (ParserConfigurationException e) {
                problem= e;
            } catch (TransformerException e) {
                problem= e;
            } catch (IOException e) {
                problem= e;
            } catch (CoreException e) {
                problem= e;
            }

            if (problem != null) {
                AntUIPlugin.log(problem);
                setErrorMessage(MessageFormat.format(DataTransferMessages.AntBuildfileExportPage_10, new String[] {problem.toString()}));
                return false;
            }
           
            try {
                if (ExportUtil.hasCyclicDependency(javaProject))
                {
                    cyclicProjects.add(javaProject.getProject().getName());
                }
            } catch (CoreException e) {
                AntUIPlugin.log(e);
                setErrorMessage(MessageFormat.format(DataTransferMessages.AntBuildfileExportPage_10, new String[] {e.toString()}));
                return false;
            }
        }

        // show warning if project has cycle
        if (cyclicProjects.size() > 0)
        {
            String warningMessage= MessageFormat.format(DataTransferMessages.AntBuildfileExportPage_6 + ExportUtil.NEWLINE + ExportUtil.NEWLINE +
                    DataTransferMessages.AntBuildfileExportPage_7 +
                    DataTransferMessages.AntBuildfileExportPage_8,
                    new String[] { ExportUtil.NEWLINE + EclipseClasspath.toString(cyclicProjects, ExportUtil.NEWLINE)});
            MessageDialog.openWarning(getShell(), DataTransferMessages.AntBuildfileExportPage_9, warningMessage);
        }
        
        // show success message
        List projectNames = new ArrayList();
        for (Iterator iter = projects.iterator(); iter.hasNext();)
        {
            IJavaProject project = (IJavaProject) iter.next();
            projectNames.add(project.getProject().getName());
        }
        String message = MessageFormat.format(DataTransferMessages.AntBuildfileExportPage_5 + ExportUtil.NEWLINE, new String[] {ExportUtil.NEWLINE + EclipseClasspath.toString(projectNames, ExportUtil.NEWLINE)});
        MessageDialog.openInformation(getShell(), DataTransferMessages.AntBuildfileExportPage_0, message);

        return true;
    }
    
    /**
     * Get list of projects which have already a build.xml file that was not created by eclipse2ant.
     * 
     * @param javaProjects list of IJavaProjects
     * @return set of project names
     */
    private List getConfirmOverwriteSet(Set javaProjects)
    {
        List result = new ArrayList(javaProjects.size());
        for (Iterator iter = javaProjects.iterator(); iter.hasNext();)
        {
            IJavaProject project = (IJavaProject) iter.next();
            if (existsBuildFile(project))
            {
                result.add(project.getProject().getName());
            }
        }
        return result;
    }
    
    /**
     * Check if build.xml exists that was not written by this export.
     */
   private boolean existsBuildFile(IJavaProject project)
    {
        String projectRoot = ExportUtil.getProjectRoot(project);
        File buildFile = new File(projectRoot + File.separator + "build.xml"); //$NON-NLS-1$
        if (buildFile.exists())
        {
            BufferedReader in = null;
            try
            {
                in = new BufferedReader(new FileReader(buildFile));
                int i = BuildFileCreator.WARNING.indexOf(ExportUtil.NEWLINE);
                String warning = BuildFileCreator.WARNING.substring(0, i);
                String line;
                while ((line = in.readLine()) != null)
                {
                    if (line.indexOf(warning) != -1)
                    {
                        return false;
                    }
                }
                return true;
            } catch (FileNotFoundException e) {
               return false;
            } catch (IOException e) {
               return false;
            } finally
            {
                try
                {
                    if (in != null)
                    {
                        in.close();
                    }
                }
                catch (IOException e)
                {
                    // ignore
                }
            }
        }
        return false;
    }
}