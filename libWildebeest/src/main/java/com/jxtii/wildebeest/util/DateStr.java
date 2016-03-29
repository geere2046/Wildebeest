package com.jxtii.wildebeest.util;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;

@SuppressLint("SimpleDateFormat")
public class DateStr {


	private static String dateStr = null;

	// 获取系统时间，转换成 YYYY-MM-DD 格式的日期字符串
	public static String dateStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		dateStr = formatter.format(myDate);

		// java.util.Calendar cal = java.util.Calendar.getInstance(); //当前时间
		// SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd
		// HH:mm:ss");
		// String mDateTime = formatter.format(cal.getTime());
		return dateStr;
	}

	// 获取昨日时间，转换成 YYYY-MM-DD 格式的日期字符串
	public static String yesterdayStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		long myTime = (myDate.getTime() / 1000) - 60 * 60 * 24;
		myDate.setTime(myTime * 1000);
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取前日时间，转换成 YYYY-MM-DD 格式的日期字符串
	public static String beforeyesterdayStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		long myTime = (myDate.getTime() / 1000) - 2 * 60 * 60 * 24;
		myDate.setTime(myTime * 1000);
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取上一周时间，转换成 YYYY-MM-DD 格式的日期字符串
	public static String beforeweekStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		long myTime = (myDate.getTime() / 1000) - 7 * 60 * 60 * 24;
		myDate.setTime(myTime * 1000);
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取明日时间，转换成 YYYY-MM-DD 格式的日期字符串
	public static String tomorrowStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		long myTime = (myDate.getTime() / 1000) + 60 * 60 * 24;
		myDate.setTime(myTime * 1000);
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取系统时间，转换成 YYYY-MM-DD HH:mm:ss格式的日期时间字符串
	public static String datetimeStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取系统时间，转换成 YYYY-MM-DD HH:mm:ss格式的日期时间字符串
	public static String datetimeStr2() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取系统时间，转换成 YYMMDD 格式的日期字符串
	public static String yymmddStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取系统时间，转换成 YYYYMMDD 格式的日期字符串
	public static String yyyymmddStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取系统时间，转换成 yyMMddHHmmss 格式的日期字符串
	public static String yymmddHHmmssStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取系统时间，转换成 yyMMddHHmmss 格式的日期字符串
	public static String yyyymmddHHmmssStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取系统时间，转换成 yyMMddHHmmss 格式的日期字符串
	public static String yyyymmddHHmmssSSSStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyymmddHHmmssSSS");
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取系统时间，转换成 yyMMddHHmmss 格式的日期字符串
	public static String ddHHmmssssStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("ddHHmmssss");
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取系统时间，转换成 yyMMddHHmmss 格式的日期字符串
	public static String HHmmssStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 计算参数时间与系统时间的月份差 时间格式为“200306” 字符串
	public static int CalculationMonths(String time) {
		int months;
		int yearSecond = Integer.parseInt(time.substring(0, 4));
		int monthSecond = Integer.parseInt(time.substring(4, 6));
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		String sysTime = sdf.format(date);
		System.out.println(sysTime);
		int yearFirst = Integer.parseInt(sysTime.substring(0, 4));
		int monthFirst = Integer.parseInt(sysTime.substring(4, 6));
		months = (yearFirst - yearSecond) * 12 + (monthFirst - monthSecond);
		return months;
	}

	public final static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	// 缩放图片
	public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {

		// load the origial Bitmap
		Bitmap BitmapOrg = bitmap;

		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;

		// 计算缩放率，新尺寸除原始尺寸
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		// 旋转图片 动作
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);

		// make a Drawable from Bitmap to allow to set the Bitmap
		// to the ImageView, ImageButton or what ever
		return resizedBitmap;

	}

	public static double multiply(double v1, int v2, int scale) {
		if (scale < 0)
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.multiply(b2).setScale(scale, BigDecimal.ROUND_HALF_UP)
				.doubleValue();
	}

	// 获取昨天时间，转换成 YYYYMMDD 格式的日期字符串
	public static String yyyymmddnextStr() {
		Date d = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		dateStr = formatter.format(d);
		return dateStr;
	}

	// 获取昨天时间，转换成 YYYYMMDD 格式的日期字符串
	public static String yyyymmnextStr() {
		Date d = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM");
		dateStr = formatter.format(d);
		return dateStr;
	}

	// 获取昨天时间，转换成 YYYYMMDD 格式的日期字符串
	public static String yyyynextStr() {
		Date d = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
		dateStr = formatter.format(d);
		return dateStr;
	}

	// 获取系统时间，转换成 YYMMDD 格式的日期字符串
	public static String yyyymmStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM");
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取系统时间，转换成 YYMMDD 格式的日期字符串
	public static String yyyyStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取系统时间，转换成 YYMMDD 格式的日期字符串
	public static String HHmmStr() {
		Date myDate = new Date(System.currentTimeMillis()); // 获取系统时间
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		dateStr = formatter.format(myDate);
		return dateStr;
	}

	// 获取昨天时间，转换成 YYYYMMDD 格式的日期字符串
	public static String yyyymmlastStr() {
		Date d = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM");
		dateStr = formatter.format(d);
		return dateStr;
	}

	// 获取昨天时间，转换成 YYYYMMDD 格式的日期字符串
	public static String yyyylastStr() {
		Date d = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
		dateStr = formatter.format(d);
		return dateStr;
	}

	// 获取昨天时间，转换成 YYYYMMDD 格式的日期字符串
	public static String yyyymmddlastStr() {
		Date d = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		dateStr = formatter.format(d);
		return dateStr;
	}

	// 获取昨天时间，转换成 YYYYMMDD 格式的日期字符串
	public static String mmddStr() {
		Date d = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");
		dateStr = formatter.format(d);
		return dateStr;
	}

	// String pTime = "2012-03-12";
	public static String getWeek(String pTime) {

		String Week = "";

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		try {

			c.setTime(format.parse(pTime));

		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (c.get(Calendar.DAY_OF_WEEK) == 1) {
			Week += "周日";
		}
		if (c.get(Calendar.DAY_OF_WEEK) == 2) {
			Week += "周一";
		}
		if (c.get(Calendar.DAY_OF_WEEK) == 3) {
			Week += "周二";
		}
		if (c.get(Calendar.DAY_OF_WEEK) == 4) {
			Week += "周三";
		}
		if (c.get(Calendar.DAY_OF_WEEK) == 5) {
			Week += "周四";
		}
		if (c.get(Calendar.DAY_OF_WEEK) == 6) {
			Week += "周五";
		}
		if (c.get(Calendar.DAY_OF_WEEK) == 7) {
			Week += "周六";
		}

		return Week;
	}

	/**
	 * 获取两个日期之间的间隔天数
	 *
	 * @return
	 */
	public static int getGapCount(Date startDate, Date endDate) {
		Calendar fromCalendar = Calendar.getInstance();
		fromCalendar.setTime(startDate);
		fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
		fromCalendar.set(Calendar.MINUTE, 0);
		fromCalendar.set(Calendar.SECOND, 0);
		fromCalendar.set(Calendar.MILLISECOND, 0);

		Calendar toCalendar = Calendar.getInstance();
		toCalendar.setTime(endDate);
		toCalendar.set(Calendar.HOUR_OF_DAY, 0);
		toCalendar.set(Calendar.MINUTE, 0);
		toCalendar.set(Calendar.SECOND, 0);
		toCalendar.set(Calendar.MILLISECOND, 0);

		return (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime()
				.getTime()) / (1000 * 60 * 60 * 24));
	}

}
