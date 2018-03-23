package ar.com.hjg.pngj;

import java.util.*;

public enum FilterType
{
    FILTER_NONE(0), 
    FILTER_SUB(1), 
    FILTER_UP(2), 
    FILTER_AVERAGE(3), 
    FILTER_PAETH(4), 
    FILTER_DEFAULT(-1), 
    FILTER_AGGRESSIVE(-2), 
    FILTER_VERYAGGRESSIVE(-4), 
    FILTER_ADAPTIVE_FULL(-4), 
    FILTER_ADAPTIVE_MEDIUM(-3), 
    FILTER_ADAPTIVE_FAST(-2), 
    FILTER_SUPER_ADAPTIVE(-10), 
    FILTER_PRESERVE(-40), 
    FILTER_CYCLIC(-50), 
    FILTER_UNKNOWN(-100);
    
    public final int val;
    private static HashMap<Integer, FilterType> byVal;
    
    private FilterType(final int val) {
        this.val = val;
    }
    
    public static FilterType getByVal(final int i) {
        return FilterType.byVal.get(i);
    }
    
    public static boolean isValidStandard(final int i) {
        return i >= 0 && i <= 4;
    }
    
    public static boolean isValidStandard(final FilterType fy) {
        return fy != null && isValidStandard(fy.val);
    }
    
    public static boolean isAdaptive(final FilterType fy) {
        return fy.val <= -2 && fy.val >= -4;
    }
    
    public static FilterType[] getAllStandard() {
        return new FilterType[] { FilterType.FILTER_NONE, FilterType.FILTER_SUB, FilterType.FILTER_UP, FilterType.FILTER_AVERAGE, FilterType.FILTER_PAETH };
    }
    
    public static FilterType[] getAllStandardNoneLast() {
        return new FilterType[] { FilterType.FILTER_SUB, FilterType.FILTER_UP, FilterType.FILTER_AVERAGE, FilterType.FILTER_PAETH, FilterType.FILTER_NONE };
    }
    
    public static FilterType[] getAllStandardExceptNone() {
        return new FilterType[] { FilterType.FILTER_SUB, FilterType.FILTER_UP, FilterType.FILTER_AVERAGE, FilterType.FILTER_PAETH };
    }
    
    static FilterType[] getAllStandardForFirstRow() {
        return new FilterType[] { FilterType.FILTER_SUB, FilterType.FILTER_NONE };
    }
    
    static {
        FilterType.byVal = new HashMap<Integer, FilterType>();
        for (final FilterType ft : values()) {
            FilterType.byVal.put(ft.val, ft);
        }
    }
}
