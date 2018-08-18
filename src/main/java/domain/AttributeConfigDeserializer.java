package domain;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class AttributeConfigDeserializer implements JsonDeserializer<AttributeConfig> {

	@Override
	public AttributeConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject jsonObject = json.getAsJsonObject();		
		return new AttributeConfig(jsonObject.get("name").getAsString(), jsonObject.get("is") == null ? null : jsonObject.get("is").getAsString(), jsonObject.get("matches") == null ? null :Pattern.compile(jsonObject.get("matches").getAsString()));
	}

}
