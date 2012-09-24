/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.undertow.servlet.handlers;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that maintains the complete set of servlet path matches
 *
 * @author Stuart Douglas
 */
public class ServletPathMatches {

    private final Map<String, ServletInitialHandler> exactPathMatches;

    private final Map<String, PathMatch> prefixMatches;

    private final Map<String, ServletInitialHandler> nameMatches;

    private final ServletInitialHandler defaultServlet;

    public ServletPathMatches(final Map<String, ServletInitialHandler> exactPathMatches, final Map<String, PathMatch> prefixMatches, final Map<String, ServletInitialHandler> nameMatches, final ServletInitialHandler defaultServlet) {
        this.exactPathMatches = exactPathMatches;
        this.prefixMatches = prefixMatches;
        this.nameMatches = nameMatches;
        this.defaultServlet = defaultServlet;
    }

    public ServletInitialHandler getServletHandlerByName(final String name) {
        return nameMatches.get(name);
    }

    public ServletInitialHandler getServletHandlerByPath(final String path) {
        ServletInitialHandler exact = exactPathMatches.get(path);
        if (exact != null) {
            return exact;
        }
        PathMatch match = prefixMatches.get(path);
        if (match != null) {
            return  handleMatch(path, match);
        }
        for (int i = path.length() -1; i >= 0; --i) {
            if (path.charAt(i) == '/') {
                final String part = path.substring(0, i);
                match = prefixMatches.get(part);
                if (match != null) {
                    return  handleMatch(path, match);
                }
            }
        }
        return defaultServlet;
    }

    private ServletInitialHandler handleMatch(final String path, final PathMatch match) {
        if (match.extensionMatches.isEmpty()) {
            return match.defaultHandler;
        } else {
            int c = path.lastIndexOf('.');
            if (c == -1) {
                return match.defaultHandler;
            } else {
                final String ext = path.substring(c + 1, path.length());
                ServletInitialHandler handler = match.extensionMatches.get(ext);
                if (handler != null) {
                    return handler;
                } else {
                    return match.defaultHandler;
                }
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final Map<String, ServletInitialHandler> exactPathMatches = new HashMap<String, ServletInitialHandler>();

        private final Map<String, PathMatch> prefixMatches = new HashMap<String, PathMatch>();

        private final Map<String, ServletInitialHandler> nameMatches = new HashMap<String, ServletInitialHandler>();

        private ServletInitialHandler defaultServlet;

        public void addExactMatch(final String exactMatch, final ServletInitialHandler match) {
            exactPathMatches.put(exactMatch, match);
        }

        public void addPrefixMatch(final String prefix, final ServletInitialHandler match) {
            PathMatch m = prefixMatches.get(prefix);
            if(m == null) {
                prefixMatches.put(prefix, m = new PathMatch(match));
            }
            m.defaultHandler = match;
        }

        public void addExtensionMatch(final String prefix, final String extension, final ServletInitialHandler match) {
            PathMatch m = prefixMatches.get(prefix);
            if(m == null) {
                prefixMatches.put(prefix, m = new PathMatch(null));
            }
            m.extensionMatches.put(extension, match);
        }

        public void addNameMatch(final String name, final ServletInitialHandler match) {
            nameMatches.put(name, match);
        }

        public ServletInitialHandler getDefaultServlet() {
            return defaultServlet;
        }

        public void setDefaultServlet(final ServletInitialHandler defaultServlet) {
            this.defaultServlet = defaultServlet;
        }

        public ServletPathMatches build() {
            return new ServletPathMatches(exactPathMatches, prefixMatches, nameMatches, defaultServlet);
        }

    }


    private static class PathMatch {

        private final Map<String, ServletInitialHandler> extensionMatches = new HashMap<String, ServletInitialHandler>();
        private volatile ServletInitialHandler defaultHandler;

        public PathMatch(final ServletInitialHandler defaultHandler) {
            this.defaultHandler = defaultHandler;
        }

    }
}