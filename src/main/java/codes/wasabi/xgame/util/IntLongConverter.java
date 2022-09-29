package codes.wasabi.xgame.util;

public final class IntLongConverter {

    public static long intToLong(int a, int b) {
        return (((long)a) << 32) | (b & 0xffffffffL);
    }

    public static int[] longToInt(long value) {
        int a = (int) (value >> 32);
        int b = (int) (value);
        return new int[]{ a, b };
    }

}
