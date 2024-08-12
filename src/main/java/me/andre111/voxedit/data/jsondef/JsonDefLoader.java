/*
 * Copyright (c) 2024 André Schweiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.andre111.voxedit.data.jsondef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;

public class JsonDefLoader {

	public static JsonDef getDef(String kind, Identifier type) {
		if(type == null) return null;
		
		CacheKey key = new CacheKey(kind, type);
		return CACHE.computeIfAbsent(key, k -> {
			URL url = JsonDefLoader.class.getResource("/voxedit/definitions/"+kind+"/"+type.getNamespace()+"/"+type.getPath()+".json");
			if(url != null) {
				try(InputStreamReader reader = new InputStreamReader(url.openStream())) {
					JsonObject obj = (new Gson()).fromJson(reader, JsonObject.class);
					if(obj != null) {
						return parseComplexDef(obj);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			System.err.println("Could not get definition for: "+kind+" - "+type);
			return null;
		});
	}
	
	public static Collection<Identifier> getKnownTypes(String kind) {
		//TODO: implement cache?
		Set<Identifier> types = new HashSet<>();
		URL url = JsonDefLoader.class.getResource("/voxedit/definitions/"+kind+"/");
		if(url == null) return types;
		
		try(BufferedReader namespaceReader = new BufferedReader(new InputStreamReader(url.openStream()))) {
			for(String namespace : namespaceReader.lines().toList()) {
				try(BufferedReader pathReader = new BufferedReader(new InputStreamReader(JsonDefLoader.class.getResourceAsStream("/voxedit/definitions/"+kind+"/"+namespace+"/")))) {
					for(String path : pathReader.lines().toList()) {
						if(!path.endsWith(".json")) continue;
						Identifier id = Identifier.tryParse(namespace, path.substring(0, path.length()-".json".length()));
						if(id != null) types.add(id);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return types;
	}
	
	private static JsonDef parseDef(JsonObject obj) throws IOException {
		if(!obj.has("type")) return parseComplexDef(obj);
		boolean optional = false;
		if(obj.has("optional")) optional = obj.get("optional").getAsBoolean();
		
		String type = obj.get("type").getAsString();
		return parseTypeDef(obj, type, optional);
	}
	
	private static JsonDef parseTypeDef(JsonObject obj, String type, boolean optional) throws IOException {
		switch(type) {
		case "boolean":
			return new JsonDef.Boolean(obj.get("defaultValue").getAsBoolean(), optional);
		case "integer":
			return new JsonDef.Integer(obj.get("defaultValue").getAsInt(), obj.has("minValue") ? obj.get("minValue").getAsInt() : Integer.MIN_VALUE, obj.has("maxValue") ? obj.get("maxValue").getAsInt() : Integer.MAX_VALUE, optional);
		case "list":
			JsonDef listType = parseDef(obj.get("list_type").getAsJsonObject());
			return new JsonDef.List(listType, optional);
		case "blockstate":
			return new JsonDef.BlockState();
		default:
			if(JsonDefLoader.class.getResource("/voxedit/definitions/"+type+"/") != null) {
				return new JsonDef.Defined(type, optional);
			}
			throw new RuntimeException("Unknown type: "+type);
		}
	}
	
	private static JsonDef parseComplexDef(JsonObject obj) throws IOException {
		Map<String, JsonDef> properties = new HashMap<>();
		for(String key : obj.keySet()) {
			JsonElement element = obj.get(key);
			if(!element.isJsonObject()) throw new IOException("Definition contains non object entry: "+element);
			properties.put(key, parseDef(element.getAsJsonObject()));
		}
		return new JsonDef.Complex(properties, false);
	}
	
	private static final Map<CacheKey, JsonDef> CACHE = new HashMap<>();
	private static record CacheKey(String kind, Identifier type) {
	}
}
