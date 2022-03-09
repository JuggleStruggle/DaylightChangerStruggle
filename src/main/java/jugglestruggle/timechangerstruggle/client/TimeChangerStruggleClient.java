package jugglestruggle.timechangerstruggle.client;

import jugglestruggle.timechangerstruggle.TimeChangerStruggle;
import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.client.util.render.RainbowShader;
import jugglestruggle.timechangerstruggle.client.util.render.RenderUtils;
import jugglestruggle.timechangerstruggle.config.Configuration;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBuilder;
import jugglestruggle.timechangerstruggle.daynight.type.LowToHighHeightTime;
import jugglestruggle.timechangerstruggle.daynight.type.MovingTime;
import jugglestruggle.timechangerstruggle.daynight.type.RandomizedTime;
import jugglestruggle.timechangerstruggle.daynight.type.StaticTime;
import jugglestruggle.timechangerstruggle.daynight.type.SystemTime;
import jugglestruggle.timechangerstruggle.mixin.client.world.ClientWorldMixin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

/**
 *
 * @author JuggleStruggle
 * @implNote Created on 26-Jan-2022, Wednesday
 * 
 * @see TimeChangerStruggle
 */
@Environment(EnvType.CLIENT)
public class TimeChangerStruggleClient implements ClientModInitializer
{
	/**
	 * A cached list of day cycle builders which are primarily used
	 * to load their corresponding day cycle type. 
	 * 
	 * <p> This is a linked hashmap to keep things ordered from when
	 * they were first added.
	 */
	private static final Map<String, DayNightCycleBuilder> CYCLE_BUILDERS;
	/**
	 * Uses the client world's time. 
	 * 
	 * <p> Setting it to {@code false} uses JuggleStruggle's version 
	 * and takes control over most (or all) of the time-related calls.
	 * 
	 * @see {@linkplain ClientWorldMixin Client World Mixin} 
	 * where almost all of the calls are overriden
	 */
	public static boolean worldTime = true;
	/**
	 * Shows the user the date rather than ticks, which the game uses
	 * ticks for its daylight cycle.
	 */
	public static boolean dateOverTicks = false;
	/**
	 * A feature not used in the vanilla game where the daylight cycle
	 * moves by combining the previous time with the current while
	 * accounting for the delta. 
	 * 
	 * <p> Disable this to restore the classic cycle where it only uses
	 * the current cycle as the deciding factor.
	 */
	public static boolean smoothButterCycle = true;
	/**
	 * While this might be counter-intuitive to gameplay, this provides
	 * no advantage as it removes the night potion effect from the world
	 * and does nothing regarding giving user the advantage for gamma or
	 * the brightened-up fog.
	 * 
	 * <p> The main reason why this was created was to avoid night time
	 * from looking like day when the original goal was to have it this
	 * way. There are servers that give users night vision to help with
	 * seeing things better, but the users should be the one to decide
	 * if this is something they want.
	 * 
	 * <p> To get some advantages, resort to using options.txt or use a 
	 * mod that will give such lighting advantage.
	 */
	public static boolean disableNightVisionEffect = false;
	/**
	 * Updates the current cycle type {@link #timeChanger} to match
	 * the new values provided on the property list. 
	 * 
	 * <p> It can either be by the active's daylight cycle or by
	 * just having it loaded in the properties list, which it is 
	 * written to the {@link #config}'s {@link Configuration#configData}
	 * as long as this field is set to {@code true}.
	 */
	public static boolean applyOnPropertyListValueUpdate = false;
	/**
	 * Used instead of an enumerator to avoid being locked to specific
	 * day-night cycles if locks were to ever be defined by the mod.
	 */
	private static DayNightCycleBasis timeChanger = null;
	/**
	 * Used as a "key" for {@link #CYCLE_BUILDERS}.
	 */
	private static String timeChangerKey = null;
	/**
	 * Used as a "key" to get the first item on the map.
	 * 
	 * @see TimeChangerStruggleClient#timeChangerKey
	 * @see TimeChangerStruggleClient#timeChangerKeyLast
	 */
	private static String timeChangerKeyFirst = null;
	/**
	 * Used as a "key" to get the last item on the map. Usually this
	 * is the recently added item into the builder cache.
	 * 
	 * @see TimeChangerStruggleClient#timeChangerKey
	 * @see TimeChangerStruggleClient#timeChangerKeyFirst
	 */
	private static String timeChangerKeyLast = null;
	/**
	 * Tell the user client-sided about the changes that was made in
	 * the command. This only affects command actions that aren't too
	 * important.
	 */
	public static boolean commandsCommandFeedbackOnLessImportant = true;
	/**
	 * Whenever the user wishes to use a cycle or uses options which
	 * uses a cycle, disable world time if enabled to make it seem
	 * that it has any effect. 
	 * 
	 * <p> Users who already know the workings of the mod already 
	 * should be aware that it's working by guessing, even if the
	 * world time is ON.
	 */
	public static boolean commandsDisableWorldTimeOnCycleUsage = true;
	
