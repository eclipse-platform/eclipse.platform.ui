/*******************************************************************************
 * Copyright (c) 2004, 2005 Richard Hoefter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Richard Hoefter (richard.hoefter@web.de) - initial API and implementation
 *     IBM Corporation - nlsing and incorporating into Eclipse. 
 *                          Class created from combination of all utility classes of contribution
 *******************************************************************************/

package org.eclipse.ant.internal.ui.datatransfer;

import java.io.StringWriter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.w3c.dom.Document;

/**
 * Eclipse API shortcuts.
 */
public class ExportUtil
{
    private ExportUtil()
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
        String res = removePrefix(file, '/' + project.getName() + '/');
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
                IJavaProject subProject = ExportUtil.getJavaProject(subProjectRoot);
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
            if (result.add(javaProject)) {
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
    
    public static final String NEWLINE = System.getProperty("line.separator"); //$NON-NLS-1$

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
    
    /**
     * Convert document to formatted XML string.
     */
    public static String toString(Document doc) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException
    {
        StringWriter writer = new StringWriter();
        Source source = new DOMSource(doc);
        Result result = new StreamResult(writer);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
        transformer.transform(source, result);
        return writer.toString();
    }

    /**
     * Include a file into an XML document by adding an entity reference.
     * @param doc     XML document
     * @param name    name of the entity reference to create
     * @param file    name of file to include 
     * @return        XML document with entity reference
     */
    public static String addEntity(Document doc, String name, String file) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException
    {
        String xml = toString(doc);
        return addEntity(xml, name, file);
    }
    
    /**
     * Include a file into an XML document by adding an entity reference.
     * @param xml     XML document
     * @param name    name of the entity reference to create
     * @param file    name of file to include 
     * @return        XML document with entity reference
     */
    public static String addEntity(String xml, String name, String file)
    {
        // NOTE: It is not possible to write a DOCTYPE with an internal DTD using transformer.
        //       It is also not possible to write an entity reference with JAXP.
        StringBuffer xmlBuffer = new StringBuffer(xml);
        int index = xmlBuffer.indexOf(ExportUtil.NEWLINE) != -1 ? xmlBuffer.indexOf(ExportUtil.NEWLINE) : 0;
        StringBuffer entity= new StringBuffer();
        entity.append(ExportUtil.NEWLINE);
        entity.append("<!DOCTYPE project [<!ENTITY "); //$NON-NLS-1$
        entity.append(name);
        entity.append(" SYSTEM \"file:"); //$NON-NLS-1$
        entity.append(file);
        entity.append("\">]>"); //$NON-NLS-1$
        xmlBuffer.insert(index, entity.toString());
        index = xmlBuffer.indexOf("basedir") != -1 ? xmlBuffer.indexOf("basedir") : 0; //$NON-NLS-1$ //$NON-NLS-2$
        index = xmlBuffer.indexOf(ExportUtil.NEWLINE, index);
        if (index != -1)
        {
            xmlBuffer.insert(index, ExportUtil.NEWLINE + "    &" + name + ';'); //$NON-NLS-1$
        }
        return xmlBuffer.toString();
    }
}
