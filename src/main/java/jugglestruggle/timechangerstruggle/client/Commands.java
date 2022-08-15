package jugglestruggle.timechangerstruggle.client;

import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.config.property.LongValue;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBuilder;
import jugglestruggle.timechangerstruggle.util.DaylightUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.UnaryOperator;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

/**
 * 
 *
 * @author JuggleStruggle
 * @implNote Created on 27-Feb-2022, Sunday
 */
@Environment(EnvType.CLIENT)
public class Commands
{
	private boolean commandsRegistered;
	
	public void registerCommands()
	{
		if (this.commandsRegistered)
			return;
		
		ClientCommandRegistrationCallback.EVENT.register(this::register);
	}
	

    private void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess access)
    {
        LiteralCommandNode<FabricClientCommandSource> baseCommand = dispatcher.register(this.getBaseCommand());

        for (String alias : this.getAliases()) {
            dispatcher.register(ClientCommandManager.literal(alias).redirect(baseCommand));
        }
        
        this.commandsRegistered = true;
    }
	
	public LiteralArgumentBuilder<FabricClientCommandSource> getBaseCommand()
	{
		LiteralArgumentBuilder<FabricClientCommandSource> base = ClientCommandManager.literal("daylightchanger");
		
		base.executes(this::displayWorldTime);
		
		//
		// World Time: Toggle between world time and user time
		//
		base.then
		(
			ClientCommandManager.literal("worldtime")
			.executes(new WorldTimeCommand(true)).then
			(
				ClientCommandManager.argument("enabled", BoolArgumentType.bool())
				.executes(new WorldTimeCommand(false))
			)
		);
		
		//
		// Cycles: Gives the user a list of available cycles and modify their option or use them
		//
		base.then(this.generateCycleSubcommand());
		
		//
		// Option: A list of options which does not fit as a command itself (like World Time)
		//
		base.then(this.generateOptionSubcommand());
		
		//
		// Time: Quick-way to use Static Time elements and is changed to if done so,
		// just not directly when using "time" subcommand alone
		//
		base.then
		(
			ClientCommandManager.literal("time")
			.executes(this::displayWorldTime)
			.then(StaticTimeSetCommand.addSubtimeCommands(StaticTimeMode.ADD))
			.then(StaticTimeSetCommand.addSubtimeCommands(StaticTimeMode.SET))
			.then(StaticTimeSetCommand.addSubtimeCommands(StaticTimeMode.REMOVE))
		);

		return base;
	}
	public Set<String> getAliases() {
		return ImmutableSet.of("dcs", "tcs", "timechanger");
	}
	
	private LiteralArgumentBuilder<FabricClientCommandSource> generateCycleSubcommand()
	{
		LiteralArgumentBuilder<FabricClientCommandSource> cycleSubcommand = ClientCommandManager.literal("cycle");
		
		cycleSubcommand.executes
		(
			ctx -> 
			{
				// Lists all of the cycle types into the chat; neatly organized
				// and formatted!
				TimeChangerStruggleClient.getCachedCycleTypeBuilders().forEach(cycle -> 
				{
					MutableText options = Text.literal("[\u26A1]");
					MutableText useCycle = Text.literal("[\u2192]");
					
					final String baseName = cycle.getKeyName();
					final Text displayName = cycle.getTranslatableName();
					boolean isCurrentCycle = TimeChangerStruggleClient.isCycleTypeCurrentCycle(baseName);
					
					useCycle.styled(style -> 
					{
						return style
							.withColor(isCurrentCycle ? 0xFF5511 : 0x55FF11)
							.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tcs cycle "+baseName))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
								Text.translatable("jugglestruggle.tcs.cmd.cycle.listing.use", displayName)))
							.withBold(true);
					});
					
					options.styled(style -> 
					{
						Style currentStyle = style.withBold(true)
							.withColor(cycle.hasOptionsToEdit() ? 0xFFDD00 : 0x666666);
						
						if (cycle.hasOptionsToEdit())
						{
							currentStyle = currentStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
								Text.translatable("jugglestruggle.tcs.cmd.cycle.listing.option", displayName)))
							.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tcs cycle "+baseName+" option"));
						}
							
						return currentStyle;
					});
					
					MutableText displayNameAsDisplay = displayName.copy();
					
					displayNameAsDisplay.styled(style -> 
					{
						Text displayDesc = cycle.getTranslatableDescription();
						
						if (displayDesc == null)
							return style;
						else
							return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, displayDesc));
					});
					
					Commands.sendTextToChat(ctx, Text.translatable("%1$s %2$s %3$s", options, useCycle, displayNameAsDisplay));
				});
				
				
				return 1;
			}
		);
		
		cycleSubcommand.then
		(
			ClientCommandManager.literal("remove")
			.executes(ctx -> 
			{
				if (TimeChangerStruggleClient.getTimeChanger() == null)
				{
					Commands.sendTextToChat
					(
						ctx, style -> style.withColor(0xFF2222).withBold(true),
						"jugglestruggle.tcs.cmd.cycle.remove.use.error"
					);
					
					return 0;
				}
				
				Optional<DayNightCycleBuilder> previousCycle = 
					TimeChangerStruggleClient.getCurrentCycleBuilder();
				
				TimeChangerStruggleClient.setTimeChanger((DayNightCycleBasis)null);
				TimeChangerStruggleClient.config.writeIfModified();
				
				final MutableText cycleRemovedText = Text.translatable
					("jugglestruggle.tcs.cmd.cycle.cycle", previousCycle.isPresent() ? previousCycle.get().getTranslatableName() : "??");
				final MutableText worldTimeText = Text.translatable("jugglestruggle.tcs.screen.toggleworldtime");
				
				cycleRemovedText.styled(style -> style.withColor(0xFFDD33));
				worldTimeText.styled(style -> style.withColor(0xFFCC22));
				
				Commands.sendTextToChat
				(
					ctx, style -> style.withColor(0x22FF22).withBold(true),
					"jugglestruggle.tcs.cmd.cycle.remove.use",
					cycleRemovedText, worldTimeText
				);
				
				return 1;
			})
		);
		
		Iterator<DayNightCycleBuilder> cycleTypes = TimeChangerStruggleClient.getCachedCycleTypeBuilders().iterator();
