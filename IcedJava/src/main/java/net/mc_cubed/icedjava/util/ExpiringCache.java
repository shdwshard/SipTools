/*
 * Copyright 2009 Charles Chappell.
 *
 * This file is part of IcedJava.
 *
 * IcedJava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * IcedJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with IcedJava.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.mc_cubed.icedjava.util;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A map class that expires objects after a set interval, a certain amount of
 * inactivity or both.
 *
 * @param <K> Key to map objects by
 * @param <T> Type of Object to cache
 * @author Charles Chappell
 * @since 0.9
 */
public class ExpiringCache<K extends Object, T extends Object> implements Map<K, T> {

    public ExpiringCache() {
        cacheMap = new ConcurrentHashMap<K, CachedObject<T>>();
        initialize();
    }

    public ExpiringCache(long ttl, long ato, long tiv) {
        this.ttl = ttl;
        this.ato = ato;
        this.tiv = tiv;

        cacheMap = new ConcurrentHashMap<K, CachedObject<T>>();
        initialize();
    }
    static Logger logger = Logger.getLogger(ExpiringCache.class.getPackage().getName());
    public static final long DEFAULT_TIME_TO_LIVE = 10 * 60 * 1000;
    public static final long DEFAULT_ACCESS_TIMEOUT = 5 * 60 * 1000;
    public static final long DEFAULT_TIMER_INTERVAL = 2 * 60 * 1000;
    long ttl = DEFAULT_TIME_TO_LIVE;
    long ato = DEFAULT_ACCESS_TIMEOUT;
    long tiv = DEFAULT_TIMER_INTERVAL;
//    LRUMap cacheMap;
    ConcurrentHashMap<K, CachedObject<T>> cacheMap;
    Timer cacheManager;

    @Override
    protected void finalize() throws Throwable {
        if (cacheManager != null) {
            cacheManager.cancel();
            cacheManager = null;
        }
        super.finalize();
    }

    @SuppressWarnings("unchecked")
    public Map<K, T> getMap() {
        HashMap<K, T> hm = new HashMap<K, T>();

        for (Entry<K, CachedObject<T>> e : cacheMap.entrySet()) {
            T value = e.getValue().cachedData.get();
            if (value != null) {
                hm.put(e.getKey(), value);
            }
        }

        return hm;
    }

    public void setTimeToLive(long milliSecs) {
        ttl = milliSecs;
        initialize();
    }

    public void setAccessTimeout(long milliSecs) {
        ato = milliSecs;
        initialize();
    }

    public void setCleaningInterval(long milliSecs) {
        tiv = milliSecs;
        initialize();
    }

    public final void initialize() {
        logger.entering(getClass().getName(), "initialize()");
        if (cacheManager != null) {
            cacheManager.cancel();
        }
        cacheManager = new Timer(true);        
        cacheManager.schedule(
                new TimerTask() {

                    @Override
                    @SuppressWarnings("CallToThreadYield")
                    public void run() {
                        Logger.getAnonymousLogger().entering("TimerTask", "run()");
//                        NDC.push("TimerTask");
                        long now = System.currentTimeMillis();
                        try {
                            @SuppressWarnings("unchecked")
                            Iterator<Entry<K, CachedObject<T>>> itr = cacheMap.entrySet().iterator();
                            while (itr.hasNext()) {
                                Entry<K, CachedObject<T>> entry = itr.next();
                                K key = entry.getKey();
                                CachedObject<T> cobj = entry.getValue();
                                if (cobj == null || cobj.hasExpired(now)) {
                                    Logger.getAnonymousLogger().log(Level.FINE,
                                            "Removing {0}: Idle time={1}; Stale time:{2}",
                                            new Object[]{key, now - cobj.timeAccessedLast, now - cobj.timeCached});
                                    itr.remove();
                                    Thread.yield();
                                }
                            }
                        } catch (ConcurrentModificationException cme) {
                            /*
                            Ignorable.  This is just a timer cleaning up.
                            It will catchup on cleaning next time it runs.
                             */
                            Logger.getAnonymousLogger().log(Level.FINE, "Ignorable ConcurrentModificationException", cme);
                        }
                        Logger.getAnonymousLogger().exiting("TimerTask", "run()");
                    }
                },
                0,
                tiv);
    }

    public int howManyObjects() {
        return cacheMap.size();
    }

    @Override
    public void clear() {
        cacheMap.clear();
    }

    /**
    If the given key already maps to an existing object and the new object
    is not equal to the existing object, existing object is overwritten
    and the existing object is returned; otherwise null is returned.
    You may want to check the return value for null-ness to make sure you
    are not overwriting a previously cached object.  May be you can use a
    different key for your object if you do not intend to overwrite.
     * @param key
     * @param dataToCache
     * @return Object replaced by the just submitted object, or null if none
     */
    public T admit(K key, T dataToCache) {
        //cacheMap.put(key, new CachedObject<T>(dataToCache));
        //return null;

        CachedObject<T> cobj = cacheMap.get(key);
        if (cobj == null) {
            cacheMap.put(key, new CachedObject<T>(dataToCache));
            return null;
        } else {
            T obj = cobj.getCachedData(key);
            if (obj == null) {
                if (dataToCache == null) {
                    // Avoids creating unnecessary new cachedObject
                    // Number of accesses is not reset because object is the same
                    cobj.timeCached = cobj.timeAccessedLast = System.currentTimeMillis();
                    return null;
                } else {
                    cacheMap.put(key, new CachedObject<T>(dataToCache));
                    return null;
                }
            } else if (obj.equals(dataToCache)) {
                // Avoids creating unnecessary new cachedObject
                // Number of accesses is not reset because object is the same
                cobj.timeCached = cobj.timeAccessedLast = System.currentTimeMillis();
                return null;
            } else {
                cacheMap.put(key, new CachedObject<T>(dataToCache));
                return obj;
            }
        }
    }

