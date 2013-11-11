/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common;

import java.util.UUID;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

public class MemoryTransfer extends ByteArrayTransfer {
	private static final String NAME = MemoryTransfer.class.getName() + ".tranfername"; //$NON-NLS-1$

	private static final int ID = registerType(NAME);

	private static MemoryTransfer INSTANCE;

	private String uid;

	private Object object;

	public static MemoryTransfer getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MemoryTransfer();
		}
		return INSTANCE;
	}

	@Override
	protected int[] getTypeIds() {
		return new int[] { ID };
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] { NAME };
	}

	@Override
	public void javaToNative(Object object, TransferData transferData) {
		this.uid = UUID.randomUUID().toString();
		this.object = object;
		if (transferData != null) {
			super.javaToNative(uid.getBytes(), transferData);
		}
	}

	@Override
	public Object nativeToJava(TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		if (bytes == null)
			return null;

		if (new String(bytes).equals(uid)) {
			return object;
		}
		return null;
	}
}