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

package org.eclipse.ant.internal.ui.editor.outline;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.ant.core.Type;
import org.eclipse.ant.internal.ui.editor.model.AntElementNode;
import org.eclipse.ant.internal.ui.editor.model.AntImportNode;
import org.eclipse.ant.internal.ui.editor.model.AntProjectNode;
import org.eclipse.ant.internal.ui.editor.model.AntPropertyNode;
import org.eclipse.ant.internal.ui.editor.model.AntTargetNode;
import org.eclipse.ant.internal.ui.editor.model.AntTaskNode;
import org.eclipse.ant.internal.ui.editor.model.IAntModelConstants;
import org.eclipse.ant.internal.ui.editor.text.PartiallySynchronizedDocument;
import org.eclipse.ant.internal.ui.editor.utils.ProjectHelper;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

public class AntModel {

	private XMLCore fCore;
	private IDocument fDocument;
	private IProblemRequestor fProblemRequestor;
	private LocationProvider fLocationProvider;

	private AntProjectNode fProjectNode;
	private AntTargetNode fCurrentTargetNode;
	private AntElementNode fLastNode;
	private AntElementNode fNodeBeingResolved;
	
	private AntTargetNode fIncrementalTarget= null;
	private boolean fReplaceHasOccurred= false;
	private int fRemoveLengthOfReplace= 0;
	
	 /**
     * Stack of still open elements.
     * <P>
     * On top of the stack is the innermost element.
     */
	private Stack fStillOpenElements = new Stack();
	
	private Map fTaskToNode= new HashMap();

	private List fProperties= null;
	
	private Map fEntityNameToPath;

	private final Object fDirtyLock= new Object();
	private boolean fIsDirty= true;
	private IDocumentListener fListener;
	private File fEditedFile= null;	
	
	//TODO Bug 50302
	private boolean fValidateFully= false; //AntUIPlugin.getDefault().getPreferenceStore().getBoolean(AntEditorPreferenceConstants.VALIDATE_BUILDFILES);
	
	/**
     * The find replace adapter for the document
     */
    private FindReplaceDocumentAdapter fFindReplaceAdapter;
    
    //TODO bug 37180
	//private static final String BUILDFILE_PROBLEM_MARKER = AntUIPlugin.PI_ANTUI + ".problem"; //$NON-NLS-1$

	public AntModel(XMLCore core, IDocument document, IProblemRequestor problemRequestor, LocationProvider locationProvider) {
		fCore= core;
		fDocument= document;
		if (document instanceof PartiallySynchronizedDocument) {
			((PartiallySynchronizedDocument)document).setAntModel(this);
		}
		fFindReplaceAdapter= new FindReplaceDocumentAdapter(document);
		fProblemRequestor= problemRequestor;
		fLocationProvider= locationProvider;
	}

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
	
	public void dispose() {		

		if (fDocument != null) {
			fDocument.removeDocumentListener(fListener);
		}

		synchronized (this) {
			fDocument= null;
			fCore= null;
		}
	}
	
	public void reconcile(DirtyRegion region) {
		//TODO turn off incremental as it is a work in progress
		region= null; 
		synchronized (fDirtyLock) {
			if (!fIsDirty) {
				return;
			}
			if (fReplaceHasOccurred && region != null) {
				//this is the removed part of a replace
				//the insert region will be along shortly
				fRemoveLengthOfReplace= region.getLength();
				fReplaceHasOccurred= false;
				return;
			} else {
				fIsDirty= false;
			}
		}

		synchronized (this) {
			if (fCore == null) {
				// disposed
				notifyAll();
				return;
			}
			
			if (fDocument == null) {
				fProjectNode= null;
			} else {
				//long start= System.currentTimeMillis();
				reset(region);
				parseDocument(fDocument, region);
				fRemoveLengthOfReplace= 0;
				//System.out.println(System.currentTimeMillis() - start);
			} 
	
			fCore.notifyDocumentModelListeners(new DocumentModelChangeEvent(this));
			notifyAll();
		}
	}

