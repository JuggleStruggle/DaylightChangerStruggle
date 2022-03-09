package jugglestruggle.timechangerstruggle.config;

import jugglestruggle.timechangerstruggle.client.TimeChangerStruggleClient;
import jugglestruggle.timechangerstruggle.client.config.property.FancySectionProperty;
import jugglestruggle.timechangerstruggle.config.property.BaseProperty;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBasis;
import jugglestruggle.timechangerstruggle.daynight.DayNightCycleBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.compress.utils.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

/**
 *
 * @author JuggleStruggle
 * @implNote Created on 31-Jan-2022, Monday
 */
public class Configuration
{
	private JsonObject configData;
	private File configFile;
	private Gson configDataBaseGson;
	
	public Configuration(File configFile) 
	{
		this.configFile = configFile;
		
		GsonBuilder builder = new GsonBuilder();
		
		builder.setPrettyPrinting()
		       .setLenient();
		
		this.configDataBaseGson = builder.create();
	}
	
	public void read()
	{
		boolean populateConfig = false;
		boolean populateNewFile = false;

		if (this.configFile == null) {
			populateConfig = true;
		}
		else
		{
			try 
			{
				populateConfig = !(this.configFile.exists() && this.configFile.canRead());
				
				/*
				if (this.configFile.exists())
				{
					if (!this.configFile.canRead())
						populateConfig = true;
				}
				else
				{
					this.configFile.createNewFile();
					populateConfig = true;
				}
				 */
			}
			catch (Exception ex) 
			{
				ex.printStackTrace(); 
				populateConfig = true;
			}
		}
		
		if (populateConfig) 
		{
			this.checkAndPopulateConfigData();
			
			if (populateNewFile)
				this.write();
		}
		else
		{
			FileReader reader = null;
			boolean shouldPopulate = false;
			
			try
			{
				reader = new FileReader(this.configFile);
				JsonElement parsedElement = JsonParser.parseReader(reader);
				
				if (parsedElement != null && parsedElement.isJsonObject()) 
				{
					this.configData = parsedElement.getAsJsonObject();
					this.checkAndPopulateConfigData();
					
					TimeChangerStruggleClient.dateOverTicks = this.configData.get("dateOverTicks").getAsBoolean();
					TimeChangerStruggleClient.worldTime = this.configData.get("worldTime").getAsBoolean();
					TimeChangerStruggleClient.smoothButterCycle = this.configData.get("smoothButterCycle").getAsBoolean();
					TimeChangerStruggleClient.disableNightVisionEffect = this.configData.get("disableNightVisionEffect").getAsBoolean();
					TimeChangerStruggleClient.applyOnPropertyListValueUpdate = this.configData.get("applyOnPropertyListValueUpdate").getAsBoolean();
					TimeChangerStruggleClient.commandsCommandFeedbackOnLessImportant = this.configData.get("commandFeedbackOnLessImportant").getAsBoolean();
					TimeChangerStruggleClient.commandsDisableWorldTimeOnCycleUsage = this.configData.get("disableWorldTimeOnCycleUsage").getAsBoolean();
					
					JsonElement elem = this.configData.get("activeDaylightChanger");
					
					if (elem != null && elem.isJsonPrimitive()) 
					{
						String activeDaylightChangerText = elem.getAsString();
						
						if (!activeDaylightChangerText.isEmpty() && !activeDaylightChangerText.isBlank())
						{
							TimeChangerStruggleClient.setTimeChanger(elem.getAsString());
							this.createOrModifyDaylightCycleConfig(TimeChangerStruggleClient.getTimeChanger(), false);
						}
					}
				}
				else
				{
					shouldPopulate = true;
				}
			} 
			catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally  {
				IOUtils.closeQuietly(reader);
			}
			
			if (shouldPopulate) {
				this.checkAndPopulateConfigData();
			}
		}
	}
	
