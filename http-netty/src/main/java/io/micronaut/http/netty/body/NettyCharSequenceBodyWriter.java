/*
 * Copyright 2017-2024 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.http.netty.body;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.MutableHeaders;
import io.micronaut.http.ByteBodyHttpResponse;
import io.micronaut.http.ByteBodyHttpResponseWrapper;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.body.ByteBodyFactory;
import io.micronaut.http.body.CharSequenceBodyWriter;
import io.micronaut.http.body.CloseableByteBody;
import io.micronaut.http.body.MessageBodyWriter;
import io.micronaut.http.body.ResponseBodyWriter;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.netty.NettyHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import jakarta.inject.Singleton;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A JSON body should not be escaped or parsed as a JSON value.
 *
 * @author Denis Stepanov
 * @since 4.6
 */
@Singleton
@Replaces(CharSequenceBodyWriter.class)
@Internal
public final class NettyCharSequenceBodyWriter implements ResponseBodyWriter<CharSequence> {
    private final CharSequenceBodyWriter defaultHandler = new CharSequenceBodyWriter(StandardCharsets.UTF_8);

    @Override
    public ByteBodyHttpResponse<?> write(@NonNull ByteBodyFactory bodyFactory, HttpRequest<?> request, MutableHttpResponse<CharSequence> outgoingResponse, Argument<CharSequence> type, MediaType mediaType, CharSequence object) throws CodecException {
        NettyHttpHeaders nettyHttpHeaders = (NettyHttpHeaders) outgoingResponse.getHeaders();
        if (!nettyHttpHeaders.contains(HttpHeaderNames.CONTENT_TYPE)) {
            nettyHttpHeaders.set(HttpHeaderNames.CONTENT_TYPE, mediaType);
        }
        return ByteBodyHttpResponseWrapper.wrap(outgoingResponse, writePiece(bodyFactory, mediaType, object, nettyHttpHeaders));
    }

    @Override
    public CloseableByteBody writePiece(@NonNull ByteBodyFactory bodyFactory, @NonNull HttpRequest<?> request, @NonNull HttpResponse<?> response, @NonNull Argument<CharSequence> type, @NonNull MediaType mediaType, CharSequence object) {
        return writePiece(bodyFactory, mediaType, object, response.getHeaders());
    }

    private static @NonNull CloseableByteBody writePiece(@NonNull ByteBodyFactory bodyFactory, @NonNull MediaType mediaType, CharSequence object, @NonNull HttpHeaders headers) {
        Charset charset = MessageBodyWriter.getCharset(mediaType, headers);
        return bodyFactory.copyOf(object, charset);
    }

    @Override
    public void writeTo(Argument<CharSequence> type, MediaType mediaType, CharSequence object, MutableHeaders outgoingHeaders, OutputStream outputStream) throws CodecException {
        defaultHandler.writeTo(type, mediaType, object, outgoingHeaders, outputStream);
    }

}
