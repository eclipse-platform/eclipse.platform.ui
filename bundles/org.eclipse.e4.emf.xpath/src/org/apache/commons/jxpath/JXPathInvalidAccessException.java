/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath;

/**
 * Similary to {@link java.lang.reflect.InvocationTargetException} in that
 * it is thrown when JXPath cannot access properties, collection etc on the
 * target object model.
 */
public class JXPathInvalidAccessException extends JXPathException {

    private static final long serialVersionUID = -8875537628056117241L;

    /**
     * Create a new JXPathInvalidAccessException.
     * @param message exception message
     */
    public JXPathInvalidAccessException(final String message) {
        super(message);
    }

    /**
     * Create a new JXPathInvalidAccessException.
     * @param message exception message
     * @param ex precipitating exception
     */
    public JXPathInvalidAccessException(final String message, final Throwable ex) {
        super(message, ex);
    }
}