	public static Configuration config;

	private static Commands commands;
	
	static
	{
		// Add default day/night cycle builders for use
		CYCLE_BUILDERS = new LinkedHashMap<>(5);
		
		TimeChangerStruggleClient.registerCycleBuilder(new SystemTime.Builder());
		TimeChangerStruggleClient.registerCycleBuilder(new StaticTime.Builder());
		TimeChangerStruggleClient.registerCycleBuilder(new MovingTime.Builder());
		TimeChangerStruggleClient.registerCycleBuilder(new RandomizedTime.Builder());
		// TimeChangerStruggleClient.registerCycleBuilder(new ShrugTime.Builder()); (will not be used in initial release since it does nothing)
		TimeChangerStruggleClient.registerCycleBuilder(new LowToHighHeightTime.Builder());
	}
	
	public static <B extends DayNightCycleBuilder> void registerCycleBuilder(B builder)
	{
		String key = builder.getKeyName();
		
		if (TimeChangerStruggleClient.CYCLE_BUILDERS.containsKey(key))
			return;
		
		if (TimeChangerStruggleClient.timeChangerKeyFirst == null)
			TimeChangerStruggleClient.timeChangerKeyFirst = key;
		
		TimeChangerStruggleClient.timeChangerKeyLast = key;
			
		TimeChangerStruggleClient.CYCLE_BUILDERS.put(key, builder);
	}
	

	
	
	
	
	public static final DayNightCycleBasis getTimeChanger() {
		return TimeChangerStruggleClient.timeChanger;
	}
	public static final String getTimeChangerKey() {
		return TimeChangerStruggleClient.timeChangerKey;
	}
	public static final void setTimeChanger(DayNightCycleBasis timeToChange) 
	{
		TimeChangerStruggleClient.timeChanger = timeToChange;
		
		Optional<DayNightCycleBuilder> cachedCycleBuilder = (timeToChange == null) ? Optional.empty() :
			TimeChangerStruggleClient.getCachedCycleBuilderByClass(timeToChange.getBuilderClass());
		
		TimeChangerStruggleClient.timeChangerKey = cachedCycleBuilder.isPresent() ? 
			cachedCycleBuilder.get().getKeyName() : null;
	}
	public static final void setTimeChanger(String cycleType) 
	{
		if (!TimeChangerStruggleClient.cachedCycleTypeExists(cycleType)) {
			return;
		}
		
		TimeChangerStruggleClient.timeChanger = TimeChangerStruggleClient.CYCLE_BUILDERS.get(cycleType).create();
		TimeChangerStruggleClient.timeChangerKey = cycleType;
	}
	public static final boolean useWorldTime() {
		return TimeChangerStruggleClient.worldTime || TimeChangerStruggleClient.timeChanger == null;
	}
	public static final Collection<DayNightCycleBuilder> getCachedCycleTypeBuilders() {
		return TimeChangerStruggleClient.CYCLE_BUILDERS.values();
	}
	public static final int getCachedCycleTypeSize() {
		return TimeChangerStruggleClient.CYCLE_BUILDERS.size();
	}
	public static final boolean cachedCycleTypeExists(String cycleTypeToCheck) 
	{
		return cycleTypeToCheck != null && !cycleTypeToCheck.isEmpty() &&
			TimeChangerStruggleClient.CYCLE_BUILDERS.containsKey(cycleTypeToCheck);
	}
	public static final void quickSwitchCachedCycleType(boolean backwards)
	{
		// if our key is empty, just pick the first or last item in the map
		if (TimeChangerStruggleClient.timeChangerKey == null) 
		{
			if (backwards) {
				TimeChangerStruggleClient.setTimeChanger(TimeChangerStruggleClient.timeChangerKeyLast);
			} else {
				TimeChangerStruggleClient.setTimeChanger(TimeChangerStruggleClient.timeChangerKeyFirst);
			}
		}
		// Else, just get us a way to go for previous/next depending on our key index
		else
		{
			Iterator<String> cycleKeys = TimeChangerStruggleClient.CYCLE_BUILDERS.keySet().iterator();
			
			String previousKey = null;
			
			while (cycleKeys.hasNext())
			{
				String key = cycleKeys.next();
				
				if (TimeChangerStruggleClient.timeChangerKey.equals(key))
				{
					if (backwards)
					{
						TimeChangerStruggleClient.setTimeChanger((previousKey == null) ? 
							TimeChangerStruggleClient.timeChangerKeyLast : previousKey);
					}
					else
					{
						TimeChangerStruggleClient.setTimeChanger(cycleKeys.hasNext() ? 
							cycleKeys.next() : TimeChangerStruggleClient.timeChangerKeyFirst);
					}
					
					break;
				}
				else
				{
					previousKey = key;
				}
			}
		}
	}
	public static final Optional<DayNightCycleBuilder> getCachedCycleBuilderByClass(Class<?> builderClass) 
	{
		if (builderClass != null)
		{
			Class<?>[] builderClassInterfaces = builderClass.getInterfaces();
			
			if (builderClassInterfaces != null && builderClassInterfaces.length == 1 && 
				DayNightCycleBuilder.class.equals(builderClassInterfaces[0]))
			{
				return TimeChangerStruggleClient.CYCLE_BUILDERS.values().stream()
					.filter(b -> { return builderClass.equals(b.getClass()); }).findFirst();
			}
		}
		
		return Optional.empty();
	}
	/**
	 * Returns the current cycle's builder class if there are any.
	 * @return an optional containing nothing or the current cycle's builder.
	 */
	public static Optional<DayNightCycleBuilder> getCurrentCycleBuilder()
	{
		if (TimeChangerStruggleClient.timeChanger != null && TimeChangerStruggleClient.timeChangerKey != null)
		{
			return Optional.ofNullable(TimeChangerStruggleClient.CYCLE_BUILDERS
				.get(TimeChangerStruggleClient.timeChangerKey));
		}
		
		return Optional.empty();
	}
	public static final boolean isCycleTypeCurrentCycle(String cycleTypeToCheck) 
	{
		if (cycleTypeToCheck == null || cycleTypeToCheck.isEmpty())
			return false;
		
		final Optional<DayNightCycleBuilder> builder = TimeChangerStruggleClient.getCurrentCycleBuilder();
		return builder.isPresent() && builder.get().getKeyName().equals(cycleTypeToCheck);
	}
	
	
	
	
	
	
	@Override
	@SuppressWarnings("resource")
	public void onInitializeClient()
	{
		// Register keybindings to client
		Keybindings.registerKeybindings();

		// Load the configuration settings
		File config = new File(MinecraftClient.getInstance().runDirectory, "config");
		
		// Check if the configuration directory exists, if not then 
		// create it nonetheless
		if (!config.exists())
			config.mkdir();
		
		// Then create a file which represents the real config, the file creation
		// isn't really made unless there are user-values to write or the file
		// already exists which should also allow for reads :)
		config = new File(config, "DaylightChangerStruggle.json");
		TimeChangerStruggleClient.config = new Configuration(config);
		
		// Then create the commands...
		TimeChangerStruggleClient.commands = new Commands();
		TimeChangerStruggleClient.commands.registerCommands();
		
		// Add fabric events for use in creating a shader, keyboard detection 
		// and in ticking the cycle types
		ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStart);
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
		ClientTickEvents.END_WORLD_TICK.register(this::onWorldTick);
		
