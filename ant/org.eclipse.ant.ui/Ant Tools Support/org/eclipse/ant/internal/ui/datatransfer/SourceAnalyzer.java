/*******************************************************************************
 * Copyright (c) 2005, 2011 Richard Hoefter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Richard Hoefter (richard.hoefter@web.de) - initial API and implementation, bug 313386
 *     IBM Corporation - incorporating into Eclipse
 *******************************************************************************/

package org.eclipse.ant.internal.ui.datatransfer;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Provides a method to analyze sources if it is possible to export projects to
 * an Ant buildfile which compiles correctly.
 */
public class SourceAnalyzer {
    
    /**
     * Utility class.
     */
    private SourceAnalyzer() {

    }

    /**
     * Check if source directories of project has cycles or if there are
     * dependencies between them that are not conform with classpath order.
     * 
     * <p>
     * NOTE: Unused references in classes are not considered if they cause
     * cycles or classpath order problems. This is because this class analyzes
     * the bytecode and indeed the compiler throws unused references away.
     */
    public static void checkCycles(IJavaProject currentProject,
            EclipseClasspath classpath, Shell shell) {

        StringBuffer message = new StringBuffer();
        Map src2dir = new TreeMap(); // map string to string
        Map srcdir2classes = new TreeMap(); // map string to Set of strings

        determineSources(currentProject, classpath, src2dir, srcdir2classes);

        Map srcdir2sourcedirs = determineRequiredSrcDirs(src2dir,
                srcdir2classes);
        String projectName = currentProject.getProject().getName();

        List cycle = new ArrayList();
        if (isCyclic(srcdir2sourcedirs, cycle)) {
            showCycleWarning(projectName, shell, cycle, message);
            return;
        }

        checkBuildOrder(classpath, projectName, shell, srcdir2sourcedirs);
    }

    /**
     * Determine all sources belonging to a source directory.
     */
    private static void determineSources(IJavaProject currentProject,
            EclipseClasspath classpath, Map src2dir, Map srcdir2classes) {

        for (int i = 0; i < classpath.srcDirs.size(); i++) {
            String srcDir = (String) classpath.srcDirs.get(i);
            String classDir = (String) classpath.classDirs.get(i);
            if (EclipseClasspath.isReference(srcDir)) {
                continue;
            }
            File dir;
            if (srcDir.equals(".")) { //$NON-NLS-1$
                dir = currentProject.getResource().getLocation().toFile();
            } else {
                IFile file = currentProject.getProject().getFile(srcDir);
                dir = file.getLocation().toFile();
            }
            if (EclipseClasspath.isLinkedResource(srcDir)) {
                String link = classpath.resolveLinkedResource(srcDir);
                dir = new File(link);
                if (!dir.isAbsolute()) {
                    // make absolute
                    dir = new File(ExportUtil.getProjectRoot(currentProject), link);
                }
            }
            Set sources = findFiles(dir, ".java"); //$NON-NLS-1$

            // find all required classfiles for each source directory
            for (Iterator iter = sources.iterator(); iter.hasNext();) {
                String srcFile = (String) iter.next();
                src2dir.put(srcFile, srcDir);
                IFile classFile = currentProject.getProject().getFile(
                        classDir + '/' + srcFile + ".class"); //$NON-NLS-1$
                if (!classFile.exists()) {
                    // project was not compiled, check not possible
                    continue;
                }
                Set classes = (Set) srcdir2classes.get(srcDir);
                if (classes == null) {
                    classes = new TreeSet();
                }
                classes.addAll(getRequiredClasses(classFile));
                srcdir2classes.put(srcDir, classes);
            }
        }
    }

    /**
     * Determine for each source directory which other source directories it
     * requires.
     * 
     * @return Map string to Set of strings. (Maps source dir to Set of required
     *         source dirs.)
     */
    private static Map determineRequiredSrcDirs(Map src2dir, Map srcdir2classes) {

        Map srcdir2sourcedirs = new TreeMap(); // map string to Set of strings
        for (Iterator iter = srcdir2classes.keySet().iterator(); iter.hasNext();) {
            String srcDir = (String) iter.next();
            Set classes = (Set) srcdir2classes.get(srcDir);
            for (Iterator iterator = classes.iterator(); iterator.hasNext();) {
                String classname = (String) iterator.next();
                String classsrc = (String) src2dir.get(classname);
                // don't add reference to itself
                if (classsrc != null && !classsrc.equals(srcDir)) {
                    Set sourcedirs = (Set) srcdir2sourcedirs.get(srcDir);
                    if (sourcedirs == null) {
                        sourcedirs = new TreeSet();
                    }
                    sourcedirs.add(classsrc);
                    srcdir2sourcedirs.put(srcDir, sourcedirs);
                }
            }
        }
        return srcdir2sourcedirs;
    }

    private static void showCycleWarning(String projectName, Shell shell,
            List cycle, StringBuffer message) {

        String m = MessageFormat.format(DataTransferMessages.SourceAnalyzer_0,
                new String[] { projectName });
        message.append(m);
        message.append(ExportUtil.NEWLINE);

        // print cycle path
        for (Iterator iter = cycle.iterator(); iter.hasNext();) {
            String s = (String) iter.next();
            s = EclipseClasspath.getLinkedResourceName(s);
            message.append(s);
            message.append(" -> "); //$NON-NLS-1$
        }
        message.append(EclipseClasspath.getLinkedResourceName((String) cycle
                .get(0)));

        MessageDialog.openWarning(shell, DataTransferMessages.SourceAnalyzer_1,
                message.toString());
    }