	private void reset(DirtyRegion region) {
		fCurrentTargetNode= null;
		if (region == null ) {
			fStillOpenElements= new Stack();
			fTaskToNode= new HashMap();
			fProperties= new ArrayList();
			fProjectNode= null;
			fNodeBeingResolved= null;
		}
		//removeProblems();
	}

	public synchronized AntElementNode[] getRootElements() {
		possiblyWaitForReconcile();
		if (fProjectNode == null) {
			return new AntElementNode[0];
		} else {
			return new AntElementNode[] {fProjectNode};
		}
	}

	private void parseDocument(IDocument input, DirtyRegion region) {
		boolean parsed= true;
		if (input.getLength() == 0) {
			parsed= false;
			return;
		}
		Project project= null;
    	try {
    		String textToParse= null;
    		ProjectHelper projectHelper= null;
			if (region == null) {  //full parse
				if (fProjectNode == null) {
					project = new Project();
					projectHelper= prepareForFullParse(project);
				} else {
					project= fProjectNode.getProject();
					projectHelper= (ProjectHelper)project.getReference("ant.projectHelper"); //$NON-NLS-1$
				}
				textToParse= input.get(); //the entire document
			} else { //incremental
				project= fProjectNode.getProject();
				textToParse= prepareForIncrementalParse(project, region, input);
				if (textToParse == null) {
					parsed= false;
					return;
				}
				projectHelper= (ProjectHelper)project.getReference("ant.projectHelper"); //$NON-NLS-1$
			}
			beginReporting();
			projectHelper.parse(project, textToParse);
			
    	} catch(BuildException e) {
			handleBuildException(e, null);
    	} finally {
    		if (parsed) {
    			resolveBuildfile();
    			endReporting();
    			project.fireBuildFinished(null); //cleanup
    		}
    	}
    	if (fIncrementalTarget != null && parsed) {
    		updateForIncrementalParse(region);
    	}
	}
	
	private void updateForIncrementalParse(DirtyRegion region) {
		if (fProjectNode == null || !fProjectNode.hasChildren()) {
			return;
		}
		int editAdjustment= determineEditAdjustment(region);
		if (editAdjustment == 0) {
			return;
		}
		List children= fProjectNode.getChildNodes();
		int index= children.indexOf(fIncrementalTarget) + 1;
		updateNodesForIncrementalParse(editAdjustment, children, index);
	}

	private void updateNodesForIncrementalParse(int editAdjustment, List children, int index) {
		AntElementNode node;
		for (int i = index; i < children.size(); i++) {
			node= (AntElementNode)children.get(i);
			node.setOffset(node.getOffset() + editAdjustment);
			if (node.hasChildren()) {
				updateNodesForIncrementalParse(editAdjustment, node.getChildNodes(), 0);
			}
		}
	}

	private ProjectHelper prepareForFullParse(Project project) {
		fIncrementalTarget= null;
		initializeProject(project);
    	// Ant's parsing facilities always works on a file, therefore we need
    	// to determine the actual location of the file. Though the file 
    	// contents will not be parsed. We parse the passed document string
    	File file = getEditedFile();
    	String filePath= ""; //$NON-NLS-1$
    	if (file != null) {
    		filePath= file.getAbsolutePath();
    	}
    	project.setUserProperty("ant.file", filePath); //$NON-NLS-1$

		ProjectHelper projectHelper= new ProjectHelper(this);
		projectHelper.setBuildFile(file);
		project.addReference("ant.projectHelper", projectHelper); //$NON-NLS-1$
		return projectHelper;
	}
	
