/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nico Seessle - bug 51332
 *******************************************************************************/

package org.eclipse.ant.internal.ui.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import org.apache.tools.ant.AntTypeDefinition;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Main;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskAdapter;
import org.apache.tools.ant.UnknownElement;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.Property;
import org.eclipse.ant.core.Type;
import org.eclipse.ant.internal.core.IAntCoreConstants;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.editor.DecayCodeCompletionDataStructuresThread;
import org.eclipse.ant.internal.ui.editor.outline.AntEditorMarkerUpdater;
import org.eclipse.ant.internal.ui.editor.utils.ProjectHelper;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ISynchronizable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

public class AntModel implements IAntModel {

	private static ClassLoader fgClassLoader;
	private static int fgInstanceCount= 0;
	
	private IDocument fDocument;
	private IProblemRequestor fProblemRequestor;
	private LocationProvider fLocationProvider;

	private AntProjectNode fProjectNode;
	private AntTargetNode fCurrentTargetNode;
	private AntElementNode fLastNode;
	private AntElementNode fNodeBeingResolved;
	
	private Map fEntityNameToPath;
	
	 /**
     * Stack of still open elements.
     * <P>
     * On top of the stack is the innermost element.
     */
	private Stack fStillOpenElements = new Stack();
	
	private Map fTaskToNode= new HashMap();

	private List fTaskNodes= new ArrayList();

	private final Object fDirtyLock= new Object();
	private boolean fIsDirty= true;
	private File fEditedFile= null;	
	private Set fNamesOfOldDefiningNodes;
   
    private ClassLoader fLocalClassLoader= null;
    
    private boolean fHasLexicalInfo= true;
    private boolean fHasPositionInfo= true;
    private boolean fHasTaskInfo= true;
    
	private IDocumentListener fListener;	
	private AntEditorMarkerUpdater fMarkerUpdater= null;
	private List fNonStructuralNodes= new ArrayList(1);
	
	private Preferences.IPropertyChangeListener fCorePropertyChangeListener;
	
	private Preferences.IPropertyChangeListener fUIPropertyChangeListener;
	
	public AntModel(IDocument document, IProblemRequestor problemRequestor, LocationProvider locationProvider) {
		init(document, problemRequestor, locationProvider);
		
		fMarkerUpdater= new AntEditorMarkerUpdater();
		fMarkerUpdater.setModel(this);
		fCorePropertyChangeListener= new Preferences.IPropertyChangeListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
			 */
			public void propertyChange(Preferences.PropertyChangeEvent event) {
				if (event.getProperty().equals(IAntCoreConstants.PREFERENCE_CLASSPATH_CHANGED)) {
					if (((Boolean)event.getNewValue()) == Boolean.TRUE) {
						reconcileForPropertyChange(true);
					}
				}
			}
		};
		AntCorePlugin.getPlugin().getPluginPreferences().addPropertyChangeListener(fCorePropertyChangeListener);
		
