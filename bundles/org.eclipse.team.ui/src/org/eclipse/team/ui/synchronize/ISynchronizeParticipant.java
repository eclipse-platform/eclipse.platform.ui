/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.synchronize;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A synchronize participant is a visual compoment that can be displayed within any
 * control (e.g. view, editor, dialog).  Typically a participant is used to show changes between 
 * local resources and variant states of those resources and allows the user to perform actions
 * to manipulate the changes. For example, a participant could show the relative synchronization
 * between local resources and those on an FTP server, or alternatively, between local
 * resources and local history.
 * <p>
 * When a participant is registered with the {@link ISynchronizeManager} it will automatically display 
 * in the <i>Synchronize View</i> and if the participant extension point
 * enabled <code>globalSynchronize</code> it will also appear in the global synchronize action
 * toolbar.
 * <p>
 * A participant is added to the workbench as follows:
 * <ul>
 * <li>A <code>synchronizeParticipant</code> extension is contributed to 
 * the team registry. This extension defines the participant id, name, icon, type, and 
 * participant class.
 * <li>The participant type is <code>static</code> it is automatically added
 * to the {@link ISynchronizeManager}.
 * <li>If a participant is not static, plug-in developers can add the participant to the 
 * manager via {@link ISynchronizeManager#addSynchronizeParticipants(ISynchronizeParticipant[]) and
 * remove it using {@link ISynchronizeManager#removeSynchronizeParticipants(ISynchronizeParticipant[]).
 * Note that you don't have to add the participant to the manager. You can instead create the
 * participant, display it, and then dispose of it yourself.
 * <li>For non-static participants you can configure the participant to support multiple instances. This will
 * allow multiple instances to be created and registered with the synchronize manager.
 * </ul></p>
 * <p>
 * Once a participant is added to the synchronize manager its lifecycle will be managed. On shutdown if
 * the <code>persistent</code> property is set, the participant will be asked to persist state via 
 * the <code>saveState()</code> method. At startup the <code>init()</code> method is called
 * with a handle to the state that was saved. The dispose method is called when the participant is
 * removed from the manager and at shutdown.
 * </p><p>
 * Clients are not intended to implement this interface. Instead, sublcass {@link AbstractSynchronizeParticpant}.
 * </p>
 * @see ISynchronizeView
 * @see ISynchronizeManager
 * @see AbstractSynchronizeParticpant
 * @since 3.0
 */
public interface ISynchronizeParticipant extends IExecutableExtension {
	/**
	 * Returns the unique id that identified the <i>type</i> of this
	 * synchronize participant. The synchronize manager supports registering
	 * several instances of the same participant type.
	 * 
	 * @return the unique id that identified the <i>type</i> of this
	 * synchronize participant.
	 */
	public String getId();
	
	/**
	 * Returns the instance id that identified the unique instance of this
	 * participant. The synchronize manager supports registering
	 * several instances of the same participant type and this id is used
	 * to differentiate between them.
	 * 
	 * @return  the instance id that identified the unique instance of this
	 * participant or <code>null</code> if this participant doesn't support
	 * multiple instances.
	 */	
	public String getSecondaryId();
	
	/**
	 * Returns the name of this synchronize participant. This name is displayed to the user.
	 * 
	 * @return the name of this synchronize participant
	 */
	public String getName();
	
	/**
	 * Returns an image descriptor for this synchronize participant, or <code>null</code>
	 * if none.
	 * 
	 * @return an image descriptor for this synchronize participant, or <code>null</code>
	 * if none
	 */
	public ImageDescriptor getImageDescriptor();
	
	/**
	 * Creates the configuration for the participant page. The configuration controls the
	 * options for displaying the participant. The configuration used to initialize the page
	 * when {@link #createPage(ISynchronizePageConfiguration)} is called and as such
	 * can be used to pre-configure visual properties of the displayed page.
	 * 
	 * @return the configuration for the participant page.
	 */
	public ISynchronizePageConfiguration createPageConfiguration();
	
	/**
	 * Creates and returns a new page for this synchronize participant. The
	 * page is displayed using the parameters from the configuration. For example,
	 * the configuration defines the context in which the page is shown, via the
	 * {@link ISynchronizePageSite}. 
	 * 
	 * @param configuration used to initialize the page
	 * @return a page book view page representation of this synchronize
	 * participant
	 */
	public IPageBookViewPage createPage(ISynchronizePageConfiguration configuration);
	
	/**
	 * Creates and returns a wizard page used to globally synchronize this participant. Participants
	 * returning a wizard will get added to the global Team synchronize action and users can
	 * easily initiate a synchronization on the participant. The implementor can decide exactly
	 * what information is needed from the user to synchronize and perform the action
	 * when the wizard is closed.
	 * 
	 * @return a wizard that prompts the user for information necessary to synchronize this
	 * participant or <code>null</code> if this participant doesn't want to support global refresh.
	 */
	public IWizard createSynchronizeWizard();
	
	/**
	 * Initializes this participant with the given participant state.  
	 * A memento is passed to the participant which contains a snapshot 
	 * of the participants state from a previous session.
	 * <p>
	 * This method is automatically called by the team plugin shortly after
	 * participant construction. It marks the start of the views
	 * lifecycle. Clients must not call this method.
	 * </p> 
	 * @param secondaryId the secondayId of this participant instance or <code>null</code>
	 * if this participant doesn't support multiple instances.
	 * @param memento the participant state or <code>null</code> if there 
	 * is no previous saved state
	 * @exception PartInitException if this participant was not initialized 
	 * successfully
	 */
	public void init(String secondaryId, IMemento memento) throws PartInitException;
	
	/**
	 * Disposes of this synchronize participant and is called to free the 
	 * resources associated with a participant. When a participant is added
	 * to the {@link ISynchronizeManager} this method is called when the
	 * manager is shutdown or the participant is removed from the manager.
	 * </p>
	 * <p>
	 * Within this method a participant may release any resources, fonts, images, etc. 
	 * held by this part.  It is also very important to deregister all listeners.
	 * </p>
	 * <p>
	 * Clients should not call this method (the synchronize manager calls this 
	 * method at appropriate times).
	 * </p>
	 */
	public void dispose();
	
	/**
	 * Saves the participants object state within the memento. This state
	 * will be available when the participant is restored via <code>init</code>.
	 * <p>
	 * This method can be called multiple times during the lifetime of the
	 * participant object.
	 * </p>
	 * @param memento a memento to receive the object state
	 */
	public void saveState(IMemento memento);
	
	/**
	 * Adds a listener for changes to properties of this synchronize
	 * participant. Has no effect if an identical listener is already
	 * registered.
	 * <p>
	 * The changes supported by the synchronize view are as follows:
	 * <ul>
	 * <li><code>IBasicPropertyConstants.P_TEXT</code>- indicates the name
	 * of a synchronize participant has changed</li>
	 * <li><code>IBasicPropertyConstants.P_IMAGE</code>- indicates the
	 * image of a synchronize participant has changed</li>
	 * </ul></p>
	 * <p>
	 * Clients may define additional properties as required.
	 * </p>
	 * @param listener a property change listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener);
	
	/**
	 * Removes the given property listener from this synchronize participant.
	 * Has no effect if an identical listener is not alread registered.
	 * 
	 * @param listener a property listener
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener);
}