    public T admit(K key, T dataToCache, long objectTimeToLive, long objectIdleTimeout) {
        //cacheMap.put(key, new CachedObject<T>(dataToCache));
        //return null;

        CachedObject<T> cobj = cacheMap.get(key);
        if (cobj == null) {
            cacheMap.put(key, new CachedObject<T>(dataToCache, objectTimeToLive, objectIdleTimeout));
            return null;
        } else {
            T obj = cobj.getCachedData(key);
            if (obj == null) {
                if (dataToCache == null) {
                    // Avoids creating unnecessary new cachedObject
                    // Number of accesses is not reset because object is the same
                    cobj.timeCached = cobj.timeAccessedLast = System.currentTimeMillis();
                    cobj.objectTTL = objectTimeToLive;
                    cobj.objectIdleTimeout = objectIdleTimeout;
                    cobj.userTimeouts = true;
                    return null;
                } else {
                    cacheMap.put(key, new CachedObject<T>(dataToCache, objectTimeToLive, objectIdleTimeout));
                    return null;
                }
            } else if (obj.equals(dataToCache)) {
                // Avoids creating unnecessary new cachedObject
                // Number of accesses is not reset because object is the same
                cobj.timeCached = cobj.timeAccessedLast = System.currentTimeMillis();
                cobj.objectTTL = objectTimeToLive;
                cobj.objectIdleTimeout = objectIdleTimeout;
                cobj.userTimeouts = true;
                return null;
            } else {
                cacheMap.put(key, new CachedObject<T>(dataToCache, objectTimeToLive, objectIdleTimeout));
                return obj;
            }
        }
    }

    public T recover(Object key) {
        CachedObject<T> cobj = cacheMap.get((K) key);
        if (cobj == null) {
            return null;
        } else {
            return cobj.getCachedData(key);
        }
    }

    public void discard(Object key) {
        cacheMap.remove((K) key);
    }

    public long whenCached(Object key) {
        CachedObject<T> cobj = cacheMap.get((K) key);
        if (cobj == null) {
            return 0;
        }
        return cobj.timeCached;
    }

    public long whenLastAccessed(Object key) {
        CachedObject<T> cobj = cacheMap.get((K) key);
        if (cobj == null) {
            return 0;
        }
        return cobj.timeAccessedLast;
    }

    public int howManyTimesAccessed(Object key) {
        CachedObject<T> cobj = cacheMap.get((K) key);
        if (cobj == null) {
            return 0;
        }
        return cobj.numberOfAccesses;
    }

    @Override
    public int size() {
        return cacheMap.size();
    }

    @Override
    public boolean isEmpty() {
        return cacheMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return cacheMap.containsKey((K) key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T get(Object key) {
        return recover(key);
    }

    @Override
    public T put(K key, T value) {
        return admit(key, value);
    }

    @Override
    public T remove(Object key) {
        discard(key);
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(Map t) {
        Iterator<Entry<K, T>> i = t.entrySet().iterator();
        while (i.hasNext()) {
            Entry<? extends K, ? extends T> e = i.next();
            admit(e.getKey(), e.getValue());
        }
    }

    @Override
    public Set<K> keySet() {
        return cacheMap.keySet();
    }

    @Override
    public Collection<T> values() {
        Collection<T> retval = new LinkedList<T>();
        for (CachedObject<T> obj : cacheMap.values()) {
            T value = obj.cachedData.get();
            if (value != null) {
                retval.add(value);
            }
        }
        return retval;
    }

    @Override
    public Set<Entry<K, T>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * A cached object, needed to store attributes such as the last time
     * it was accessed.
     * @param <T>
     */
    protected class CachedObject<T extends Object> {

        SoftReference<T> cachedData;
        long timeCached;
        long timeAccessedLast;
        int numberOfAccesses;
        long objectTTL;
        long objectIdleTimeout;
        boolean userTimeouts;

        CachedObject(T cachedData) {
            long now = System.currentTimeMillis();
            this.cachedData = new SoftReference<T>(cachedData);
            timeCached = now;
            timeAccessedLast = now;
            ++numberOfAccesses;
        }

        CachedObject(T cachedData, long timeToLive, long idleTimeout) {
            long now = System.currentTimeMillis();
            this.cachedData = new SoftReference<T>(cachedData);
            objectTTL = timeToLive;
            objectIdleTimeout = idleTimeout;
            userTimeouts = true;
            timeCached = now;
            timeAccessedLast = now;
            ++numberOfAccesses;
        }

        T getCachedData(Object key) {
            long now = System.currentTimeMillis();
            if (hasExpired(now) || cachedData.get() == null) {
                cachedData = null;
                cacheMap.remove((K) key);
                return null;
            }
            timeAccessedLast = now;
            ++numberOfAccesses;
            return cachedData.get();
        }

        boolean hasExpired(long now) {
            long usedTTL = userTimeouts ? objectTTL : ttl;
            long usedATO = userTimeouts ? objectIdleTimeout : ato;

            if (now > timeAccessedLast + usedATO
                    || now > timeCached + usedTTL) {
                return true;
            } else {
                return false;
            }
        }
    }
}
