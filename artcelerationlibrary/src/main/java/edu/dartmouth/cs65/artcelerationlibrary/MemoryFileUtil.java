package edu.dartmouth.cs65.artcelerationlibrary;

import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MemoryFileUtil {
    private static final Method sMethodGetFileDescriptor;
    static {
        sMethodGetFileDescriptor = get("getFileDescriptor");
    }


    public static FileDescriptor getFileDescriptor(MemoryFile file) {
        try {
            return (FileDescriptor) sMethodGetFileDescriptor.invoke(file);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    public static ParcelFileDescriptor getParcelFileDescriptor(MemoryFile file) throws IOException {
        return ParcelFileDescriptor.dup(getFileDescriptor(file));

    }
    private static Method get(String name) {
        try {
            return MemoryFile.class.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
