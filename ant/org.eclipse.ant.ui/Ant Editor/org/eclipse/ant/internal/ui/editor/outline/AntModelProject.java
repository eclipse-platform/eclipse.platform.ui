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

/*
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import org.apache.tools.ant.Project;

/**
 * Derived from the original Ant Project class
 * This class allows property values to be written multiple times.
 * This facilitates incremental parsing of the Ant build file
 */
public class AntModelProject extends Project {
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#setNewProperty(java.lang.String, java.lang.String)
	 */
	public void setNewProperty(String name, String value) {
		// always allow property values to be over-written
		super.setProperty(name, value);
	}
}