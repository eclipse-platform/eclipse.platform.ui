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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.ui.IFileEditorInput;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
     * Convert Eclipse path to absolute filename. 
     * @param file      project root optionally followed by resource name
     * @return full qualified path
     */
    public static String resolve(IPath file)
    {
        if (file == null)
        {
            return null;
        }
        try
        {
            return ResourcesPlugin.getWorkspace().getRoot().getFile(file).toString();
        }
        catch (IllegalArgumentException e)
        {
            // resource is missing
            String projectName = ExportUtil.removePrefix(file.toString(), "/"); //$NON-NLS-1$
            IJavaProject project = ExportUtil.getJavaProjectByName(projectName);
            return ExportUtil.getProjectRoot(project);
        }
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
        if (newProjectRoot == null)
        {
            return res;
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
    public static List getClasspathProjects(IJavaProject project) throws JavaModelException
    {
        List projects = new ArrayList();
        IClasspathEntry entries[] = project.getRawClasspath();
        addClasspathProjects(projects, entries);
        return projects;
    }
    
    private static void addClasspathProjects(List projects, IClasspathEntry[] entries) {
        for (int i = 0; i < entries.length; i++)
        {
            IClasspathEntry classpathEntry = entries[i];
            if (classpathEntry.getContentKind() == IPackageFragmentRoot.K_SOURCE &&
                classpathEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                    // found required project on build path
                    String subProjectRoot = classpathEntry.getPath().toString();
                    IJavaProject subProject = ExportUtil.getJavaProject(subProjectRoot);
                    // is project available in workspace
                    if (subProject != null)
                    {
                        projects.add(subProject);
                    }
            }
        }
    }
    
    /**
     * Get for given project all directly and indirectly dependent projects.
     * 
     * @return set of IJavaProject objects
     */
    public static List getClasspathProjectsRecursive(IJavaProject project) throws JavaModelException
    {
        LinkedList result = new LinkedList();
        getClasspathProjectsRecursive(project, result);
        return result;
    }
    
    private static void getClasspathProjectsRecursive(IJavaProject project, LinkedList result) throws JavaModelException
    {
        List projects = getClasspathProjects(project);
        for (Iterator iter = projects.iterator(); iter.hasNext();)
        {
            IJavaProject javaProject = (IJavaProject) iter.next();
            if (!result.contains(javaProject)) {
                result.addFirst(javaProject);
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
     * <p>NOTE: Copied from <code>org.eclipse.jdt.internal.junit.launcher.JUnitBaseLaunchConfiguration#findTestsInContainer}</code>
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
        Set fFailed = new HashSet();
        Set fMatches = new HashSet();
        
        public JUnitSearchResultCollector(List list) {
            fList = list;
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
        int matchRule = SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH;
        SearchPattern suitePattern= SearchPattern.createPattern("suite() Test", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, matchRule); //$NON-NLS-1$
        SearchParticipant[] participants= new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
        new SearchEngine().search(suitePattern, participants, scope, requestor, null); 
        return v;
    }

    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static void doFindTests(Object[] elements, Set result) {
        int nElements= elements.length;
        for (int i = 0; i < nElements; i++) {
            try {
                collectTypes(elements[i], result);
            } catch (CoreException e) {
                AntUIPlugin.log(e.getStatus());
            }
        }
    }

    // copied from org.eclipse.jdt.internal.junit.util.TestSearchEngine
    private static void collectTypes(Object element, Set result) throws CoreException/*, InvocationTargetException*/ {
        element = computeScope(element);
        while ((element instanceof ISourceReference) && !(element instanceof ICompilationUnit)) {
            if (element instanceof IType) {
                if (hasSuiteMethod((IType)element) || isTestType((IType)element)) {
                    result.add(element);
                    return;
                }
            }
            element = ((IJavaElement)element).getParent();
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
    public static synchronized Comparator getJavaProjectComparator()
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
    
    /**
     * Compares objects by classname.
     */
    public static synchronized Comparator getClassnameComparator()
    {
        if (classnameComparator == null)
        {
            classnameComparator = new ClassnameComparator();
        }
        return classnameComparator;
    }
    
    private static Comparator classnameComparator;
    
    private static class ClassnameComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            return o1.getClass().getName().compareTo(o2.getClass().getName());
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

    /**
     * Compares IFile objects.
     */
    public static synchronized Comparator getIFileComparator()
    {
        if (fileComparator == null)
        {
            fileComparator = new IFileComparator();
        }
        return fileComparator;
    }
    
    private static Comparator fileComparator;
    
    private static class IFileComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            IFile f1 = (IFile) o1;
            IFile f2 = (IFile) o2;
            return f1.toString().compareTo(f2.toString());
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
    
    /**
     * Compares IType objects.
     */
    public static synchronized Comparator getITypeComparator()
    {
        if (typeComparator == null)
        {
            typeComparator = new TypeComparator();
        }
        return typeComparator;
    }
    
    private static Comparator typeComparator;
    
    private static class TypeComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            IType t1 = (IType) o1;
            IType t2 = (IType) o2;
            return t1.getFullyQualifiedName().compareTo(t2.getFullyQualifiedName());
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

    /**
     * Platform specific newline character(s).
     */
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

    /**
     * Remove suffix from given string.
     */
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

    /**
     * Remove prefix and suffix from given string.
     */
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
     * Read XML file.
     */
    public static Document parseXmlFile(File file) throws SAXException, IOException, ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        Document doc = factory.newDocumentBuilder().parse(file);
        return doc;
    }

    /**
     * Read XML string.
     */
    public static Document parseXmlString(String s) throws SAXException, IOException, ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(s.getBytes()));
        return doc;
    }

    /**
     * Converts collection to a separated string. 
     * @param c            collection
     * @param separator    string to separate items
     * @return             collection items separated with given separator
     */
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
    public static List removeDuplicates(List l)
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

    /**
     * Check if given file exists that was not written by this export.
     */
    public static boolean existsUserFile(String filename)
    {
        File buildFile = new File(filename);
        if (buildFile.exists())
        {
            BufferedReader in = null;
            try
            {
                in = new BufferedReader(new FileReader(buildFile));
                int i = BuildFileCreator.WARNING.indexOf(NEWLINE);
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

    /**
     * Asks user for confirmation.
     * @param message    message to display
     * @param shell      parent instance for dialogs
     */
    public static boolean confirm(String message, Shell shell)
    {
        return MessageDialog.openConfirm(shell,
                DataTransferMessages.AntBuildfileExportPage_4, message);
    }
    
    /**
     * Get file modification validator for given file.
     */
    public static IFileModificationValidator getFileModificationValidator(IFile file)
    {
        List files = new ArrayList();
        files.add(file);
        Map validator2files = getFileModificationValidator(files);
        return (IFileModificationValidator) validator2files.keySet().iterator().next();
    }
    
    /**
     * Get file modification validators for given files.
     * Each file may under control of a different file modification validator,
     * so it's required to use a map to represent result.
     * 
     * @param files                  IFile objects
     * @return                       Validator map. Key is validator, value is
     *                               List of IFile objects.
     */
    public static Map getFileModificationValidator(List files)
    {
        Map validator2files = new TreeMap(getClassnameComparator());
        for (Iterator iter = files.iterator(); iter.hasNext();)
        {
            IFile file = (IFile) iter.next();
            RepositoryProvider rp = RepositoryProvider.getProvider(file.getProject());
            IFileModificationValidator validator = (rp != null)
                ? rp.getFileModificationValidator()
                : getDefaultValidator();
            if (validator == null)
            {
                // some version control plugins don't provide a validator
                // (e.g. subversion)
                validator = getDefaultValidator();
            }
            List list =  (List) validator2files.get(validator);
            if (list == null)
            {
                list = new ArrayList();
            }
            list.add(file);
            validator2files.put(validator, list);
        }
        return validator2files;
    }
    
    /**
     * Request write access to given file.
     * Depending on the version control plugin opens a confirm checkout dialog.
     * 
     * @param shell              parent instance for dialogs
     * @param file               file to request write access for 
     * @return                   true if user confirmed checkout
     * @throws CoreException     thrown if project is under version control, but
     *                           not connected
     */
    public static boolean validateEdit(Shell shell, IFile file) throws CoreException
    {
        List files = new ArrayList();
        files.add(file);
        Map validator2files = getFileModificationValidator(files);
        return validateEdit(shell, validator2files).size() > 0;
    }
    
    /**
     * Request write access to given files in validatorname2files map.
     * Depending on the version control plugin opens a confirm checkout dialog.
     * 
     * @param shell              parent instance for dialogs
     * @param validator2files    Validator map. Key is validator name, value is
     *                           List of IFile objects.
     * @return                   IFile objects for which user confirmed checkout
     * @throws CoreException     thrown if project is under version control,
     *                           but not connected
     */
    public static Set validateEdit(Shell shell, Map validator2files) throws CoreException
    {
        Set confirmedFiles = new TreeSet(getIFileComparator());
        for (Iterator iter = validator2files.keySet().iterator(); iter.hasNext();)
        {
            IFileModificationValidator validator = (IFileModificationValidator) iter.next();
            List files = (List) validator2files.get(validator);
            IFile[] filesArray = (IFile[]) files.toArray(new IFile[files.size()]);
            IStatus status = validator.validateEdit(filesArray, shell);
            if (status.isMultiStatus() && status.getChildren().length > 0)
            {
                for (int i = 0; i < status.getChildren().length; i++)
                {
                    IStatus statusChild = status.getChildren()[i];
                    if (statusChild.isOK())
                    {
                        confirmedFiles.add(filesArray[i]);
                    }
                }
            }
            else if (status.isOK())
            {
                for (Iterator iterator = files.iterator(); iterator.hasNext();)
                {
                    IFile file = (IFile) iterator.next();
                    confirmedFiles.add(file);                    
                }
            }
            // default validator sets error status if user pressed cancel, so
            // ignore its error
            if (validator != getDefaultValidator() &&
                status.getSeverity() == IStatus.ERROR)
            {
                // not possible to checkout files: not connected to version
                // control plugin or hijacked files and made read-only, so
                // collect error messages provided by validator and rethrow
                StringBuffer message = new StringBuffer(status.getPlugin() + ": " //$NON-NLS-1$
                        + status.getMessage() + NEWLINE);
                if (status.isMultiStatus())
                {
                    for (int i = 0; i < status.getChildren().length; i++)
                    {
                        IStatus statusChild = status.getChildren()[i];
                        message.append(statusChild.getMessage() + NEWLINE);
                    }
                }
                throw new CoreException(new Status(IStatus.ERROR, AntUIPlugin.PI_ANTUI, 0,
                        message.toString(), null));               
            }
            
        }
        return confirmedFiles;
    }
    
    private static IFileModificationValidator defaultValidator;        

    /**
     * Get default file modification validator.
     */
    public static synchronized IFileModificationValidator getDefaultValidator() {
        if (defaultValidator == null) {
            defaultValidator = loadUIValidator();
        }
        return defaultValidator;
    }

    // copied from org.eclipse.team.internal.core.DefaultFileModificationValidator
    private static IFileModificationValidator loadUIValidator() {
        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.team.core", "defaultFileModificationValidator"); //$NON-NLS-1$ //$NON-NLS-2$
        if (extension != null) {
            IExtension[] extensions =  extension.getExtensions();
            if (extensions.length > 0) {
                IConfigurationElement[] configElements = extensions[0].getConfigurationElements();
                if (configElements.length > 0) {
                    try {
                        Object o = configElements[0].createExecutableExtension("class"); //$NON-NLS-1$
                        if (o instanceof IFileModificationValidator) {
                            return (IFileModificationValidator)o;
                        }
                    } catch (CoreException e) {
                        AntUIPlugin.log(e);
                    }
                }
            }
        }
        // default validator not found, return validator that always accepts 
        return new IFileModificationValidator()
        {
            public IStatus validateEdit(IFile[] files, Object context)
            {
                return Status.OK_STATUS;
            }

            public IStatus validateSave(IFile file)
            {
                return Status.OK_STATUS;
            }           
        };
    }

    /**
     * Check if given classpath is a reference to the default classpath of the project.
     * Ideal for testing if runtime classpath was customized.
     */
    public static boolean isDefaultClasspath(IJavaProject project, EclipseClasspath classpath)
    {
        List list = removeDuplicates(classpath.rawClassPathEntries);
        if (list.size() != 1)
        {
            return false;
        }
        String entry = (String) list.iterator().next();
        if (EclipseClasspath.isProjectReference(entry))
        {
            IJavaProject referencedProject = EclipseClasspath.resolveProjectReference(entry); 
            if (referencedProject == null)
            {
                // project was not loaded in workspace
                return false;
            }
            else if (referencedProject.getProject().getName().equals(project.getProject().getName()))
            {               
                return true;
            }
        }
            
        return false;
    }

    /**
     * Add variable/value for Eclipse variable. If given string is no variable, nothing is added.
     * 
     * @param variable2valueMap   property map to add variable/value
     * @param s                   String which may contain Eclipse variables, e.g. ${project_name}
     */
    public static void addVariable(Map variable2valueMap, String s)
    {
        if (s == null || s.equals("")) //$NON-NLS-1$
        {
            return;
        }
        Pattern pattern = Pattern.compile("\\$\\{.*?\\}"); // ${var} //$NON-NLS-1$
        Matcher matcher = pattern.matcher(s);
        while (matcher.find())
        {           
            String variable = matcher.group();
            String value;
            try
            {
                value = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(variable);    
            }
            catch (CoreException e)
            {
                // cannot resolve variable
                value = variable;
            }
            variable = removePrefixAndSuffix(variable, "${", "}"); //$NON-NLS-1$ //$NON-NLS-2$
            // if it is an environment variable, convert to Ant environment syntax 
            if (variable.startsWith("env_var:")) //$NON-NLS-1$
            {
                value = "env." + variable.substring("env_var:".length()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            variable2valueMap.put(variable, value);
        }
    }

    /**
     * Calculate relative path from one directory to another directory or file.
     * 
     * @param from
     *            directory to start from
     * @param to
     *            directory or file to reach relatively
     */
    public static String getRelativeLocation(String from, String to) {

        StringBuffer b = new StringBuffer();
        File fromFile = new File(from);
        File toFile = new File(to);
        if (!fromFile.isAbsolute() || !toFile.isAbsolute()) {
            return to;
        }
        
        if (fromFile.equals(toFile)) {
            return "."; //$NON-NLS-1$
        }

        StringTokenizer fromSt = new StringTokenizer(
                fromFile.getAbsolutePath(), File.separator);
        StringTokenizer toSt = new StringTokenizer(toFile.getAbsolutePath(),
                File.separator);
        List fromSegments = Collections.list(fromSt);
        List toSegments = Collections.list(toSt);

        // calculate length of common prefix
        int min = Math.min(fromSegments.size(), toSegments.size());
        int length;
        for (length = 0; length < min; length++) {
            String s1 = (String) fromSegments.get(length);
            String s2 = (String) toSegments.get(length);
            if (!s1.equals(s2)) {
                if (length == 0) {
                    // no common prefix
                    return to;
                }
                break;
            }
        }
        
        // add dots for each segment 'from' is longer than the prefix
        for (int i = length; i < fromSegments.size(); i++) {
            b.append("../"); //$NON-NLS-1$
        }
        
        // add segments of 'to' that are longer than the prefix
        for (int i = length; i < toSegments.size(); i++) {
            String s = (String) toSegments.get(i);
            b.append(s);
            if (length < toSegments.size() - 1) {
                b.append("/"); //$NON-NLS-1$
            }
        }
 
        /*
        File f = new File(from + "/" + b.toString());
        System.out.println("from=" + from);
        System.out.println("  to=" + to);
        System.out.println(" rel=" + b + ": " +f.exists());
        */
        
        return b.toString();
    }

}