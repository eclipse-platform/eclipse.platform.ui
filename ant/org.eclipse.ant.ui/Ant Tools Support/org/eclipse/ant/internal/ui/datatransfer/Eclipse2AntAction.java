package org.eclipse.ant.internal.ui.datatransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class Eclipse2AntAction implements IObjectActionDelegate
{
    private IResource selectedResource;
    private String projectRoot;
    private List rawClassPathEntries;
    private List resolvedClassPathEntries;
    private String defaultClassDir;
    private List srcDirs;
    private List classDirs;
    private List inclusionLists;
    private List exclusionLists;
    private Map variable2valueMap;
    
    public Eclipse2AntAction()
    {
        super();
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart)
    {
    }

    public void run(IAction action)
    {
        convert(selectedResource);
    }

    public void selectionChanged(IAction action, ISelection selection)
    {
        selectedResource = getResource(selection);
    }
    
    public static IResource getResource(ISelection selection)
    {
        if (selection instanceof IStructuredSelection)
        {
            for (Iterator iter = ((IStructuredSelection) selection).iterator(); iter.hasNext();)
            {
                IAdaptable adaptable = (IAdaptable) iter.next();
                return (IResource) adaptable.getAdapter(IResource.class);
            }
        }
        return null;
    }

    public static IJavaProject getJavaProject(IResource resource)
    {
        IJavaProject result = null;
        IWorkspaceRoot rootWorkspace = ResourcesPlugin.getWorkspace().getRoot();
        IJavaModel javaModel = JavaCore.create(rootWorkspace);
        result = javaModel.getJavaProject(resource.getProject().getName());
        return result;
    }

    /**
     * Convert relative path to absolute path. Path must include project and resource name, otherwise
     * returns <code>null</code>.
     * @param path path which is relative to project root. 
     * @return full qualified path including project root
     */
    public static String resolve(IPath path)
    {
        if (path == null)
        {
            return null;
        }      
        try
        {
            IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
            return folder.getLocation().toString();
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    /**
     * Initialize members with project settings.
     */
    private void init(IJavaProject project)
    {
        projectRoot = project.getResource().getLocation().toString();
        rawClassPathEntries = new ArrayList();
        resolvedClassPathEntries = new ArrayList();
        variable2valueMap = new TreeMap();
        srcDirs = new ArrayList();
        classDirs = new ArrayList();
        inclusionLists = new ArrayList();
        exclusionLists = new ArrayList();
        
        try
        {
            defaultClassDir = project.getOutputLocation().toString();
            IClasspathEntry entries[] = project.getRawClasspath();
            for (int i = 0; i < entries.length; i++)
            {
                if (entries[i].getContentKind() == IPackageFragmentRoot.K_BINARY &&
                    entries[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY)
                {
                    String jarFile = removeProjectRoot(entries[i].getPath().toString(), project.getProject());
                    String jarFileAbsolute = resolve(entries[i].getPath());
                    if (jarFile.indexOf("/jre/lib/") != -1)
                    {
                        continue; // ignore JRE libraries
                    }
                    rawClassPathEntries.add(jarFile);
                    resolvedClassPathEntries.add(jarFileAbsolute);                   
                }
                else if (entries[i].getContentKind() == IPackageFragmentRoot.K_SOURCE &&
                         entries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE)
                {
                    // found source path
                    IPath srcDirPath = entries[i].getPath();
                    IPath classDirPath = entries[i].getOutputLocation();
                    String srcDir = removeProjectRoot((srcDirPath != null) ? srcDirPath.toString() : projectRoot, project.getProject());
                    String classDir = removeProjectRoot((classDirPath != null) ? classDirPath.toString() : defaultClassDir, project.getProject());
                    srcDirs.add(srcDir.equals("") ? "." : srcDir);
                    classDirs.add(classDir.equals("") ? "." : classDir);
                    IPath[] inclusions = entries[i].getInclusionPatterns();                   
                    List inclusionList = new ArrayList();
                    for (int j = 0; j < inclusions.length; j++)
                    {
                        if (inclusions[j] != null)
                        {
                            inclusionList.add(removeProjectRoot(inclusions[j].toString(), project.getProject()));
                        }
                    }
                    inclusionLists.add(inclusionList);
                    IPath[] exclusions = entries[i].getExclusionPatterns();
                    List exclusionList = new ArrayList();
                    for (int j = 0; j < exclusions.length; j++)
                    {
                        if (exclusions[j] != null)
                        {
                            exclusionList.add(removeProjectRoot(exclusions[j].toString(), project.getProject()));
                        }
                    }
                    exclusionLists.add(exclusionList);
                }
                else if (entries[i].getContentKind() == IPackageFragmentRoot.K_SOURCE &&
                         entries[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE)
                {
                    // found variable
                    String entry = entries[i].getPath().toString();
                    int index = entry.indexOf('/');
                    if (index == -1)
                    {
                        index = entry.indexOf('\\');
                    }
                    String variable = entry;
                    String path = "";
                    if (index != -1)
                    {
                        variable = entry.substring(0, index);
                        path = entry.substring(index);
                    }
                    variable2valueMap.put(variable, JavaCore.getClasspathVariable(variable).toString());
                    rawClassPathEntries.add("${" + variable + "}" + path);
                    resolvedClassPathEntries.add(JavaCore.getClasspathVariable(variable).toString() + path);
                }
                else if (entries[i].getContentKind() == IPackageFragmentRoot.K_SOURCE &&
                         entries[i].getEntryKind() == IClasspathEntry.CPE_PROJECT)
                {
                    // found required project on build path
                    String included = entries[i].getPath().toString();
                    IJavaProject includedProject = getJavaProject(included);
                    //Eclipse2AntPlugin.log("WARNING: Included project " + included + " ignored. " +
                    //        "Instead please add project jars explicitly to classpath."); 
                }
            }
        }
        catch(JavaModelException e)
        {
        	AntUIPlugin.log(e);
        }
        
        StringBuffer b = new StringBuffer();
        b.append("projectRoot=" + projectRoot + "\n");
        b.append("defaultClassDir=" + defaultClassDir + "\n");
        b.append("rawClassPathEntries=" + rawClassPathEntries + "\n");
        b.append("resolvedClassPathEntries=" + resolvedClassPathEntries + "\n");
        b.append("variable2value=" + variable2valueMap + "\n");
        b.append("srcDirs=" + srcDirs + "\n");
        b.append("classDirs=" + classDirs + "\n");
        b.append("inclusionLists=" + inclusionLists + "\n");
        b.append("exclusionLists=" + exclusionLists + "\n");
        System.out.println(b);
    }

    public static IJavaProject getJavaProject(String root)
    {
        IWorkspaceRoot rootWorkspace = ResourcesPlugin.getWorkspace().getRoot();
        IJavaModel javaModel = JavaCore.create(rootWorkspace);
        IJavaProject[] javaProjects;
        try
        {
            javaProjects = javaModel.getJavaProjects();
        }
        catch (JavaModelException e)
        {
            return null;
        }
        for (int i = 0; i < javaProjects.length; i++)
        {
            IJavaProject javaProject = javaProjects[i];
            if (root.equals(javaProject.getPath().toString()))
            {
                return javaProject;
            }
        }      
        return null;
    }

    /**
     * Convert Eclipse project to ant build file. Displays error and success dialogs.
     */
    public static void convert(IResource selectedResource)
    {
        Shell shell = new Shell();
        if (selectedResource == null)
        {
            MessageDialog.openInformation(shell, "eclipse2ant Plug-in", "Please select project first. Then try again.");
            return;
        }
        IJavaProject javaProject = getJavaProject(selectedResource);
        String projectRoot = javaProject.getResource().getLocation().toString();
        if (BuildFileCreator.existsBuildFile(projectRoot))
        {
            if (! MessageDialog.openConfirm(shell, "eclipse2ant Plug-in", "Overwrite existing build.xml?"))
            {
                return;
            }
        }       
        Eclipse2AntAction.convert(javaProject);
        String message = "build.xml was created for project: " + selectedResource.getProject().getName();
        MessageDialog.openInformation(shell, "eclipse2ant Plug-in", message);
    }

    /**
     * Convert Eclipse project to ant build file.
     */
    public static void convert(IJavaProject project)
    {
        Eclipse2AntAction instance = new Eclipse2AntAction();
        instance.init(project);
        try
        {
            BuildFileCreator.create(project.getProject().getName(),
                    instance.projectRoot, instance.rawClassPathEntries,
                    instance.srcDirs,
                    instance.classDirs, instance.inclusionLists,
                    instance.exclusionLists,
                    instance.variable2valueMap,
                    project);
        }
        catch (Exception e)
        {
            AntUIPlugin.log(e);
        }
    }

    public static String removeProjectRoot(String s, IProject project)
    {
        String res = removePrefix(s, "/" + project.getName());
        return removePrefix(res, "/");
    }
    
    public static String removePrefix(String s, String prefix)
    {
        if (s == null)
        {
            return null;
        }
        if (s.startsWith(prefix))
        {
            return s.substring(prefix.length());
        }
        return s;
    }

    public static String removeSuffix(String s, String suffix)
    {
        if (s == null)
        {
            return null;
        }
        if (s.endsWith(suffix))
        {
            return s.substring(0, s.length() - suffix.length());
        }
        return s;
    }
    
    public static String removePrefixAndSuffix(String s, String prefix, String suffix)
    {
        return removePrefix(removeSuffix(s, suffix), prefix);
    }
    
    public static String getClasspath(IJavaProject project)
    {
        Eclipse2AntAction instance = new Eclipse2AntAction();
        instance.init(project);
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < instance.resolvedClassPathEntries.size(); i++)
        {
            String entry = (String) instance.resolvedClassPathEntries.get(i);
            b.append(entry);
            if (i < instance.resolvedClassPathEntries.size() - 1)
            {
                b.append(File.pathSeparatorChar);
            }
        }
        return b.toString();
    }
}
