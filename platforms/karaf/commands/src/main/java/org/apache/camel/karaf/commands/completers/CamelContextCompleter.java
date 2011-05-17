/**
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
package org.apache.camel.karaf.commands.completers;

import java.util.List;

import jline.console.completer.StringsCompleter;
import org.apache.camel.CamelContext;
import org.apache.camel.karaf.commands.CamelController;
import org.apache.karaf.shell.console.Completer;

/**
 * A JLine completer for the Camel contexts.
 */
public class CamelContextCompleter implements Completer {

    private CamelController camelController;

    public void setCamelController(CamelController camelController) {
        this.camelController = camelController;
    }

    @SuppressWarnings("unchecked")
    public int complete(String buffer, int cursor, List candidates) {
        try {
            StringsCompleter delegate = new StringsCompleter();
            List<CamelContext> camelContexts = camelController.getCamelContexts();
            for (CamelContext camelContext : camelContexts) {
                delegate.getStrings().add(camelContext.getName());
            }
            return delegate.complete(buffer, cursor, candidates);
        } catch (Exception e) {
            // nothing to do, no completion
        }
        return 0;
    }

}
