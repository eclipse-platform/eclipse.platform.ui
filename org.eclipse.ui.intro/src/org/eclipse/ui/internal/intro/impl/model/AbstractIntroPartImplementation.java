/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.intro.impl.model;

import java.util.*;
import java.util.List;

import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.internal.intro.impl.*;
import org.eclipse.ui.internal.intro.impl.model.viewer.*;
import org.eclipse.ui.internal.intro.impl.util.*;
import org.eclipse.ui.intro.*;
import org.eclipse.ui.intro.config.*;

/**
 * UI Implementation class that represents a Presentation Part. This class is
 * instantiated from plugin markup and so we need a default constructor,
 * including in subclasses. It has some utility methods, including maintaining a
 * history of navigation locations.
 *  
 */
public abstract class AbstractIntroPartImplementation {

    // CustomizableIntroPart instance.
    private CustomizableIntroPart introPart = null;

    private Vector history = null;

    private int navigationLocation = 0;

    // REVISIT: revisit this whole history stuff.
    // state flag.
    private boolean navigation = false;

    private Action viewIntroModelAction = new Action() {

        {
            setToolTipText(IntroPlugin
                    .getString("IntroPart.showContentButton_tooltip")); //$NON-NLS-1$
            setImageDescriptor(ImageUtil
                    .createImageDescriptor("contents_view.gif")); //$NON-NLS-1$
        }

        public void run() {
            ElementTreeSelectionDialog treeViewer = new ElementTreeSelectionDialog(
                    getIntroPart().getIntroSite().getShell(),
                    new IntroModelLabelProvider(),
                    new IntroModelContentProvider());
            treeViewer.setInput(getModelRoot());
            treeViewer.open();
        }
    };

    /**
     * Creates the UI based on this implementation class. .
     * 
     * @param parent
     */
    public abstract void createPartControl(Composite parent);

    /**
     * Called when the init method is called in the IIntroPart. Subclasses may
     * extend. Make sure you include a call to super.
     * 
     * @param introPart
     * @throws PartInitException
     */
    public void init(IIntroPart introPart) throws PartInitException {
        // we know the class type to cast to.
        this.introPart = (CustomizableIntroPart) introPart;
        history = new Vector();
    }

    /**
     * @return
     */
    public IntroModelRoot getModelRoot() {
        return IntroPlugin.getDefault().getIntroModelRoot();
    }

    /**
     * @return Returns the introPart.
     */
    public CustomizableIntroPart getIntroPart() {
        return introPart;
    }

    /**
     * Updates the UI navigation history with either a real URL, or a page ID.
     * If the navigation state is true, it is assumed that we need no updates to
     * the history, and so a call to this method does nothing.
     * 
     * @param pageId
     */
    public void updateHistory(String location) {
        if (navigation) {
            // if we got here due to a navigation event. reset state, and do
            // nothing.
            navigation = false;
            return;
        }

        // quick exit.
        if (!history.isEmpty() && getCurrentLocation().equals(location))
                // resetting the same location is useless.
                return;

        doUpdateHistory(location);
    }

    private void doUpdateHistory(String location) {
        // we got here due to an intro URL listener or an SWT Form hyperlink
        // listener.
        if (navigationLocation == getHistoryEndPosition())
            // we are at the end of the vector, just push.
            pushToHistory(location);
        else
            // we already navigated. add item at current location, and clear
            // rest of history. (Same as browser behavior.)
            trimHistory(location);
    }

    private void pushToHistory(String location) {
        history.add(location);
        // point the nav location to the end of the vector, and watch out if
        navigationLocation = getHistoryEndPosition();
    }

    private void trimHistory(String location) {
        List newHistory = history.subList(0, navigationLocation + 1);
        history = new Vector(newHistory);
        history.add(location);
        // point the nav location to the end of the vector.
        navigationLocation = getHistoryEndPosition();
    }

    /**
     * Return the position of the last element in the navigation history. If
     * vector is empty, return 0.
     * 
     * @param vector
     * @return
     */
    private int getHistoryEndPosition() {
        if (history.isEmpty())
            return 0;
        else
            return history.size() - 1;
    }

    protected void navigateBackward() {
        if (badNavigationLocation(navigationLocation - 1))
                // do nothing. We are at the begining.
                return;
        --navigationLocation;
    }

    /**
     * Navigate forward in the history. Returns true if you can navigate forward
     * 
     * @return
     */
    protected void navigateForward() {
        if (badNavigationLocation(navigationLocation + 1))
                // do nothing. We are at the begining.
                return;
        ++navigationLocation;
    }

    protected void setNavigationState(boolean state) {
        navigation = state;
    }

    private boolean badNavigationLocation(int navigationLocation) {
        if (navigationLocation < 0 | navigationLocation >= history.size())
            // bad nav location.
            return true;
        else
            return false;
    }
    
    /**
     * Save the current state of the intro.  Currently, we only store
     * information about the most recently visited intro page
     * @param memento
     */
    public void saveState(IMemento memento){  
    	saveCurrentPage(memento);
    }
    
    /**
     * This method saves the most recently visited dynamic intro page 
     * in the memento.  If a given implementation requires saving 
     * alternative information (e.g., information about the most
     * recently visited static page) it should override this method.
     * @param memento
     */
    protected void saveCurrentPage(IMemento memento) {
    	IntroModelRoot model = getModelRoot();
    	
		if(memento == null || model == null)
			return;
		
		String currentPage = model.getCurrentPageId();
    	if(currentPage != null && currentPage.length() > 0){
    		IMemento introMemento = memento.createChild(IIntroConstants.INTRO_MEMENTO_TYPE);
    		introMemento.putString(IIntroConstants.CURRENT_DYNAMIC_PAGE, currentPage);
    	}
    }
    
    public void setFocus() {
    }

    /**
     * Called when the IntroPart is disposed. Subclasses should override to
     * dispose of resources. By default, this implementation does nothing.
     */
    public void dispose() {

    }

    /**
     * Returns true if the current location in the navigation history represents
     * a URL. False if the location is an Intro Page id.
     * 
     * @return Returns the locationIsURL.
     */
    protected boolean locationIsURL() {
        return getCurrentLocation().startsWith("http") ? true : false; //$NON-NLS-1$
    }

    /**
     * Returns true if the current location in the navigation history represents
     * a URL. False if the location is an Intro Page id.
     * 
     * @return Returns the locationIsURL.
     */
    public String getCurrentLocation() {
        return (String) history.elementAt(navigationLocation);
    }

    public boolean canNavigateForward() {
        return navigationLocation != getHistoryEndPosition() ? true : false;
    }

    public boolean canNavigateBackward() {
        return navigationLocation == 0 ? false : true;
    }

    /*
     * Add the Intro Model Viewer as an action to all implementations.
     */
    protected void addToolBarActions() {
        // Handle menus:
        IActionBars actionBars = getIntroPart().getIntroSite().getActionBars();
        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        toolBarManager.add(viewIntroModelAction);
        toolBarManager.update(true);
        actionBars.updateActionBars();
    }

}