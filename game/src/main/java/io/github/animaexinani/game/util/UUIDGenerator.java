package io.github.animaexinani.game.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class UUIDGenerator {
    private UUIDGenerator() {
    }

    /**
     * Generates a version 7 UUID.
     * 
     * @return the generated UUID
     * 
     * @implNote This implementation is taken from io.github.robsonkades:uuidv7,
     *           which is licensed under the MIT license. The reason the
     *           implementation is copied rather than imported is due to
     *           io.github.robsonkades:uuidv7 not having an importable module, which
     *           is required for libraries to be usable in this project.
     */
    public UUID generateV7Uuid() {
        var random = ThreadLocalRandom.current();

        long currentTimeMillis = System.currentTimeMillis();
        long maskedTimestamp = currentTimeMillis & 0xFFFFFFFFFFFFL;

        long random64 = random.nextLong();
        int random32 = random.nextInt();

        long highBits = (maskedTimestamp << 16);
        long randomHighBits = (random64 >> 52) & 0x0FFFL;
        highBits |= randomHighBits;
        highBits |= 0x0000000000007000L; // Set the version number to 7;

        long lowBits = 0x8000000000000000L; // Set the variant to 2;
        long randomLowBits64 = random64 & 0x000FFFFFFFFFFFFFL;
        int randomLowBits32 = (random32 >>> 22) & 0x3FF;
        lowBits |= (randomLowBits64 << 10);
        lowBits |= randomLowBits32;

        return new UUID(highBits, lowBits);
    }
}
