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
package org.apache.camel.spi;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * A resolver that can find resources based on package scanning.
 */
public interface PackageScanClassResolver {

    /**
     * Sets the ClassLoader instances that should be used when scanning for
     * classes. If none is set then the context classloader will be used.
     *
     * @param classLoaders loaders to use when scanning for classes
     */
    void setClassLoaders(Set<ClassLoader> classLoaders);

    /**
     * Gets the ClassLoader instances that should be used when scanning for classes.
     *
     * @return the class loaders to use
     */
    Set<ClassLoader> getClassLoaders();

    /**
     * Adds the class loader to the existing loaders
     *
     * @param classLoader the loader to add
     */
    void addClassLoader(ClassLoader classLoader);

    /**
     * Attempts to discover classes that are annotated with to the annotation.
     *
     * @param annotation   the annotation that should be present on matching classes
     * @param packageNames one or more package names to scan (including subpackages) for classes
     * @return the classes found, returns an empty set if none found
     */
    Set<Class<?>> findAnnotated(Class<? extends Annotation> annotation, String... packageNames);

    /**
     * Attempts to discover classes that are annotated with to the annotation.
     *
     * @param annotations   the annotations that should be present (any of them) on matching classes
     * @param packageNames one or more package names to scan (including subpackages) for classes
     * @return the classes found, returns an empty set if none found
     */
    Set<Class<?>> findAnnotated(Set<Class<? extends Annotation>> annotations, String... packageNames);

    /**
     * Attempts to discover classes that are assignable to the type provided. In
     * the case that an interface is provided this method will collect
     * implementations. In the case of a non-interface class, subclasses will be
     * collected.
     *
     * @param parent       the class of interface to find subclasses or implementations of
     * @param packageNames one or more package names to scan (including subpackages) for classes
     * @return the classes found, returns an empty set if none found
     */
    Set<Class<?>> findImplementations(Class<?> parent, String... packageNames);

    /**
     * Attemsp to discover classes filter by the provided filter
     *
     * @param fiter  filter to filter desired classes.
     * @param packageNames one or more package names to scan (including subpackages) for classes
     * @return the classes found, returns an empty set if none found
     */
    Set<Class<?>> findByFilter(PackageScanFilter fiter, String... packageNames);
    
    /**
     * Add a filter that will be applied to all scan operations
     * 
     * @param filter filter to filter desired classes in all scan operations
     */
    void addFilter(PackageScanFilter filter);
}
