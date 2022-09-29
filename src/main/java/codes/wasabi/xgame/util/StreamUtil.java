package codes.wasabi.xgame.util;

import java.io.IOException;
import java.io.InputStream;

public final class StreamUtil {

    public static byte[] readNBytes(InputStream stream, int count) throws IOException {
        count = Math.max(count, 0);
        if (count == 0) return new byte[0];
        byte[] ret = new byte[count];
        int read = 0;
        while (read < count) {
            int ct = stream.read(ret, read, count - read);
            if (ct < 0) throw new IOException("Unexpected end of stream");
            read += ct;
        }
        return ret;
    }

    public static byte[] readAllBytes(InputStream stream) throws IOException {
        byte[] buffer = new byte[8192];
        byte[] out = new byte[0];
        int read;
        while ((read = stream.read(buffer)) >= 0) {
            if (read == 0) continue;
            int curLen = out.length;
            byte[] newOut = new byte[curLen + read];
            System.arraycopy(out, 0, newOut, 0, curLen);
            System.arraycopy(buffer, 0, newOut, curLen, read);
            out = newOut;
        }
        return out;
    }

}
