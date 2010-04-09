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
package org.apache.camel.example.gauth;

import java.net.URLEncoder;

import org.apache.camel.builder.RouteBuilder;

public class TutorialRouteBuilder extends RouteBuilder {

    private String application;

    public void setApplication(String application) {
        this.application = application;
    }

    @Override
    public void configure() throws Exception {

        String encodedCallback = URLEncoder.encode(String.format("https://%s.appspot.com/camel/handler", application), "UTF-8");
        String encodedScope = URLEncoder.encode("http://www.google.com/calendar/feeds/", "UTF-8");

        from("ghttp:///authorize")
            .to("gauth:authorize?callback=" + encodedCallback + "&scope=" + encodedScope);
        
        from("ghttp:///handler")
            .to("gauth:upgrade")
            .process(new TutorialTokenProcessor());
    }

}
