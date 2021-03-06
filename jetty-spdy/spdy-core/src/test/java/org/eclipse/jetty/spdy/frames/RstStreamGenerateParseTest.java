/*
 * Copyright (c) 2012 the original author or authors.
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

package org.eclipse.jetty.spdy.frames;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;

import org.eclipse.jetty.spdy.StandardByteBufferPool;
import org.eclipse.jetty.spdy.StandardCompressionFactory;
import org.eclipse.jetty.spdy.api.SPDY;
import org.eclipse.jetty.spdy.api.StreamStatus;
import org.eclipse.jetty.spdy.generator.Generator;
import org.eclipse.jetty.spdy.parser.Parser;
import org.junit.Assert;
import org.junit.Test;

public class RstStreamGenerateParseTest
{
    @Test
    public void testGenerateParse() throws Exception
    {
        int streamId = 13;
        int streamStatus = StreamStatus.UNSUPPORTED_VERSION.getCode(SPDY.V2);
        RstStreamFrame frame1 = new RstStreamFrame(SPDY.V2, streamId, streamStatus);
        Generator generator = new Generator(new StandardByteBufferPool(), new StandardCompressionFactory().newCompressor());
        ByteBuffer buffer = generator.control(frame1);

        assertThat("buffer is not null", buffer, not(nullValue()));

        TestSPDYParserListener listener = new TestSPDYParserListener();
        Parser parser = new Parser(new StandardCompressionFactory().newDecompressor());
        parser.addListener(listener);
        parser.parse(buffer);
        ControlFrame frame2 = listener.getControlFrame();

        assertThat("frame2 is not null", frame2, not(nullValue()));
        assertThat("frame2 is type RST_STREAM",ControlFrameType.RST_STREAM, equalTo(frame2.getType()));
        RstStreamFrame rstStream = (RstStreamFrame)frame2;
        assertThat("rstStream version is SPDY.V2",SPDY.V2, equalTo(rstStream.getVersion()));
        assertThat("rstStream id is equal to streamId",streamId, equalTo(rstStream.getStreamId()));
        assertThat("rstStream flags are 0",(byte)0, equalTo(rstStream.getFlags()));
        assertThat("stream status is equal to rstStream statuscode",streamStatus, is(rstStream.getStatusCode()));
    }

    @Test
    public void testGenerateParseOneByteAtATime() throws Exception
    {
        int streamId = 13;
        int streamStatus = StreamStatus.UNSUPPORTED_VERSION.getCode(SPDY.V2);
        RstStreamFrame frame1 = new RstStreamFrame(SPDY.V2, streamId, streamStatus);
        Generator generator = new Generator(new StandardByteBufferPool(), new StandardCompressionFactory().newCompressor());
        ByteBuffer buffer = generator.control(frame1);

        Assert.assertNotNull(buffer);

        TestSPDYParserListener listener = new TestSPDYParserListener();
        Parser parser = new Parser(new StandardCompressionFactory().newDecompressor());
        parser.addListener(listener);
        while (buffer.hasRemaining())
            parser.parse(ByteBuffer.wrap(new byte[]{buffer.get()}));
        ControlFrame frame2 = listener.getControlFrame();

        Assert.assertNotNull(frame2);
        Assert.assertEquals(ControlFrameType.RST_STREAM, frame2.getType());
        RstStreamFrame rstStream = (RstStreamFrame)frame2;
        Assert.assertEquals(SPDY.V2, rstStream.getVersion());
        Assert.assertEquals(streamId, rstStream.getStreamId());
        Assert.assertEquals(0, rstStream.getFlags());
        Assert.assertEquals(streamStatus, rstStream.getStatusCode());
    }
}
