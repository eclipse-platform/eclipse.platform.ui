/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionListener;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.IExpressionsListener;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.icu.text.MessageFormat;

/**
 * The expression manager manages all registered expressions
 * for the debug plug-in. It is instantiated by the debug plug-in
 * at startup.
 *
 * @see IExpressionManager
 */
public class ExpressionManager extends PlatformObject implements IExpressionManager {
	
	/**
	 * Ordered collection of registered expressions.
	 */
	private Vector fExpressions = null;
	
	/**
	 * List of expression listeners
	 */
	private ListenerList fListeners = null;
	
	/**
	 * List of expressions listeners (plural)
	 */
	private ListenerList fExpressionsListeners = null;
	
	/**
	 * Mapping of debug model identifiers (String) to
	 * expression delegate extensions (IConfigurationElement)
	 */
	private Map fWatchExpressionDelegates= new HashMap();	
	
	// Constants for add/remove/change/insert/move notification
	private static final int ADDED = 1;
	private static final int CHANGED = 2;
	private static final int REMOVED = 3;
	private static final int INSERTED = 4;
	private static final int MOVED = 5;

	// Preference for persisted watch expressions
	private static final String PREF_WATCH_EXPRESSIONS= "prefWatchExpressions"; //$NON-NLS-1$
	// Persisted watch expression XML tags
	private static final String WATCH_EXPRESSIONS_TAG= "watchExpressions"; //$NON-NLS-1$
	private static final String EXPRESSION_TAG= "expression"; //$NON-NLS-1$
	private static final String TEXT_TAG= "text"; //$NON-NLS-1$
	private static final String ENABLED_TAG= "enabled"; //$NON-NLS-1$
	// XML values
	private static final String TRUE_VALUE= "true"; //$NON-NLS-1$
	private static final String FALSE_VALUE= "false"; //$NON-NLS-1$
	
	public ExpressionManager() {
		loadPersistedExpressions();
		loadWatchExpressionDelegates();
	}
	
