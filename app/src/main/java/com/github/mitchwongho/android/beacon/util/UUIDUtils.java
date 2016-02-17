package com.github.mitchwongho.android.beacon.util;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 *
 */
public class UUIDUtils {

    public static byte[] toBytes(@NonNull final UUID uuid) {
        final ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
