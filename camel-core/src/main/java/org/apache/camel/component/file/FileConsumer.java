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
package org.apache.camel.component.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.util.FileUtil;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.StringHelper;

/**
 * File consumer.
 */
public class FileConsumer extends GenericFileConsumer<File> {

    private String endpointPath;
    private Set<String> extendedAttributes;

    public FileConsumer(FileEndpoint endpoint, Processor processor, GenericFileOperations<File> operations, GenericFileProcessStrategy<File> processStrategy) {
        super(endpoint, processor, operations, processStrategy);
        this.endpointPath = endpoint.getConfiguration().getDirectory();

        if (endpoint.getExtendedAttributes() != null) {
            this.extendedAttributes = new HashSet<>();

            for (String attribute : endpoint.getExtendedAttributes().split(",")) {
                extendedAttributes.add(attribute);
            }
        }
    }

    @Override
    protected boolean pollDirectory(String fileName, List<GenericFile<File>> fileList, final int depth) {
        log.trace("pollDirectory from fileName: {}", fileName);

        File directory = new File(fileName);
        if (!directory.exists() || !directory.isDirectory()) {
            log.debug("Cannot poll as directory does not exists or its not a directory: {}", directory);
            if (getEndpoint().isDirectoryMustExist()) {
                throw new GenericFileOperationFailedException("Directory does not exist: " + directory);
            }
            return true;
        }

        log.trace("Polling directory: {}", directory.getPath());

        try (Stream<Path> dirStream = list(directory.toPath())) {

            if (isDirectoryEmpty(directory.toPath())) {
                // no files in this directory to poll
                if (log.isTraceEnabled()) {
                    log.trace("No files found in directory: {}", directory.getPath());
                }
                return true;
            } else {
                // we found some files
                if (log.isTraceEnabled()) {
                    log.trace("Found files in directory: {}", directory.getPath());
                }
            }

            final CanPollMore canPollMore = new CanPollMore();
            if (!canPollMore.check(canPollMoreFiles(fileList))) {
                return false;
            }

            dirStream.forEach(file -> {
                // check if we can continue polling in files
                if (canPollMore.check(canPollMoreFiles(fileList))) {

                    // trace log as Windows/Unix can have different views what the file is?
                    if (log.isTraceEnabled()) {
                        log.trace("Found file: {} [isAbsolute: {}, isDirectory: {}, isFile: {}, isHidden: {}]",
                                new Object[]{file, file.isAbsolute(), Files.isDirectory(file), Files.isRegularFile(file), file.toFile().isHidden()});
                    }

                    // creates a generic file
                    GenericFile<File> gf = asGenericFile(endpointPath, file.toFile(), getEndpoint().getCharset(), getEndpoint().isProbeContentType());

                    if (Files.isDirectory(file)) {
                        if (endpoint.isRecursive() && depth + 1 < endpoint.getMaxDepth() && isValidFile(gf, true, Collections.emptyList())) {
                            // recursive scan and add the sub files and folders
                            String subDirectory = fileName + File.separator + file.getFileName();
                            canPollMore.check(pollDirectory(subDirectory, fileList, depth + 1));
                        }
                    } else {
                        // Windows can report false to a file on a share so regard it always as a file (if its not a directory)
                        if (depth + 1 >= endpoint.minDepth && isValidFile(gf, false, Collections.emptyList())) {
                            log.trace("Adding valid file: {}", file);
                            // matched file so add
                            if (extendedAttributes != null) {
                                Path path = file;
                                Map<String, Object> allAttributes = new HashMap<>();
                                for (String attribute : extendedAttributes) {
                                    try {
                                        String prefix = null;
                                        if (attribute.endsWith(":*")) {
                                            prefix = attribute.substring(0, attribute.length() - 1);
                                        } else if (attribute.equals("*")) {
                                            prefix = "basic:";
                                        }

                                        if (ObjectHelper.isNotEmpty(prefix)) {
                                            Map<String, Object> attributes = Files.readAttributes(path, attribute);
                                            if (attributes != null) {
                                                for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                                                    allAttributes.put(prefix + entry.getKey(), entry.getValue());
                                                }
                                            }
                                        } else if (!attribute.contains(":")) {
                                            allAttributes.put("basic:" + attribute, Files.getAttribute(path, attribute));
                                        } else {
                                            allAttributes.put(attribute, Files.getAttribute(path, attribute));
                                        }
                                    } catch (IOException e) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("Unable to read attribute {} on file {}", attribute, file, e);
                                        }
                                    }
                                }

                                gf.setExtendedAttributes(allAttributes);
                            }

                            fileList.add(gf);
                        }
                    }
                }
            });
            return canPollMore.value;
        } catch (IOException ex) {
            throw new GenericFileOperationFailedException("IOException while listing files in: " + directory, ex);
        }
    }

    @Override
    protected boolean isMatched(GenericFile<File> file, String doneFileName, List<File> files) {
        String onlyName = FileUtil.stripPath(doneFileName);
        Path parentDirectory = file.getFile().toPath().getParent();
        Path doneFile = Paths.get(parentDirectory.toString(), onlyName);
        // the done file name must be among the files
        if (Files.exists(doneFile)) {
            return true;
        } else {
            log.trace("Done file: {} does not exist", doneFileName);
            return false;
        }
    }

    private Stream<Path> list(Path directory) throws IOException {
        return getEndpoint().isPreSort()
                ? Files.list(directory).sorted(Comparator.comparing(Path::toAbsolutePath))
                : Files.list(directory);
    }

    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (Stream<Path> entries = Files.list(directory)) {
            return !entries.findFirst().isPresent();
        }
    }

    /**
     * Creates a new GenericFile<File> based on the given file.
     *
     * @param endpointPath the starting directory the endpoint was configured with
     * @param file the source file
     * @return wrapped as a GenericFile
     * @deprecated use {@link #asGenericFile(String, File, String, boolean)}
     */
    @Deprecated
    public static GenericFile<File> asGenericFile(String endpointPath, File file, String charset) {
        return asGenericFile(endpointPath, file, charset, false);
    }

    /**
     * Creates a new GenericFile<File> based on the given file.
     *
     * @param endpointPath the starting directory the endpoint was configured with
     * @param file the source file
     * @param probeContentType whether to probe the content type of the file or not
     * @return wrapped as a GenericFile
     */
    public static GenericFile<File> asGenericFile(String endpointPath, File file, String charset, boolean probeContentType) {
        GenericFile<File> answer = new GenericFile<>(probeContentType);
        // use file specific binding
        answer.setBinding(new FileBinding());

        answer.setCharset(charset);
        answer.setEndpointPath(endpointPath);
        answer.setFile(file);
        answer.setFileNameOnly(file.getName());
        answer.setDirectory(Files.isDirectory(file.toPath()));
        // must use FileUtil.isAbsolute to have consistent check for whether the file is
        // absolute or not. As windows do not consider \ paths as absolute where as all
        // other OS platforms will consider \ as absolute. The logic in Camel mandates
        // that we align this for all OS. That is why we must use FileUtil.isAbsolute
        // to return a consistent answer for all OS platforms.
        answer.setAbsolute(FileUtil.isAbsolute(file));
        answer.setAbsoluteFilePath(file.getAbsolutePath());

        // file length and last modified are loaded lazily
        answer.setFileLengthSupplier(file::length);
        answer.setLastModifiedSupplier(file::lastModified);

        // compute the file path as relative to the starting directory
        File path;
        String endpointNormalized = FileUtil.normalizePath(endpointPath);
        if (file.getPath().startsWith(endpointNormalized + File.separator)) {
            // skip duplicate endpoint path
            path = new File(StringHelper.after(file.getPath(), endpointNormalized + File.separator));
        } else {
            path = new File(file.getPath());
        }

        if (path.getParent() != null) {
            answer.setRelativeFilePath(path.getParent() + File.separator + file.getName());
        } else {
            answer.setRelativeFilePath(path.getName());
        }

        // the file name should be the relative path
        answer.setFileName(answer.getRelativeFilePath());

        // use file as body as we have converters if needed as stream
        answer.setBody(file);
        return answer;
    }

    @Override
    protected void updateFileHeaders(GenericFile<File> file, Message message) {
        File upToDateFile = file.getFile();
        if (fileHasMoved(file)) {
            upToDateFile = new File(file.getAbsoluteFilePath());
        }
        long length = upToDateFile.length();
        long modified = upToDateFile.lastModified();
        file.setFileLength(length);
        file.setLastModified(modified);
        if (length >= 0) {
            message.setHeader(Exchange.FILE_LENGTH, length);
        }
        if (modified >= 0) {
            message.setHeader(Exchange.FILE_LAST_MODIFIED, modified);
        }
    }

    @Override
    public FileEndpoint getEndpoint() {
        return (FileEndpoint) super.getEndpoint();
    }

    private boolean fileHasMoved(GenericFile<File> file) {
        // GenericFile's absolute path is always up to date whereas the underlying file is not
        return !file.getFile().getAbsolutePath().equals(file.getAbsoluteFilePath());
    }

    private static class CanPollMore {

        public boolean value = true;

        public boolean check(boolean value) {
            this.value = this.value && value;
            return this.value;
        }

    }

}
