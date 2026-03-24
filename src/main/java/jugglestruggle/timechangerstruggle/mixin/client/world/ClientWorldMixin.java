package jugglestruggle.timechangerstruggle.mixin.client.world;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;
import jugglestruggle.timechangerstruggle.daynight.DayNightGetterType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.client.world.ClientClocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;

/**
 * Client world mixin; the main class that handles our glorious time which cannot
 * be ignored upon our hands
 *
 * @author JuggleStruggle
 * @implNote Created on 26-Jan-2022, Wednesday
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World
{
    protected ClientWorldMixin() {
		super(null, null, null, null, false, false, 0, 0);
    }
    
    /**
     * Created in response to the environment attribute access taking everything at once when it
     * comes to retrieving the world clock. DCS needs to also perform an override to ensure the
     * world environment attribute held by this world can use the DCS variant when needed.
     * 
     * @implNote Introduced in v0.0.4+26.1
     */
    @Override @Overwrite
    public ClientClocks getClocks() {
    	return TimeChangerStruggleClient.dcsClock;
    }

    // Introduced in v0.0.4+26.1 
    @Override
    public long getDimensionTime() 
    {
    	if (TimeChangerStruggleClient.useWorldTime())
    		return super.getDimensionTime();

    	return TimeChangerStruggleClient.getTimeChanger().getModifiedTime(
    		(ClientWorld)(Object)this, DayNightGetterType.DEFAULT, false);
    }
}