	private String prepareForIncrementalParse(Project project, DirtyRegion region, IDocument input) {
		String textToParse= null;
		AntElementNode node= fProjectNode.getNode(region.getOffset());
		if (node == null) {
			//outside of any element...not interesting
			return null;
		}
		
		while (node != null && !(node instanceof AntTargetNode)) {
			node= node.getParentNode();
		}
		if (node == null) { //no enclosing target node found
			if (region.getText() != null && region.getText().trim().length() == 0) {
				return null; //no need to parse for whitespace additions
			}
			textToParse= input.get();
			fIncrementalTarget= null;
			fProjectNode.reset();
		} else {
			fIncrementalTarget= (AntTargetNode)node;
			StringBuffer temp= new StringBuffer("<project");//$NON-NLS-1$
			String defltTarget= project.getDefaultTarget();
			if (defltTarget != null) {
				temp.append(" default=\""); //$NON-NLS-1$
				temp.append(defltTarget);
				temp.append("\""); //$NON-NLS-1$
			}
			temp.append(">\n"); //$NON-NLS-1$
			
			fIncrementalTarget.reset();
			try {
				int editAdjustment = determineEditAdjustment(region) + 1;
				String targetString= input.get(node.getOffset() - 1, node.getLength() + editAdjustment);
				temp.append(targetString);
				temp.append("\n</project>"); //$NON-NLS-1$
				textToParse= temp.toString();
			} catch (BadLocationException e) {
				textToParse= input.get();
			}
		}
		return textToParse;
	}

	private int determineEditAdjustment(DirtyRegion region) {
		int editAdjustment= 0;
		if (region.getType().equals(DirtyRegion.INSERT)) {
			editAdjustment+= region.getLength() + fRemoveLengthOfReplace;
		} else {
			editAdjustment-= region.getLength();
		}
		return editAdjustment;
	}

	private void initializeProject(Project project) {
		//ClassLoader loader= getClassLoader();
		if (fValidateFully) {
    		project.setCoreLoader(getClassLoader());
    	}
		project.init();
		//setTasks(project, loader);
		//setTypes(project, loader);
	}
//	TODO Bug 50302	
//	private void setTasks(Project project, ClassLoader loader) {
//		List tasks = AntCorePlugin.getPlugin().getPreferences().getTasks();
//		
//		for (Iterator iterator = tasks.iterator(); iterator.hasNext();) {
//			org.eclipse.ant.core.Task task = (org.eclipse.ant.core.Task) iterator.next();
//			try {
//				Class taskClass = loader.loadClass(task.getClassName());
//				try {
//					project.checkTaskClass(taskClass);
//				} catch (BuildException e) {
//					continue;
//				}
//				project.addTaskDefinition(task.getTaskName(), taskClass);
//			} catch (ClassNotFoundException e) {
//			} catch (NoClassDefFoundError e) {
//			}
//		}
//	}
	
//TODO Bug 50302
	
//	private void setTypes(Project project, ClassLoader loader) {
//		List types = AntCorePlugin.getPlugin().getPreferences().getTypes();
//		
//		for (Iterator iterator = types.iterator(); iterator.hasNext();) {
//			Type type = (Type) iterator.next();
//			try {
//				Class typeClass = loader.loadClass(type.getClassName());
//				project.addDataTypeDefinition(type.getTypeName(), typeClass);
//			} catch (ClassNotFoundException e) {
//			}
//		}
//	}

	private void resolveBuildfile() {	
		resolveProperties();
		Collection nodes= fTaskToNode.values();
		Collection nodeCopy= new ArrayList();
		nodeCopy.addAll(nodes);
		Iterator iter= nodeCopy.iterator();
		while (iter.hasNext()) {
			AntTaskNode node = (AntTaskNode) iter.next();
			if (!(node instanceof AntPropertyNode)) {
				fNodeBeingResolved= node;
				if (node.configure(fValidateFully)) {
					//resolve any new elements
					resolveBuildfile();
			}
		}
	}
		fNodeBeingResolved= null;
	}

	private void resolveProperties() {
		if (fProperties != null) {
			Iterator itr= fProperties.iterator();
			while (itr.hasNext()) {
				AntPropertyNode node = (AntPropertyNode)itr.next();
				node.configure(fValidateFully);
			}
		}
	}