    /**
     * Check if build order is correct.
     */
    private static void checkBuildOrder(EclipseClasspath classpath,
            String projectName, Shell shell, Map srcdir2sourcedirs) {

        for (Iterator iter = srcdir2sourcedirs.keySet().iterator(); iter
                .hasNext();) {
            String srcdir = (String) iter.next();
            Set sourcedirs = (Set) srcdir2sourcedirs.get(srcdir);
            int classpathIndex = classpath.srcDirs.indexOf(srcdir);
            for (Iterator iterator = sourcedirs.iterator(); iterator.hasNext();) {
                String requiredSrc = (String) iterator.next();
                int i = classpath.srcDirs.indexOf(requiredSrc);
                if (i > classpathIndex) // wrong order
                {
                    String s = MessageFormat.format(
                            DataTransferMessages.SourceAnalyzer_3,
                            new String[] { projectName });

                    MessageDialog.openWarning(shell,
                            DataTransferMessages.SourceAnalyzer_2, s
                                    + ExportUtil.NEWLINE + requiredSrc
                                    + " <-> " + srcdir //$NON-NLS-1$
                                    + ExportUtil.NEWLINE);
                    break;
                }
            }
        }
    }

    /**
     * Find all classes that are required by given class file.
     * 
     * @param file
     *            a ".class" file
     * @return set of strings, each contains a full qualified classname (forward
     *         slash as package separator)
     */
    public static Set getRequiredClasses(IFile file) {

        Set classes = new TreeSet();
        IClassFile classFile = JavaCore.createClassFileFrom(file);
        IClassFileReader reader = ToolFactory.createDefaultClassFileReader(
                classFile, IClassFileReader.CONSTANT_POOL);
        if (reader == null) {
            // class not compiled
            return classes;
        }
        IConstantPool pool = reader.getConstantPool();
        for (int i = 0; i < pool.getConstantPoolCount(); i++) {
            if (pool.getEntryKind(i) == IConstantPoolConstant.CONSTANT_Class) {
                IConstantPoolEntry entry = pool.decodeEntry(i);
                String classname = new String(entry.getClassInfoName());
                // don't return inner classes
                int index = classname.indexOf('$');
                if (index != -1) {
                    classname = classname.substring(0, index);
                }
                classes.add(classname);
            }
        }
        return classes;
    }

    /**
     * Find all files with particular extension under given directory.
     * 
     * @param dir
     *            directory to start search
     * @param extension
     *            extension to search
     * @return filenames relative to dir (without extension and with forward
     *         slashes)
     */
    public static Set findFiles(File dir, String extension) {

        Set visited = new TreeSet();
        findFiles(dir, dir, extension, visited);
        return visited;
    }

    private static void findFiles(File base, File dir, String extension,
            Set visited) {

        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                findFiles(base, children[i], extension, visited);
            }
        } else if (dir.getAbsolutePath().endsWith(extension)) {
            // remove base directory
            String filename = ExportUtil.removePrefixAndSuffix(dir
                    .getAbsolutePath(),
                    base.getAbsolutePath() + File.separator, extension);
            visited.add(filename.replace('\\', '/'));
        }
    }

    /**
     * Check if given graph that is described through a map is cyclic.
     * 
     * @param srcdir2sourcedirs
     *            Maps string to set of strings. The keys are the graph nodes
     *            which are mapped to its neighbours.
     * @param cycle
     *            filled with name of nodes which cause cycle
     */
    private static boolean isCyclic(Map srcdir2sourcedirs, List cycle) {

        return !isAcyclic(srcdir2sourcedirs, cycle);
    }

    private static boolean isAcyclic(Map srcdir2sourcedirs, List cycle) {

        // standard graph theory
        List visited = new ArrayList();
        List exited = new ArrayList();

        for (Iterator iter = srcdir2sourcedirs.keySet().iterator(); iter
                .hasNext();) {
            String srcdir = (String) iter.next();
            if (!visited.contains(srcdir)) {
                if (circleSearch(srcdir, srcdir2sourcedirs, visited, exited,
                        cycle)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean circleSearch(String srcdir, Map srcdir2sourcedirs,
            List visited, List exited, List cycle) {

        boolean res = false;
        visited.add(srcdir);
        cycle.add(srcdir);

        Set sourcedirs = (Set) srcdir2sourcedirs.get(srcdir); // neighbours
        if (sourcedirs != null) {
            for (Iterator iter = sourcedirs.iterator(); iter.hasNext();) {
                String src = (String) iter.next();
                if (!visited.contains(src)) {
                    res = circleSearch(src, srcdir2sourcedirs, visited, exited,
                            cycle);
                } else if (!exited.contains(src)) {
                    res = true;
                }
                if (res) {
                    break;
                }
            }
        }
        if (!res) {
            cycle.clear();
        }
        exited.add(srcdir);
        return res;
    }
}
