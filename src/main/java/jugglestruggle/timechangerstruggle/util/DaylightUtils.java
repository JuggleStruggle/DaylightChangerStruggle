package jugglestruggle.timechangerstruggle.util;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import net.minecraft.world.World;

/**
 * https://minecraft.gamepedia.com/wiki/Daylight_cycle
 * 
 * @author JuggleStruggle
 * @implNote Created on 27-Jan-2022, Thursday
 */
public final class DaylightUtils
{
	// 1,000 / 50 seconds real-time
	public static final double ONE_HOUR = 1000d;
	// 16.6 (repeats forever in 6 as a decimal) / 0.83 seconds
	public static final double ONE_MINUTE = ONE_HOUR / 60d;   
	// 0.27 (repeats forever in 7 as a decimal) / 0.0138 seconds
	public static final double ONE_SECOND = ONE_MINUTE / 60d; 
	
	// 24,000 / 20 minutes real-time
	public static final double ONE_DAY = ONE_HOUR * 24d; 
	// 168,000 / 2.3 or 2h 20 mins
	public static final double ONE_WEEK = ONE_DAY * 7d; 
	// 192,000 / 2.6 or 2h 40 mins
	public static final double ONE_LUNAR_CYCLE = ONE_DAY * 8d;
	// 8,760,000
	public static final double ONE_YEAR = ONE_DAY * 365d; 
	
	
	public static final int SUNRISE = 0; 
	public static final int MIDDAY = 6000; 
	public static final int SUNSET = 12000; 
	public static final int MIDNIGHT = 18000;
	
	

	public static SimpleDateFormat DATE_FORMAT = 
		new SimpleDateFormat("DD yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
	
	
	
	public static Date minecraftTicksToDate(long ticks, boolean addDay)
	{
		// Align to the real-world format as Minecraft always starts at 
		// 06:00 (0 ticks). If ticks is 0, then it is subtracted by
		// MIDNIGHT (-18000) then added by ONE_DAY (24000) which ends
		// up resulting in 6000, 6 AM
		long newTicks = ticks - (long)MIDNIGHT + (long)ONE_DAY;
		
		
		// Divide the ticks by the amount of days then subtract our created
		// daylight ticks to avoid getting larger values in hours, so on and
		// so for
		long days = newTicks / (long)ONE_DAY;
		newTicks -= days * ONE_DAY;
		
		// Do the same with hours and so on and so for
		int hours = (int)(newTicks / (long)ONE_HOUR);
		newTicks -= hours * ONE_HOUR;
		
		int minutes = (int)(newTicks / (long)ONE_MINUTE);
		newTicks -= minutes * ONE_MINUTE;
		
		int seconds = (int)(newTicks / ONE_SECOND);
		
		// Align to local time rather than GMT since the game doesn't care if we us
		// players live in a different timezone.
//		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);
		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		
		cal.setLenient(true);
		
		if (addDay) {
			days += 1L;
		}
		
		cal.set(0, Calendar.JANUARY, (int)days, hours, minutes, seconds);
		
		return cal.getTime();
	}
	
	public static String getParsedTime(World world, boolean dateOverTicks)
	{
		long ticksToParse;
		boolean addDay;
		
		if (TimeChangerStruggleClient.useWorldTime()) 
		{
			ticksToParse = world.getTimeOfDay();
			addDay = true;
		}
		else
		{
			DayNightCycleBasis cycle = TimeChangerStruggleClient.getTimeChanger();
			
			ticksToParse = cycle.getCachedTime();
			addDay = cycle.shouldAddDayForDateDisplay();
		}
		
		return dateOverTicks ? DaylightUtils.DATE_FORMAT.format(
			DaylightUtils.minecraftTicksToDate(ticksToParse, addDay)) : ""+ticksToParse;
	}
}
