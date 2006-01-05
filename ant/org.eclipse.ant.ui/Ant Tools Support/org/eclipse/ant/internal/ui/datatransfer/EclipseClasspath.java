/*******************************************************************************
 * Copyright (c) 2004, 2006 Richard Hoefter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Richard Hoefter (richard.hoefter@web.de) - initial API and implementation, bug 95298
 *     IBM Corporation - nlsing and incorporating into Eclipse, bug 108276
 *******************************************************************************/

package org.eclipse.ant.internal.ui.datatransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Class to store classpath settings of an Eclipse project.
 * 
 * <p>NOTE: The constructed classpath does not contain items of the subprojects.
 * Instead create an object of this class for each project returned by
 * {@link eclipse2ant.util.EclipseUtil#getClasspathProjectsRecursive(IJavaProject)}.
 */
public class EclipseClasspath
{
    public List srcDirs = new ArrayList();
    public List classDirs = new ArrayList();
    public List inclusionLists = new ArrayList();
    public List exclusionLists = new ArrayList();
    
    public Map class2sourcesMap = new TreeMap();
    public Map class2includesMap = new TreeMap();
    public Map class2excludesMap = new TreeMap();
    
    public Map variable2valueMap = new TreeMap();
    public List rawClassPathEntries = new ArrayList();
    public List rawClassPathEntriesAbsolute = new ArrayList();
 
    private IJavaProject project;
    private String newProjectRoot;
    private String jreLocation;
        
    public EclipseClasspath(IJavaProject project) throws JavaModelException
    {
        this(project, null);
    }

    /**
     * @param newProjectRoot replace project root, e.g. with a variable ${project.location}
     */
    public EclipseClasspath(IJavaProject project, String newProjectRoot) throws JavaModelException
    {
        this.project = project;
        jreLocation = getJRELocation();
        this.newProjectRoot = newProjectRoot;
        IClasspathEntry entries[] = project.getRawClasspath();
        for (int i = 0; i < entries.length; i++)
        {
            handleSources(entries[i]);
            handleVariables(entries[i]);
            handleJars(entries[i]);
            handleUserLibraries(entries[i]);
        }
        addClasses();
        initClassMaps();
    }
    
    private String getJRELocation() {
    	try {
    		IVMInstall install= JavaRuntime.getVMInstall(project);
    		if (install != null) {
	    		File installLocation= install.getInstallLocation();
	    		if (installLocation != null) {
	    			return new Path(installLocation.toString()).toString();
	    		}
    		}
		} catch (CoreException e) {
			AntUIPlugin.log(e);
		}
		return ""; //$NON-NLS-1$
	}

    /**
     * Get class directories without duplicates.
     */
    public List getClassDirsUnique()
    {
        return removeDuplicates(classDirs);
    }
    
    private void handleSources(IClasspathEntry entry) throws JavaModelException
    {
        String projectRoot = ExportUtil.getProjectRoot(project);
        String defaultClassDir = project.getOutputLocation().toString();
        if (entry.getContentKind() == IPackageFragmentRoot.K_SOURCE &&
            entry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
        {
            // found source path
            IPath srcDirPath = entry.getPath();
            IPath classDirPath = entry.getOutputLocation();
            String srcDir = ExportUtil.removeProjectRoot((srcDirPath != null) ? srcDirPath.toString() : projectRoot, project.getProject());
            String classDir = ExportUtil.removeProjectRoot((classDirPath != null) ? classDirPath.toString() : defaultClassDir, project.getProject());
            srcDirs.add(srcDir);
            classDirs.add(classDir);
            IPath[] inclusions = entry.getInclusionPatterns();                   
            List inclusionList = new ArrayList();
            for (int j = 0; j < inclusions.length; j++)
            {
                if (inclusions[j] != null)
                {
                    inclusionList.add(ExportUtil.removeProjectRoot(inclusions[j].toString(), project.getProject()));
                }
            }
            inclusionLists.add(inclusionList);
            IPath[] exclusions = entry.getExclusionPatterns();
            List exclusionList = new ArrayList();
            for (int j = 0; j < exclusions.length; j++)
            {
                if (exclusions[j] != null)
                {
                    exclusionList.add(ExportUtil.removeProjectRoot(exclusions[j].toString(), project.getProject()));
                }
            }
            exclusionLists.add(exclusionList);
        }
    }
    
    /**
     * Convert sources/classes directories and inclusion/exclusion filters to map representation.
     * As several source directories may compile to same class directory this is a useful conversion
     * of the result of {@link #handleSources}.
     */
    private void initClassMaps()
    {
        for (int i = 0; i < srcDirs.size(); i++)
        {
            String srcDir = (String) srcDirs.get(i);
            String classDir = (String) classDirs.get(i);
            List includeList = (List) inclusionLists.get(i);
            List excludeList = (List) exclusionLists.get(i);
            Set sources = (Set) class2sourcesMap.get(classDir);
            if (sources == null)
            {
                sources = new TreeSet();
            }
            sources.add(srcDir);
            class2sourcesMap.put(classDir, sources);
            Set includes = (Set) class2includesMap.get(classDir);
            if (includes == null)
            {
                includes = new TreeSet();
            }
            includes.addAll(includeList);
            class2includesMap.put(classDir, includes);
            Set excludes = (Set) class2excludesMap.get(classDir);
            if (excludes == null)
            {
                excludes = new TreeSet();
            }
            excludes.addAll(excludeList);
            class2excludesMap.put(classDir, excludes);
        }
    }
    
    private void handleJars(IClasspathEntry entry)
    {
        if (entry.getContentKind() == IPackageFragmentRoot.K_BINARY &&
            entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
        {
            String jarFile = entry.getPath().toString();
            // ignore JRE libraries
            if (jarFile.startsWith(jreLocation))
            {
                return;
            }
            StringBuffer jarFileBuffer = new StringBuffer();
            StringBuffer jarFileAbsoluteBuffer = new StringBuffer();
            String jarFileAbsolute = ExportUtil.resolve(entry.getPath());
            if (jarFileAbsolute == null)
            {
                jarFileAbsolute = jarFile; // jarFile was already absolute
                if (handleSubProjectClassesDirectory(jarFile, jarFileBuffer, jarFileAbsoluteBuffer))
                {
                    jarFile = jarFileBuffer.toString();
                    jarFileAbsolute = jarFileAbsoluteBuffer.toString();
                }
            }
            String jarFileOld = jarFile;
            if (newProjectRoot == null)
            {
                jarFile = ExportUtil.removeProjectRoot(jarFile, project.getProject());
            }
            else
            {
                jarFile = ExportUtil.replaceProjectRoot(jarFile, project.getProject(), newProjectRoot);
            }
            if (jarFile.equals(jarFileOld))
            {
                if (handleSubProjectClassesDirectory(jarFile, jarFileBuffer, jarFileAbsoluteBuffer))
                {
                    jarFile = jarFileBuffer.toString();
                    jarFileAbsolute = jarFileAbsoluteBuffer.toString();
                }
            }
            rawClassPathEntries.add(jarFile);
            rawClassPathEntriesAbsolute.add(jarFileAbsolute);
        }
    }

    /**
     * Checks if file is a class directory of a subproject and fills string buffers with resolved values.
     * @param file               file to check
     * @param jarFile            filled with file location with varibale reference ${project.location},
     *                           which is also added to variable2valueMap
     * @param jarFileAbsolute    filled with absolute file location
     * @return                   true if file is a classes directory 
     */
    private boolean handleSubProjectClassesDirectory(String file, StringBuffer jarFile, StringBuffer jarFileAbsolute)
    {
        // class directory of a subproject?
        if (file != null && file.indexOf('/') == 0)
        {
            int i = file.indexOf("/", 1); //$NON-NLS-1$
            i = (i != -1) ? i : file.length(); 
            String subproject = file.substring(1, i);
            IJavaProject javaproject = ExportUtil.getJavaProjectByName(subproject);
            if (javaproject != null)
            {
                jarFile.setLength(0);
                jarFileAbsolute.setLength(0);
                String location = javaproject.getProject().getName() + ".location"; //$NON-NLS-1$
                jarFileAbsolute.append(ExportUtil.replaceProjectRoot(file, javaproject.getProject(), ExportUtil.getProjectRoot(javaproject)));
                jarFile.append(ExportUtil.replaceProjectRoot(file, javaproject.getProject(), "${" + location + "}")); //$NON-NLS-1$ //$NON-NLS-2$
                String projectRoot= ExportUtil.getProjectRoot(project);
                if (newProjectRoot != null && !newProjectRoot.startsWith("${")) { //$NON-NLS-1$
                    projectRoot= newProjectRoot;
                }
                String relativePath= BuildFileCreator.getRelativePath(ExportUtil.getProjectRoot(javaproject), projectRoot);
                variable2valueMap.put(location, relativePath);
                return true;
            }
        }
        return false;
    }

    private void handleVariables(IClasspathEntry entry)
    {
        if (entry.getContentKind() == IPackageFragmentRoot.K_SOURCE &&
            entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE)
        {
            // found variable
            String e = entry.getPath().toString();
            int index = e.indexOf('/');
            if (index == -1)
            {
                index = e.indexOf('\\');
            }
            String variable = e;
            String path = ""; //$NON-NLS-1$
            if (index != -1)
            {
                variable = e.substring(0, index);
                path = e.substring(index);
            }
            String value = JavaCore.getClasspathVariable(variable).toString();
            variable2valueMap.put(variable, value);
            rawClassPathEntriesAbsolute.add(value + path);                  
            rawClassPathEntries.add("${" + variable + "}" + path); //$NON-NLS-1$ //$NON-NLS-2$
        }  
    }
    
    private void handleUserLibraries(IClasspathEntry entry) throws JavaModelException
    {
        if (entry.getContentKind() == IPackageFragmentRoot.K_SOURCE &&
            entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER)
        {
            // found (user) library
            // String e = entry.getPath().toString(); // org.eclipse.jdt.USER_LIBRARY/MyLib
            //JavaCore.USER_LIBRARY_CONTAINER_ID
            IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
            IClasspathEntry entries[] = container.getClasspathEntries();
            for (int i = 0; i < entries.length; i++)
            {
                handleJars(entries[i]);
            }
        }
    }
    
    /**
     *  Add classDirs in front of classpath.
     */
    private void addClasses()
    {
        for (Iterator iter = classDirs.iterator(); iter.hasNext();)
        {
            String classDir = (String) iter.next();
            if (newProjectRoot != null)
            {
                classDir = newProjectRoot + '/' + classDir;
            }
            rawClassPathEntries.add(0, classDir);
            rawClassPathEntriesAbsolute.add(0, classDir);
        }
    }

    /**
     * Get classpath for given project.
     */
    public static String getClasspath(IJavaProject project) throws JavaModelException
    {
        List items = EclipseClasspath.getClasspathList(project);
        return toString(items, File.pathSeparator);
    }

    /**
     * Get classpath for given project.
     */
    public static String getClasspath(IJavaProject project, boolean includeSubProjects) throws JavaModelException
    {
        List items = EclipseClasspath.getClasspathList(project, includeSubProjects);
        return toString(items, File.pathSeparator);
    }

    /**
     * Get classpath for given project.
     */
    public static List getClasspathList(IJavaProject project) throws JavaModelException
    {
        EclipseClasspath instance = new EclipseClasspath(project, ExportUtil.getProjectRoot(project));
        return instance.removeDuplicates(instance.rawClassPathEntriesAbsolute);
    }

    /**
     * Get classpath for given project.
     */
    public static List getClasspathList(IJavaProject project, boolean includeSubProjects) throws JavaModelException
    {
        EclipseClasspath instance = new EclipseClasspath(project, ExportUtil.getProjectRoot(project));
        List classpath = instance.rawClassPathEntriesAbsolute;
        if (!includeSubProjects)
        {
            return instance.removeDuplicates(classpath);            
        }
        Set subprojects = ExportUtil.getClasspathProjectsRecursive(project);
        for (Iterator iter = subprojects.iterator(); iter.hasNext();)
        {
            IJavaProject subproject = (IJavaProject) iter.next();
            instance = new EclipseClasspath(subproject, ExportUtil.getProjectRoot(subproject));
            classpath.addAll(instance.rawClassPathEntriesAbsolute);
        }
        return instance.removeDuplicates(classpath);
    }
    
    public static String toString(Collection c, String separator)
    {
        StringBuffer b = new StringBuffer();
        for (Iterator iter = c.iterator(); iter.hasNext();)
        {
            b.append((String) iter.next());
            b.append(separator);
        }
        if (c.size() > 0) {
            b.delete(b.length() - separator.length(), b.length());
        }
        return b.toString();
    }
    
    /**
     * Remove duplicates preserving original order.
     * @param l list to remove duplicates from
     * @return new list without duplicates 
     */
    protected List removeDuplicates(List l)
    {
        List res = new ArrayList();
        for (Iterator iter = l.iterator(); iter.hasNext();)
        {
            Object element = iter.next();
            if (!res.contains(element))
            {
                res.add(element);
            }
        }
        return res;
    }
}