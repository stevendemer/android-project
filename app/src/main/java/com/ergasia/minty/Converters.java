package com.ergasia.minty;

import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.ergasia.minty.entities.ExpenseCategory;

import java.util.Date;

/**
 * Room does not support enums or dates natively so we need to convert them
 */
public class Converters {

    @TypeConverter
    public static ExpenseCategory fromString(String value) {
        return ExpenseCategory.valueOf(value);
    }

    @TypeConverter
    public static String toString(ExpenseCategory category) {
        return category.name();
    }


    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
