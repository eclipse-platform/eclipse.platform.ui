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
package org.eclipse.ltk.core.refactoring.participants;

import java.util.HashMap;
import java.util.Map;

public class SharableParticipants {
	
	private Map fMap= new HashMap();
	
	/* package */ void put(ParticipantDescriptor descriptor, RefactoringParticipant participant) {
		fMap.put(descriptor, participant);		
	}
	/* package */ RefactoringParticipant get(ParticipantDescriptor descriptor) {
		return (RefactoringParticipant)fMap.get(descriptor);
	}
}
