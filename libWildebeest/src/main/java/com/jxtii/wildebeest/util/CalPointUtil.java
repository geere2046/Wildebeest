package com.jxtii.wildebeest.util;

/**
 * 计算扣分工具类
 * Created by huangyc on 2016/3/11.
 */
public class CalPointUtil {

    static float maxSpeed = CommUtil.MAX_SPEED;
    static double gThreshold = 0.3;

    /**
     * 计算超速扣分
     *
     * @param speed
     * @return
     */
    public static int calSpeeding(float speed) {
        if (speed <= maxSpeed * 1.2) {
            return 0;
        } else if (maxSpeed * 1.2 <= speed && speed < maxSpeed * 1.3) {
            return 2;
        } else if (maxSpeed * 1.3 <= speed && speed < maxSpeed * 1.43) {
            return 3;
        } else if (maxSpeed * 1.43 <= speed && speed < maxSpeed * 1.57) {
            return 4;
        } else {
            return 6;
        }
    }

    /**
     * 计算加速度额外扣分
     *
     * @param g
     * @return
     */
    public static int calAccOrDec(double g) {
        if (g < gThreshold * 1.1) {
            return 0;
        } else if (g < gThreshold * 1.3 && g >= gThreshold * 1.1) {
            return 1;
        } else if (g < gThreshold * 1.43 && g >= gThreshold * 1.3) {
            return 3;
        } else if (g < gThreshold * 1.57 && g >= gThreshold * 1.43) {
            return 4;
        } else {
            return 6;
        }
    }
}
