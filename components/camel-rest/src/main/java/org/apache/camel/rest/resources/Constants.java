/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.rest.resources;

import javax.ws.rs.core.MediaType;

/**
 * @version $Revision: 1.1 $
 */
public class Constants {
    public static final String[] HTML_VIEW = {
            MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML,
            MediaType.WILDCARD, MediaType.MEDIA_TYPE_WILDCARD,
            MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_PLAIN};

    public static final String[] DATA_VIEW = {MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON};

}
