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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.UnknownElement;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.internal.core.IAntCoreConstants;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.editor.DecayCodeCompletionDataStructuresThread;
import org.eclipse.ant.internal.ui.editor.outline.AntEditorMarkerUpdater;
import org.eclipse.ant.internal.ui.editor.utils.ProjectHelper;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

public class AntModel extends AntModelLite {
	
	private IDocumentListener fListener;	
	private AntEditorMarkerUpdater fMarkerUpdater= null;
	private List fNonStructuralNodes= new ArrayList(1);
	
	private Preferences.IPropertyChangeListener fCorePropertyChangeListener= new Preferences.IPropertyChangeListener() {
		public void propertyChange(Preferences.PropertyChangeEvent event) {
			if (event.getProperty().equals(IAntCoreConstants.PREFERENCE_CLASSPATH_CHANGED)) {
				if (((Boolean)event.getNewValue()) == Boolean.TRUE) {
					reconcileForPropertyChange(true);		
				}
			}
		}
	};
	
	private Preferences.IPropertyChangeListener fUIPropertyChangeListener= new Preferences.IPropertyChangeListener() {
		public void propertyChange(Preferences.PropertyChangeEvent event) {
			String property= event.getProperty();
			if (property.equals(AntEditorPreferenceConstants.PROBLEM)) {
				AntUIPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(fUIPropertyChangeListener);
				reconcileForPropertyChange(false);
				AntUIPlugin.getDefault().getPluginPreferences().setToDefault(AntEditorPreferenceConstants.PROBLEM);
				AntUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(fUIPropertyChangeListener);
			} else if (property.equals(AntEditorPreferenceConstants.CODEASSIST_USER_DEFINED_TASKS)) {
				if (((Boolean)event.getNewValue()).booleanValue()) {
					reconcileForPropertyChange(false);		
				}
			}
		}
	};

	public AntModel(IDocument document, IProblemRequestor problemRequestor, LocationProvider locationProvider) {
		super(document, problemRequestor, locationProvider);
		
		setCanGetLexicalInfo(true);
		setCanGetPositionInfo(true);
		setCanGetTaskInfo(true);
		fMarkerUpdater= new AntEditorMarkerUpdater();
		fMarkerUpdater.setModel(this);
		
		AntCorePlugin.getPlugin().getPluginPreferences().addPropertyChangeListener(fCorePropertyChangeListener);
		AntUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(fUIPropertyChangeListener);
		
		DecayCodeCompletionDataStructuresThread.cancel();
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
		synchronized (this) {
			if (fDocument != null && fListener != null) {
				fDocument.removeDocumentListener(fListener);
			}
			fDocument= null;
			fLocationProvider= null;
			ProjectHelper.setAntModel(null);
		}
		
		AntCorePlugin.getPlugin().getPluginPreferences().removePropertyChangeListener(fCorePropertyChangeListener);
		AntUIPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(fUIPropertyChangeListener);
		fgInstanceCount--;
		if (fgInstanceCount == 0) {
			fgClassLoader= null;
			DecayCodeCompletionDataStructuresThread.getDefault().start();
		}
		cleanup();
	}
	
	protected void reset() {
		super.reset();
		fNonStructuralNodes= new ArrayList();
	}

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

	private IProblem createProblem(Exception exception, int offset, int length,  int severity) {
		return createProblem(exception.getMessage(), offset, length, severity);
	}
	
	private IProblem createProblem(String message, int offset, int length, int severity) {
		return new AntModelProblem(message, severity, offset, length, getLine(offset));
	}

	protected void notifyProblemRequestor(Exception exception, AntElementNode element, int severity) {
		AntElementNode importNode= element.getImportNode();
		if (importNode != null) {
			element= importNode;
		}
		IProblem problem= createProblem(exception, element.getOffset(), element.getLength(), severity);
		acceptProblem(problem);
		element.associatedProblem(problem);
	}
	
	protected void notifyProblemRequestor(Exception exception, int offset, int length, int severity) {
		if (fProblemRequestor != null) {
			IProblem problem= createProblem(exception, offset, length, severity);
			acceptProblem(problem);
		}
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
	
	public String getTargetDescription(String targetName) {
		AntTargetNode target= getTargetNode(targetName);
		if (target != null) {
			return target.getTarget().getDescription();
		}
		return null;
	}
	
	public void updateMarkers() {
		reconcile();
		fMarkerUpdater.updateMarkers();
	}
	
	/**
	 * The Ant model has been reconciled for the first time with the contents displayed in the Ant editor.
	 * Since problem marker creation has been added after many buildfiles have been created (or if the file has been
	 * created outside of Eclipse) we need to update the markers to match the problems.
	 */
	public void updateForInitialReconcile() {
		fMarkerUpdater.updateMarkers();
	}

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

	/**
	 * Returns the nodes that are not part of the normal structural outline such
	 * as DTD nodes or comment nodes
	 * @return The nodes that are not part of the normal structural outline
	 */
	public List getNonStructuralNodes() {
		return fNonStructuralNodes;
	}
	
	public AntElementNode getPropertyNode(String text) {
		AntProjectNode node= getProjectNode();
		if (node == null || !node.hasChildren()) {
			return null;
		}
		
		return findPropertyNode(text, node.getChildNodes());
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
}