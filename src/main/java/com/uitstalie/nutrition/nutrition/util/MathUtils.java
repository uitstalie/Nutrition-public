package com.uitstalie.nutrition.nutrition.util;

public class MathUtils {
    public static long log2Floor(long x){
        if(x <= 0){
            return 0L;
        }
        return 63L-Long.numberOfLeadingZeros(x);
    }

    public static long log2Ceil(long x){
        if(x <=0){
            return 0L;
        }else{
            return 64 - Long.numberOfLeadingZeros(x - 1);
        }

    }
}
