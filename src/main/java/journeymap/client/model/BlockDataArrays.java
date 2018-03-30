package journeymap.client.model;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class BlockDataArrays
{
    private HashMap<MapView, Dataset> datasets;
    
    public BlockDataArrays() {
        this.datasets = new HashMap<MapView, Dataset>(8);
    }
    
    public void clearAll() {
        this.datasets.clear();
    }
    
    public Dataset get(final MapView mapView) {
        Dataset dataset = this.datasets.get(mapView);
        if (dataset == null) {
            dataset = new Dataset();
            this.datasets.put(mapView, dataset);
        }
        return dataset;
    }
    
    public static boolean areIdentical(final int[][] arr, final int[][] arr2) {
        boolean match = true;
        for (int j = 0; j < arr.length; ++j) {
            final int[] row = arr[j];
            final int[] row2 = arr2[j];
            match = IntStream.range(0, row.length).map(i -> ~row[i] | row2[i]).allMatch(n -> n == -1);
            if (!match) {
                break;
            }
        }
        return match;
    }
    
    public static class Dataset
    {
        private DataArray<Integer> ints;
        private DataArray<Float> floats;
        private DataArray<Boolean> booleans;
        private DataArray<Object> objects;
        
        Dataset() {
        }
        
        public Dataset(final MapView mapView) {
        }
        
        protected void clear() {
            this.ints = null;
            this.floats = null;
            this.booleans = null;
            this.objects = null;
        }
        
        public DataArray<Integer> ints() {
            if (this.ints == null) {
                this.ints = new DataArray<Integer>(() -> new Integer[16][16]);
            }
            return this.ints;
        }
        
        public DataArray<Float> floats() {
            if (this.floats == null) {
                this.floats = new DataArray<Float>(() -> new Float[16][16]);
            }
            return this.floats;
        }
        
        public DataArray<Boolean> booleans() {
            if (this.booleans == null) {
                this.booleans = new DataArray<Boolean>(() -> new Boolean[16][16]);
            }
            return this.booleans;
        }
        
        public DataArray<Object> objects() {
            if (this.objects == null) {
                this.objects = new DataArray<Object>(() -> new Object[16][16]);
            }
            return this.objects;
        }
    }
    
    public static class DataArray<T>
    {
        private final HashMap<String, T[][]> map;
        private final Supplier<T[][]> initFn;
        
        protected DataArray(final Supplier<T[][]> initFn) {
            this.map = new HashMap<String, T[][]>(4);
            this.initFn = initFn;
        }
        
        public boolean has(final String name) {
            return this.map.containsKey(name);
        }
        
        public T[][] get(final String name) {
            return this.map.computeIfAbsent(name, s -> this.initFn.get());
        }
        
        public T get(final String name, final int x, final int z) {
            return this.get(name)[z][x];
        }
        
        public boolean set(final String name, final int x, final int z, final T value) {
            final T[][] arr = this.get(name);
            final T old = arr[z][x];
            arr[z][x] = value;
            return value != old;
        }
        
        public T[][] copy(final String name) {
            final T[][] src = this.get(name);
            final T[][] dest = this.initFn.get();
            for (int i = 0; i < src.length; ++i) {
                System.arraycopy(src[i], 0, dest[i], 0, src[0].length);
            }
            return dest;
        }
        
        public void copyTo(final String srcName, final String dstName) {
            this.map.put(dstName, this.copy(srcName));
        }
        
        public void clear(final String name) {
            this.map.remove(name);
        }
    }
}
