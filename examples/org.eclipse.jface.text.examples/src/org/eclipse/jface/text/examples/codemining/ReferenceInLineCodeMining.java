/*******************************************************************************
 *  Copyright (c) 2025, Advantest Europe GmbH
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *  
 *  Contributors:
 *  Dietrich Travkin <dietrich.travkin@solunar.de> - Fix code mining redrawing - Issue 3405
 *  
 *******************************************************************************/
package org.eclipse.jface.text.examples.codemining;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineContentCodeMining;

public class ReferenceInLineCodeMining extends LineContentCodeMining {

	public ReferenceInLineCodeMining(String label, int positionOffset, IDocument document,
			ICodeMiningProvider provider) {
		super(new Position(positionOffset, 1), provider);
		this.setLabel(label);
	}

}
