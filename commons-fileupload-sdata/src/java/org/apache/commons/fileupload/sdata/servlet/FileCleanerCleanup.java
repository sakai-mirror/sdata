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
package org.apache.commons.fileupload.sdata.servlet;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

import org.apache.commons.io.FileCleaner;


/**
 * A servlet context listener, which ensures that the
 * {@link org.apache.commons.io.FileCleaner FileCleaner's}
 * reaper thread is terminated,
 * when the web application is destroyed.
 */
public class FileCleanerCleanup implements ServletContextListener {
    /**
     * Called when the web application is initialized. Does
     * nothing.
     * @param sce The servlet context (ignored).
     */
    public void contextInitialized(ServletContextEvent sce) {
        // Does nothing.
    }

    /**
     * Called when the web application is being destroyed.
     * Calls {@link FileCleaner#exitWhenFinished()}.
     * @param sce The servlet context (ignored).
     */
    public void contextDestroyed(ServletContextEvent sce) {
        FileCleaner.exitWhenFinished();
    }
}
