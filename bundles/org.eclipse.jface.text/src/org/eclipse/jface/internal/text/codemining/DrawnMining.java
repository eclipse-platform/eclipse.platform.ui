/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.internal.text.codemining;

import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.text.codemining.ICodeMining;

/**
 * Tuple holding the bounds and the drawn code mining
 */
record DrawnMining(Rectangle bounds, ICodeMining mining) {

}
