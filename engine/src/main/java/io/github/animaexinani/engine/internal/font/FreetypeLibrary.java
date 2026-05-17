package io.github.animaexinani.engine.internal.font;

import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.lwjgl.PointerBuffer;
import io.github.animaexinani.engine.internal.GlobalCleaner;

public final class FreetypeLibrary implements AutoCloseable {
    private static final class NativeState implements Runnable {
        private static final Lock stateLock = new ReentrantLock();
        private final AtomicInteger refCount;
        private final long libraryHandle;

        public void run() {
            var newRefCount = this.refCount.decrementAndGet();
            if (newRefCount == 0) {
                org.lwjgl.util.freetype.FreeType.FT_Done_FreeType(this.libraryHandle);
            }
        }

        public NativeState(AtomicInteger refCount, PointerBuffer libraryHandleBuffer) {
            this.refCount = refCount;
            
            stateLock.lock();
            try {
                var oldRefCount = this.refCount.getAndIncrement();
                if (oldRefCount == 0) {
                    var result = org.lwjgl.util.freetype.FreeType.FT_Init_FreeType(libraryHandleBuffer);
                    if (result != 0) {
                        throw new RuntimeException("Failed to initialize FreeType library: " + result);
                    }
                }

                this.libraryHandle = libraryHandleBuffer.get(0);
            } finally {
                stateLock.unlock();
            }
        }
    }

    private static final AtomicInteger refCount = new AtomicInteger(0);
    private static final PointerBuffer libraryHandleBuffer = PointerBuffer.allocateDirect(1);

    private final NativeState nativeState;
    private final Cleaner.Cleanable cleanable;
    private final AtomicBoolean closed;

    public long libraryHandle() {
        if (this.closed.getAcquire()) {
            throw new IllegalStateException("FreetypeLibrary has been closed");
        }
        return this.nativeState.libraryHandle;
    }
    
    public FreetypeLibrary() {
        this.nativeState = new NativeState(refCount, libraryHandleBuffer);
        this.cleanable = GlobalCleaner.register(this, this.nativeState);
        this.closed = new AtomicBoolean(false);
    }
    
    @Override
    public void close() {
        if (this.closed.getAndSet(true)) {
            return;
        }
        this.cleanable.clean();
    }
}
