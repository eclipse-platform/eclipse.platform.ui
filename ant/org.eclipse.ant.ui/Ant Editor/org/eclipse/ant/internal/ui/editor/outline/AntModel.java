/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.outline;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.eclipse.ant.internal.ui.editor.utils.ProjectHelper;
import org.eclipse.ant.internal.ui.editor.model.AntElementNode;
import org.eclipse.ant.internal.ui.editor.model.AntProjectNode;
import org.eclipse.ant.internal.ui.editor.model.AntPropertyNode;
import org.eclipse.ant.internal.ui.editor.model.AntTargetNode;
import org.eclipse.ant.internal.ui.editor.model.AntTaskNode;
import org.eclipse.ant.internal.ui.editor.model.IAntEditorConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

/**
 * Experimental ant model using new parsing
 */
public class AntModel {

	private XMLCore fCore;
	private IDocument fDocument;
	private IProblemRequestor fProblemRequestor;
	private ILocationProvider fLocationProvider;

	private AntProjectNode fProjectNode;
	private AntTargetNode fCurrentTargetNode;
	private AntElementNode fLastNode;
	
	 /**
     * Stack of still open elements.
     * <P>
     * On top of the stack is the innermost element.
     */
	private Stack fStillOpenElements = new Stack();
	
	private Map fTaskToNode= new HashMap();

	private final Object fDirtyLock= new Object();
	private boolean fIsDirty= true;
	private IDocumentListener fListener;
	
	/**
     * The find replace adapter for the document
     */
    private FindReplaceDocumentAdapter fFindReplaceAdapter;

	public AntModel(XMLCore core, IDocument document, IProblemRequestor problemRequestor, ILocationProvider locationProvider) {
		fCore= core;
		fDocument= document;
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
	
	public void reconcile() {
		
		synchronized (fDirtyLock) {
			if (!fIsDirty) {
				return;
			}
			fIsDirty= false;
		}

		synchronized (this) {
			if (fCore == null) {
				// disposed
				return;
			}
			
			if (fDocument == null) {
				fProjectNode= null;
			} else {
				reset();
				parseDocument(fDocument);
			} 
	
			fCore.notifyDocumentModelListeners(new DocumentModelChangeEvent(this));
		}
	}

	private void reset() {
		fCurrentTargetNode= null;
		fStillOpenElements= new Stack();
		fTaskToNode= new HashMap();
	}

	public synchronized AntElementNode[] getRootElements() {
		reconcile();
		if (fProjectNode == null) {
			return new AntElementNode[0];
		} else {
			return new AntElementNode[] {fProjectNode};
		}
	}

	/**
	 * Gets the content outline for a given input element.
	 * Returns the root Project, or <code>null</code> if the
	 * outline could not be generated.
	 */
	private void parseDocument(IDocument input) {
		if (input.getLength() == 0) {
			return;
		}
    	Project project = new Project();
    	project.init();
    	
    	/* 
    	 * Ant's parsing facilities always works on a file, therefore we need
    	 * to determine the actual location of the file. Though the file 
    	 * contents will not be parsed. We parse the passed document string.
    	 */
    	File file = getEditedFile();
    	String filePath= ""; //$NON-NLS-1$
    	if (file != null) {
    		filePath= file.getAbsolutePath();
    	}
    	project.setUserProperty("ant.file", filePath); //$NON-NLS-1$

    	try {
			ProjectHelper projectHelper= new ProjectHelper(this);
    		projectHelper.setBuildFile(file);
			beginReporting();
			project.addReference("ant.projectHelper", projectHelper); //$NON-NLS-1$
    		projectHelper.parse(project, input.get());  // File will be parsed here
    	} catch(BuildException e) {
			handleBuildException(e, null);
    	} finally {
    		configureProperties();
    		endReporting();
    		fTaskToNode= new HashMap();
    	}
	}

	public void handleBuildException(BuildException e, AntElementNode node) {
		try {
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
				originalOffset= getOffset(line, 1);
				nonWhitespaceOffset= originalOffset;
				length= getLastCharColumn(line) - (nonWhitespaceOffset - originalOffset);
				try {
					nonWhitespaceOffset= getNonWhitespaceOffset(line, 1);
				} catch (BadLocationException be) {
				}
			}
			
			notifyProblemRequestor(e, nonWhitespaceOffset, length, XMLProblem.SEVERTITY_ERROR);
		} catch (BadLocationException e1) {
		}
	}

