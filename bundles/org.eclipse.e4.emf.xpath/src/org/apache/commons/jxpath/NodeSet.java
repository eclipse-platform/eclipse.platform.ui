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
package org.apache.commons.jxpath;

import java.util.List;

/**
 * NodeSet interface can be used as the type of an argument of an extension
 * function.  Alternatively, the function can declare the argument as
 * a Collection (or List or Set), in which case it will be given a collection
 * of <em>values</em> matching the path.
 */
public interface NodeSet {

    /**
     * Returns a list of nodes.
     * @return List
     */
    List getNodes();

    /**
     * Returns a list of pointers for all nodes in the set.
     * @return List
     */
    List getPointers();

    /**
     * Returns a list of values of all contained pointers.
     * @return List
     */
    List getValues();

}
