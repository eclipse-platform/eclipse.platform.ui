/*******************************************************************************
 * Copyright (c) 2004, 2005 Richard Hoefter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Richard Hoefter (richard.hoefter@web.de) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.datatransfer;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Eclipse API shortcuts.
 */
public class EclipseUtil
{
    private EclipseUtil()
    {
    }

    /**
     * Get resource from selection.
     */
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

    /**
     * Get Java project from resource.
     */
    public static IJavaProject getJavaProjectByName(String name)
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
            if (name.equals(javaProject.getProject().getName()))
            {
                return javaProject;
            }
        }      
        return null;
    }

    /**
     * Get project root for given project.
     */
    public static String getProjectRoot(IJavaProject project)
    {
        return project.getResource().getLocation().toString();
    }

    /**
     * Convert relative path to absolute path. Path must include project and resource name, otherwise
     * returns null.
     * @param path path which is relative to project root. 
     * @return full qualified path including project root
     */
    public static String resolve(IPath path)
    {
        if (path == null)
        {
            return null;
        }      
        IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
        if (folder.exists()) {
            return folder.getLocation().toString();
        }
        
        return null;
    }

    /**
     * Get Java project for given root.
     */
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
     * Remove project root from given project file.
     */
    public static String removeProjectRoot(String file, IProject project)
    {
        String res = StringUtil.removePrefix(file, '/' + project.getName() + '/');
        if (res.equals('/' + project.getName()))
        {
            return "."; //$NON-NLS-1$
        }
        return res;
    }

    /**
     * Remove project root from given project file.
     * @param newProjectRoot    replace project root, e.g. with a variable ${project.location}
     */
    public static String replaceProjectRoot(String file, IProject project, String newProjectRoot)
    {
        String res = removeProjectRoot(file, project);
        if (res.equals(".")) //$NON-NLS-1$
        {
            return newProjectRoot;
        }
        if (!res.equals(file))
        {
            return newProjectRoot + '/' + res;
        }
        return res;
    }
    
    /**
     * Get for given project all directly dependent projects.
     * 
     * @return set of IJavaProject objects
     */
    public static Set getClasspathProjects(IJavaProject project) throws JavaModelException
    {
        Set result = new TreeSet(getJavaProjectComparator());
        IClasspathEntry entries[] = project.getRawClasspath();
        for (int i = 0; i < entries.length; i++)
        {
            if (entries[i].getContentKind() == IPackageFragmentRoot.K_SOURCE &&
                entries[i].getEntryKind() == IClasspathEntry.CPE_PROJECT)
            {
                // found required project on build path
                String subProjectRoot = entries[i].getPath().toString();
                IJavaProject subProject = EclipseUtil.getJavaProject(subProjectRoot);
                result.add(subProject);
            }
        }
        return result;
    }
    
    /**
     * Get for given project all directly and indirectly dependent projects.
     * 
     * @return set of IJavaProject objects
     */
    public static Set getClasspathProjectsRecursive(IJavaProject project) throws JavaModelException
    {
        Set result = new TreeSet(getJavaProjectComparator());
        getClasspathProjectsRecursive(project, result);
        return result;
    }
    
    private static void getClasspathProjectsRecursive(IJavaProject project, Set result) throws JavaModelException
    {
        Set projects = getClasspathProjects(project);
        for (Iterator iter = projects.iterator(); iter.hasNext();)
        {
            IJavaProject javaProject = (IJavaProject) iter.next();
            if (! result.contains(javaProject))
            {
                result.add(javaProject);
                getClasspathProjectsRecursive(javaProject, result); // recursion
            }
        }
    }
    
    /**
     * Check if given project has a cyclic dependency.
     * 
     * <p>See org.eclipse.jdt.core.tests.model.ClasspathTests.numberOfCycleMarkers.
     */
    public static boolean hasCyclicDependency(IJavaProject javaProject)
            throws CoreException
    {
        IMarker[] markers = javaProject.getProject().findMarkers(
                IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false,
                IResource.DEPTH_ONE);
        for (int i = 0; i < markers.length; i++)
        {
            IMarker marker = markers[i];
            String cycleAttr = (String) marker.getAttribute(IJavaModelMarker.CYCLE_DETECTED);
            if (cycleAttr != null && cycleAttr.equals("true")) //$NON-NLS-1$
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Find JUnit tests. Same tests are also returned by Eclipse run configuration wizard.
     * 
     * <p>NOTE: You might see a dialog flashing up for a very short time.
     *  
     * @param containerHandle    project, package or source folder
     * 
     * @see org.eclipse.jdt.internal.junit.launcher.JUnitBaseLaunchConfiguration
     */
    public static IType[] findTestsInContainer(String containerHandle)
    {
//        IJavaElement container;
//        try
//        {
//            container = JavaCore.create(containerHandle);
//        }
//        catch (Exception e)
//        {
//            return new IType[0];
//        }
//        try
//        {
//            return TestSearchEngine.findTests(new Object[] { container });
//        }
//        catch (Exception e)
//        {
//            return new IType[0];
//        }
        return new IType[0];
    }

    /**
     * Compares projects by project name.
     */
    public static Comparator getJavaProjectComparator()
    {
        if (javaProjectComparator == null)
        {
            javaProjectComparator = new JavaProjectComparator();
        }
        return javaProjectComparator;
    }
    
    private static Comparator javaProjectComparator;
    
    private static class JavaProjectComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            IJavaProject j1 = (IJavaProject) o1;
            IJavaProject j2 = (IJavaProject) o2;
            return j1.getProject().getName().compareTo(j2.getProject().getName());
        }
        
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            return compare(this, obj) == 0;
        }

    }
}
