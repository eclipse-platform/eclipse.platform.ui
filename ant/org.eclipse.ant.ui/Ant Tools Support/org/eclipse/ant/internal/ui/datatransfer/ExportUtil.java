/*******************************************************************************
 * Copyright (c) 2004, 2005 Richard Hoefter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Richard Hoefter (richard.hoefter@web.de) - initial API and implementation, bug 95300, bug 95297 
 *     IBM Corporation - nlsing and incorporating into Eclipse. 
 *                          Class created from combination of all utility classes of contribution
 *******************************************************************************/

package org.eclipse.ant.internal.ui.datatransfer;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IFileEditorInput;
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
        Set projects = new TreeSet(getJavaProjectComparator());
        IClasspathEntry entries[] = project.getRawClasspath();
        addClasspathProjects(projects, project, entries);
        return projects;
    }
    
    private static void addClasspathProjects(Set projects, IJavaProject project, IClasspathEntry[] entries) throws JavaModelException {
        for (int i = 0; i < entries.length; i++)
        {
            IClasspathEntry classpathEntry = entries[i];
            if (classpathEntry.getContentKind() == IPackageFragmentRoot.K_SOURCE) {
                if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                    // found required project on build path
                    String subProjectRoot = classpathEntry.getPath().toString();
                    IJavaProject subProject = ExportUtil.getJavaProject(subProjectRoot);
                    projects.add(subProject);
                } else if ( classpathEntry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                    IClasspathContainer container = JavaCore.getClasspathContainer(classpathEntry.getPath(), project);
                    IClasspathEntry containerEntries[] = container.getClasspathEntries();
                    addClasspathProjects(projects, project, containerEntries);
                }
            }
        }
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
     * <p>NOTE: Copied from {@link from org.eclipse.jdt.internal.junit.launcher.JUnitBaseLaunchConfiguration#findTestsInContainer}
     * to use non-api functionality
     *  
     * @param containerHandle    project, package or source folder
     */
    public static IType[] findTestsInContainer(String containerHandle)
    {
        IJavaElement container= JavaCore.create(containerHandle);
        if (container == null) {
            return new IType[0];
        }
        final Object[] elements = new Object[] { container };
        final Set result= new HashSet();            
        if (elements.length > 0) {
            doFindTests(elements, result);
        }
        return (IType[]) result.toArray(new IType[result.size()]) ;
    }
    
    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    // to avoid dependency to other component
    private static class JUnitSearchResultCollector extends SearchRequestor {
        List fList;
        Set fFailed= new HashSet();
        Set fMatches= new HashSet();
        
        public JUnitSearchResultCollector(List list) {
            fList= list;
        }
        
        public void acceptSearchMatch(SearchMatch match) throws CoreException {
            Object enclosingElement= match.getElement();
            if (!(enclosingElement instanceof IMethod)) 
                return;
            
            IMethod method= (IMethod)enclosingElement;      
            
            IType declaringType= method.getDeclaringType();
            if (fMatches.contains(declaringType) || fFailed.contains(declaringType))
                return;
            if (!hasSuiteMethod(declaringType) && !isTestType(declaringType)) {
                fFailed.add(declaringType);
                return;
            }
            fMatches.add(declaringType);
        }
        
        public void endReporting() {
            fList.addAll(fMatches);
        }
    }
    
    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static List searchMethod(final IJavaSearchScope scope) throws CoreException {
        final List typesFound= new ArrayList(200);  
        searchMethod(typesFound, scope);
        return typesFound;  
    }

    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static List searchMethod(final List v, IJavaSearchScope scope) throws CoreException {      
        SearchRequestor requestor= new JUnitSearchResultCollector(v);
        int matchRule= SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH;
        SearchPattern suitePattern= SearchPattern.createPattern("suite() Test", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, matchRule); //$NON-NLS-1$
        SearchParticipant[] participants= new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
        new SearchEngine().search(suitePattern, participants, scope, requestor, null); 
        return v;
    }

    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static void doFindTests(Object[] elements, Set result) {
        int nElements= elements.length;
        for (int i= 0; i < nElements; i++) {
            try {
                collectTypes(elements[i], result);
            } catch (CoreException e) {
                AntUIPlugin.log(e.getStatus());
            }
        }
    }

    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static void collectTypes(Object element, Set result) throws CoreException/*, InvocationTargetException*/ {
        element= computeScope(element);
        while((element instanceof ISourceReference) && !(element instanceof ICompilationUnit)) {
            if(element instanceof IType) {
                if (hasSuiteMethod((IType)element) || isTestType((IType)element)) {
                    result.add(element);
                    return;
                }
            }
            element= ((IJavaElement)element).getParent();
        }
        if (element instanceof ICompilationUnit) {
            ICompilationUnit cu= (ICompilationUnit)element;
            IType[] types= cu.getAllTypes();

            for (int i= 0; i < types.length; i++) {
                if (hasSuiteMethod(types[i])  || isTestType(types[i]))
                    result.add(types[i]);
            }
        } 
        else if (element instanceof IJavaElement) {
            List testCases= findTestCases((IJavaElement)element);
            List suiteMethods= searchSuiteMethods((IJavaElement)element);            
            while (!suiteMethods.isEmpty()) {
                if (!testCases.contains(suiteMethods.get(0))) {
                    testCases.add(suiteMethods.get(0));
                }
                suiteMethods.remove(0);
            }
            result.addAll(testCases);
        }
    }

    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static List findTestCases(IJavaElement element) throws JavaModelException {
        List found = new ArrayList();
        IJavaProject javaProject= element.getJavaProject();

        IType testCaseType = testCaseType(javaProject); 
        if (testCaseType == null)
            return found;
        
        IType[] subtypes= javaProject.newTypeHierarchy(testCaseType, getRegion(javaProject), null).getAllSubtypes(testCaseType);
            
        if (subtypes == null)
            throw new JavaModelException(new CoreException(new Status(IStatus.ERROR, AntUIPlugin.PI_ANTUI, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE, null/*JUnitMessages.JUnitBaseLaunchConfiguration_error_notests*/, null))); 

        for (int i = 0; i < subtypes.length; i++) {
            try {
                if (element.equals(subtypes[i].getAncestor(element.getElementType())) && hasValidModifiers(subtypes[i]))
                    found.add(subtypes[i]);
            } catch (JavaModelException e) {
                AntUIPlugin.log(e.getStatus());
            }
        }
        return found;
    }

    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static IType testCaseType(IJavaProject javaProject) {
        try {
            return javaProject.findType("junit.framework.TestCase"); //$NON-NLS-1$
        } catch (JavaModelException e) {
            AntUIPlugin.log(e.getStatus());
            return null;
        } 
    }

    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static IRegion getRegion(IJavaProject javaProject) throws JavaModelException{
        IRegion region = JavaCore.newRegion();
        IJavaElement[] elements= javaProject.getChildren();
        for(int i=0; i<elements.length; i++) {
            if (((IPackageFragmentRoot)elements[i]).isArchive())
                continue;
            region.add(elements[i]);
        }
        return region;
    }

    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static Object computeScope(Object element) throws JavaModelException {
        if (element instanceof IFileEditorInput)
            element= ((IFileEditorInput)element).getFile();
        if (element instanceof IResource)
            element= JavaCore.create((IResource)element);
        if (element instanceof IClassFile) {
            IClassFile cf= (IClassFile)element;
            element= cf.getType();
        }
        return element;
    }

    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static List searchSuiteMethods(IJavaElement element) throws CoreException {    
        // fix for bug 36449  JUnit should constrain tests to selected project [JUnit]
        IJavaSearchScope scope= SearchEngine.createJavaSearchScope(new IJavaElement[] { element },
                IJavaSearchScope.SOURCES);
        return searchMethod(scope);
    }

    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static boolean hasSuiteMethod(IType type) throws JavaModelException {
        IMethod method= type.getMethod("suite", new String[0]); //$NON-NLS-1$
        if (method == null || !method.exists()) 
            return false;
        
        if (!Flags.isStatic(method.getFlags()) ||   
            !Flags.isPublic(method.getFlags()) ||           
            !Flags.isPublic(method.getDeclaringType().getFlags())) { 
            return false;
        }
        if (!Signature.getSimpleName(Signature.toString(method.getReturnType())).equals("Test" /*JUnitPlugin.SIMPLE_TEST_INTERFACE_NAME*/)) { //$NON-NLS-1$
            return false;
        }
        return true;
    }

    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static boolean isTestType(IType type) throws JavaModelException {
        if (!hasValidModifiers(type))
            return false;
        
        IType[] interfaces= type.newSupertypeHierarchy(null).getAllSuperInterfaces(type);
        for (int i= 0; i < interfaces.length; i++)
            if(interfaces[i].getFullyQualifiedName('.').equals("junit.framework.Test" /*JUnitPlugin.TEST_INTERFACE_NAME*/)) //$NON-NLS-1$
                return true;
        return false;
    }

    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static boolean hasValidModifiers(IType type) throws JavaModelException {
        if (Flags.isAbstract(type.getFlags())) 
            return false;
        if (!Flags.isPublic(type.getFlags())) 
            return false;
        return true;
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
        // NOTE: There are different transformer implementations in the wild, which are configured differently
        //       regarding the indent size:
        //       Java 1.4: org.apache.xalan.transformer.TransformerIdentityImpl 
        //       Java 1.5: com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl

        StringWriter writer = new StringWriter();
        Source source = new DOMSource(doc);
        Result result = new StreamResult(writer);
        TransformerFactory factory = TransformerFactory.newInstance();
        boolean indentFallback = false;
        try
        {
            // indent using TransformerImpl
            factory.setAttribute("indent-number", "4"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (IllegalArgumentException e)
        {
            // option not supported, set indent size below
            indentFallback = true;
        }
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        if (indentFallback)
        {
            // indent using TransformerIdentityImpl
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
        }
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