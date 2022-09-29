package codes.wasabi.xgame.persistent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import static codes.wasabi.xgame.util.StreamUtil.readNBytes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class DataType<T> {

    private static final Map<Byte, DataType<?>> CODE_MAP = new HashMap<>();
    private static final Map<Class<?>, DataType<?>> CLASS_MAP = new HashMap<>();

    public static DataType<Byte> BYTE = new DataType<Byte>() {
        @Override
        public Class<Byte> getType() {
            return Byte.class;
        }

        @Override
        public String getName() {
            return "BYTE";
        }

        @Override
        public Byte readData(InputStream is) throws IOException {
            return (byte) is.read();
        }

        @Override
        public void writeData(OutputStream os, Byte data) throws IOException {
            os.write(data);
        }

        @Override
        public byte getCode() {
            return (byte) 0;
        }
    };

    public static DataType<Short> SHORT = new DataType<Short>() {
        @Override
        public Class<Short> getType() {
            return Short.class;
        }

        @Override
        public String getName() {
            return "SHORT";
        }

        @Override
        public Short readData(InputStream is) throws IOException {
            return ByteBuffer.wrap(readNBytes(is, Short.BYTES)).getShort();
        }

        @Override
        public void writeData(OutputStream os, Short data) throws IOException {
            os.write(ByteBuffer.allocate(Short.BYTES).putShort(data).array());
        }

        @Override
        public byte getCode() {
            return (byte) 1;
        }
    };

    public static DataType<Integer> INTEGER = new DataType<Integer>() {
        @Override
        public Class<Integer> getType() {
            return Integer.class;
        }

        @Override
        public String getName() {
            return "INTEGER";
        }

        @Override
        public Integer readData(InputStream is) throws IOException {
            return ByteBuffer.wrap(readNBytes(is, Integer.BYTES)).getInt();
        }

        @Override
        public void writeData(OutputStream os, Integer data) throws IOException {
            os.write(ByteBuffer.allocate(Integer.BYTES).putInt(data).array());
        }

        @Override
        public byte getCode() {
            return (byte) 2;
        }
    };

    public static DataType<Long> LONG = new DataType<Long>() {
        @Override
        public Class<Long> getType() {
            return Long.class;
        }

        @Override
        public String getName() {
            return "LONG";
        }

        @Override
        public Long readData(InputStream is) throws IOException {
            return ByteBuffer.wrap(readNBytes(is, Long.BYTES)).getLong();
        }

        @Override
        public void writeData(OutputStream os, Long data) throws IOException {
            os.write(ByteBuffer.allocate(Long.BYTES).putLong(data).array());
        }

        @Override
        public byte getCode() {
            return (byte) 3;
        }
    };

    public static DataType<Float> FLOAT = new DataType<Float>() {
        @Override
        public Class<Float> getType() {
            return Float.class;
        }

        @Override
        public String getName() {
            return "FLOAT";
        }

        @Override
        public Float readData(InputStream is) throws IOException {
            return ByteBuffer.wrap(readNBytes(is, Float.BYTES)).getFloat();
        }

        @Override
        public void writeData(OutputStream os, Float data) throws IOException {
            os.write(ByteBuffer.allocate(Float.BYTES).putFloat(data).array());
        }

        @Override
        public byte getCode() {
            return (byte) 4;
        }
    };

    public static DataType<Double> DOUBLE = new DataType<Double>() {
        @Override
        public Class<Double> getType() {
            return Double.class;
        }

        @Override
        public String getName() {
            return "DOUBLE";
        }

        @Override
        public Double readData(InputStream is) throws IOException {
            return ByteBuffer.wrap(readNBytes(is, Double.BYTES)).getDouble();
        }

        @Override
        public void writeData(OutputStream os, Double data) throws IOException {
            os.write(ByteBuffer.allocate(Double.BYTES).putDouble(data).array());
        }

        @Override
        public byte getCode() {
            return (byte) 5;
        }
    };

    public static DataType<String> STRING = new DataType<String>() {
        @Override
        public Class<String> getType() {
            return String.class;
        }

        @Override
        public String getName() {
            return "STRING";
        }

        @Override
        public String readData(InputStream is) throws IOException {
            int len = ByteBuffer.wrap(readNBytes(is, Integer.BYTES)).getInt();
            byte[] bytes = readNBytes(is, len);
            return new String(bytes, StandardCharsets.UTF_8);
        }

        @Override
        public void writeData(OutputStream os, String data) throws IOException {
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            os.write(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
            os.write(bytes);
        }

        @Override
        public byte getCode() {
            return (byte) 6;
        }
    };

    public static DataType<byte[]> BYTE_ARRAY = new DataType<byte[]>() {
        @Override
        public Class<byte[]> getType() {
            return byte[].class;
        }

        @Override
        public String getName() {
            return "BYTE_ARRAY";
        }

        @Override
        public byte[] readData(InputStream is) throws IOException {
            int len = ByteBuffer.wrap(readNBytes(is, Integer.BYTES)).getInt();
            return readNBytes(is, len);
        }

        @Override
        public void writeData(OutputStream os, byte[] data) throws IOException {
            os.write(ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array());
            os.write(data);
        }

        @Override
        public byte getCode() {
            return (byte) 7;
        }
    };

    public static DataType<int[]> INTEGER_ARRAY = new DataType<int[]>() {
        @Override
        public Class<int[]> getType() {
            return int[].class;
        }

        @Override
        public String getName() {
            return "INTEGER_ARRAY";
        }

        @Override
        public int[] readData(InputStream is) throws IOException {
            int len = ByteBuffer.wrap(readNBytes(is, Integer.BYTES)).getInt();
            int[] ret = new int[len];
            for (int i=0; i < len; i++) {
                ret[i] = ByteBuffer.wrap(readNBytes(is, Integer.BYTES)).getInt();
            }
            return ret;
        }

        @Override
        public void writeData(OutputStream os, int[] data) throws IOException {
            os.write(ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array());
            for (int i : data) os.write(ByteBuffer.allocate(Integer.BYTES).putInt(i).array());
        }

        @Override
        public byte getCode() {
            return (byte) 8;
        }
    };

    public static DataType<long[]> LONG_ARRAY = new DataType<long[]>() {
        @Override
        public Class<long[]> getType() {
            return long[].class;
        }

        @Override
        public String getName() {
            return "LONG_ARRAY";
        }

        @Override
        public long[] readData(InputStream is) throws IOException {
            int len = ByteBuffer.wrap(readNBytes(is, Integer.BYTES)).getInt();
            long[] ret = new long[len];
            for (int i=0; i < len; i++) {
                ret[i] = ByteBuffer.wrap(readNBytes(is, Long.BYTES)).getLong();
            }
            return ret;
        }

        @Override
        public void writeData(OutputStream os, long[] data) throws IOException {
            os.write(ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array());
            for (long i : data) os.write(ByteBuffer.allocate(Long.BYTES).putLong(i).array());
        }

        @Override
        public byte getCode() {
            return (byte) 9;
        }
    };

    private static final List<DataType<?>> ALL = Collections.unmodifiableList(Arrays.asList(BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, STRING, BYTE_ARRAY, INTEGER_ARRAY, LONG_ARRAY));

    private DataType() {
        CODE_MAP.put(getCode(), this);
        CLASS_MAP.put(getType(), this);
    }

    public static @NotNull @UnmodifiableView List<DataType<?>> getAll() {
        return ALL;
    }

    public static @Nullable DataType<?> fromCode(byte code) {
        return CODE_MAP.get(code);
    }

    public static @Nullable DataType<?> fromObject(@NotNull Object object) {
        DataType<?> ret = CLASS_MAP.get(object.getClass());
        if (ret != null) return ret;
        for (DataType<?> dt : ALL) {
            if (dt.getType().isInstance(object)) {
                ret = dt;
                break;
            }
        }
        return ret;
    }

    public abstract Class<T> getType();

    public abstract String getName();

    private boolean bukkitDataTypeSet = false;
    private Object bukkitDataType = null;
    public Object getBukkitDataType() {
        if (!bukkitDataTypeSet) {
            try {
                Class<?> clazz = Class.forName("org.bukkit.persistence.PersistentDataType");
                Field f = clazz.getField(getName());
                bukkitDataType = f.get(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            bukkitDataTypeSet = true;
        }
        return bukkitDataType;
    }

    public abstract T readData(InputStream is) throws IOException;

    public abstract void writeData(OutputStream os, T data) throws IOException;

    public void writeDataGeneric(OutputStream os, Object data) throws IOException {
        writeData(os, getType().cast(data));
    }

    public abstract byte getCode();

}
