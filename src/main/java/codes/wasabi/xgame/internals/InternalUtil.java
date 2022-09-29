package codes.wasabi.xgame.internals;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class InternalUtil {

    public static Object invokeMethod(Object object, Method method, Object... args) throws IllegalStateException {
        try {
            return method.invoke(object, args);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T invokeMethod(Object object, Method method, Class<T> clazz, Object... args) throws IllegalStateException {
        try {
            return clazz.cast(method.invoke(object, args));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static Object getField(Object object, Field field) throws IllegalStateException {
        try {
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            return field.get(object);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T getField(Object object, Field field, Class<T> clazz) throws IllegalStateException {
        try {
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            return clazz.cast(field.get(object));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void setField(Object object, Field field, Object value) throws IllegalStateException {
        try {
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            field.set(object, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static @NotNull <T> T construct(Constructor<T> constructor, Object... args) throws IllegalStateException {
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
