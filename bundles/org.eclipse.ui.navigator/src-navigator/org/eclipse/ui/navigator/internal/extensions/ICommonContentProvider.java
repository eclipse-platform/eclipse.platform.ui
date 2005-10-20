/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ui.navigator.internal.extensions;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.IMementoAware;

public interface ICommonContentProvider extends ITreeContentProvider, IMementoAware {

	void init(IExtensionStateModel aStateModel, IMemento aMemento);

}