//		boolean temp1 = TimeChangerStruggleClient.getTimeChanger() != null && TimeChangerStruggleClient.getTimeChangerKey() != null;
			
		while (cycleTypes.hasNext())
		{
			final DayNightCycleBuilder cycle = cycleTypes.next();
			final String cycleName = cycle.getKeyName();
			
			if (cycleName.toLowerCase(Locale.ROOT).equals("remove")) {
				continue;
			}
			
			LiteralArgumentBuilder<FabricClientCommandSource> cycleArg = ClientCommandManager.literal(cycleName);
			
			cycleArg.executes(new CycleUseCommand(cycle));
			
			cycleArg.then(ClientCommandManager.literal("use").executes(new CycleUseCommand(cycle)));
			
			// Only add "option" if the cycle supports or has any options
			// Unfortunately, if there were a way to load the configs related to the cycle once we need them then
			// we wouldn't already create an artificial barrier for this
//			if (cycle.hasOptionsToEdit() && (temp1 && cycle.getKeyName().equals(TimeChangerStruggleClient.getTimeChangerKey())))
			if (cycle.hasOptionsToEdit())
			{
				LiteralArgumentBuilder<FabricClientCommandSource> cycleOptionArg = ClientCommandManager.literal("option");
				
				cycleOptionArg.executes(ctx -> 
				{
					// TODO: This doesn't work when executing it directly; but from clicking in the chat logs/history for some weird reason does.
					ctx.getSource().getClient().setScreen(new TimeChangerScreen(cycle));
					return 1;
				});
				
				// Unfortunately, attempting to register command options is met with futile attempts as
				// they are registered during mod initialization and not when the user types the command
				// which is a bummer especially considering that options are a runtime thing and are not
				// pre-generated. 
				//
				// So the solution at the moment is to open the screen for an specific option and
				// check if the cycle supports pregenerated options.
				
				/*
				if (!cycle.hasDynamicOptions())
				{
					DayNightCycleBasis cycleBasis;
					
//					if (temp1 && cycle.getKeyName().equals(TimeChangerStruggleClient.getTimeChangerKey()))
//					{
//						cycleBasis = TimeChangerStruggleClient.getTimeChanger();
//					}
//					else
//					{
						cycleBasis = cycle.create();
						
						// Load its options from config; this is perhaps such a bad idea straight off the bat, so maybe we will have to
						// resort to being sure that the actual cycle is loaded to avoid config creation on a bunch of cycles that we
						// will likely never use on one run
						TimeChangerStruggleClient.config.createOrModifyDaylightCycleConfig(cycleBasis, false);
//					}
						
					// Then create our properties and make sure that there are actual properties; not just fancy sections 
					// as that are not going to be used in a command-like setting
					Set<BaseProperty<?, ?>> cycleProps = cycleBasis.createProperties();
					
					if (cycleProps != null && !cycleProps.isEmpty())
					{
						boolean nonSectionsAdded = false;
						
						
						for (BaseProperty<?, ?> prop : cycleProps)
						{
							if (prop instanceof FancySectionProperty)
								continue;
							
	
							LiteralArgumentBuilder<FabricClientCommandSource> propArg = 
								ClientCommandManager.literal(prop.property());
							
							if (prop.onCommandOptionNoValueShouldBeExecuted())
							{
								propArg.executes(ctx -> 
								{
									int returnValue = prop.onCommandOptionNoValueExecute(ctx);
									
									if (returnValue > 1)
									{
										cycleBasis.writePropertyValueToCycle(prop);
										
										if (returnValue > 2)
										{
											TimeChangerStruggleClient.config.createOrModifyDaylightCycleConfig(cycleBasis, true);
											TimeChangerStruggleClient.config.writeIfModified();
										}
									}
									
									return returnValue;
								});
							}
							
							propArg.then
							(
								ClientCommandManager.argument("value", prop.onCommandOptionGetArgType())
								.executes(ctx -> 
								{
									int returnValue = prop.onCommandOptionWithValueExecute(ctx);
									
									if (returnValue > 1)
									{
										cycleBasis.writePropertyValueToCycle(prop);
										
										if (returnValue > 2)
										{
											TimeChangerStruggleClient.config.createOrModifyDaylightCycleConfig(cycleBasis, true);
											TimeChangerStruggleClient.config.writeIfModified();
										}
									}
									
									return returnValue;
								})
							);
							
							
							cycleOptionArg.then(propArg);
							
							if (!nonSectionsAdded)
								nonSectionsAdded = true;
						}
					}
				}
				 */
					
				cycleArg.then(cycleOptionArg);
			}
			
			cycleSubcommand.then(cycleArg);
		}
		
		return cycleSubcommand;
	}
	
	private LiteralArgumentBuilder<FabricClientCommandSource> generateOptionSubcommand()
	{
		return ClientCommandManager.literal("option")
			.then(this.generateOptionSubcommandBoolAction
			(
				"dateOverTicks", 
				Text.translatable("jugglestruggle.tcs.screen.toggledate"), 
				() -> TimeChangerStruggleClient.dateOverTicks, 
				currentValue -> TimeChangerStruggleClient.dateOverTicks = currentValue
			))
			.then(this.generateOptionSubcommandBoolAction
			(
				"butterySmoothCycle", 
				Text.translatable("jugglestruggle.tcs.screen.togglesmoothbutterdaylightcycle"), 
				() -> TimeChangerStruggleClient.smoothButterCycle, 
				currentValue -> TimeChangerStruggleClient.smoothButterCycle = currentValue
			))
			.then(this.generateOptionSubcommandBoolAction
			(
				"disableNightVisionEffect", 
				Text.translatable("jugglestruggle.tcs.cmd.option.disablenightvision"), 
				() -> TimeChangerStruggleClient.disableNightVisionEffect, 
				currentValue -> TimeChangerStruggleClient.disableNightVisionEffect = currentValue
			))
			.then(this.generateOptionSubcommandBoolAction
			(
				"disableWorldTimeOnCycleUsage", 
				Text.translatable("jugglestruggle.tcs.cmd.option.disableworldtimeoncycleusage"), 
				() -> TimeChangerStruggleClient.commandsDisableWorldTimeOnCycleUsage, 
				currentValue -> TimeChangerStruggleClient.commandsDisableWorldTimeOnCycleUsage = currentValue
			))
			.then(this.generateOptionSubcommandBoolAction
			(
				"commandFeedbackOnLessImportant", 
				Text.translatable("jugglestruggle.tcs.cmd.option.commandfeedbackonlessimportant"), 
				() -> TimeChangerStruggleClient.commandsCommandFeedbackOnLessImportant, 
				currentValue -> TimeChangerStruggleClient.commandsCommandFeedbackOnLessImportant = currentValue
			));
	}
	
	private LiteralArgumentBuilder<FabricClientCommandSource> generateOptionSubcommandBoolAction
	(String subcommandName, Text displayName, BooleanSupplier suppliedValue, UnaryOperator<Boolean> onApplyConsumer)
	{
		LiteralArgumentBuilder<FabricClientCommandSource> subcommand = ClientCommandManager.literal(subcommandName);
		
		subcommand.executes(ctx -> 
		{
			final boolean newValue = onApplyConsumer.apply(!suppliedValue.getAsBoolean());
			TimeChangerStruggleClient.config.writeIfModified();
			
			Commands.sendTextToChat(ctx, "jugglestruggle.tcs.cmd.option.set", 
				displayName, ScreenTexts.onOrOff(newValue));
			
			return 1;
		});
		
		subcommand.then
		(
			ClientCommandManager.argument("enable", BoolArgumentType.bool())
			.executes(ctx ->
			{
				final boolean previousValue = suppliedValue.getAsBoolean();
				final boolean newValue = onApplyConsumer.apply(BoolArgumentType.getBool(ctx, "enable"));
				
				final boolean prevAndNewValueEquals = previousValue == newValue;
				
				Commands.sendTextToChat
				(
					ctx, "jugglestruggle.tcs.cmd.option.set" + (prevAndNewValueEquals ? ".error.equals" : ""), 
					displayName, ScreenTexts.onOrOff(newValue)
				);
				
				if (prevAndNewValueEquals)
					return 0;
				
				TimeChangerStruggleClient.config.writeIfModified();
				
				return 1;
			})
		);
		
		return subcommand;
	}
	
	private int displayWorldTime(CommandContext<FabricClientCommandSource> ctx) 
	{
		World w = ctx.getSource().getWorld();
		
		Commands.sendTextToChat
		(
			ctx, style -> style.withColor(0xFFDD22),
			"jugglestruggle.tcs.cmd.time.use", 
			w.getTimeOfDay(), DaylightUtils.getParsedTime(w, true)
		);
		
		return 1;
	}
	public static void sendTextToChat(CommandContext<FabricClientCommandSource> ctx, String key, Object... args) {
		Commands.sendTextToChat(ctx, null, key, args);
	}
	public static void sendTextToChat(CommandContext<FabricClientCommandSource> ctx, UnaryOperator<Style> styleUpdater, String key, Object... args)
	{
		final MutableText text = (args == null || args.length <= 0) ? 
			Text.translatable(key) : Text.translatable(key, args);
		
		if (styleUpdater != null) {
			text.styled(styleUpdater);
		}
		
		Commands.sendTextToChat(ctx, text);
	}
	@SuppressWarnings("resource")
	public static void sendTextToChat(CommandContext<FabricClientCommandSource> ctx, Text text) {
		ctx.getSource().getClient().inGameHud.getChatHud().addMessage(text);
	}
	
	// Don't mind the silly method wording
	static void cycleSetAndWeNeedToKnowIfWeCanDisableWorldTime() 
	{
		if (TimeChangerStruggleClient.commandsDisableWorldTimeOnCycleUsage)
			TimeChangerStruggleClient.worldTime = false;
	}
	static String getStarterCommand(CommandContext<FabricClientCommandSource> ctx) {
		return ctx.getLastChild().getInput().split(" ", 2)[0];
	}

	private class WorldTimeCommand implements Command<FabricClientCommandSource>
	{
		final boolean toggleMode;

		public WorldTimeCommand(boolean toggleMode) {
			this.toggleMode = toggleMode;
		}
		
		@Override
		public int run(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException
		{
			boolean setWorldTime = this.toggleMode ? 
				!TimeChangerStruggleClient.worldTime : BoolArgumentType.getBool(ctx, "enabled");
			
			if (setWorldTime == TimeChangerStruggleClient.worldTime) {
				
				return 0;
			}
			
			TimeChangerStruggleClient.worldTime = setWorldTime;
			
			
			if (TimeChangerStruggleClient.useWorldTime() && setWorldTime == false)
			{
				final String starterCommand = Commands.getStarterCommand(ctx);
				final String realCmd = String.format("/%1$s cycle", starterCommand);
				final String langCmd = "jugglestruggle.tcs.cmd.worldtime.set.warn";
				
				MutableText clickableText = Text.literal(realCmd);
				
				clickableText.styled(style -> {
					return style.withColor(0xDD44FF).withUnderline(true).withBold(false)
						.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, realCmd))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable(langCmd + ".hover")));
				});
				
				Commands.sendTextToChat
				(
					ctx, style -> style.withColor(0xFFDD00).withBold(true),
					langCmd, clickableText
				);
			}
			else if (TimeChangerStruggleClient.commandsCommandFeedbackOnLessImportant) 
			{
				Commands.sendTextToChat
				(
					ctx, style -> style.withColor(0x44FF00).withBold(true),
					"jugglestruggle.tcs.cmd.worldtime.set",
					ScreenTexts.onOrOff(TimeChangerStruggleClient.worldTime)
				);
			}
			
			return 1;
		}
	}
	
	private class CycleUseCommand implements Command<FabricClientCommandSource>
	{
		final DayNightCycleBuilder cycleToUse;

		public CycleUseCommand(DayNightCycleBuilder cycle) {
			this.cycleToUse = cycle;
		}
		
		@Override
		public int run(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException
		{
			final String langCmd = "jugglestruggle.tcs.cmd.cycle.";
			final Optional<DayNightCycleBuilder> previousCycle = 
				TimeChangerStruggleClient.getCurrentCycleBuilder();
			
			if (previousCycle.isPresent() && previousCycle.get().getKeyName().equals(this.cycleToUse.getKeyName()))
			{
				MutableText cycleText = Text.translatable(langCmd+"cycle", 
					this.cycleToUse.getTranslatableName().copy().styled(style -> style.withColor(0xFF22FF)));
				
				Commands.sendTextToChat
				(
					ctx, style -> style.withColor(0xFF2222).withBold(true),
					langCmd + "use.error.inuse", cycleText
				);
				
				return 0;
			}
			
			DayNightCycleBasis createdCycle = this.cycleToUse.create();
			TimeChangerStruggleClient.setTimeChanger(createdCycle);
			TimeChangerStruggleClient.config.createOrModifyDaylightCycleConfig(createdCycle, false);

			Commands.cycleSetAndWeNeedToKnowIfWeCanDisableWorldTime();
			
			TimeChangerStruggleClient.config.writeIfModified();
			
			if (TimeChangerStruggleClient.commandsCommandFeedbackOnLessImportant)
			{
				MutableText cycleText = Text.translatable(langCmd+"cycle", 
					this.cycleToUse.getTranslatableName().copy().styled(style -> style.withColor(0xFFFF22)));
				
				Commands.sendTextToChat
				(
					ctx, style -> style.withColor(0x22FF22).withBold(true),
					langCmd + "use.success", cycleText
				);
			}
			
			return 1;
		}
	}
	

	static class StaticTimeSetCommand implements Command<FabricClientCommandSource>
	{
		public static LiteralArgumentBuilder<FabricClientCommandSource> addSubtimeCommands(final StaticTimeMode mode)
		{
			final LiteralArgumentBuilder<FabricClientCommandSource> base = ClientCommandManager.literal(mode.name().toLowerCase(Locale.ROOT))
			.then
			(
				ClientCommandManager.argument("time", TimeArgumentType.time())
				.executes(new StaticTimeSetCommand(null, mode))
			);
			
			/*
			.then(ClientCommandManager.literal(    "noon").executes(new StaticTimeSetCommand( 6000L, mode)))
			.then(ClientCommandManager.literal("midnight").executes(new StaticTimeSetCommand(18000L, mode)))
			.then(ClientCommandManager.literal( "sunrise").executes(new StaticTimeSetCommand(    0L, mode)))
			.then(ClientCommandManager.literal(  "sunset").executes(new StaticTimeSetCommand(12000L, mode)))
			.then(ClientCommandManager.literal(     "day").executes(new StaticTimeSetCommand( 1000L, mode)))
			.then(ClientCommandManager.literal(   "night").executes(new StaticTimeSetCommand(13000L, mode)));
			 */
			
			Lists.newArrayList(jugglestruggle.timechangerstruggle.daynight.type.StaticTime.PresetSetTimes.values())
			.stream().filter(presetTime -> presetTime.shouldShowInCommand()).forEach(presetTime -> {
				base.then(ClientCommandManager.literal(presetTime.name().toLowerCase(Locale.ROOT))
					.executes(new StaticTimeSetCommand(presetTime.getTime(), mode)));
			});
			
			return base;
		}
		
		final Long timeToSet;
		final StaticTimeMode mode;
		
		public StaticTimeSetCommand(Long timeToSet, StaticTimeMode mode) {
			this.timeToSet = timeToSet; this.mode = mode;
		}
		
		@Override
		public int run(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException
		{
			long timeTicks = (this.timeToSet == null) ? (long)IntegerArgumentType.getInteger(ctx, "time") : this.timeToSet;
			
			if (TimeChangerStruggleClient.cachedCycleTypeExists("statictime")) 
			{
				if (!TimeChangerStruggleClient.isCycleTypeCurrentCycle("statictime")) 
				{
					Optional<DayNightCycleBuilder> cycleBuilder = 
						TimeChangerStruggleClient.getCurrentCycleBuilder();
					
					if (TimeChangerStruggleClient.commandsCommandFeedbackOnLessImportant)
					{
						Commands.sendTextToChat
						(
							ctx, style ->  style.withColor(0xFFDD44),
							"jugglestruggle.tcs.cmd.time.replacedto", 
							
							cycleBuilder.isPresent() ? cycleBuilder.get().getTranslatableName() :
							Text.translatable("jugglestruggle.tcs.screen.switchcyclemenu.desc.using.none"),
							Text.translatable("jugglestruggle.tcs.dnt.statictime")
						);
					}
					
					TimeChangerStruggleClient.setTimeChanger("statictime");
				}
				
				jugglestruggle.timechangerstruggle.daynight.type.StaticTime staticTime = 
					(jugglestruggle.timechangerstruggle.daynight.type.StaticTime)TimeChangerStruggleClient.getTimeChanger();
				
				final World w = ctx.getSource().getWorld();
				final long previousTimeOfDay = w.getTimeOfDay();
				
				long totalTimeOfDay;
				long timeToActuallySet;
				
				switch (this.mode)
				{
					case SET:
					{
						totalTimeOfDay = 0;
						timeToActuallySet = timeTicks;
						
						break;
					}
					case ADD:
					{
						timeToActuallySet = totalTimeOfDay = 
							staticTime.timeSet + timeTicks;
						
						break;
					}
					case REMOVE:
					{
						timeToActuallySet = totalTimeOfDay = 
							staticTime.timeSet - timeTicks;
						
						break;
					}
					
					default:
					{
						totalTimeOfDay = 0;
						timeToActuallySet = staticTime.timeSet;
						break;
					}
				}

				staticTime.writePropertyValueToCycle(new LongValue("worldtime", timeToActuallySet, null, null));
				
				Commands.cycleSetAndWeNeedToKnowIfWeCanDisableWorldTime();
				
				TimeChangerStruggleClient.config.createOrModifyDaylightCycleConfig(staticTime, true);
				TimeChangerStruggleClient.config.writeIfModified();
				
				if (TimeChangerStruggleClient.commandsCommandFeedbackOnLessImportant)
				{
					final String starterCommand = Commands.getStarterCommand(ctx);
					final String langCmd = "jugglestruggle.tcs.cmd.time.";
					final String modeName = this.mode.name().toLowerCase(Locale.ROOT);
					
					MutableText myTimeTicks = Text.translatable(langCmd + "ticks", timeTicks);
					MutableText totalTicks = Text.translatable(langCmd + "ticks", totalTimeOfDay);
					MutableText prevTicks = Text.translatable(langCmd + "ticks", previousTimeOfDay);
					
					myTimeTicks.styled(style ->
						style.withColor(0xFFDD22).withBold(false).withUnderline(true)
						.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
							String.format("/%1$s time %2$s %3$s", starterCommand, modeName, timeTicks)))
					);
					totalTicks.styled(style ->
						style.withColor(0xFF22DD).withBold(false).withUnderline(true)
						.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
							String.format("/%1$s time set %2$s", starterCommand, totalTimeOfDay)))
						);
					prevTicks.styled(style ->
						style.withColor(0x22DDFF).withBold(false).withUnderline(true)
						.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, 
							String.format("/%1$s time set %2$s", starterCommand, previousTimeOfDay)))
					);
					
					Commands.sendTextToChat
					(
						ctx, style -> style.withColor(0x22FF22).withBold(true), 
						langCmd + modeName, myTimeTicks, totalTicks, prevTicks
					);
				}
				
				return 1;
			}
			else
			{
				
				Commands.sendTextToChat
				(
					ctx, style -> {
						return style.withColor(0xFF2222).withBold(true);
					}, 
					"jugglestruggle.tcs.cmd.time.error.statictimenotfound", 
					new Object[0]
				);
				
				return 0;
			}
		}
	}
	
	public enum StaticTimeMode
	{
		SET, ADD, REMOVE
	}
}
