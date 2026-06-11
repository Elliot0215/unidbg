package com.github.unidbg.linux.android.dvm.api;

import com.github.unidbg.Emulator;
import com.github.unidbg.linux.android.dvm.DvmObject;
import com.github.unidbg.linux.android.dvm.VM;
import com.sun.jna.Pointer;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Bitmap extends DvmObject<BufferedImage> {

    public Bitmap(VM vm, BufferedImage image) {
        super(vm.resolveClass("android/graphics/Bitmap"), image);
    }

    public Pointer lockPixels(Emulator<?> emulator, BufferedImage image, ByteBuffer buffer) {
        Pointer pointer = allocateMemoryBlock(emulator, image.getWidth() * image.getHeight() * 4);
        pointer.write(0, buffer.array(), 0, buffer.capacity());
        return pointer;
    }

    public void unlockPixels() {
        if (memoryBlock != null) {
            BufferedImage image = getValue();
            if (image != null) {
                int width = image.getWidth();
                int height = image.getHeight();
                Pointer pointer = memoryBlock.getPointer();
                byte[] pixels = pointer.getByteArray(0, width * height * 4);
                ByteBuffer buffer = ByteBuffer.wrap(pixels);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgba = buffer.getInt();
                        int a = (rgba >> 24) & 0xff;
                        int b = (rgba >> 16) & 0xff;
                        int g = (rgba >> 8) & 0xff;
                        int r = rgba & 0xff;
                        image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                    }
                }
            }
        }
        freeMemoryBlock(null);
    }

}