	private void configureProperties() throws BuildException {
		Collection nodes= fTaskToNode.values();
		Iterator iter= nodes.iterator();
		while (iter.hasNext()) {
			AntTaskNode node = (AntTaskNode) iter.next();
			if (node instanceof AntPropertyNode) {
				((AntPropertyNode)node).configure();
			}
		}
	}

	protected File getEditedFile() {
		if (fLocationProvider != null) {
        	return fLocationProvider.getLocation().toFile();
		}
		return null;
    }

	private void generateExceptionOutline(AntElementNode openElement) {
		while (openElement != null) {
			openElement.setIsErrorNode(true);
			openElement= openElement.getParentNode();
		}
	}
	
	public ILocationProvider getLocationProvider() {
		return fLocationProvider;
	}

	public void addTarget(Target newTarget, int line, int column) {
		AntTargetNode targetNode= new AntTargetNode(newTarget);
		fProjectNode.addChildNode(targetNode);
		fCurrentTargetNode= targetNode;
		fStillOpenElements.push(targetNode);
		computeOffset(targetNode, line, column);
	}
	
	public void addProject(Project project, int line, int column) {
		fProjectNode= new AntProjectNode(project, this);
		fStillOpenElements.push(fProjectNode);
		computeOffset(fProjectNode, line, column);
	}

	public void addTask(Task newTask, Task parentTask, Attributes attributes, int line, int column) {
		AntTaskNode taskNode= newTaskNode(newTask, attributes);
		fTaskToNode.put(newTask, taskNode);
		if (parentTask == null) {
			if (fCurrentTargetNode == null) {
				fProjectNode.addChildNode(taskNode);
			} else {
				fCurrentTargetNode.addChildNode(taskNode);
			}
		} else {
			((AntTaskNode)fTaskToNode.get(parentTask)).addChildNode(taskNode);
		}
		fStillOpenElements.push(taskNode);
		computeOffset(taskNode, line, column);
	}
	
