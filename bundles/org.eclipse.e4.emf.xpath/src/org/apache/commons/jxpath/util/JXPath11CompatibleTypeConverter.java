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
package org.apache.commons.jxpath.util;

import org.apache.commons.jxpath.NodeSet;

/**
 * TypeConverter implementation to circumvent automagic {@link NodeSet}
 * decoding. Suggested by JIRA issue JXPATH-10.
 *
 * @since JXPath 1.3
 */
public class JXPath11CompatibleTypeConverter extends BasicTypeConverter {

    @Override
    public boolean canConvert(final Object object, final Class toType) {
        return object instanceof NodeSet ? toType.isInstance(object) : super.canConvert(object, toType);
    }

    @Override
    public Object convert(final Object object, final Class toType) {
        return object instanceof NodeSet && toType.isInstance(object) ? object : super.convert(object, toType);
    }
}
