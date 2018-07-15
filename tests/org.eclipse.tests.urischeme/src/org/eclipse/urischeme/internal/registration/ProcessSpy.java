/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessSpy implements IProcessExecutor {

	List<Record> records = new ArrayList<>();
	String result;

	@Override
	public String execute(String process, String... args) throws IOException {
		records.add(new Record(process, args));
		return result;
	}

	class Record {
		String process;
		String[] args;

		public Record(String process, String[] args) {
			this.process = process;
			this.args = args;

		}
	}
}