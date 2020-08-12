package com.knoban.atlas.claims;

/**
 * EstatePermissions map with bitwise operations to a single short value. Each permission is represented as a bit
 * of this short (max 16 bits, 16 permissions). Keep in mind the last bit of a short represents the sign of it. So the
 * 16th permission will need to be controlled using a negative (-).
 *
 * A 1 in a bit position means the permission is granted.
 * A 0 in a bit position means the permission is missing.
 *
 * The short definitions below represent their respective bit positions. (1, 2, 4, 8, etc.). It is encouraged
 * to use hexadecimal for representing bits to keep things simple. Feel free to add up to 16 permissions to this
 * class.
 * @author Alden Bansemer (kNoAPP)
 */
public enum EstatePermission {

    FULL((short) -0x1), // 1111 1111 1111 1111
    ACCESS((short) 0x1),
    INVENTORY((short) 0x2),
    PLACE((short) 0x4),
    BREAK((short) 0x8),
    BUILD((short) 0xc),

    // CUSTOM (non-implemented permissions) START HERE:
    MANAGE((short) 0x10);

    private short code;

    EstatePermission(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
    }
}
