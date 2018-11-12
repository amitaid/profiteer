package com.xen.profiteer.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

/**
 * Vert.x core example for {@link io.vertx.core.eventbus.EventBus} and {@link MessageCodec}
 * Adapted to everyday use
 */
public class JsonCodec<T> implements MessageCodec<T, T> {

    private Class<T> clazz;

    public JsonCodec(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void encodeToWire(Buffer buffer, T customMessage) {
        String json = Json.encode(customMessage);
        int length = json.getBytes().length;

        buffer.appendInt(length);
        buffer.appendString(json);
    }

    @Override
    public T decodeFromWire(int position, Buffer buffer) {
        int _pos = position;
        int length = buffer.getInt(_pos);
        String jsonStr = buffer.getString(_pos += 4, _pos += length);

        return Json.decodeValue(jsonStr, clazz);
    }

    @Override
    public T transform(T customMessage) {
        return customMessage;
    }

    @Override
    public String name() {
        return this.clazz.getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        // Always -1
        return -1;
    }
}