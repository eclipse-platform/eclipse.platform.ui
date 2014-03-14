/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.cssbridge.core;

import java.util.List;

import org.eclipse.e4.demo.cssbridge.model.FolderType;
import org.eclipse.e4.demo.cssbridge.model.Mail;

public interface IMailService {
	String getMailboxName();

	List<Mail> getMails(FolderType type);
}
