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
package io.micronaut.http.body;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.body.stream.BodySizeLimits;
import org.reactivestreams.Publisher;

import java.nio.ByteBuffer;

/**
 * Adapter from {@link Publisher} of NIO {@link ByteBuffer} to a {@link ReactiveByteBufferByteBody}.
 *
 * @since 4.8.0
 * @author Jonas Konrad
 */
@Internal
final class ByteBufferBodyAdapter extends AbstractBodyAdapter<ByteBuffer, ReactiveByteBufferByteBody.SharedBuffer> {
    private ByteBufferBodyAdapter(Publisher<ByteBuffer> source, @Nullable Runnable onDiscard) {
        super(source, onDiscard);
    }

    @NonNull
    public static ReactiveByteBufferByteBody adapt(Publisher<ByteBuffer> source) {
        return adapt(source, null, null);
    }

    @NonNull
    public static ReactiveByteBufferByteBody adapt(Publisher<ByteBuffer> publisher, @Nullable HttpHeaders headersForLength, @Nullable Runnable onDiscard) {
        ByteBufferBodyAdapter adapter = new ByteBufferBodyAdapter(publisher, onDiscard);
        adapter.sharedBuffer = new ReactiveByteBufferByteBody.SharedBuffer(BodySizeLimits.UNLIMITED, adapter);
        if (headersForLength != null) {
            adapter.sharedBuffer.setExpectedLengthFrom(headersForLength.get(HttpHeaders.CONTENT_LENGTH));
        }
        return new ReactiveByteBufferByteBody(adapter.sharedBuffer);
    }

    @Override
    public void onNext(ByteBuffer buffer) {
        long newDemand = demand.addAndGet(-buffer.remaining());
        sharedBuffer.add(buffer);
        if (newDemand > 0) {
            subscription.request(1);
        }
    }
}
