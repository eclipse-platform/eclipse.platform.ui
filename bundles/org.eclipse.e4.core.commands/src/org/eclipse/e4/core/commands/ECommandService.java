/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.commands;

import java.util.Map;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.ParameterizedCommand;

/**
 * @noimplement
 */
public interface ECommandService {
	public ParameterizedCommand createCommand(String id, Map<String, Object> parameters);

	/**
	 * @param id
	 * @param name
	 * @param description
	 * @return
	 * @noreference
	 */
	public Category defineCategory(String id, String name, String description);

	/**
	 * @param id
	 * @param name
	 * @param description
	 * @param category
	 * @param parameters
	 * @return
	 * @noreference
	 */
	public Command defineCommand(String id, String name, String description, Category category,
			IParameter[] parameters);

	public Category getCategory(String categoryId);

	public Command getCommand(String commandId);
}