	public void handleBuildException(BuildException e, AntElementNode node) {
		try {
			if (node != null) {
				generateExceptionOutline(node);
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
					if (getProjectNode() != null) {
						length= getProjectNode().getSelectionLength();
						nonWhitespaceOffset= getProjectNode().getOffset();
						getProjectNode().setIsErrorNode(true);
					} else {
						return;
					}
				} else {
					originalOffset= getOffset(line, 1);
					nonWhitespaceOffset= originalOffset;
					length= getLastCharColumn(line) - (nonWhitespaceOffset - originalOffset);
					try {
						nonWhitespaceOffset= getNonWhitespaceOffset(line, 1);
					} catch (BadLocationException be) {
					}
				}
			}
			notifyProblemRequestor(e, nonWhitespaceOffset, length, XMLProblem.SEVERTITY_ERROR);
		} catch (BadLocationException e1) {
		}
	}

	protected File getEditedFile() {
		if (fLocationProvider != null && fEditedFile == null) {
        	fEditedFile= fLocationProvider.getLocation().toFile();
		}
		return fEditedFile;
    }

	private void generateExceptionOutline(AntElementNode openElement) {
		while (openElement != null) {
			openElement.setIsErrorNode(true);
			openElement= openElement.getParentNode();
		}
	}
	
	public LocationProvider getLocationProvider() {
		return fLocationProvider;
	}

	public void addTarget(Target newTarget, int line, int column) {
		if (fIncrementalTarget != null) {
			fCurrentTargetNode= fIncrementalTarget;
			fCurrentTargetNode.setTarget(newTarget);
			fStillOpenElements.push(fCurrentTargetNode);
		} else {
			AntTargetNode targetNode= new AntTargetNode(newTarget);
			fProjectNode.addChildNode(targetNode);
			fCurrentTargetNode= targetNode;
			fStillOpenElements.push(targetNode);
			computeOffset(targetNode, line, column);
			if (fNodeBeingResolved instanceof AntImportNode) {
				targetNode.setImportNode(fNodeBeingResolved);
			}
		}
	}
	
	public void addProject(Project project, int line, int column) {
		if (fIncrementalTarget != null) {
			return;
		}
		fProjectNode= new AntProjectNode(project, this);
		fStillOpenElements.push(fProjectNode);
		computeOffset(fProjectNode, line, column);
	}

	public void addTask(Task newTask, Task parentTask, Attributes attributes, int line, int column) {
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
			taskNode= createNotWellKnownTask(newTask, attributes);
			((AntTaskNode)fTaskToNode.get(parentTask)).addChildNode(taskNode);
		}
		fTaskToNode.put(newTask, taskNode);
		fStillOpenElements.push(taskNode);
		if (fIncrementalTarget != null) {
			line = computeCorrection(line);
		}
		computeOffset(taskNode, line, column);
		if (fNodeBeingResolved instanceof AntImportNode) {
			taskNode.setImportNode(fNodeBeingResolved);
	}
	}
	
	private int computeCorrection(int line) {
		try {
			int realLineCorrection= 0;
			realLineCorrection= getLine(fIncrementalTarget.getOffset()) - 2;
			line= line + realLineCorrection;
		} catch (BadLocationException e) {
		}
		return line;
	}

	public void addEntity(String entityName, String entityPath) {
		if (fEntityNameToPath == null) {
			fEntityNameToPath= new HashMap();
		}
		fEntityNameToPath.put(entityName, entityPath);
	}

	private AntTaskNode newTaskNode(Task newTask, Attributes attributes) {
		AntTaskNode newNode= null;
		String taskName= newTask.getTaskName();
		if (taskName.equalsIgnoreCase("property")) { //$NON-NLS-1$
			newNode= new AntPropertyNode(newTask, attributes);
			if (fProperties == null) {
				fProperties= new ArrayList();
			}
			fProperties.add(newNode);
		} else if (taskName.equalsIgnoreCase("import")) { //$NON-NLS-1$
			newNode= new AntImportNode(newTask, attributes);
		} else if (taskName.equalsIgnoreCase("macrodef")  //$NON-NLS-1$
        		|| taskName.equalsIgnoreCase("presetdef")) { //$NON-NLS-1$
                    String name = attributes.getValue(IAntModelConstants.ATTR_NAME);
                    newNode= new AntTaskNode(newTask, name);
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
        	
        } else if(taskName.equalsIgnoreCase("import")) { //$NON-NLS-1$
        	newNode= new AntTaskNode(newTask, generateLabel(taskName, attributes, IAntModelConstants.ATTR_FILE)); //$NON-NLS-1$
        } else {   
        	newNode = createNotWellKnownTask(newTask, attributes);
        }
		String taskFileName= newTask.getLocation().getFileName();
		boolean external= isTaskExternal(taskFileName);
		newNode.setExternal(external);
		if (external) {
			newNode.setFilePath(taskFileName);
		}
		return newNode;
	}
            
	private boolean isTaskExternal(String taskFileName) {
		File taskFile= new File(taskFileName);
		return !taskFile.equals(getEditedFile());
	}

	private AntTaskNode createNotWellKnownTask(Task newTask, Attributes attributes) {
		AntTaskNode newNode;
		newNode= new AntTaskNode(newTask);
		String id= attributes.getValue("id"); //$NON-NLS-1$
		if (id != null) {
			newNode.setId(id);
		}
		return newNode;
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
			return;
		}
		
		try {
			int length;
				int offset;
				if (column <= 0) {
					int lineOffset= getOffset(line, 1);
					StringBuffer searchString= new StringBuffer("</"); //$NON-NLS-1$
					searchString.append(element.getName());
					searchString.append('>'); 
					IRegion result= fFindReplaceAdapter.search(lineOffset, searchString.toString(), true, true, false, false); //$NON-NLS-1$
					if (result == null) {
						result= fFindReplaceAdapter.search(lineOffset, "/>", true, true, false, false); //$NON-NLS-1$
						if (result == null) {
							offset= -1;
						} else {
							offset= result.getOffset() + 2;
						}
					} else {
						offset= result.getOffset() + searchString.length() - 1;
					}
					if (offset < 0 || getLine(offset) != line) {
						offset= lineOffset;
					} else {
						offset++;
					}
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
		if (element.isExternal()) {
			return;
		}
		
		try {
			int offset;
				String prefix= "<"; //$NON-NLS-1$
				if (column <= 0) {
					offset= getOffset(line, 0);
					int lastCharColumn= getLastCharColumn(line);
					String lineText= fDocument.get(fDocument.getLineOffset(line - 1), lastCharColumn);
					int lastIndex= lineText.indexOf(prefix + element.getName());
					if (lastIndex > -1) {
						offset+= lastIndex + 1;
					} else {
						offset= getOffset(line, lastCharColumn);
						IRegion result= fFindReplaceAdapter.search(offset - 1, prefix, false, false, false, false);
						offset= result.getOffset();
					}
				} else {
					offset= getOffset(line, column);
					IRegion result= fFindReplaceAdapter.search(offset - 1, prefix, false, false, false, false);
					offset= result.getOffset();
				}
 			
			element.setOffset(offset + 1);
			element.setSelectionLength(element.getName().length());
		} catch (BadLocationException e) {
			//ignore as the parser may be out of sync with the document during reconciliation
		}
	}
	
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
	
	private int getLine(int offset) throws BadLocationException {
		return fDocument.getLineOfOffset(offset) + 1;
	}
	
	private int getLastCharColumn(int line) throws BadLocationException {
		String lineDelimiter= fDocument.getLineDelimiter(line - 1);
		int lineDelimiterLength= lineDelimiter != null ? lineDelimiter.length() : 0;
		return fDocument.getLineLength(line - 1) - lineDelimiterLength;
	}

	public void setCurrentElementLength(int lineNumber, int column) {
		fLastNode= (AntElementNode)fStillOpenElements.pop();
		if (fLastNode == fCurrentTargetNode) {
			//the current target element has been closed
			fCurrentTargetNode= null;
			if (fLastNode == fIncrementalTarget) {
				lineNumber= computeCorrection(lineNumber);
			}
		}
		computeLength(fLastNode, lineNumber, column);
	}
	
	public void acceptProblem(IProblem problem) {
		if (fProblemRequestor != null) {
			fProblemRequestor.acceptProblem(problem);
		}
		
		//createMarker(problem);
	}

	//TODO bug 37180
//	private void createMarker(IProblem problem) {
//		IFile file = getResource();
//		int lineNumber= 1;
//		try {
//			lineNumber = getLine(problem.getOffset());
//		} catch (BadLocationException e1) {
//		}
//	
//		try {
//			IMarker marker = file.createMarker(AntModel.BUILDFILE_PROBLEM_MARKER);
//		
//			marker.setAttributes(
//					new String[] { 
//							IMarker.MESSAGE, 
//							IMarker.SEVERITY, 
//							IMarker.LOCATION,
//							IMarker.CHAR_START, 
//							IMarker.CHAR_END, 
//							IMarker.LINE_NUMBER
//					},
//					new Object[] {
//							problem.getMessage(),
//							new Integer(IMarker.SEVERITY_ERROR), 
//							problem.getMessage(),
//							new Integer(problem.getOffset()),
//							new Integer(problem.getOffset() + problem.getLength()),
//							new Integer(lineNumber)
//					}
//			);
//		} catch (CoreException e) {
//			
//		} 
//		
//	}
	
//	private IFile getResource() {
//		IPath location= fLocationProvider.getLocation();
//		IFile[] files= ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(location);
//		return files[0];
//	}

//	private void removeProblems() {
//		IFile file= getResource();
//		
//		try {
//			if (file != null && file.exists())
//				file.deleteMarkers(AntModel.BUILDFILE_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
//		} catch (CoreException e) {
//			// assume there were no problems
//		}
//	}

	private void beginReporting() {
		if (fProblemRequestor != null) {
			fProblemRequestor.beginReporting();
		}
	}
	
	private void endReporting() {
		if (fProblemRequestor != null) {
			fProblemRequestor.endReporting();
		}
	}

	private IProblem createProblem(Exception exception, int offset, int length,  int severity) {
		return new XMLProblem(exception.getMessage(), severity, offset, length);
	}

	protected void notifyProblemRequestor(Exception exception, AntElementNode element, int severity) {
		IProblem problem= createProblem(exception, element.getOffset(), element.getLength(), severity);
		acceptProblem(problem);
		element.associatedProblem(problem);
	}
	
	protected void notifyProblemRequestor(Exception exception, int offset, int length, int severity) {
		IProblem problem= createProblem(exception, offset, length, severity);
		acceptProblem(problem);
	}

	public void warning(Exception exception) {
		notifyProblemRequestor(exception, (AntElementNode)fStillOpenElements.pop(), XMLProblem.SEVERTITY_WARNING);
	}
	
	public void error(Exception exception) {
		AntElementNode node= null;
		if (fStillOpenElements.empty()) {
			if (exception instanceof SAXParseException) {
				node= createProblemElement((SAXParseException)exception);
			}
		} else {
			node= (AntElementNode)fStillOpenElements.peek();
			generateExceptionOutline(node);
		}
	
		notifyProblemRequestor(exception, node, XMLProblem.SEVERTITY_ERROR);
	}
	
	public void errorFromElementText(Exception exception, int start, int count) {
		computeEndLocationForErrorNode(fLastNode, start, count);
		notifyProblemRequestor(exception, start, count, XMLProblem.SEVERTITY_ERROR);
		generateExceptionOutline(fLastNode);
	}
	
	public void errorFromElement(Exception exception, AntElementNode node, int lineNumber, int column) {
		if (node == null) {
			if (!fStillOpenElements.empty()) {
				node= (AntElementNode)fStillOpenElements.peek();
			} else {
				node= fLastNode;
			}
		}
		computeEndLocationForErrorNode(node, lineNumber, column);
		notifyProblemRequestor(exception, node, XMLProblem.SEVERTITY_ERROR);
		generateExceptionOutline(node);
	}

	private AntElementNode createProblemElement(SAXParseException exception) {
		int lineNumber= exception.getLineNumber();
		StringBuffer message= new StringBuffer(exception.getMessage());
		if (lineNumber != -1){
			message.append(AntOutlineMessages.getString("OutlinePreparingHandler._line___2") + lineNumber); //$NON-NLS-1$
		}

		AntElementNode errorNode= new AntElementNode(message.toString());
		errorNode.setFilePath(exception.getSystemId());
		//errorNode.setExternal(isExternal());
		errorNode.setIsErrorNode(true);
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

	public void fatalError(Exception exception) {
		if (fStillOpenElements.isEmpty()) {
			//TODO do we need to handle this better
			return;
		}
		AntElementNode node= (AntElementNode)fStillOpenElements.peek();
		generateExceptionOutline(node);
		
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
						node.setIsErrorNode(true);
					} else {
						node= createProblemElement(parseException);
					}
				} catch (BadLocationException be) {
					node= createProblemElement(parseException);
				}
			}
		}
		
		notifyProblemRequestor(exception, node, XMLProblem.SEVERTITY_FATAL_ERROR);
		
		while (node.getParentNode() != null) {
			AntElementNode parentNode= node.getParentNode();
			if (parentNode.getLength() == -1) {
				parentNode.setLength(node.getOffset() - parentNode.getOffset() + node.getLength());
			}
			node= parentNode;
		}
	}
	
	public AntElementNode getOpenElement() {
		if (fStillOpenElements.isEmpty()) {
			return null;
		}
		return (AntElementNode)fStillOpenElements.peek();
	}

	
	public String getEntityPath(String entityName) {
		if (fEntityNameToPath != null) {
			return (String)fEntityNameToPath.get(entityName);
		} 
		return null;
	}
	
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
	
	public String getPropertyValue(String propertyName) {
		AntProjectNode projectNode= getProjectNode();
		if (projectNode == null) {
			return null;
		}
		return projectNode.getProject().getProperty(propertyName);
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

	public void setResolveFully(boolean resolveFully) {
		fValidateFully= resolveFully;
		resolveBuildfile();
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
	
	private ClassLoader getClassLoader() {
		AntCorePreferences corePreferences = AntCorePlugin.getPlugin().getPreferences();
		URL[] urls = corePreferences.getURLs();
		//ClassLoader[] pluginLoaders = corePreferences.getPluginClassLoaders();
		//return new AntClassLoader(urls, pluginLoaders);
		org.apache.tools.ant.AntClassLoader loader= new org.apache.tools.ant.AntClassLoader(this.getClass().getClassLoader(), false);
		for (int i = 0; i < urls.length; i++) {
			URL url = urls[i];
			loader.addPathElement(url.getFile());
		}
		return loader;
	}

	
	public String getTargetDescription(String targetRename) {
		AntProjectNode projectNode= getProjectNode();
		if (projectNode == null) {
			return null;
		}
		Project project= projectNode.getProject();
		Map targets= project.getTargets();
		Target target= (Target)targets.get(targetRename);
		if (target != null) {
			return target.getDescription();
		}
		return null;
	}
	
	public AntProjectNode getProjectNode() {
		possiblyWaitForReconcile();
		return fProjectNode;
	}
	
	private void possiblyWaitForReconcile() {
		synchronized (fDirtyLock) {
			if (!fIsDirty) {
				return;
			}
		}
		synchronized (this) {
			//wait for the reconcile from the edit
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * 
	 */
	public void setReplaceHasOccurred() {
		fReplaceHasOccurred= true;
	}
}