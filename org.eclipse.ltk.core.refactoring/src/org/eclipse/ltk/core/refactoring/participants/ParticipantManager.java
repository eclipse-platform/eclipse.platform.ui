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
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Facade to access the rename, move, delete, create and copy participant
 * extension point provided by the org.eclipse.ltk.core.refactoring plug-in.
 * <p> 
 * Note: this class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 */
public class ParticipantManager {
	
	private ParticipantManager() {
		// no instance 
	}
	
	//---- Rename participants ----------------------------------------------------------------
	
	private static final String RENAME_PARTICIPANT_EXT_POINT= "renameParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgRenameInstance= 
		new ParticipantExtensionPoint("Rename", RENAME_PARTICIPANT_EXT_POINT, RenameParticipant.class); //$NON-NLS-1$
	
	/**
	 * Loads the rename participants for the given element.
	 * 
	 * @param status a refactoring status to report status if problems occurred while
	 *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be renamed
	 * @param arguments the rename arguments describing the rename
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 * 
	 * @return an array of rename participants
	 */
	public static RenameParticipant[] loadRenameParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, RenameArguments arguments, String[] affectedNatures, SharableParticipants shared) {
		RefactoringParticipant[] participants= fgRenameInstance.getParticipants(status, processor, element, arguments, affectedNatures, shared);
		RenameParticipant[] result= new RenameParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}
	
	//---- Move participants ----------------------------------------------------------------
	
	private static final String MOVE_PARTICIPANT_EXT_POINT= "moveParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgMoveExtensions= 
		new ParticipantExtensionPoint("Move", MOVE_PARTICIPANT_EXT_POINT, MoveParticipant.class); //$NON-NLS-1$

	/**
	 * Loads the move participants for the given element.
	 * 
	 * @param status a refactoring status to report status if problems occurred while
	 *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be moved
	 * @param arguments the move arguments describing the move
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 * 
	 * @return an array of move participants
	 */
	public static MoveParticipant[] loadMoveParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, MoveArguments arguments, String[] affectedNatures, SharableParticipants shared) {
		RefactoringParticipant[] participants= fgMoveExtensions.getParticipants(status, processor, element, arguments, affectedNatures, shared);
		MoveParticipant[] result= new MoveParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}

	//---- Delete participants ----------------------------------------------------------------
	
	private static final String DELETE_PARTICIPANT_EXT_POINT= "deleteParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgDeleteInstance= 
		new ParticipantExtensionPoint("Delete", DELETE_PARTICIPANT_EXT_POINT, DeleteParticipant.class); //$NON-NLS-1$
	
	/**
	 * Loads the delete participants for the given element.
	 * @param status a refactoring status to report status if problems occurred while
     *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be deleted
	 * @param arguments the delete arguments describing the delete
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 * 
	 * @return an array of delete participants
	 */
	public static DeleteParticipant[] loadDeleteParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, DeleteArguments arguments, String[] affectedNatures, SharableParticipants shared) {
		RefactoringParticipant[] participants= fgDeleteInstance.getParticipants(status, processor, element, arguments, affectedNatures, shared);
		DeleteParticipant[] result= new DeleteParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}

	//---- Create participants ----------------------------------------------------------------
	
	private static final String CREATE_PARTICIPANT_EXT_POINT= "createParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgCreateInstance= 
		new ParticipantExtensionPoint("Create", CREATE_PARTICIPANT_EXT_POINT, CreateParticipant.class); //$NON-NLS-1$
	
	/**
	 * Loads the create participants for the given element.
	 * 
	 * @param status a refactoring status to report status if problems occurred while
	 *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be created or a corresponding descriptor
	 * @param arguments the create arguments describing the create
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 * 
	 * @return an array of create participants
	 */
	public static CreateParticipant[] loadCreateParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, CreateArguments arguments, String affectedNatures[], SharableParticipants shared) {
		RefactoringParticipant[] participants= fgCreateInstance.getParticipants(status, processor, element, arguments, affectedNatures, shared);
		CreateParticipant[] result= new CreateParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}

	//---- Copy participants ----------------------------------------------------------------
	
	private static final String COPY_PARTICIPANT_EXT_POINT= "copyParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgCopyInstance= 
		new ParticipantExtensionPoint("Copy", COPY_PARTICIPANT_EXT_POINT, CopyParticipant.class); //$NON-NLS-1$
	
	/**
	 * Loads the copy participants for the given element.
	 * 
	 * @param status a refactoring status to report status if problems occurred while
	 *  loading the participants
	 * @param processor the processor that will own the participants
	 * @param element the element to be copied or a corresponding descriptor
	 * @param arguments the copy arguments describing the copy operation
	 * @param affectedNatures an array of project natures affected by the refactoring
	 * @param shared a list of shared participants
	 * 
	 * @return an array of copy participants
	 */
	public static CopyParticipant[] loadCopyParticipants(RefactoringStatus status, RefactoringProcessor processor, Object element, CopyArguments arguments, String affectedNatures[], SharableParticipants shared) {
		RefactoringParticipant[] participants= fgCopyInstance.getParticipants(status, processor, element, arguments, affectedNatures, shared);
		CopyParticipant[] result= new CopyParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}
}
