package com.marspotato.supportsmallshop.util;

import java.lang.reflect.Type;

import org.joda.time.DateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateTimeTypeConverter implements JsonSerializer<DateTime>,
		JsonDeserializer<DateTime> {
	// No need for an InstanceCreator since DateTime provides a no-args
	// constructor
	@Override
	public JsonElement serialize(DateTime src, Type srcType,
			JsonSerializationContext context) {
		
		return new JsonPrimitive(Config.defaultDateTimeFormatter.print(src));
	}

	@Override
	public DateTime deserialize(JsonElement json, Type type,
			JsonDeserializationContext context) throws JsonParseException {
		return Config.defaultDateTimeFormatter.parseDateTime(json.getAsString());
	}
}
