package com.movideo.whitelabel.util;

import android.content.Context;
import android.graphics.Bitmap;

import com.squareup.picasso.Cache;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

/**
 * Picasso helper class to get new instance of {@link Picasso} class.
 */
public class PicassoHelper {

    private static PicassoHelper singleton;

    private LruCache lruCache;
    private Picasso picasso;
    private Cache nocache = new Cache() {
        @Override
        public Bitmap get(String key) {
            return null;
        }

        @Override
        public void set(String key, Bitmap bitmap) {

        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public int maxSize() {
            return 0;
        }

        @Override
        public void clear() {

        }

        @Override
        public void clearKeyUri(String keyPrefix) {

        }
    };


    private PicassoHelper() {
    }

    public static PicassoHelper getInstance(Context context) {
        if (singleton == null) {
            singleton = new PicassoHelper();
            LruCache cache = new LruCache(context.getApplicationContext());
            singleton.lruCache = cache;
            singleton.picasso = new Picasso.Builder(context.getApplicationContext())
                    .memoryCache(cache)
                    .debugging(true)
                    .build();
        }
        return singleton;
    }

    /**
     * Returns Picasso cache.
     *
     * @return {@link LruCache}
     */
    public LruCache getLruCache() {
        return lruCache;
    }

    /**
     * Returns Picasso instance.
     *
     * @return {@link Picasso}
     */
    public Picasso getPicasso() {
        return picasso;
    }

    /**
     * Call on destroy.
     */
    public void onDestroy() {
        lruCache.clear();
        picasso.shutdown();
        lruCache = null;
        picasso = null;
    }

    /**
     * Clears the cache.
     */
    public void clearCache() {
        lruCache.clear();
    }
}
