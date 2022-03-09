package jugglestruggle.timechangerstruggle.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 *
 * @author JuggleStruggle
 * @implNote
 * Created on 26-Jan-2022 Wednesday
 */
public class Keybindings
{
	public static KeyBinding timeChangerMenuKey;
	public static KeyBinding toggleWorldTimeKey;
	
	public static void registerKeybindings() 
	{
		if (Keybindings.timeChangerMenuKey != null)
			return;
		 
		Keybindings.timeChangerMenuKey = Keybindings.registerBinding("timechangermenu");
		Keybindings.toggleWorldTimeKey = Keybindings.registerBinding("toggleworldtime");
	}
	
	private static KeyBinding registerBinding(String keyName) {
		return Keybindings.registerBinding(keyName, "timechanger");
	}
	private static KeyBinding registerBinding(String keyName, String category)
	{
		return KeyBindingHelper.registerKeyBinding
		(
			new KeyBinding
			(
				"jugglestruggle.tcs.key." + keyName, 
				InputUtil.Type.KEYSYM, -1,
				"jugglestruggle.tcs.keycategory." + category
			)
		);
	}
}
