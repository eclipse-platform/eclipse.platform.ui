/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Helper class to serialize a project refactoring history.
 * <p>
 * The file format of a project refactoring history is as follows:
 * 
 * <pre>
 *   History ::= Descriptor*
 *   Descriptor ::= TimeStamp (long) ID (String) ArgCount (int) Arg* Description (String) StringCount (int) Project? Comment? Size (long)
 *   Arg ::= Key (String) Value (String)
 * </pre>
 * 
 * </p>
 * 
 * @since 3.2
 */
public final class ProjectRefactoringHistorySerializer {

	/**
	 * Reads a refactoring descriptor from disk.
	 * 
	 * @param input
	 *            the data input
	 * @return the refactoring descriptor
	 * @throws IOException
	 *             if an input/output error occurs
	 */
	public static RefactoringDescriptor readDescriptor(final DataInput input) throws IOException {
		Assert.isNotNull(input);
		final long stamp= input.readLong();
		final String id= input.readUTF();
		int count= input.readInt();
		final Map arguments= new HashMap(count);
		for (int index= 0; index < count; index++) {
			final String key= input.readUTF();
			final String value= input.readUTF();
			arguments.put(key, value);
		}
		final String description= input.readUTF();
		String project= null;
		String comment= null;
		count= input.readInt();
		if (count > 0) {
			project= input.readUTF();
			if (count > 1)
				comment= input.readUTF();
		}
		final RefactoringDescriptor descriptor= new RefactoringDescriptor(id, project, description, comment, arguments);
		descriptor.setTimeStamp(stamp);
		return descriptor;
	}

	/**
	 * Writes the refactoring descriptor to disk.
	 * 
	 * @param output
	 *            the data output
	 * @param descriptor
	 *            the refactoring descriptor
	 * @throws IOException
	 *             if an input/output error occurs
	 */
	public static void writeDescriptor(final DataOutput output, final RefactoringDescriptor descriptor) throws IOException {
		Assert.isNotNull(output);
		Assert.isNotNull(descriptor);
		output.writeLong(descriptor.getTimeStamp());
		output.writeUTF(descriptor.getID());
		final Set arguments= descriptor.getArguments().entrySet();
		output.writeInt(arguments.size());
		Map.Entry entry= null;
		for (final Iterator iterator= arguments.iterator(); iterator.hasNext();) {
			entry= (Entry) iterator.next();
			output.writeUTF(entry.getKey().toString());
			output.writeUTF(entry.getValue().toString());
		}
		output.writeUTF(descriptor.getDescription());
		int size= 0;
		final String project= descriptor.getProject();
		boolean writeProject= false;
		if (project != null && !"".equals(project)) { //$NON-NLS-1$
			writeProject= true;
			size++;
		}
		final String comment= descriptor.getComment();
		boolean writeComment= false;
		if (comment != null && !"".equals(comment)) { //$NON-NLS-1$
			writeComment= true;
			size++;
		}
		output.writeInt(size);
		if (writeProject)
			output.writeUTF(project);
		if (writeComment)
			output.writeUTF(comment);
		// Here comes Size
	}

	/**
	 * Creates a new project refactoring history serializer.
	 */
	private ProjectRefactoringHistorySerializer() {
		// Not for instantiation
	}
}