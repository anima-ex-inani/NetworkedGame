package io.github.animaexinani.engine.internal.assets;

import io.github.animaexinani.engine.assets.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class AssetManagerImpl extends AssetManager {
    private final ExecutorService assetLoadingExecutor = Executors.newCachedThreadPool(runnable -> {
        var thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });

    private final Map<@NotNull AssetKey<? extends Asset>, @NotNull CompletableFuture<? extends Asset>> pendingAssetLoads = new ConcurrentHashMap<>();

    private static final class KeyedSoftReference<T extends Asset> extends SoftReference<T> {
        private final AssetKey<T> key;

        public KeyedSoftReference(T referent, AssetKey<T> key, ReferenceQueue<? super T> q) {
            super(referent, q);
            this.key = key;
        }
    }

    private final AtomicInteger assetCacheWrites = new AtomicInteger(0);
    private static final int ASSET_CACHE_WRITE_THRESHOLD = 100;
    private final Map<@NotNull AssetKey<? extends Asset>, @NotNull KeyedSoftReference<? extends Asset>> assetCache = new ConcurrentHashMap<>();

    private final ReferenceQueue<Asset> assetCacheReferenceQueue = new ReferenceQueue<>();

    private void cleanAssetCache() {
        var ref = (KeyedSoftReference<? extends Asset>)this.assetCacheReferenceQueue.poll();

        while (Objects.nonNull(ref)) {
            var key = ref.key;
            this.assetCache.remove(key, ref);
            ref = (KeyedSoftReference<? extends Asset>)this.assetCacheReferenceQueue.poll();
        }
    }

    private <T extends Asset> void cacheAsset(@NotNull AssetKey<T> key, @NotNull T asset) {
        if (this.assetCacheWrites.getAndIncrement() % AssetManagerImpl.ASSET_CACHE_WRITE_THRESHOLD == 0) {
            this.cleanAssetCache();
        }

        this.assetCache.put(key, new KeyedSoftReference<>(asset, key, this.assetCacheReferenceQueue));
    }

    private <T extends Asset> @NotNull CompletableFuture<T> fetchAsset(@NotNull AssetKey<T> key, @NotNull AssetLoadingContextImpl context) {
        return CompletableFuture.supplyAsync(() -> {
            var loaderExceptions = new ArrayList<Exception>(this.loaders.size());
            for (var loader : this.loaders) {
                if (!loader.supports(key.type())) {
                    continue;
                }

                try {
                    var asset = loader.load(key, context);
                    this.cacheAsset(key, asset);
                    return asset;
                }
                catch (UnsupportedFormatException | IOException e) {
                    loaderExceptions.add(e);
                }
            }

            var finalException = new NoSuchFileException(key.key(), null, "No loader could load the asset");
            for (var exception : loaderExceptions) {
                finalException.addSuppressed(exception);
            }
            throw new CompletionException(finalException);
        }, this.assetLoadingExecutor);
    }

    private <T extends Asset> @NotNull CompletableFuture<T> loadAsset(@NotNull AssetKey<T> key, @NotNull AssetLoadingContextImpl context) {
        var future = new CompletableFuture<T>();
        @SuppressWarnings("unchecked") var existingFuture = (CompletableFuture<T>)this.pendingAssetLoads.putIfAbsent(key, future);

        if (Objects.nonNull(existingFuture)) {
            return existingFuture;
        }

        this.fetchAsset(key, context).whenComplete((asset, throwable) -> {
            if (Objects.nonNull(throwable)) {
                future.completeExceptionally(throwable);
            }
            else {
                future.complete(asset);
            }

            this.pendingAssetLoads.remove(key, future);
        });

        return future;
    }

    private <T extends Asset> @Nullable T getCachedAsset(@NotNull AssetKey<T> key) {
        @SuppressWarnings("unchecked") var assetReference = (KeyedSoftReference<T>)this.assetCache.get(key);
        if (Objects.isNull(assetReference)) {
            return null;
        }

        var asset = assetReference.get();
        if (Objects.isNull(asset)) {
            this.assetCache.remove(key, assetReference);
            return null;
        }

        if (!asset.isValid()) {
            this.assetCache.remove(key, assetReference);
            return null;
        }

        return asset;
    }

    @Override
    public <T extends Asset> Future<T> load(@NotNull AssetKey<T> key) {
        var cachedAsset = this.getCachedAsset(key);
        if (Objects.nonNull(cachedAsset)) {
            return CompletableFuture.completedFuture(cachedAsset);
        }

        var context = new AssetLoadingContextImpl(this, new AssetKey[]{ key });
        return this.loadAsset(key, context);
    }

    public <T extends Asset> Future<T> loadSubasset(@NotNull AssetKey<T> key, @NotNull AssetLoadingContextImpl context) {
        var currentLoadStack = context.loadStack();
        var newLoadStack = Arrays.copyOf(currentLoadStack, currentLoadStack.length + 1);
        newLoadStack[newLoadStack.length - 1] = key;
        if (Arrays.asList(currentLoadStack).contains(key)) {
            return CompletableFuture.failedFuture(new RecursiveAssetException(newLoadStack));
        }

        var cachedAsset = this.getCachedAsset(key);
        if (Objects.nonNull(cachedAsset)) {
            return CompletableFuture.completedFuture(cachedAsset);
        }

        var newContext = new AssetLoadingContextImpl(this, newLoadStack);
        return this.loadAsset(key, newContext);
    }
}
