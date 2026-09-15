package jugglestruggle.timechangerstruggle.client;

import jugglestruggle.timechangerstruggle.TimeChangerStruggle;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

/**
 * @author JuggleStruggle
 * @implNote Created on 26-Jan-2022, Wednesday
 */
public class Keybindings
{
	public static KeyBinding timeChangerMenuKey;
	public static KeyBinding toggleWorldTimeKey;
	public static KeyBinding disableNightVisionKey;
	public static KeyBinding prevCycleKey;
	public static KeyBinding nextCycleKey;
	
	// 0.0.3+1.21.9 port: required due to vanilla changes
	// also removes the unused category naming in the meantime
	public static KeyBinding.Category dcsCategory;
	
	public static void registerKeybindings() 
	{
		if (Keybindings.timeChangerMenuKey != null)
			return;
		
		Keybindings.dcsCategory = KeyBinding.Category.create
			(Identifier.of(TimeChangerStruggle.MOD_ID, "timechanger"));
		
		Keybindings.timeChangerMenuKey = Keybindings.register("timechangermenu");
		Keybindings.toggleWorldTimeKey = Keybindings.register("toggleworldtime");
		Keybindings.disableNightVisionKey = Keybindings.register("disablenightvision");
		Keybindings.prevCycleKey = Keybindings.register("prevcycle");
		Keybindings.nextCycleKey = Keybindings.register("nextcycle");
	}
	
	private static KeyBinding register(String keyName)
	{
		return KeyBindingHelper.registerKeyBinding
		(
			new KeyBinding
			(
				"jugglestruggle.tcs.key." + keyName, 
				InputUtil.Type.KEYSYM, -1, Keybindings.dcsCategory
			)
		);
	}
}
