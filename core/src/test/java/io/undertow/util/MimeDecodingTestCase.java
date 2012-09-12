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

package io.undertow.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.undertow.test.utils.FileUtils;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Stuart Douglas
 */
public class MimeDecodingTestCase {

    @Test
    public void testSimpleMimeDecodingWithPreamble() throws Mime.MalformedMessageException {
        final String data =  fixLineEndings(FileUtils.readFile(MimeDecodingTestCase.class, "mime1.txt"));
        TestPartHandler handler = new TestPartHandler();
        Mime.ParseState parser = Mime.beginParse(handler, "unique-boundary-1".getBytes());

        ByteBuffer buf = ByteBuffer.wrap(data.getBytes());
        parser.parse(buf);
        Assert.assertTrue(parser.isComplete());
        Assert.assertEquals(2, handler.parts.size());
        Assert.assertEquals("Here is some text.", handler.parts.get(0).data.toString());
        Assert.assertEquals("Here is some more text.", handler.parts.get(1).data.toString());

        Assert.assertEquals("text/plain", handler.parts.get(0).map.get(Headers.CONTENT_TYPE).getFirst());
    }

    @Test
    public void testSimpleMimeDecodingWithoutPreamble() throws Mime.MalformedMessageException {
        final String data =  fixLineEndings(FileUtils.readFile(MimeDecodingTestCase.class, "mime2.txt"));
        TestPartHandler handler = new TestPartHandler();
        Mime.ParseState parser = Mime.beginParse(handler, "unique-boundary-1".getBytes());

        ByteBuffer buf = ByteBuffer.wrap(data.getBytes());
        parser.parse(buf);
        Assert.assertTrue(parser.isComplete());
        Assert.assertEquals(2, handler.parts.size());
        Assert.assertEquals("Here is some text.", handler.parts.get(0).data.toString());
        Assert.assertEquals("Here is some more text.", handler.parts.get(1).data.toString());

        Assert.assertEquals("text/plain", handler.parts.get(0).map.get(Headers.CONTENT_TYPE).getFirst());
    }

    private static class TestPartHandler implements Mime.PartHandler {

        private List<Part> parts = new ArrayList<Part>();
        private Part current;


        @Override
        public void beginPart(final HeaderMap headers) {
            current = new Part(headers);
            parts.add(current);
        }

        @Override
        public void data(final ByteBuffer buffer) {
            while (buffer.hasRemaining()) {
                current.data.append((char) buffer.get());
            }
        }

        @Override
        public void endPart() {

        }
    }

    private static class Part {
        private final HeaderMap map;
        private final StringBuilder data = new StringBuilder();

        private Part(final HeaderMap map) {
            this.map = map;
        }
    }

    private static String fixLineEndings(final String string) {
        final StringBuilder builder = new StringBuilder();
        for(int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if(c == '\n') {
                if(i == 0 || string.charAt(i-1) != '\r') {
                    builder.append("\r\n");
                } else {
                    builder.append('\n');
                }
            } else if(c == '\r') {
                if(i+1 == string.length() || string.charAt(i+1) != '\n') {
                    builder.append("\r\n");
                } else {
                    builder.append('\r');
                }
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
