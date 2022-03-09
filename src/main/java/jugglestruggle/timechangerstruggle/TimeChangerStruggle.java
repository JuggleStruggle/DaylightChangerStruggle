package jugglestruggle.timechangerstruggle;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimeChangerStruggle implements ModInitializer 
{
	public static final String MOD_ID;
	public static final String PACKAGE_ID;
	
	public static final Logger LOGGER;
	
	static
	{
		MOD_ID = "timechangerstruggle";
		PACKAGE_ID = "jugglestruggle.timechangerstruggle";
		
		LOGGER = LogManager.getLogger("TimeChangerStruggle");
	}
	

	@Override
	public void onInitialize() {}
}