	public void write()
	{
		this.configData.addProperty("worldTime", TimeChangerStruggleClient.worldTime);
		this.configData.addProperty("dateOverTicks", TimeChangerStruggleClient.dateOverTicks);
		this.configData.addProperty("smoothButterCycle", TimeChangerStruggleClient.smoothButterCycle);
		this.configData.addProperty("disableNightVisionEffect", TimeChangerStruggleClient.disableNightVisionEffect);
		this.configData.addProperty("applyOnPropertyListValueUpdate", TimeChangerStruggleClient.applyOnPropertyListValueUpdate);
		this.configData.addProperty("commandFeedbackOnLessImportant", TimeChangerStruggleClient.commandsCommandFeedbackOnLessImportant);
		this.configData.addProperty("disableWorldTimeOnCycleUsage", TimeChangerStruggleClient.commandsDisableWorldTimeOnCycleUsage);
		
		this.configData.addProperty("activeDaylightChanger", (TimeChangerStruggleClient.getTimeChangerKey() == null) ? "" : TimeChangerStruggleClient.getTimeChangerKey());
		
		if (this.configFile == null)
			return;
		
		FileWriter writer = null;
		JsonWriter jsonWriter = null;
		
		try
		{
			if (this.configFile.exists()) {
				this.configFile.delete();
			}
			
			this.configFile.createNewFile();
			
			writer = new FileWriter(this.configFile);
			jsonWriter = new JsonWriter(writer);
			
			this.configDataBaseGson.toJson(this.configData, jsonWriter);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		finally
		{
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(jsonWriter);
		}
		
//		if (this.configFile.)
	}
	

	/**
	 * Attempts to write only if the properties on memory aren't their default
	 * setting. This is to avoid writes that aren't needed. 
	 * 
	 * <p> This might, in the future, be expanded to account for user changes 
	 * that haven't really occurred from a game session.
	 */
	public void writeIfModified()
	{
		if (TimeChangerStruggleClient.worldTime != true || 
			TimeChangerStruggleClient.dateOverTicks != false ||
			TimeChangerStruggleClient.applyOnPropertyListValueUpdate != false || 
			TimeChangerStruggleClient.smoothButterCycle != true ||
			TimeChangerStruggleClient.disableNightVisionEffect != true ||
			TimeChangerStruggleClient.commandsCommandFeedbackOnLessImportant != true ||
			TimeChangerStruggleClient.commandsDisableWorldTimeOnCycleUsage != true ||
			TimeChangerStruggleClient.getTimeChangerKey() != null)
		{
			this.write();
		}
	}
	
	
	
	
	
	
	private void checkAndPopulateConfigData()
	{
		if (this.configData == null)
			this.configData = new JsonObject();
		
		Configuration.checkOrCreateProperty(this.configData, "worldTime", TimeChangerStruggleClient.worldTime);
		Configuration.checkOrCreateProperty(this.configData, "dateOverTicks", TimeChangerStruggleClient.dateOverTicks);
		Configuration.checkOrCreateProperty(this.configData, "smoothButterCycle", TimeChangerStruggleClient.smoothButterCycle);
		Configuration.checkOrCreateProperty(this.configData, "disableNightVisionEffect", TimeChangerStruggleClient.disableNightVisionEffect);
		Configuration.checkOrCreateProperty(this.configData, "applyOnPropertyListValueUpdate", TimeChangerStruggleClient.applyOnPropertyListValueUpdate);
		Configuration.checkOrCreateProperty(this.configData, "commandFeedbackOnLessImportant", TimeChangerStruggleClient.commandsCommandFeedbackOnLessImportant);
		Configuration.checkOrCreateProperty(this.configData, "disableWorldTimeOnCycleUsage", TimeChangerStruggleClient.commandsDisableWorldTimeOnCycleUsage);

		Configuration.checkOrCreateProperty(this.configData, "activeDaylightChanger", 
			(TimeChangerStruggleClient.getTimeChangerKey() == null) ? "" : TimeChangerStruggleClient.getTimeChangerKey());
		
		if (TimeChangerStruggleClient.getTimeChanger() != null)
			this.createOrModifyDaylightCycleConfig(TimeChangerStruggleClient.getTimeChanger(), false);
	}
	
	/**
	 * Creates or modifies existing daylight cycle properties; removes if
	 * the properties generated by the daylight cycle is empty.
	 * 
	 * @param cycle the daylight cycle to load/save properties onto
	 * @param overrideConfigValues {@code true} to override stored existing 
	 * options in Configuration
	 */
	@SuppressWarnings("unchecked")
	public <B extends BaseProperty<B, V>, V> void createOrModifyDaylightCycleConfig
	(DayNightCycleBasis cycle, boolean overrideConfigValues)
	{
		Optional<DayNightCycleBuilder> builder = TimeChangerStruggleClient.
			getCachedCycleBuilderByClass(cycle.getBuilderClass());
		
		if (!builder.isPresent()) {
			return;
		}
		
		final DayNightCycleBuilder cycleBuilder = builder.get();
		final String cycleKeyName = cycleBuilder.getKeyName();
		
		Set<BaseProperty<?, ?>> props = cycle.createProperties();
		
		if (props == null || props.isEmpty()) 
		{
			JsonElement cyclesSection = this.configData.get("cyclesSection");
			
			// Remove the config entry should it exist
			if (cyclesSection != null && cyclesSection.isJsonObject() &&
				cyclesSection.getAsJsonObject().has(cycleKeyName))
				cyclesSection.getAsJsonObject().remove(cycleKeyName);
			
			return;
		}
		
		final Map.Entry<JsonObject, Boolean> cycleSectEntry = 
			this.getOrCreateConfigSection(cycleKeyName);
		
		final JsonObject section = cycleSectEntry.getKey();
		final boolean sectionCreated = cycleSectEntry.getValue();
		
		Iterator<BaseProperty<?, ?>> propsIterator = props.iterator();
		
		while (propsIterator.hasNext())
		{
			BaseProperty<?, ?> prop = propsIterator.next();
			
			if (prop instanceof FancySectionProperty)
				continue;
			
			final String propKey = prop.property();
			final boolean hasPropEntry = section.has(propKey);
			
			// If it has the element, have the property instead be written only if
			// our section entry returns true, assuming that this is an existing
			// section that was returned rather than a created one
			if (!overrideConfigValues && sectionCreated && hasPropEntry) 
			{
				prop.readFromJson(section.get(propKey)); 
				cycle.writePropertyValueToCycle((B)prop);
			}
			// If not, just write it as a JSON entry and add it into the section
			else
			{
				final JsonElement propElem = prop.writeToJson();
				
				if (propElem == null)
				{
					if (overrideConfigValues && hasPropEntry) {
						section.remove(propKey);
					}
				}
				else {
					section.add(propKey, propElem);
				}
			}
		}
	}
	
	/**
	 * Gets or creates a configuration section. This is primarily used on
	 * daylight cycle types to have them store their own configs.
	 * 
	 * @param propertyName the name of the key
	 * @return 
	 * <ul>
	 * <li> a json object; can either be obtained or created,
	 * <li> a boolean; {@code true} means that the section already existed;
	 *      otherwise it was created
	 * </ul>
	 */
	public Map.Entry<JsonObject, Boolean> getOrCreateConfigSection(String propertyName)
	{
		JsonElement sections = this.configData.get("cyclesSection");
		
		if (sections == null || !sections.isJsonObject()) 
		{
			sections = new JsonObject();
			this.configData.add("cyclesSection", sections);
		}
		
		JsonElement propertyElement = sections.getAsJsonObject().get(propertyName);
		
		if (propertyElement != null && propertyElement.isJsonObject()) {
			return new SimpleImmutableEntry<>(propertyElement.getAsJsonObject(), true);
		} 
//		else if (propertyElement != null) {
//			this.configData.remove(propertyName);
//		}
		
		JsonObject createdSection = new JsonObject();
		sections.getAsJsonObject().add(propertyName, createdSection);
		
		return new SimpleImmutableEntry<>(createdSection, false);
	}
	public static <V> void checkOrCreateProperty(JsonObject section, String propertyName, V defaultValue) 
	{
		JsonElement propertyElement = section.get(propertyName);
		
		boolean expectingPrimitive = (defaultValue instanceof Number || 
			defaultValue instanceof String || defaultValue instanceof Boolean);
		
		if (propertyElement == null)
		{
			if (expectingPrimitive) {
				Configuration.addExpectedPrimitive(section, propertyName, defaultValue);
			}
		}
		else
		{
			if (expectingPrimitive)
			{
				if (propertyElement.isJsonPrimitive())
				{
					JsonPrimitive primitive = propertyElement.getAsJsonPrimitive();
					
					boolean removeAndAdd = false;
					
					if (primitive.isBoolean()) {
						removeAndAdd = (defaultValue instanceof Number || defaultValue instanceof String);
					} else if (primitive.isNumber()) {
						removeAndAdd = (defaultValue instanceof String || defaultValue instanceof Boolean);
					} else if (primitive.isString()) {
						removeAndAdd = (defaultValue instanceof Boolean || defaultValue instanceof Number);
					}
					
					if (removeAndAdd)
					{
						section.remove(propertyName);
						Configuration.addExpectedPrimitive(section, propertyName, defaultValue);
					}
				}
			}
		}
	}
	
	private static <V> JsonPrimitive addExpectedPrimitive(JsonObject section, String propertyName, V value) 
	{
		JsonPrimitive primitive;
		
		if (value instanceof Number)
			primitive = new JsonPrimitive((Number)value);
		else if (value instanceof Boolean)
			primitive = new JsonPrimitive((Boolean)value);
		else
			primitive = new JsonPrimitive((String)value);
		
		section.add(propertyName, primitive);
		
		return primitive;
	}
}
