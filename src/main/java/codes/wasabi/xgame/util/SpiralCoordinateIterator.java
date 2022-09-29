package codes.wasabi.xgame.util;

import java.util.*;

public class SpiralCoordinateIterator implements Iterator<Long> {

    // Making this table hurt my brain
    private static final Map<Integer, int[]> cache;
    static {
        Map<Integer, int[]> map = new HashMap<>();
        map.put(0, new int[]{ 1, 0 });
        map.put(8, new int[]{ 0, 1 });
        map.put(3, new int[]{ -1, 0 });
        map.put(22, new int[]{ -1, 0 });
        map.put(23, new int[]{ -1, 0 });
        map.put(20, new int[]{ 0, -1 });
        map.put(208, new int[]{ 0, -1 });
        map.put(192, new int[]{ 1, 0 });
        map.put(232, new int[]{ 1, 0 });
        map.put(104, new int[]{ 1, 0 });
        map.put(40, new int[]{ 0, 1 });
        map.put(43, new int[]{ 0, 1 });
        map.put(11, new int[]{ 0, 1 });
        map.put(212, new int[]{ 0, -1 });
        cache = Collections.unmodifiableMap(map);
    }

    private final List<Long> traversed = new ArrayList<>();
    private int curX = 0;
    private int curY = 0;

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Long next() {
        long ret = IntLongConverter.intToLong(curX, curY);
        int token = (traversed.contains(IntLongConverter.intToLong(curX - 1, curY - 1)) ? 1 : 0)
                | (traversed.contains(IntLongConverter.intToLong(curX, curY - 1)) ? 2 : 0)
                | (traversed.contains(IntLongConverter.intToLong(curX + 1, curY - 1)) ? 4 : 0)
                | (traversed.contains(IntLongConverter.intToLong(curX - 1, curY)) ? 8 : 0)
                | (traversed.contains(IntLongConverter.intToLong(curX + 1, curY)) ? 16 : 0)
                | (traversed.contains(IntLongConverter.intToLong(curX - 1, curY + 1)) ? 32 : 0)
                | (traversed.contains(IntLongConverter.intToLong(curX, curY + 1)) ? 64 : 0)
                | (traversed.contains(IntLongConverter.intToLong(curX + 1, curY + 1)) ? 128 : 0);
        int[] move = cache.get(token);
        if (move == null) throw new IllegalStateException("No case found for token \"" + token + "\" at X=" + curX + ", Y=" + curY);
        curX += move[0];
        curY += move[1];
        traversed.add(ret);
        int needed = Math.max((Math.max(Math.abs(curX), Math.abs(curY)) + 1) * 8 - 4, 8);
        int size = traversed.size();
        int diff = size - needed;
        if (diff > 0) {
            traversed.subList(0, diff).clear();
        }
        return ret;
    }

    public long nextLong() {
        return next();
    }

    public int[] nextCoordinate() {
        final int retX = curX;
        final int retY = curY;
        next();
        return new int[]{ retX, retY };
    }

}
