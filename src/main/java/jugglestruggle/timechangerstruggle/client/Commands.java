package jugglestruggle.timechangerstruggle.client;

import jugglestruggle.timechangerstruggle.client.screen.TimeChangerScreen;
import jugglestruggle.timechangerstruggle.config.property.LongValue;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis.PropertyWriterSource;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBuilder;
import jugglestruggle.timechangerstruggle.util.DaylightUtils;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import net.minecraft.client.MinecraftClient;
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
 * @author JuggleStruggle
 * @implNote Created on 27-Feb-2022, Thursday
 */
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
            dispatcher.register(ClientCommands.literal(alias).redirect(baseCommand));
        }
        
        this.commandsRegistered = true;
    }
	
	public LiteralArgumentBuilder<FabricClientCommandSource> getBaseCommand()
	{
		LiteralArgumentBuilder<FabricClientCommandSource> base = ClientCommands.literal("daylightchanger");
		
		base.executes(this::displayWorldTime);
		
		// World Time: Toggle between world time and user time
		base.then
		(
			ClientCommands.literal("worldtime")
			.executes(new WorldTimeCommand(true)).then
			(
				ClientCommands.argument("enabled", BoolArgumentType.bool())
				.executes(new WorldTimeCommand(false))
			)
		);
		
		// Cycles: Gives the user a list of available cycles and modify their option or use them
		base.then(this.generateCycleSubcommand());
		
		// Option: A list of options which does not fit as a command itself (like World Time)
		base.then(this.generateOptionSubcommand());
		
		// Time: Quick-way to use Static Time elements and is changed to if done so,
		// just not directly when using "time" subcommand alone
		base.then
		(
			ClientCommands.literal("time")
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
		LiteralArgumentBuilder<FabricClientCommandSource> cycleSubcommand = ClientCommands.literal("cycle");
		
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
							.withClickEvent(new ClickEvent.RunCommand("/tcs cycle "+baseName))
							.withHoverEvent(new HoverEvent.ShowText(Text.translatable
								("jugglestruggle.tcs.cmd.cycle.listing.use", displayName)))
							.withBold(true);
					});
					
					options.styled(style -> 
					{
						Style currentStyle = style.withBold(true)
							.withColor(cycle.hasOptionsToEdit() ? 0xFFDD00 : 0x666666);
						
						if (cycle.hasOptionsToEdit())
						{
							currentStyle = currentStyle.withHoverEvent(new HoverEvent.ShowText
								(Text.translatable("jugglestruggle.tcs.cmd.cycle.listing.option", displayName)))
							.withClickEvent(new ClickEvent.RunCommand("/tcs cycle "+baseName+" option"));
						}
							
						return currentStyle;
					});
					
					MutableText displayNameAsDisplay = displayName.copy();
					
					displayNameAsDisplay.styled(style -> 
					{
						Text displayDesc = cycle.getTranslatableDescription();
						return (displayDesc == null) ? style : style.withHoverEvent(new HoverEvent.ShowText(displayDesc));
					});
					
					Commands.sendTextToChat(ctx, Text.translatable("%1$s %2$s %3$s", options, useCycle, displayNameAsDisplay));
				});
				
				
				return 1;
			}
		);
		
		cycleSubcommand.then
		(
			ClientCommands.literal("remove")
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
				
				final MutableText cycleRemovedText = Text.translatable("jugglestruggle.tcs.cmd.cycle.cycle", 
					previousCycle.isPresent() ? previousCycle.get().getTranslatableName() : "??");
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
			
		while (cycleTypes.hasNext())
		{
			final DayNightCycleBuilder cycle = cycleTypes.next();
			final String cycleName = cycle.getKeyName();
			
			if (cycleName.toLowerCase(Locale.ROOT).equals("remove"))
				continue;
			
			LiteralArgumentBuilder<FabricClientCommandSource> cycleArg = ClientCommands.literal(cycleName);
			
			cycleArg.executes(new CycleUseCommand(cycle));
			
			cycleArg.then(ClientCommands.literal("use").executes(new CycleUseCommand(cycle)));
			
			// Only add "option" if the cycle supports or has any options
			// Unfortunately, if there were a way to load the configs related to the cycle once we need them then
			// we wouldn't already create an artificial barrier for this
			if (cycle.hasOptionsToEdit())
			{
				LiteralArgumentBuilder<FabricClientCommandSource> cycleOptionArg = ClientCommands.literal("option");
				
				cycleOptionArg.executes(ctx -> 
				{
					// TODO: This doesn't work when executing it directly. This is as a result of the chat history
					// screen clearing its own screen when the TCS screen is set before that happens and as a result,
					// it seems like nothing happened. Clicking on the chat history opens this screen without any issues.
					MinecraftClient client = ctx.getSource().getClient();
					client.guiManager.setScreen(new TimeChangerScreen(client.guiManager.getCurrentScreen(), cycle));
					
					return 1;
				});
					
				cycleArg.then(cycleOptionArg);
			}
			
			cycleSubcommand.then(cycleArg);
		}
		
		return cycleSubcommand;
	}
	
	private LiteralArgumentBuilder<FabricClientCommandSource> generateOptionSubcommand()
	{
		return ClientCommands.literal("option")
			.then(this.generateOptionSubcommandBoolAction
			(
				"dateOverTicks", 
				Text.translatable("jugglestruggle.tcs.screen.toggledate"), 
				() -> TimeChangerStruggleClient.dateOverTicks, 
				v -> TimeChangerStruggleClient.dateOverTicks = v, null
			))
			.then(this.generateOptionSubcommandBoolAction
			(
				"butterySmoothCycle", 
				Text.translatable("jugglestruggle.tcs.screen.togglesmoothbutterdaylightcycle"), 
				() -> TimeChangerStruggleClient.smoothButterCycle, 
				v -> TimeChangerStruggleClient.smoothButterCycle = v, null
			))
			.then(this.generateOptionSubcommandBoolAction
			(
				"disableNightVisionEffect", 
				Text.translatable("jugglestruggle.tcs.cmd.option.disablenightvision"), 
				() -> TimeChangerStruggleClient.disableNightVisionEffect, 
				v -> TimeChangerStruggleClient.disableNightVisionEffect = v, null
			))
			.then(this.generateOptionSubcommandBoolAction
			(
				"disableWorldTimeOnCycleUsage", 
				Text.translatable("jugglestruggle.tcs.cmd.option.disableworldtimeoncycleusage"), 
				() -> TimeChangerStruggleClient.commandsDisableWorldTimeOnCycleUsage, 
				v -> TimeChangerStruggleClient.commandsDisableWorldTimeOnCycleUsage = v, null
			))
			.then(this.generateOptionSubcommandBoolAction
			(
				"commandFeedbackOnLessImportant", 
				Text.translatable("jugglestruggle.tcs.cmd.option.commandfeedbackonlessimportant"), 
				() -> TimeChangerStruggleClient.commandsCommandFeedbackOnLessImportant, 
				v -> TimeChangerStruggleClient.commandsCommandFeedbackOnLessImportant = v, null
			))
			.then(this.generateOptionSubcommandBoolAction
			(
				"allowWorldChangeCyclesToWriteToDisk", 
				Text.translatable("jugglestruggle.tcs.cmd.option.allowworldchangecyclestowritetodisk"), 
				() -> TimeChangerStruggleClient.allowWorldChangeCyclesToWriteToDisk, 
				v -> TimeChangerStruggleClient.allowWorldChangeCyclesToWriteToDisk = v, null
			));
	}
	
	/**
	 * Generates sub-command boolean-based arguments in a more common way.
	 * 
	 * @param suppliedValue the most recent value this currently holds
	 * @param onApplyConsumer apply the new value into the option itself
	 * @param onSuccessRes invoked after everything else is done and was 
	 *        successful in changing to the new value
	 *        
	 * @return a literal argument builder with the name of the sub-command
	 */
	private LiteralArgumentBuilder<FabricClientCommandSource> generateOptionSubcommandBoolAction(String subcommandName, 
		Text displayName, BooleanSupplier suppliedValue, UnaryOperator<Boolean> onApplyConsumer, Consumer<Boolean> onSuccessRes)
	{
		LiteralArgumentBuilder<FabricClientCommandSource> subcommand = ClientCommands.literal(subcommandName);
		
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
			ClientCommands.argument("enable", BoolArgumentType.bool())
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
		World w = ctx.getSource().getLevel();
		
		Commands.sendTextToChat
		(
			ctx, s -> s.withColor(0xFFDD22),
			"jugglestruggle.tcs.cmd.time.use", 
			w.getDimensionTime(), DaylightUtils.getParsedTime(w, true)
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
		
		if (styleUpdater != null)
			text.styled(styleUpdater);
		
		Commands.sendTextToChat(ctx, text);
	}
	public static void sendTextToChat(CommandContext<FabricClientCommandSource> ctx, Text text) {
		ctx.getSource().getClient().guiManager.inGameHud.getChatHud().addClientSystemMessage(text);
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
			
			if (setWorldTime == TimeChangerStruggleClient.worldTime)
				return 0;
			
			TimeChangerStruggleClient.worldTime = setWorldTime;
			
			if (TimeChangerStruggleClient.useWorldTime() && setWorldTime == false)
			{
				final String starterCommand = Commands.getStarterCommand(ctx);
				final String realCmd = String.format("/%1$s cycle", starterCommand);
				final String langCmd = "jugglestruggle.tcs.cmd.worldtime.set.warn";
				
				MutableText clickableText = Text.literal(realCmd);
				
				clickableText.styled(style -> style.withColor(0xDD44FF)
					.withUnderline(true).withBold(false)
					.withClickEvent(new ClickEvent.RunCommand(realCmd))
					.withHoverEvent(new HoverEvent.ShowText(Text.translatable(langCmd + ".hover")))
				);
				
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
			final Optional<DayNightCycleBuilder> previousCycle = TimeChangerStruggleClient.getCurrentCycleBuilder();
			
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
			final LiteralArgumentBuilder<FabricClientCommandSource> base = ClientCommands.literal(mode.name().toLowerCase(Locale.ROOT))
			.then
			(
				ClientCommands.argument("time", TimeArgumentType.time())
				.executes(new StaticTimeSetCommand(null, mode))
			);
			
			Lists.newArrayList(jugglestruggle.timechangerstruggle.daynight.type.StaticTime.PresetSetTimes.values())
			.stream().filter(presetTime -> presetTime.shouldShowInCommand()).forEach(presetTime -> 
				base.then(ClientCommands.literal(presetTime.name().toLowerCase(Locale.ROOT))
					.executes(new StaticTimeSetCommand(presetTime.getTime(), mode)))
			);
			
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
			
			if (TimeChangerStruggleClient.cachedCycleTypeExists("statictime") || 
				TimeChangerStruggleClient.cachedCycleTypeExists("movingtime")) 
			{
				final boolean isMovingTime = TimeChangerStruggleClient.isCycleTypeCurrentCycle("movingtime");
				
				if (!(TimeChangerStruggleClient.isCycleTypeCurrentCycle("statictime") || isMovingTime)) 
				{
					Optional<DayNightCycleBuilder> cycleBuilder = TimeChangerStruggleClient.getCurrentCycleBuilder();
					
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
				
				jugglestruggle.timechangerstruggle.daynight.type.StaticTime staticTime = isMovingTime ? 
					null : (jugglestruggle.timechangerstruggle.daynight.type.StaticTime)TimeChangerStruggleClient.getTimeChanger();
				jugglestruggle.timechangerstruggle.daynight.type.MovingTime movingTime = isMovingTime ?
					(jugglestruggle.timechangerstruggle.daynight.type.MovingTime)TimeChangerStruggleClient.getTimeChanger() : null;
				
				final World w = ctx.getSource().getLevel();
				final long previousTimeOfDay = w.getDimensionTime();
				
				long totalTimeOfDay;
				long timeToActuallySet;
				
				switch (this.mode)
				{
					case SET ->
					{
						totalTimeOfDay = 0;
						timeToActuallySet = timeTicks;
					}
					case ADD ->
					{
						timeToActuallySet = totalTimeOfDay = (isMovingTime ? 
							movingTime.getCachedTime() : staticTime.timeSet) + timeTicks;
					}
					case REMOVE ->
					{
						timeToActuallySet = totalTimeOfDay = (isMovingTime ? 
							movingTime.getCachedTime() : staticTime.timeSet) - timeTicks;
					}
					
					default ->
					{
						totalTimeOfDay = 0;
						timeToActuallySet = (isMovingTime ? movingTime.getCachedTime() : staticTime.timeSet);
					}
				}

				if (isMovingTime)
					movingTime.writePropertyValueToCycle(new LongValue("cachedTime", timeToActuallySet, null, null), PropertyWriterSource.USER);
				else
					staticTime.writePropertyValueToCycle(new LongValue("worldtime", timeToActuallySet, null, null), PropertyWriterSource.USER);
				
				Commands.cycleSetAndWeNeedToKnowIfWeCanDisableWorldTime();
				
				TimeChangerStruggleClient.config.createOrModifyDaylightCycleConfig(isMovingTime ? movingTime : staticTime, true);
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
						.withClickEvent(new ClickEvent.RunCommand(String.format
							("/%1$s time %2$s %3$s", starterCommand, modeName, timeTicks)))
					);
					totalTicks.styled(style ->
						style.withColor(0xFF22DD).withBold(false).withUnderline(true)
						.withClickEvent(new ClickEvent.RunCommand(String.format
							("/%1$s time set %2$s", starterCommand, totalTimeOfDay)))
					);
					prevTicks.styled(style ->
						style.withColor(0x22DDFF).withBold(false).withUnderline(true)
						.withClickEvent(new ClickEvent.RunCommand(String.format
							("/%1$s time set %2$s", starterCommand, previousTimeOfDay)))
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
					ctx, style -> style.withColor(0xFF2222).withBold(true), 
					"jugglestruggle.tcs.cmd.time.error.statictimenotfound", 
					new Object[0]
				);
				
				return 0;
			}
		}
	}
	
	public enum StaticTimeMode {
		SET, ADD, REMOVE
	}
}