	/**
	 * Loads the mapping of debug models to watch expression delegates
	 * from the org.eclipse.debug.core.watchExpressionDelegates
	 * extension point.
	 */
	private void loadWatchExpressionDelegates() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(DebugPlugin.getUniqueIdentifier(), "watchExpressionDelegates"); //$NON-NLS-1$
		IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
		for (int i = 0; i < configurationElements.length; i++) {
			IConfigurationElement element = configurationElements[i];
			if (element.getName().equals("watchExpressionDelegate")) { //$NON-NLS-1$
				String debugModel = element.getAttribute("debugModel"); //$NON-NLS-1$
				if (debugModel == null || debugModel.length() == 0) {
					continue;
				}
				fWatchExpressionDelegates.put(debugModel, element);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionManager#newWatchExpressionDelegate(java.lang.String)
	 */
	public IWatchExpressionDelegate newWatchExpressionDelegate(String debugModel) {
		try {
			IConfigurationElement element= (IConfigurationElement) fWatchExpressionDelegates.get(debugModel);
			if (element != null) {
				return (IWatchExpressionDelegate) element.createExecutableExtension(IConfigurationElementConstants.DELEGATE_CLASS);
			} 
			return null;
		} catch (CoreException e) {
			DebugPlugin.log(e);
			return null;
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IExpressionManager#hasWatchExpressionDelegate(java.lang.String)
     */
    public boolean hasWatchExpressionDelegate(String id) {
        IConfigurationElement element= (IConfigurationElement) fWatchExpressionDelegates.get(id);
        return element != null;
    }
    
	/**
	 * Loads any persisted watch expressions from the preferences.
	 * NOTE: It's important that no setter methods are called on
	 * 		the watchpoints which will fire change events as this
	 * 		will cause an infinite loop (see Bug 27281).
	 */
	private void loadPersistedExpressions() {
		String expressionsString= DebugPlugin.getDefault().getPluginPreferences().getString(PREF_WATCH_EXPRESSIONS);
		if (expressionsString.length() == 0) {
			return;
		}
		Element root;
		try {
			root = DebugPlugin.parseDocument(expressionsString);
		} catch (CoreException e) {
			DebugPlugin.logMessage("An exception occurred while loading watch expressions.", e); //$NON-NLS-1$
			return;
		}
		if (!root.getNodeName().equals(WATCH_EXPRESSIONS_TAG)) {
			DebugPlugin.logMessage("Invalid format encountered while loading watch expressions.", null); //$NON-NLS-1$
			return;
		}
		NodeList list= root.getChildNodes();
		for (int i= 0, numItems= list.getLength(); i < numItems; i++) {
			Node node= list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element= (Element) node;
				if (!element.getNodeName().equals(EXPRESSION_TAG)) {
					DebugPlugin.logMessage(MessageFormat.format("Invalid XML element encountered while loading watch expressions: {0}", new String[] {node.getNodeName()}), null); //$NON-NLS-1$
					continue;
				}
				String expressionText= element.getAttribute(TEXT_TAG);
				if (expressionText.length() > 0) {
					boolean enabled= TRUE_VALUE.equals(element.getAttribute(ENABLED_TAG));
					IWatchExpression expression= newWatchExpression(expressionText, enabled);
					if (fExpressions == null) {
						fExpressions= new Vector(list.getLength());
					}
					fExpressions.add(expression);
				} else {
					DebugPlugin.logMessage("Invalid expression entry encountered while loading watch expressions. Expression text is empty.", null); //$NON-NLS-1$
				}
			}
		}
	}
	
	/**
	 * Creates a new watch expression with the given expression
	 * and the given enablement;
	 * 
	 * @param expressionText the text of the expression to be evaluated
	 * @param enabled whether or not the new expression should be enabled
	 * @return the new watch expression
	 */
	private IWatchExpression newWatchExpression(String expressionText, boolean enabled) {
		return new WatchExpression(expressionText, enabled);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionManager#newWatchExpression(java.lang.String)
	 */
	public IWatchExpression newWatchExpression(String expressionText) {
		return new WatchExpression(expressionText);
	}
	
	/**
	 * Persists this manager's watch expressions as XML in the
	 * preference store. 
	 */
	public void storeWatchExpressions() {
		Preferences prefs= DebugPlugin.getDefault().getPluginPreferences();
		String expressionString= ""; //$NON-NLS-1$
		try {
			expressionString= getWatchExpressionsAsXML();
		} catch (IOException e) {
			DebugPlugin.log(e);
		} catch (ParserConfigurationException e) {
			DebugPlugin.log(e);
		} catch (TransformerException e) {
			DebugPlugin.log(e);
		}
		prefs.setValue(PREF_WATCH_EXPRESSIONS, expressionString);
	}

	/**
	 * Returns this manager's watch expressions as XML.
	 * @return this manager's watch expressions as XML
	 * @throws IOException if an exception occurs while creating
	 * 		the XML document.
	 * @throws ParserConfigurationException if an exception occurs while creating
	 * 		the XML document.
	 * @throws TransformerException if an exception occurs while creating
	 * 		the XML document.
	 */
	private String getWatchExpressionsAsXML() throws IOException, ParserConfigurationException, TransformerException {
		IExpression[] expressions= getExpressions();
		Document document= LaunchManager.getDocument();
		Element rootElement= document.createElement(WATCH_EXPRESSIONS_TAG);
		document.appendChild(rootElement);
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression= expressions[i];
			if (expression instanceof IWatchExpression) {
				Element element= document.createElement(EXPRESSION_TAG); 
				element.setAttribute(TEXT_TAG, expression.getExpressionText());
				element.setAttribute(ENABLED_TAG, ((IWatchExpression) expression).isEnabled() ? TRUE_VALUE : FALSE_VALUE);
				rootElement.appendChild(element);
			}
		}
		return LaunchManager.serializeDocument(document);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionManager#addExpression(org.eclipse.debug.core.model.IExpression)
	 */
	public void addExpression(IExpression expression) {
		addExpressions(new IExpression[]{expression});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionManager#addExpressions(org.eclipse.debug.core.model.IExpression[])
	 */
	public void addExpressions(IExpression[] expressions) {
		if (fExpressions == null) {
			fExpressions = new Vector(expressions.length);
		}
		boolean addedWatchExpression= false;
		List added = new ArrayList(expressions.length);
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression = expressions[i];
			if (fExpressions.indexOf(expression) == -1) {
				added.add(expression);
				fExpressions.add(expression);
				if (expression instanceof IWatchExpression) {
					addedWatchExpression= true;
				}
			}				
		}
		if (!added.isEmpty()) {
			fireUpdate((IExpression[])added.toArray(new IExpression[added.size()]), ADDED);
		}
		if (addedWatchExpression) {
			storeWatchExpressions();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionManager#getExpressions()
	 */
	public IExpression[] getExpressions() {
		if (fExpressions == null) {
			return new IExpression[0];
		}
		IExpression[] temp= new IExpression[fExpressions.size()];
		fExpressions.copyInto(temp);
		return temp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionManager#getExpressions(java.lang.String)
	 */
	public IExpression[] getExpressions(String modelIdentifier) {
		if (fExpressions == null) {
			return new IExpression[0];
		}
		ArrayList temp= new ArrayList(fExpressions.size());
		Iterator iter= fExpressions.iterator();
		while (iter.hasNext()) {
			IExpression expression= (IExpression) iter.next();
			String id= expression.getModelIdentifier();
			if (id != null && id.equals(modelIdentifier)) {
				temp.add(expression);
			}
		}
		return (IExpression[]) temp.toArray(new IExpression[temp.size()]);
	}
	
	/**
	 * Adds the given expressions to the collection of registered expressions
	 * in the workspace and notifies all registered listeners. The expressions
	 * are inserted in the same order as the passed array at the index of the
	 * specified expressions (before or after it depending on the boolean argument).
	 * If no valid insertion location could be found, the expressions are added
	 * to the end of the collection. Has no effect on expressions already registered.
	 *
	 * @param expressions expressions to insert into the collection
	 * @param insertionLocation the expression at the location where expressions will be inserted
	 * @param insertBefore whether to insert the expressions before or after the given insertion location
	 * @since 3.4
	 */
	public void insertExpressions(IExpression[] expressions, IExpression insertionLocation, boolean insertBefore){
		if (fExpressions == null) {
			addExpressions(expressions);
			return;
		}
		
		int insertionIndex = fExpressions.indexOf(insertionLocation);
		if (insertionIndex < 0){
			addExpressions(expressions);
			return;
		}
		if (!insertBefore){
			insertionIndex++;
		}
		boolean addedWatchExpression = false;
		List added = new ArrayList(expressions.length);
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression = expressions[i];
			if (fExpressions.indexOf(expression) == -1) {
				//Insert in the same order as the array is passed
				fExpressions.add(insertionIndex+added.size(), expression);
				added.add(expression);
				if (expression instanceof IWatchExpression) {
					addedWatchExpression= true;
				}
			}				
		}
		
		if (!added.isEmpty()) {
			fireUpdate((IExpression[])added.toArray(new IExpression[added.size()]), INSERTED, insertionIndex);
		}
		if (addedWatchExpression) {
			storeWatchExpressions();
		}
	}
	
	/**
	 * Moves the given expressions from their location in the collection
	 * of registered expressions in the workspace to the specified insertion
	 * location.  Notifies all registered listeners.  This method has no effect
	 * if an expression does not exist in the collection or if no valid insertion 
	 * location could be determined.
	 *   
	 * @param expressions expressions to move
	 * @param insertionLocation the expression at the location to insert the moved expressions
	 * @param insertBefore whether to insert the moved expressions before or after the given insertion location
	 * @since 3.4
	 */
	public void moveExpressions(IExpression[] expressions, IExpression insertionLocation, boolean insertBefore){
		if (fExpressions == null){
			return;
		}
		int insertionIndex = fExpressions.indexOf(insertionLocation);
		if (insertionIndex < 0){
			return;
		}
		if (!insertBefore){
			insertionIndex++;
		}
		
		List movedExpressions = new ArrayList(expressions.length);
		for (int i = 0; i < expressions.length; i++) {
			int removeIndex = fExpressions.indexOf(expressions[i]);
			if (removeIndex >= 0){
				movedExpressions.add(expressions[i]);
				if (removeIndex < insertionIndex){
					insertionIndex--;
				}
				fExpressions.remove(removeIndex);
			}
		}
		IExpression[] movedExpressionsArray = (IExpression[])movedExpressions.toArray(new IExpression[movedExpressions.size()]);
		for (int i = 0; i < movedExpressionsArray.length; i++) {
			// Insert the expressions in the same order as the passed array
			fExpressions.add(insertionIndex+i,movedExpressionsArray[i]);
		}
				
		if (!movedExpressions.isEmpty()) {
			fireUpdate(movedExpressionsArray, MOVED, insertionIndex);
			storeWatchExpressions();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionManager#removeExpression(org.eclipse.debug.core.model.IExpression)
	 */
	public void removeExpression(IExpression expression) {
		removeExpressions(new IExpression[] {expression});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionManager#removeExpressions(org.eclipse.debug.core.model.IExpression[])
	 */
	public void removeExpressions(IExpression[] expressions) {
		if (fExpressions == null) {
			return;
		}
		List removed = new ArrayList(expressions.length);
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression = expressions[i];
			if (fExpressions.remove(expression)) {
				removed.add(expression);
				expression.dispose();
			}				
		}
		if (!removed.isEmpty()) {
			fireUpdate((IExpression[])removed.toArray(new IExpression[removed.size()]), REMOVED);
			storeWatchExpressions();
		}
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionManager#addExpressionListener(org.eclipse.debug.core.IExpressionListener)
	 */
	public void addExpressionListener(IExpressionListener listener) {
		if (fListeners == null) {
			fListeners = new ListenerList();
		}
		fListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionManager#removeExpressionListener(org.eclipse.debug.core.IExpressionListener)
	 */
	public void removeExpressionListener(IExpressionListener listener) {
		if (fListeners == null) {
			return;
		}
		fListeners.remove(listener);
	}
	
	/**
	 * The given watch expression has changed. Update the persisted
	 * expressions to store this change as indicated
	 * 
	 * @param expression the changed expression
	 * @param persist whether to persist the expressions
	 */
	protected void watchExpressionChanged(IWatchExpression expression, boolean persist) {
		if (fExpressions != null && fExpressions.contains(expression)) {
			if (persist) {
				storeWatchExpressions();
			}
			fireUpdate(new IExpression[]{expression}, CHANGED);
		}
	}

	/**
	 * Notifies listeners of the adds/removes/changes
	 * 
	 * @param expressions expressions that were modified
	 * @param update update flags
	 */
	private void fireUpdate(IExpression[] expressions, int update){
		fireUpdate(expressions, update, -1);
	}
	
	/**
	 * Notifies listeners of the adds/removes/changes/insertions/moves
	 * 
	 * @param expressions expressions that were modified
	 * @param update update flags
	 * @param index index where expressions were inserted/moved to or <code>-1</code>
	 */
	private void fireUpdate(IExpression[] expressions, int update, int index){
		// single listeners
		getExpressionNotifier().notify(expressions, update);
		
		// multi listeners
		getExpressionsNotifier().notify(expressions, update, index);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionManager#hasExpressions()
	 */
	public boolean hasExpressions() {
		return fExpressions != null && !fExpressions.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionManager#addExpressionListener(org.eclipse.debug.core.IExpressionsListener)
	 */
	public void addExpressionListener(IExpressionsListener listener) {
		if (fExpressionsListeners == null) {
			fExpressionsListeners = new ListenerList();
		}
		fExpressionsListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IExpressionManager#removeExpressionListener(org.eclipse.debug.core.IExpressionsListener)
	 */
	public void removeExpressionListener(IExpressionsListener listener) {
		if (fExpressionsListeners == null) {
			return;
		}
		fExpressionsListeners.remove(listener);
	}
	
	private ExpressionNotifier getExpressionNotifier() {
		return new ExpressionNotifier();
	}
	
	/**
	 * Notifies an expression listener (single expression) in a safe runnable to
	 * handle exceptions.
	 */
	class ExpressionNotifier implements ISafeRunnable {
		
		private IExpressionListener fListener;
		private int fType;
		private IExpression fExpression;

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, "An exception occurred during expression change notification.", exception);  //$NON-NLS-1$
			DebugPlugin.log(status);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			switch (fType) {
				case ADDED:
					fListener.expressionAdded(fExpression);
					break;
				case REMOVED:
					fListener.expressionRemoved(fExpression);
					break;
				case CHANGED:
					fListener.expressionChanged(fExpression);		
					break;
			}			
		}

		/**
		 * Notifies listeners of the add/change/remove
		 * 
		 * @param expressions the expressions that have changed
		 * @param update the type of change
		 */
		public void notify(IExpression[] expressions, int update) {
			if (fListeners != null) {
				fType = update;
				Object[] copiedListeners= fListeners.getListeners();
				for (int i= 0; i < copiedListeners.length; i++) {
					fListener = (IExpressionListener)copiedListeners[i];
					for (int j = 0; j < expressions.length; j++) {
						fExpression = expressions[j];
                        SafeRunner.run(this);
					}
				}			
			}
			fListener = null;
			fExpression = null;
		}
	}
	
	/**
	 * Returns the expressions notifier
	 * @return the expressions notifier
	 */
	private ExpressionsNotifier getExpressionsNotifier() {
		return new ExpressionsNotifier();
	}
	
	/**
	 * Notifies an expression listener (multiple expressions) in a safe runnable
	 * to handle exceptions.
	 */
	class ExpressionsNotifier implements ISafeRunnable {
		
		private IExpressionsListener fListener;
		private int fType;
		private int fIndex;
		private IExpression[] fNotifierExpressions;
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, "An exception occurred during expression change notification.", exception);  //$NON-NLS-1$
			DebugPlugin.log(status);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			switch (fType) {
				case MOVED:
					// If the listener doesn't know about moves or the insertion location is unknown, do nothing.
					if (fIndex >= 0 && fListener instanceof IExpressionsListener2){
						((IExpressionsListener2)fListener).expressionsMoved(fNotifierExpressions, fIndex);
					}
					break;
				case INSERTED:
					// If the listener doesn't know about insertions or the insertion location is unknown, notify of an ADD
					if (fIndex >= 0 && fListener instanceof IExpressionsListener2){
						((IExpressionsListener2)fListener).expressionsInserted(fNotifierExpressions, fIndex);
					} else {
						fListener.expressionsAdded(fNotifierExpressions);
					}
					break;
				case ADDED:
					fListener.expressionsAdded(fNotifierExpressions);
					break;
				case REMOVED:
					fListener.expressionsRemoved(fNotifierExpressions);
					break;
				case CHANGED:
					fListener.expressionsChanged(fNotifierExpressions);		
					break;
			}			
		}

		/**
		 * Notifies listeners of the adds/changes/removes
		 * 
		 * @param expressions the expressions that changed
		 * @param update the type of change
		 * @param index the index of the first change
		 */
		public void notify(IExpression[] expressions, int update, int index) {
			if (fExpressionsListeners != null) {
				fNotifierExpressions = expressions;
				fType = update;
				fIndex = index;
				Object[] copiedListeners = fExpressionsListeners.getListeners();
				for (int i= 0; i < copiedListeners.length; i++) {
					fListener = (IExpressionsListener)copiedListeners[i];
                    SafeRunner.run(this);
				}
			}	
			fNotifierExpressions = null;
			fListener = null;				
		}
	}
}
