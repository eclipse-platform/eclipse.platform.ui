/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - adjustment to EObject
 ******************************************************************************/
package org.eclipse.e4.emf.internal.xpath;

import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * Combines child node iterators of all elements of a collection into one
 * aggregate child node iterator.
 *
 */
public class CollectionChildNodeIterator extends CollectionNodeIterator {

	private NodeTest test;

	/**
	 * Create a new CollectionChildNodeIterator.
	 * @param pointer CollectionPointer
	 * @param test child test
	 * @param reverse iteration order
	 * @param startWith starting pointer
	 */
	public CollectionChildNodeIterator(
		CollectionPointer pointer,
		NodeTest test,
		boolean reverse,
		NodePointer startWith) {
		super(pointer, reverse, startWith);
		this.test = test;
	}

	@Override
	protected NodeIterator getElementNodeIterator(NodePointer elementPointer) {
		return elementPointer.childIterator(test, false, null);
	}
}
