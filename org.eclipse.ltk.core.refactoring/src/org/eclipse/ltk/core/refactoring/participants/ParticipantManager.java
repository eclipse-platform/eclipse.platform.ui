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


/**
 * Facade to access the rename, move, delete, create and copy participant
 * extension point provided by the org.eclipse.ltk.core.refactoring plug-in.
 * 
 * @since 3.0
 */
public class ParticipantManager {
	
	//---- Rename participants ----------------------------------------------------------------
	
	private static final String RENAME_PARTICIPANT_EXT_POINT= "renameParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgRenameInstance= new ParticipantExtensionPoint("Rename", RENAME_PARTICIPANT_EXT_POINT); //$NON-NLS-1$
	
	public static RenameParticipant[] getRenameParticipants(RefactoringProcessor processor, Object element, RenameArguments arguments, String[] affectedNatures, SharableParticipants shared) {
		RefactoringParticipant[] participants= fgRenameInstance.getParticipants(processor, element, arguments, affectedNatures, shared);
		RenameParticipant[] result= new RenameParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}
	
	//---- Move participants ----------------------------------------------------------------
	
	private static final String MOVE_PARTICIPANT_EXT_POINT= "moveParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgMoveExtensions= new ParticipantExtensionPoint("Move", MOVE_PARTICIPANT_EXT_POINT); //$NON-NLS-1$

	public static MoveParticipant[] getMoveParticipants(RefactoringProcessor processor, Object element, MoveArguments arguments, String[] affectedNatures, SharableParticipants shared) {
		RefactoringParticipant[] participants= fgMoveExtensions.getParticipants(processor, element, arguments, affectedNatures, shared);
		MoveParticipant[] result= new MoveParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}

	//---- Copy participants ----------------------------------------------------------------
	
	private static final String COPY_PARTICIPANT_EXT_POINT= "copyParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgCopyInstance= new ParticipantExtensionPoint("Copy", COPY_PARTICIPANT_EXT_POINT); //$NON-NLS-1$
	
	public static CopyParticipant[] getCopyParticipants(RefactoringProcessor processor, Object element, CopyArguments arguments, String[] affectedNatures, SharableParticipants shared) {
		RefactoringParticipant[] participants= fgCopyInstance.getParticipants(processor, element, arguments, affectedNatures, shared);
		CopyParticipant[] result= new CopyParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}

	//---- Delete participants ----------------------------------------------------------------
	
	private static final String DELETE_PARTICIPANT_EXT_POINT= "deleteParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgDeleteInstance= new ParticipantExtensionPoint("Delete", DELETE_PARTICIPANT_EXT_POINT); //$NON-NLS-1$
	
	public static DeleteParticipant[] getDeleteParticipants(RefactoringProcessor processor, Object element, DeleteArguments arguments, String[] affectedNatures, SharableParticipants shared) {
		RefactoringParticipant[] participants= fgDeleteInstance.getParticipants(processor, element, arguments, affectedNatures, shared);
		DeleteParticipant[] result= new DeleteParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}

	//---- Create participants ----------------------------------------------------------------
	
	private static final String CREATE_PARTICIPANT_EXT_POINT= "createParticipants"; //$NON-NLS-1$
	private static ParticipantExtensionPoint fgCreateInstance= new ParticipantExtensionPoint("Create", CREATE_PARTICIPANT_EXT_POINT); //$NON-NLS-1$
	
	public static CreateParticipant[] getCreateParticipants(RefactoringProcessor processor, Object element, CreateArguments arguments, String affectedNatures[], SharableParticipants shared) {
		RefactoringParticipant[] participants= fgCreateInstance.getParticipants(processor, element, arguments, affectedNatures, shared);
		CreateParticipant[] result= new CreateParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}
}