	private AntTaskNode newTaskNode(Task newTask, Attributes attributes) {
		String taskName= newTask.getTaskName();
		if (taskName.equalsIgnoreCase("property")) { //$NON-NLS-1$
			return new AntPropertyNode(newTask, attributes);
		} else if (taskName.equalsIgnoreCase("macrodef")  //$NON-NLS-1$
        		|| taskName.equalsIgnoreCase("presetdef")) { //$NON-NLS-1$
                    String name = attributes.getValue(IAntEditorConstants.ATTR_NAME);
                    return new AntTaskNode(newTask, name);
		} else if(taskName.equalsIgnoreCase("antcall")) { //$NON-NLS-1$
            return new AntTaskNode(newTask, generateLabel(taskName, attributes, IAntEditorConstants.ATTR_TARGET));
        } else if(taskName.equalsIgnoreCase("mkdir")) { //$NON-NLS-1$
            return new AntTaskNode(newTask, generateLabel(taskName, attributes, IAntEditorConstants.ATTR_DIR));
        } else if(taskName.equalsIgnoreCase("copy")) { //$NON-NLS-1$
        	 return new AntTaskNode(newTask, generateLabel(taskName, attributes, IAntEditorConstants.ATTR_DESTFILE));
        } else if(taskName.equalsIgnoreCase("tar")  //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("jar") //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("war") //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("zip")) { //$NON-NLS-1$
            return new AntTaskNode(newTask, generateLabel(newTask.getTaskName(), attributes, IAntEditorConstants.ATTR_DESTFILE));
        } else if(taskName.equalsIgnoreCase("untar")  //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("unjar") //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("unwar") //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("gunzip") //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("bunzip2") //$NON-NLS-1$
        	|| taskName.equalsIgnoreCase("unzip")) { //$NON-NLS-1$
            return new AntTaskNode(newTask, generateLabel(newTask.getTaskName(), attributes, IAntEditorConstants.ATTR_SRC));
        } else if(taskName.equalsIgnoreCase("gzip")  //$NON-NLS-1$
        		|| taskName.equalsIgnoreCase("bzip2")) { //$NON-NLS-1$
        		return new AntTaskNode(newTask, generateLabel(newTask.getTaskName(), attributes, IAntEditorConstants.ATTR_ZIPFILE));
        } else if(taskName.equalsIgnoreCase("exec")) { //$NON-NLS-1$
        	String label = "exec "; //$NON-NLS-1$
            String command = attributes.getValue(IAntEditorConstants.ATTR_COMMAND);
            if(command != null) {
            	label += command;
            }
            command = attributes.getValue(IAntEditorConstants.ATTR_EXECUTABLE);
            if(command != null) {
            	label += command;
            }
            return new AntTaskNode(newTask, label);        
		} else if(taskName.equalsIgnoreCase("delete")) { //$NON-NLS-1$
			
        	String label = "delete "; //$NON-NLS-1$
            String file = attributes.getValue(IAntEditorConstants.ATTR_FILE);
            if(file != null) {
            	label+= file;
            } else {
            	file = attributes.getValue(IAntEditorConstants.ATTR_DIR);
            	if(file != null) {
            		label+= file;
            	}
            }
            return new AntTaskNode(newTask, label);
        	
        } else if(taskName.equalsIgnoreCase("import")) { //$NON-NLS-1$
            return new AntTaskNode(newTask, generateLabel(taskName, attributes, IAntEditorConstants.ATTR_FILE)); //$NON-NLS-1$
        }
            
		return new AntTaskNode(newTask);
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
		//if (element.isExternal() && !isTopLevelRootExternal) {
		//	return;
	//	}
		
		try {
			int length;
		//	if (isTopLevelRootExternal) {
		//		length= element.getName().length() + 2;
		//	} else {
				
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
				
				//lastExternalEntityOffset= offset - 1;
			//}
 			
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
//			if (isTopLevelRootExternal) {
//				StringBuffer source= new StringBuffer();
//				source.append('&');
//				source.append(element.getName());
//				source.append(';');
//				IRegion result= findReplaceAdapter.search(lastExternalEntityOffset + 1, source.toString(), true, true, false, false);
//				offset= result.getOffset();
//				lastExternalEntityOffset= offset;
//			} else {
				String prefix= "<"; //$NON-NLS-1$
				if (column <= 0) {
					offset= getOffset(line, 0);
					String lineText= fDocument.get(fDocument.getLineOffset(line - 1), fDocument.getLineLength(line-1));
					offset+= lineText.lastIndexOf(prefix + element.getName()) + 1;
					//lastExternalEntityOffset= offset;
				} else {
					offset= getOffset(line, column);
					//lastExternalEntityOffset= offset - 1; 
					IRegion result= fFindReplaceAdapter.search(offset - 1, prefix, true, false, false, false);
					offset= result.getOffset();
				}
		//	}
 			
			element.setOffset(offset + 1);
			element.setSelectionLength(element.getName().length());
		} catch (BadLocationException e) {
			//ignore as the parser may be out of sync with the document during reconciliation
		}
	}
	
	private int getOffset(int line, int column) throws BadLocationException {
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
		computeLength(fLastNode, lineNumber, column);
	}
	
	public void acceptProblem(IProblem problem) {
		if (fProblemRequestor != null) {
			fProblemRequestor.acceptProblem(problem);
		}
	}

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

	protected IProblem createProblem(Exception exception, int offset, int length,  int severity) {
		return new XMLProblem(exception.toString(), exception.getMessage(), severity, offset, length);
	}

	protected void notifyProblemRequestor(Exception exception, AntElementNode element, int severity) {
		notifyProblemRequestor(exception, element.getOffset(), element.getLength(), severity);
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
	
	public void error(Exception exception, int start, int count) {
		error(exception, fLastNode, start, count);
	}
	
	public void error(Exception exception, AntElementNode node, int start, int count) {
		if (node == null) {
			if (!fStillOpenElements.empty()) {
				node= (AntElementNode)fStillOpenElements.peek();
			} else {
				node= fLastNode;
			}
		}
		
		computeEndLocationForErrorNode(node, start, count);
		
		if (start > -1 && count > -1) {
			notifyProblemRequestor(exception, start, count, XMLProblem.SEVERTITY_ERROR);
		} else {
			notifyProblemRequestor(exception, node, XMLProblem.SEVERTITY_ERROR);
		}
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
				startColumn= 1;
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

	/**
	 * @param propertyName
	 * @return
	 */
	public String getPropertyValue(String propertyName) {
		AntElementNode[] nodes= getRootElements();
		AntProjectNode projectNode= (AntProjectNode)nodes[0];
		return projectNode.getProject().getProperty(propertyName);
	}
}