		// Then read the configs
		TimeChangerStruggleClient.config.read();
	}
	
	private void onClientStart(MinecraftClient client)
	{
		// Create my favorite shader that PvP / Cheat Clients use:
		// Chroma/Rainbow Shader :D
		if (RenderUtils.rainbowAllTheWay == null)
		{
			try {
				RenderUtils.rainbowAllTheWay = new RainbowShader();
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
	}
	
	private void onClientTick(MinecraftClient client)
	{
		// TODO: Is there a better way to call key events on press and releases without the need of ticking?
		
		if (client.currentScreen == null && client.world != null)
		{
			if (Keybindings.timeChangerMenuKey.isPressed()) {
				client.setScreen(new TimeChangerScreen());
			}
			
			final boolean previousWorldTime = TimeChangerStruggleClient.worldTime;
			while (Keybindings.toggleWorldTimeKey.wasPressed()) {
				TimeChangerStruggleClient.worldTime = !previousWorldTime;
			}
		}
		
		/*
		boolean isTCS = (client.currentScreen instanceof TimeChangerScreen);
		if (RenderUtils.rainbowAllTheWay != null && isTCS)
		{
			// final net.minecraft.client.util.Window win = client.getWindow();
			 RenderUtils.rainbowAllTheWay.aspectRatio.set((float)win.getWidth() / (float)win.getHeight());
			 RenderUtils.rainbowAllTheWay.stripeScale.set(50.0f / 2.0f * 1.0f);
			
			// Stroke Width: No use yet so far...
			 RenderUtils.rainbowAllTheWay.strokeWidth.set(18.0f);
			// Global Rainbow Shift Offset
			 RenderUtils.rainbowAllTheWay.timeOffset.set(2500.0f);
		}
		 */
	}
	private void onWorldTick(ClientWorld world)
	{
		if (TimeChangerStruggleClient.useWorldTime())
			return;
		
		TimeChangerStruggleClient.timeChanger.tick();
	}
}