		fUIPropertyChangeListener= new Preferences.IPropertyChangeListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
			 */
			public void propertyChange(Preferences.PropertyChangeEvent event) {
				String property= event.getProperty();
				if (property.equals(AntEditorPreferenceConstants.PROBLEM)) {
					AntUIPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(fUIPropertyChangeListener);
					reconcileForPropertyChange(false);
					AntUIPlugin.getDefault().getPluginPreferences().setToDefault(AntEditorPreferenceConstants.PROBLEM);
					AntUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(fUIPropertyChangeListener);
				} else if (property.equals(AntEditorPreferenceConstants.CODEASSIST_USER_DEFINED_TASKS)) {
					reconcileForPropertyChange(false);
				}
			}
		};
		AntUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(fUIPropertyChangeListener);
		
		DecayCodeCompletionDataStructuresThread.cancel();
	}
	
	public AntModel(IDocument document, IProblemRequestor problemRequestor, LocationProvider locationProvider, boolean resolveLexicalInfo, boolean resolvePositionInfo, boolean resolveTaskInfo) {
		init(document, problemRequestor, locationProvider);
		
		fHasLexicalInfo= resolveLexicalInfo;
		fHasPositionInfo= resolvePositionInfo;
		fHasTaskInfo= resolveTaskInfo;
	}
	
	private void init(IDocument document, IProblemRequestor problemRequestor, LocationProvider locationProvider) {
        fDocument= document;
		fProblemRequestor= problemRequestor;
		fLocationProvider= locationProvider;
		AntDefiningTaskNode.setJavaClassPath();
		fgInstanceCount++;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#dispose()
	 */
	public void dispose() {		
		synchronized (getLockObject()) {
			if (fDocument != null && fListener != null) {
				fDocument.removeDocumentListener(fListener);
			}
			fDocument= null;
			fLocationProvider= null;
			ProjectHelper.setAntModel(null);
		}
		
		if (fCorePropertyChangeListener != null) {
		    AntCorePlugin.getPlugin().getPluginPreferences().removePropertyChangeListener(fCorePropertyChangeListener);
		}
		if (fUIPropertyChangeListener != null) {
		    AntUIPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(fUIPropertyChangeListener);
		}
		fgInstanceCount--;
		if (fgInstanceCount == 0) {
			fgClassLoader= null;
			DecayCodeCompletionDataStructuresThread.getDefault().start();
			cleanup();
		}
	}
	
	private Object getLockObject() {
		if (fDocument instanceof ISynchronizable) {
			return ((ISynchronizable)fDocument).getLockObject();
		}
		return this;
	}

	private void cleanup() {
        AntProjectNode projectNode= getProjectNode();
		if (projectNode != null) {
			//cleanup the introspection helpers that may have been generated
			IntrospectionHelper.getHelper(projectNode.getProject(), AntModel.class);
			projectNode.getProject().fireBuildFinished(null);
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.IAntModel#reconcile()
     */
    public void reconcile() {
		synchronized (fDirtyLock) {
			if (!fIsDirty) {
				return;
			}
			fIsDirty= false;
		}

		synchronized (getLockObject()) {
			if (fLocationProvider == null) {
				// disposed
				return;
			}
			
			if (fDocument == null) {
				fProjectNode= null;
			} else {
				reset();
				parseDocument(fDocument);
				reconcileTaskAndTypes();
			} 
			AntModelCore.getDefault().notifyAntModelListeners(new AntModelChangeEvent(this));
		}
	}

	private void reset() {
		fCurrentTargetNode= null;
		fStillOpenElements= new Stack();
		fTaskToNode= new HashMap();
		fTaskNodes= new ArrayList();
		fNodeBeingResolved= null;
		fLastNode= null;
		
		fNonStructuralNodes= new ArrayList(1);
	}
	
	private void parseDocument(IDocument input) {
		boolean parsed= true;
		if (input.getLength() == 0) {
			fProjectNode= null;
			parsed= false;
			return;
		}
		ClassLoader parsingClassLoader= getClassLoader();
		ClassLoader originalClassLoader= Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(parsingClassLoader);
		Project project= null;
    	try {
    		ProjectHelper projectHelper= null;
    		String textToParse= input.get();
			if (fProjectNode == null || !fProjectNode.hasChildren()) {
				fProjectNode= null;
				project = new AntModelProject();
				projectHelper= prepareForFullParse(project, parsingClassLoader);
			} else {
				project= fProjectNode.getProject();
				projectHelper= (ProjectHelper)project.getReference("ant.projectHelper"); //$NON-NLS-1$
				prepareForFullIncremental();
			}
			beginReporting();
			Map references= project.getReferences();
			references.remove("ant.parsing.context"); //$NON-NLS-1$
			ProjectHelper.setAntModel(this);
			projectHelper.parse(project, textToParse);
			
    	} catch(BuildException e) {
			handleBuildException(e, null);
    	} finally {
    		Thread.currentThread().setContextClassLoader(originalClassLoader);
    		if (parsed) {
    			resolveBuildfile();
    			endReporting();
    			project.fireBuildFinished(null); //cleanup (IntrospectionHelper)
    		}
    	}
	}

	private ProjectHelper prepareForFullParse(Project project, ClassLoader parsingClassLoader) {
		initializeProject(project, parsingClassLoader);
    	// Ant's parsing facilities always works on a file, therefore we need
    	// to determine the actual location of the file. Though the file 
    	// contents will not be parsed. We parse the passed document string
    	File file = getEditedFile();
    	String filePath= ""; //$NON-NLS-1$
    	if (file != null) {
    		filePath= file.getAbsolutePath();
    	}
    	project.setUserProperty("ant.file", filePath); //$NON-NLS-1$
    	project.setUserProperty("ant.version", Main.getAntVersion()); //$NON-NLS-1$

		ProjectHelper projectHelper= new ProjectHelper(this);
		projectHelper.setBuildFile(file);
		project.addReference("ant.projectHelper", projectHelper); //$NON-NLS-1$
		return projectHelper;
	}
	
	private void prepareForFullIncremental() {
		fProjectNode.reset();
		fTaskToNode= new HashMap();
		fTaskNodes= new ArrayList();
	}

	private void initializeProject(Project project, ClassLoader loader) {
		project.init();
		setProperties(project);
		setTasks(project, loader);
		setTypes(project, loader);
	}
	
	private void setTasks(Project project, ClassLoader loader) {
		List tasks = AntCorePlugin.getPlugin().getPreferences().getTasks();
		for (Iterator iterator = tasks.iterator(); iterator.hasNext();) {
			org.eclipse.ant.core.Task task = (org.eclipse.ant.core.Task) iterator.next();
			AntTypeDefinition def= new AntTypeDefinition();
			def.setName(task.getTaskName());
            def.setClassName(task.getClassName());
            def.setClassLoader(loader);
            def.setAdaptToClass(Task.class);
            def.setAdapterClass(TaskAdapter.class);
            ComponentHelper.getComponentHelper(project).addDataTypeDefinition(def);
		}
	}
		
	private void setTypes(Project project, ClassLoader loader) {
		List types = AntCorePlugin.getPlugin().getPreferences().getTypes();
		for (Iterator iterator = types.iterator(); iterator.hasNext();) {
			Type type = (Type) iterator.next();
			 AntTypeDefinition def = new AntTypeDefinition();
             def.setName(type.getTypeName());
             def.setClassName(type.getClassName());
             def.setClassLoader(loader);
             ComponentHelper.getComponentHelper(project).addDataTypeDefinition(def);
		}
	}
	
	private void setProperties(Project project) {
		setBuiltInProperties(project);
		setGlobalProperties(project);
		loadPropertyFiles(project);
	}
	
	/**
	 * Load all properties from the files 
	 */
	private void loadPropertyFiles(Project project) {
		String[] fileNames= AntCorePlugin.getPlugin().getPreferences().getCustomPropertyFiles();
		for (int i = 0; i < fileNames.length; i++) {
			String filename = fileNames[i];
           	File file= getFileRelativeToBaseDir(project, filename);
            Properties props = new Properties();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                props.load(fis);
            } catch (IOException e) {
            	AntUIPlugin.log(e);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e){
                    }
                }
            }
          
            Enumeration propertyNames = props.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String name = (String) propertyNames.nextElement();
                project.setUserProperty(name, props.getProperty(name));
            }
        }
	}
	
	private File getFileRelativeToBaseDir(Project project, String fileName) {
		IPath path= new Path(fileName);
		if (!path.isAbsolute()) {
			String base= project.getUserProperty("basedir"); //$NON-NLS-1$
			if (base != null) {
				File baseDir= new File(base);
				if (baseDir != null) {
					//relative to the base dir
					path= new Path(baseDir.getAbsolutePath());
				} 
			} else {
				//relative to the build file location
				path= new Path(getEditedFile().getAbsolutePath());
				path= path.removeLastSegments(1);
			}
			path= path.addTrailingSeparator();
			path= path.append(fileName);
		}
		
		return path.toFile();
	}

	private void setBuiltInProperties(Project project) {
		//note also see processAntHome for system properties that are set
		project.setUserProperty("ant.file", getEditedFile().getAbsolutePath()); //$NON-NLS-1$
		project.setUserProperty("ant.version", Main.getAntVersion()); //$NON-NLS-1$
	}
	
	private void setGlobalProperties(Project project) {
		List properties= AntCorePlugin.getPlugin().getPreferences().getProperties();
		if (properties != null) {
			for (Iterator iter = properties.iterator(); iter.hasNext();) {
				Property property = (Property) iter.next();
				String value= property.getValue(false);
				if (value != null) {
					project.setUserProperty(property.getName(), value);
				}
			}
		}
	}

	private void resolveBuildfile() {	
		Collection nodeCopy= new ArrayList(fTaskNodes.size());
		nodeCopy.addAll(fTaskNodes);
		Iterator iter= nodeCopy.iterator();
		while (iter.hasNext()) {
			AntTaskNode node = (AntTaskNode) iter.next();
			fNodeBeingResolved= node;
			if (node.configure(false)) {
				//resolve any new elements that may have been added
				resolveBuildfile();
			}
		}
		fNodeBeingResolved= null;
		
		checkTargets();
	}

	/**
	 * Check that we have a default target defined and that the 
	 * target dependencies exist. 
	 */
	private void checkTargets() {
		if (fProjectNode == null) {
			return;
		}
		String defaultTargetName= fProjectNode.getDefaultTargetName();
		if (defaultTargetName == null || fProjectNode.getProject().getTargets().get(defaultTargetName) == null) {
			//no default target
			String message;
			if (defaultTargetName == null) {
				message= AntModelMessages.getString("AntModel.0"); //$NON-NLS-1$
			} else {
				message= MessageFormat.format(AntModelMessages.getString("AntModel.43"), new String[]{defaultTargetName}); //$NON-NLS-1$
			}
			IProblem problem= createProblem(message, fProjectNode.getOffset(), fProjectNode.getSelectionLength(), AntModelProblem.SEVERITY_ERROR);
			acceptProblem(problem);
			markHierarchy(fProjectNode, AntModelProblem.SEVERITY_ERROR, message);
		}
		if (!fProjectNode.hasChildren()) {
			return;
		}
		List children= fProjectNode.getChildNodes();
		Iterator iter= children.iterator();
		while (iter.hasNext()) {
			AntElementNode node = (AntElementNode) iter.next();
			AntElementNode originalNode= node;
			if (node instanceof AntTargetNode) {
				String missing= ((AntTargetNode)node).checkDependencies();
				if (missing != null) {
					String message= MessageFormat.format(AntModelMessages.getString("AntModel.44"), new String[]{missing}); //$NON-NLS-1$
					AntElementNode importNode= node.getImportNode();
					if (importNode != null) {
						node= importNode;
					}
					IProblem problem= createProblem(message, node.getOffset(), node.getSelectionLength(), AntModelProblem.SEVERITY_ERROR);
					acceptProblem(problem);
					markHierarchy(originalNode, AntModelProblem.SEVERITY_ERROR, message);
				}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#handleBuildException(org.apache.tools.ant.BuildException, org.eclipse.ant.internal.ui.model.AntElementNode, int)
	 */
	public void handleBuildException(BuildException e, AntElementNode node, int severity) {
		try {
			if (node != null) {
				markHierarchy(node, severity, e.getMessage());
			}
			Location location= e.getLocation();
			int line= 0;
			int originalOffset= 0;
			int nonWhitespaceOffset= 0; 
			int length= 0;
			if (location == Location.UNKNOWN_LOCATION && node != null) {
				nonWhitespaceOffset= node.getOffset();
				length= node.getLength();
			} else {
				line= location.getLineNumber();
				if (line == 0) {
					AntProjectNode projectNode= getProjectNode();
					if (projectNode != null) {
						length= projectNode.getSelectionLength();
						nonWhitespaceOffset= projectNode.getOffset();
						if (severity == AntModelProblem.SEVERITY_ERROR) {
							projectNode.setProblemSeverity(AntModelProblem.NO_PROBLEM);
							projectNode.setProblemMessage(null);
						}
					} else {
						return;
					}
				} else {
					if (node == null) {
						originalOffset= getOffset(line, 1);
						nonWhitespaceOffset= originalOffset;
						try {
							nonWhitespaceOffset= getNonWhitespaceOffset(line, 1);
						} catch (BadLocationException be) {
						}
						length= getLastCharColumn(line) - (nonWhitespaceOffset - originalOffset);
					} else {
						nonWhitespaceOffset= node.getOffset();
						length= node.getLength();
					}
				}
			}
			notifyProblemRequestor(e, nonWhitespaceOffset, length, severity);
		} catch (BadLocationException e1) {
		}
	}

	public void handleBuildException(BuildException e, AntElementNode node) {
		handleBuildException(e, node, AntModelProblem.SEVERITY_ERROR);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#getEditedFile()
	 */
	public File getEditedFile() {
		if (fLocationProvider != null && fEditedFile == null) {
        	fEditedFile= fLocationProvider.getLocation().toFile();
		}
		return fEditedFile;
    }

	private void markHierarchy(AntElementNode openElement, int severity, String message) {
		while (openElement != null) {
			openElement.setProblemSeverity(severity);
			openElement.setProblemMessage(message);
			openElement= openElement.getParentNode();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#getLocationProvider()
	 */
	public LocationProvider getLocationProvider() {
		return fLocationProvider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#addTarget(org.apache.tools.ant.Target, int, int)
	 */
	public void addTarget(Target newTarget, int line, int column) {
		AntTargetNode targetNode= new AntTargetNode(newTarget);
		fProjectNode.addChildNode(targetNode);
		fCurrentTargetNode= targetNode;
		fStillOpenElements.push(targetNode);
		computeOffset(targetNode, line, column);
		if (fNodeBeingResolved instanceof AntImportNode) {
			targetNode.setImportNode(fNodeBeingResolved);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#addProject(org.apache.tools.ant.Project, int, int)
	 */
	public void addProject(Project project, int line, int column) {
		fProjectNode= new AntProjectNode((AntModelProject)project, this);
		fStillOpenElements.push(fProjectNode);
		computeOffset(fProjectNode, line, column);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#addDTD(java.lang.String, int, int)
	 */
	public void addDTD(String name, int line, int column) {
		AntDTDNode node= new AntDTDNode(name);
		fStillOpenElements.push(node);
		int offset= -1;
		try {
			if (column <= 0) {
				offset= getOffset(line, 0);
				int lastCharColumn= getLastCharColumn(line);
				offset= computeOffsetUsingPrefix(line, offset, "<!DOCTYPE", lastCharColumn); //$NON-NLS-1$
			} else {
				offset= getOffset(line, column);
			}
		} catch (BadLocationException e) {
			AntUIPlugin.log(e);
		}
		node.setOffset(offset);
		fNonStructuralNodes.add(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#addTask(org.apache.tools.ant.Task, org.apache.tools.ant.Task, org.xml.sax.Attributes, int, int)
	 */
	public void addTask(Task newTask, Task parentTask, Attributes attributes, int line, int column) {
	    if (!canGetTaskInfo()) {
	        return;
	    }
		AntTaskNode taskNode= null;
		if (parentTask == null) {
			taskNode= newTaskNode(newTask, attributes);
			if (fCurrentTargetNode == null) {
				fProjectNode.addChildNode(taskNode);
			} else {
				fCurrentTargetNode.addChildNode(taskNode);
				if (taskNode.isExternal()) {
					fCurrentTargetNode.setExternal(true);
					fCurrentTargetNode.setFilePath(taskNode.getFilePath());
				}
			}
		} else {
			taskNode= newNotWellKnownTaskNode(newTask, attributes);
			((AntTaskNode)fTaskToNode.get(parentTask)).addChildNode(taskNode);
		}
		fTaskToNode.put(newTask, taskNode);
		
		fStillOpenElements.push(taskNode);
		computeOffset(taskNode, line, column);
		if (fNodeBeingResolved instanceof AntImportNode) {
			taskNode.setImportNode(fNodeBeingResolved);
			//place the node in the collection right after the import node
			int index= fTaskNodes.indexOf(fNodeBeingResolved) + 1;
			fTaskNodes.add(index, taskNode);
		} else {
			fTaskNodes.add(taskNode);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#addEntity(java.lang.String, java.lang.String)
	 */
	public void addEntity(String entityName, String entityPath) {
		if (fEntityNameToPath == null) {
			fEntityNameToPath= new HashMap();
		}
		fEntityNameToPath.put(entityName, entityPath);
	}

	private AntTaskNode newTaskNode(Task newTask, Attributes attributes) {
		AntTaskNode newNode= null;
		String taskName= newTask.getTaskName();
		if (isPropertySettingTask(taskName)) { //$NON-NLS-1$
			newNode= new AntPropertyNode(newTask, attributes);
		} else if (taskName.equalsIgnoreCase("import")) { //$NON-NLS-1$
			newNode= new AntImportNode(newTask, attributes);
		} else if (taskName.equalsIgnoreCase("macrodef")  //$NON-NLS-1$
        		|| taskName.equalsIgnoreCase("presetdef") //$NON-NLS-1$
				|| taskName.equalsIgnoreCase("typedef") //$NON-NLS-1$
				|| taskName.equalsIgnoreCase("taskdef")) { //$NON-NLS-1$
                    String name = attributes.getValue(IAntModelConstants.ATTR_NAME);
                    newNode= new AntDefiningTaskNode(newTask, name);
		} else if(taskName.equalsIgnoreCase("antcall")) { //$NON-NLS-1$
            newNode= new AntTaskNode(newTask, generateLabel(taskName, attributes, IAntModelConstants.ATTR_TARGET));
        } else if(taskName.equalsIgnoreCase("mkdir")) { //$NON-NLS-1$
            newNode= new AntTaskNode(newTask, generateLabel(taskName, attributes, IAntModelConstants.ATTR_DIR));
        } else if(taskName.equalsIgnoreCase("copy")) { //$NON-NLS-1$
        	 newNode= new AntTaskNode(newTask, generateLabel(taskName, attributes, IAntModelConstants.ATTR_DESTFILE));
        } else if(taskName.equalsIgnoreCase("tar")  //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("jar") //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("war") //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("zip")) { //$NON-NLS-1$
        	newNode= new AntTaskNode(newTask, generateLabel(newTask.getTaskName(), attributes, IAntModelConstants.ATTR_DESTFILE));
        } else if(taskName.equalsIgnoreCase("untar")  //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("unjar") //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("unwar") //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("gunzip") //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("bunzip2") //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("unzip")) { //$NON-NLS-1$
        	newNode= new AntTaskNode(newTask, generateLabel(newTask.getTaskName(), attributes, IAntModelConstants.ATTR_SRC));
        } else if(taskName.equalsIgnoreCase("gzip")  //$NON-NLS-1$
        		|| taskName.equalsIgnoreCase("bzip2")) { //$NON-NLS-1$
        	newNode= new AntTaskNode(newTask, generateLabel(newTask.getTaskName(), attributes, IAntModelConstants.ATTR_ZIPFILE));
        } else if(taskName.equalsIgnoreCase("exec")) { //$NON-NLS-1$
        	String label = "exec "; //$NON-NLS-1$
            String command = attributes.getValue(IAntModelConstants.ATTR_COMMAND);
            if(command != null) {
            	label += command;
            }
            command = attributes.getValue(IAntModelConstants.ATTR_EXECUTABLE);
            if(command != null) {
            	label += command;
            }
            newNode= new AntTaskNode(newTask, label);        
		} else if(taskName.equalsIgnoreCase("delete")) { //$NON-NLS-1$
			
        	String label = "delete "; //$NON-NLS-1$
            String file = attributes.getValue(IAntModelConstants.ATTR_FILE);
            if(file != null) {
            	label+= file;
            } else {
            	file = attributes.getValue(IAntModelConstants.ATTR_DIR);
            	if(file != null) {
            		label+= file;
            	}
            }
            newNode= new AntTaskNode(newTask, label);
        } else {   
        	newNode = newNotWellKnownTaskNode(newTask, attributes);
        }
		setExternalInformation(newTask, newNode);
		return newNode;
	}
            
	/**
	 * @param taskName the name of the task to check
	 * @return whether or not a task with this name sets properties
	 */
	private boolean isPropertySettingTask(String taskName) {
		return taskName.equalsIgnoreCase("property") //$NON-NLS-1$
			|| taskName.equalsIgnoreCase("available") //$NON-NLS-1$
			|| taskName.equalsIgnoreCase("basename") //$NON-NLS-1$
			|| taskName.equalsIgnoreCase("condition") //$NON-NLS-1$
			|| taskName.equalsIgnoreCase("dirname") //$NON-NLS-1$
			|| taskName.equalsIgnoreCase("loadfile") //$NON-NLS-1$
			|| taskName.equalsIgnoreCase("pathconvert") //$NON-NLS-1$
			|| taskName.equalsIgnoreCase("uptodate") //$NON-NLS-1$
			|| taskName.equalsIgnoreCase("xmlproperty"); //$NON-NLS-1$
	}

	private boolean isTaskExternal(String taskFileName) {
		File taskFile= new File(taskFileName);
		return !taskFile.equals(getEditedFile());
	}

	private AntTaskNode newNotWellKnownTaskNode(Task newTask, Attributes attributes) {
		AntTaskNode newNode= new AntTaskNode(newTask);
		String id= attributes.getValue("id"); //$NON-NLS-1$
		if (id != null) {
			newNode.setId(id);
		}
		
		setExternalInformation(newTask, newNode);
		return newNode;
	}

	private void setExternalInformation(Task newTask, AntTaskNode newNode) {
		String taskFileName= newTask.getLocation().getFileName();
		boolean external= isTaskExternal(taskFileName);
		newNode.setExternal(external);
		if (external) {
			newNode.setFilePath(taskFileName);
		}
	}

	private String generateLabel(String taskName, Attributes attributes, String attributeName) {
		StringBuffer label = new StringBuffer(taskName);
        String srcFile = attributes.getValue(attributeName);
        if(srcFile != null) {
        	label.append(' ');
        	label.append(srcFile);
        }	
        return label.toString();
	}

	private void computeLength(AntElementNode element, int line, int column) {
		if (element.isExternal()) {
			element.setExternalInfo(line, column);
			return;
		}
		try {
			int length;
			int offset;
			if (column <= 0) {
				column= getLastCharColumn(line);
				String lineText= fDocument.get(fDocument.getLineOffset(line - 1), column);
				StringBuffer searchString= new StringBuffer("</"); //$NON-NLS-1$
				searchString.append(element.getName());
				searchString.append('>'); 
				int index= lineText.indexOf(searchString.toString());
				if (index == -1) {
					index= lineText.indexOf("/>"); //$NON-NLS-1$
					if (index == -1 ) {
						index= column; //set to the end of line 
					} else {
						index= index + 3;
					}
				} else {
					index= index + searchString.length() + 1;
				}
				offset= getOffset(line, index);
			} else {
				offset= getOffset(line, column);
			}
			
			length= offset - element.getOffset();
			element.setLength(length);
		} catch (BadLocationException e) {
			//ignore as the parser may be out of sync with the document during reconciliation
		}
	}
	
	private void computeOffset(AntElementNode element, int line, int column) {
		if (element.isExternal() || !canGetPositionInfo()) {
			return;
		}
		try {
			String prefix= "<" + element.getName(); //$NON-NLS-1$
			int offset = computeOffset(line, column, prefix);
			element.setOffset(offset + 1);
			element.setSelectionLength(element.getName().length());
		} catch (BadLocationException e) {
			//ignore as the parser may be out of sync with the document during reconciliation
		}
	}
	
	private int computeOffset(int line, int column, String prefix) throws BadLocationException {
		int offset;
		if (column <= 0) {
			offset= getOffset(line, 0);
			int lastCharColumn= getLastCharColumn(line);
			offset= computeOffsetUsingPrefix(line, offset, prefix, lastCharColumn);
		} else {
			offset= getOffset(line, column);
			offset= computeOffsetUsingPrefix(line, offset, prefix, column);
		}
		return offset;
	}

	private int computeOffsetUsingPrefix(int line, int offset, String prefix, int column) throws BadLocationException {
		String lineText= fDocument.get(fDocument.getLineOffset(line - 1), column);
		int lastIndex= lineText.indexOf(prefix);
		if (lastIndex > -1) {
			offset= getOffset(line, lastIndex + 1);
		} else {
			return computeOffsetUsingPrefix(line - 1, offset, prefix, getLastCharColumn(line - 1));
		}
		return offset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#getOffset(int, int)
	 */
	public int getOffset(int line, int column) throws BadLocationException {
		return fDocument.getLineOffset(line - 1) + column - 1;
	}
	
	private int getNonWhitespaceOffset(int line, int column) throws BadLocationException {
		int offset= fDocument.getLineOffset(line - 1) + column - 1;
		while(Character.isWhitespace(fDocument.getChar(offset))) {
			offset++;
		}
		return offset;
	}
	
	private int getLine(int offset) {
		try {
			return fDocument.getLineOfOffset(offset) + 1;
		} catch (BadLocationException be) {
			return -1;
		}
	}
	
	private int getLastCharColumn(int line) throws BadLocationException {
		String lineDelimiter= fDocument.getLineDelimiter(line - 1);
		int lineDelimiterLength= lineDelimiter != null ? lineDelimiter.length() : 0;
		return fDocument.getLineLength(line - 1) - lineDelimiterLength;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#setCurrentElementLength(int, int)
	 */
	public void setCurrentElementLength(int lineNumber, int column) {
		fLastNode= (AntElementNode)fStillOpenElements.pop();
		if (fLastNode == fCurrentTargetNode) {
			fCurrentTargetNode= null; //the current target element has been closed
		}
		if (canGetPositionInfo()) {
		    computeLength(fLastNode, lineNumber, column);
		}
	}
	
	public void acceptProblem(IProblem problem) {
		if (fProblemRequestor != null) {
			fProblemRequestor.acceptProblem(problem);
		}
		if (fMarkerUpdater != null) {
			fMarkerUpdater.acceptProblem(problem);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#getFile()
	 */
	public IFile getFile() {
		IPath location= fLocationProvider.getLocation();
		if (location == null) {
			return null;
		}
		IFile[] files= ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(location);
		if (files.length > 0) {
			return files[0];
		} 
		return null;
	}

	private void beginReporting() {
		if (fProblemRequestor != null) {
			fProblemRequestor.beginReporting();
		}
		if (fMarkerUpdater != null) {
			fMarkerUpdater.beginReporting();
		}
	}
	
	private void endReporting() {
		if (fProblemRequestor != null) {
			fProblemRequestor.endReporting();
		}
	}

	private IProblem createProblem(Exception exception, int offset, int length,  int severity) {
		return createProblem(exception.getMessage(), offset, length, severity);
	}
	
	private IProblem createProblem(String message, int offset, int length, int severity) {
		return new AntModelProblem(message, severity, offset, length, getLine(offset));
	}

	private void notifyProblemRequestor(Exception exception, AntElementNode element, int severity) {
		AntElementNode importNode= element.getImportNode();
		if (importNode != null) {
			element= importNode;
		}
		IProblem problem= createProblem(exception, element.getOffset(), element.getLength(), severity);
		acceptProblem(problem);
		element.associatedProblem(problem);
	}
	
	private void notifyProblemRequestor(Exception exception, int offset, int length, int severity) {
		if (fProblemRequestor != null) {
			IProblem problem= createProblem(exception, offset, length, severity);
			acceptProblem(problem);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#warning(java.lang.Exception)
	 */
	public void warning(Exception exception) {
		notifyProblemRequestor(exception, (AntElementNode)fStillOpenElements.pop(), AntModelProblem.SEVERITY_WARNING);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#error(java.lang.Exception)
	 */
	public void error(Exception exception) {
		handleError(exception, AntModelProblem.SEVERITY_ERROR);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#errorFromElementText(java.lang.Exception, int, int)
	 */
	public void errorFromElementText(Exception exception, int start, int count) {
		AntElementNode node= fLastNode;
		if (node == null) {
			if (!fStillOpenElements.empty()) {
				node= (AntElementNode)fStillOpenElements.peek();
			} 
		}
		if (node == null) {
			return;
		}
		computeEndLocationForErrorNode(node, start, count);
		notifyProblemRequestor(exception, start, count, AntModelProblem.SEVERITY_ERROR);
		markHierarchy(fLastNode, AntModelProblem.SEVERITY_ERROR, exception.getMessage());
	} 
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#errorFromElement(java.lang.Exception, org.eclipse.ant.internal.ui.model.AntElementNode, int, int)
	 */
	public void errorFromElement(Exception exception, AntElementNode node, int lineNumber, int column) {
		if (node == null) {
			if (!fStillOpenElements.empty()) {
				node= (AntElementNode)fStillOpenElements.peek();
			} else {
				node= fLastNode;
			}
		}
		computeEndLocationForErrorNode(node, lineNumber, column);
		notifyProblemRequestor(exception, node, AntModelProblem.SEVERITY_ERROR);
		markHierarchy(node, AntModelProblem.SEVERITY_ERROR, exception.getMessage());
	}

	private AntElementNode createProblemElement(SAXParseException exception) {
		int lineNumber= exception.getLineNumber();
		StringBuffer message= new StringBuffer(exception.getMessage());
		if (lineNumber != -1){
			message.append(AntModelMessages.getString("AntModel.1") + lineNumber); //$NON-NLS-1$
		}

		AntElementNode errorNode= new AntElementNode(message.toString());
		errorNode.setFilePath(exception.getSystemId());
		errorNode.setProblemSeverity(AntModelProblem.SEVERITY_ERROR);
		errorNode.setProblemMessage(exception.getMessage());
		computeErrorLocation(errorNode, exception);
		return errorNode;
	}
	
	private void computeErrorLocation(AntElementNode element, SAXParseException exception) {
		if (element.isExternal()) {
			return;
		}
		
		int line= exception.getLineNumber();
		int startColumn= exception.getColumnNumber();
		computeEndLocationForErrorNode(element, line, startColumn);	
	}
	
	private void computeEndLocationForErrorNode(AntElementNode element, int line, int startColumn) {
		try {
			if (line <= 0) {
				line= 1;
			}
			int endColumn;
			if (startColumn <= 0) {
				if (element.getOffset() > -1) {
					startColumn= element.getOffset() + 1;
				} else {
					startColumn= 1;
				}
				endColumn= getLastCharColumn(line) + 1;
			} else {
				if (startColumn > 1) {
					--startColumn;
				}
				
				endColumn= startColumn;
				if (startColumn <= getLastCharColumn(line)) {
					++endColumn;
				}
			}
			
			int correction= 0;
			if (element.getOffset() == -1) {
				int originalOffset= getOffset(line, startColumn);
				int nonWhitespaceOffset= originalOffset; 
				try {
					nonWhitespaceOffset= getNonWhitespaceOffset(line, startColumn);
				} catch (BadLocationException be) {
				}
				element.setOffset(nonWhitespaceOffset);
				correction= nonWhitespaceOffset - originalOffset;
			}
			if (endColumn - startColumn == 0) {
				int offset= getOffset(line, startColumn);
				element.setLength(offset - element.getOffset() - correction);
			} else {
				element.setLength(endColumn - startColumn - correction);
			}
		} catch (BadLocationException e) {
			//ignore as the parser may be out of sync with the document during reconciliation
		}
	}

	private void handleError(Exception exception, int severity) {
		AntElementNode node= null;
		if (fStillOpenElements.isEmpty()) {
			if (exception instanceof SAXParseException) {
				node= createProblemElement((SAXParseException)exception);
			}
		} else {
			node= (AntElementNode)fStillOpenElements.peek();
		}
		if (node == null) {
			return;
		}
		markHierarchy(node, severity, exception.getMessage());
		
		if (exception instanceof SAXParseException) {
			SAXParseException parseException= (SAXParseException)exception;
			if (node.getOffset() == -1) { 
				computeEndLocationForErrorNode(node, parseException.getLineNumber() - 1, parseException.getColumnNumber());
			} else {
				int lineNumber= parseException.getLineNumber();
				int columnNumber= parseException.getColumnNumber();
				if (columnNumber == -1) {
					columnNumber= 1;
				}
				try {
					AntElementNode childNode= node.getNode(getNonWhitespaceOffset(lineNumber, columnNumber) + 1);
					if (childNode != null && childNode != node) {
						node= childNode;
						node.setProblemSeverity(severity);
						node.setProblemMessage(exception.getMessage());
					} else {
						node= createProblemElement(parseException);
					}
				} catch (BadLocationException be) {
					node= createProblemElement(parseException);
				}
			}
		}
		
		notifyProblemRequestor(exception, node, severity);
		
		if (node != null) {
			while (node.getParentNode() != null) {
				AntElementNode parentNode= node.getParentNode();
				if (parentNode.getLength() == -1) {
					parentNode.setLength(node.getOffset() - parentNode.getOffset() + node.getLength());
				}
				node= parentNode;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#fatalError(java.lang.Exception)
	 */
	public void fatalError(Exception exception) {
		handleError(exception, AntModelProblem.SEVERITY_FATAL_ERROR);
	}
	
	public AntElementNode getOpenElement() {
		if (fStillOpenElements.isEmpty()) {
			return null;
		}
		return (AntElementNode)fStillOpenElements.peek();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#getEntityName(java.lang.String)
	 */
	public String getEntityName(String path) {
		if (fEntityNameToPath != null) {
			Iterator itr= fEntityNameToPath.keySet().iterator();
			String entityPath;
			String name;
			while (itr.hasNext()) {
				name= (String) itr.next();
				entityPath= (String) fEntityNameToPath.get(name);
				if (entityPath.equals(path)) {
					return name;
				}
			}
		} 
		return null;
	}

	private ClassLoader getClassLoader() {
	    if (fLocalClassLoader != null) {
	        return fLocalClassLoader;
	    }
		if (fgClassLoader == null) {
			fgClassLoader= AntCorePlugin.getPlugin().getNewClassLoader(true);
		}
		return fgClassLoader;
	}
	
	public String getTargetDescription(String targetName) {
		AntTargetNode target= getTargetNode(targetName);
		if (target != null) {
			return target.getTarget().getDescription();
		}
		return null;
	}
	
	public AntTargetNode getTargetNode(String targetName ) {
		AntProjectNode projectNode= getProjectNode();
		if (projectNode == null) {
			return null;
		}
		if (projectNode.hasChildren()) {
			List possibleTargets= projectNode.getChildNodes();
			Iterator iter= possibleTargets.iterator();
			while (iter.hasNext()) {
				AntElementNode node = (AntElementNode) iter.next();
				if (node instanceof AntTargetNode) {
					AntTargetNode targetNode = (AntTargetNode) node;
					if (targetName.equalsIgnoreCase(targetNode.getTarget().getName())) {
						return targetNode;
					}
				}
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#getProjectNode(boolean)
	 */
	public AntProjectNode getProjectNode(boolean doReconcile) {
		if (doReconcile) {
			synchronized (getLockObject()) { //ensure to wait for any current synchronization
				reconcile();
			}
		}
		return fProjectNode;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#getProjectNode()
	 */
	public AntProjectNode getProjectNode() {
		return getProjectNode(true);
	}
	
	public AntElementNode getNode(int offset, boolean waitForReconcile) {
		if (getProjectNode(waitForReconcile) != null) {
			return getProjectNode(waitForReconcile).getNode(offset);
		}
		return null;
	}

	/**
     * Provides the set of names of the defining nodes that existed from the previous
     * parse of the build file.
     */
	public void setNamesOfOldDefiningNodes(Set set) {
		fNamesOfOldDefiningNodes= set;
	}
	
	/**
     * Removes any type definitions that no longer exist in the buildfile
     */
	private void reconcileTaskAndTypes() {
		if (fNamesOfOldDefiningNodes == null) {
			return;
		}
		Iterator iter= fNamesOfOldDefiningNodes.iterator();
		while (iter.hasNext()) {
			String nodeLabel = (String) iter.next();
			if (fProjectNode.getDefininingTaskNode(nodeLabel) == null) {
				ComponentHelper helper= ComponentHelper.getComponentHelper(fProjectNode.getProject());
				helper.getAntTypeTable().remove(nodeLabel);
				iter.remove();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.IAntModel#addComment(int, int, int)
	 */
	public void addComment(int lineNumber, int columnNumber, int length) {
		AntCommentNode commentNode= new AntCommentNode();
		int offset= -1;
		try {
			offset= computeOffset(lineNumber, columnNumber, "-->"); //$NON-NLS-1$
		} catch (BadLocationException e) {
			AntUIPlugin.log(e);
		}
		commentNode.setOffset(offset - length);
		commentNode.setLength(length);
		fNonStructuralNodes.add(commentNode);
	}

    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.IAntModel#needsTaskResolution()
     */
    public boolean canGetTaskInfo() {
        return fHasTaskInfo;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.IAntModel#needsLexicalResolution()
     */
    public boolean canGetLexicalInfo() {
        return fHasLexicalInfo;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.IAntModel#setClassLoader(java.net.URLClassLoader)
     */
    public void setClassLoader(URLClassLoader loader) {
        AntDefiningTaskNode.setJavaClassPath(loader.getURLs());
    	fLocalClassLoader= loader;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.IAntModel#needsPositionResolution()
     */
    public boolean canGetPositionInfo() {
        return fHasPositionInfo;
    }

    public String getPath(String text, int offset) {
		if (fEntityNameToPath != null) {
			String path= (String)fEntityNameToPath.get(text);
			if (path != null) {
				return path;
			}
		} 
		AntElementNode node= getNode(offset, true);
		if (node != null) {
			return node.getReferencedElement(offset);
		}
		return null;
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.IAntModel#getText(int, int)
     */
    public String getText(int offset, int length) {
		try {
			return fDocument.get(offset, length);
		} catch (BadLocationException e) {
		}
		return null;
	}

    private AntElementNode findPropertyNode(String text, List children) {
    	Iterator iter= children.iterator();
    	while (iter.hasNext()) {
    		AntElementNode element = (AntElementNode) iter.next();
    		if (element instanceof AntPropertyNode) {
    			if (((AntPropertyNode)element).getProperty(text) != null){
    				return element;
    			}
    		} else if (element.hasChildren()) {
    			AntElementNode found= findPropertyNode(text, element.getChildNodes());
    			if (found != null) {
    				return found;
    			}
    		}
    	}
    	return null;
    }

    public AntElementNode getPropertyNode(String text) {
    	AntProjectNode node= getProjectNode();
    	if (node == null || !node.hasChildren()) {
    		return null;
    	}
    	
    	return findPropertyNode(text, node.getChildNodes());
    }

    public List getNonStructuralNodes() {
    	return fNonStructuralNodes;
    }

    public void updateForInitialReconcile() {
    	fMarkerUpdater.updateMarkers();
    }

    public void updateMarkers() {
    	reconcile();
    	fMarkerUpdater.updateMarkers();
    }

    public AntElementNode getReferenceNode(String text) {
    	Object reference= getReferenceObject(text);
    	if (reference == null) {
    		return null;
    	}
    	
    	Collection nodes= fTaskToNode.keySet();
    	Iterator iter= nodes.iterator();
    	while (iter.hasNext()) {
    		Object original = iter.next();
    		Object object= original;
    		if (object instanceof UnknownElement) {
    			UnknownElement element= (UnknownElement) object;
    			object= element.getRealThing();
    			if (object == null) {
    				continue;
    			}
    		} 
    		if (object == reference) {
    			return (AntElementNode)fTaskToNode.get(original);
    		}
    	}
    	return null;
    }

    public Object getReferenceObject(String refId) {
    	AntProjectNode projectNode= getProjectNode();
    	if (projectNode == null) {
    		return null;
    	}
    	try {
    		Project project= projectNode.getProject();
    		Object ref= project.getReference(refId);
    		return ref;
    		
    	} catch (BuildException be) {
    		handleBuildException(be, null);
    	}
    	return null;
    }

    public String getPropertyValue(String propertyName) {
    	AntProjectNode projectNode= getProjectNode();
    	if (projectNode == null) {
    		return null;
    	}
    	return projectNode.getProject().getProperty(propertyName);
    }

    /**
     * Only called if the AntModel is associated with an AntEditor
     */
    public void install() {
    	fListener= new IDocumentListener() {
    		public void documentAboutToBeChanged(DocumentEvent event) {
    			synchronized (fDirtyLock) {
    				fIsDirty= true;
    			}
    		}
    		public void documentChanged(DocumentEvent event) {}
    	};
    	fDocument.addDocumentListener(fListener);
    }

    private void reconcileForPropertyChange(boolean classpathChanged) {
    	if (classpathChanged) {
    		fProjectNode= null; //need to reset tasks, types and properties
    		fgClassLoader= null;
    		AntDefiningTaskNode.setJavaClassPath();
    	}
    	fIsDirty= true;
    	reconcile();
    	AntModelCore.getDefault().notifyAntModelListeners(new AntModelChangeEvent(this, true));
    	fMarkerUpdater.updateMarkers();
    }
}