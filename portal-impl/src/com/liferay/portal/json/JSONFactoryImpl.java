/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.json;

import com.liferay.alloy.util.json.StringTransformer;
import com.liferay.portal.json.jabsorb.serializer.LiferayJSONSerializer;
import com.liferay.portal.json.jabsorb.serializer.LiferaySerializer;
import com.liferay.portal.json.jabsorb.serializer.LocaleSerializer;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONDeserializer;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONSerializer;
import com.liferay.portal.kernel.json.JSONTransformer;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.pacl.DoPrivileged;
import com.liferay.portal.kernel.util.Validator;

import java.lang.reflect.InvocationTargetException;

import java.util.List;

import org.jabsorb.serializer.MarshallException;

import org.json.JSONML;

/**
 * @author Brian Wing Shun Chan
 */
@DoPrivileged
public class JSONFactoryImpl implements JSONFactory {

	public JSONFactoryImpl() {
		JSONInit.init();

		_jsonSerializer = new LiferayJSONSerializer();

		try {
			_jsonSerializer.registerDefaultSerializers();

			_jsonSerializer.registerSerializer(new LiferaySerializer());
			_jsonSerializer.registerSerializer(new LocaleSerializer());
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	@Override
	public String convertJSONMLArrayToXML(String jsonml) {
		try {
			org.json.JSONArray jsonArray = new org.json.JSONArray(jsonml);

			return JSONML.toString(jsonArray);
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e, e);
			}

			throw new IllegalStateException("Unable to convert to XML", e);
		}
	}

	@Override
	public String convertJSONMLObjectToXML(String jsonml) {
		try {
			org.json.JSONObject jsonObject = new org.json.JSONObject(jsonml);

			return JSONML.toString(jsonObject);
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e, e);
			}

			throw new IllegalStateException("Unable to convert to XML", e);
		}
	}

	@Override
	public String convertXMLtoJSONMLArray(String xml) {
		try {
			org.json.JSONArray jsonArray = JSONML.toJSONArray(xml);

			return jsonArray.toString();
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e, e);
			}

			throw new IllegalStateException("Unable to convert to JSONML", e);
		}
	}

	@Override
	public String convertXMLtoJSONMLObject(String xml) {
		try {
			org.json.JSONObject jsonObject = JSONML.toJSONObject(xml);

			return jsonObject.toString();
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e, e);
			}

			throw new IllegalStateException("Unable to convert to JSONML", e);
		}
	}

	@Override
	public JSONTransformer createJavaScriptNormalizerJSONTransformer(
		List<String> javaScriptAttributes) {

		StringTransformer stringTransformer = new StringTransformer();

		stringTransformer.setJavaScriptAttributes(javaScriptAttributes);

		return stringTransformer;
	}

	@Override
	public JSONArray createJSONArray() {
		return new JSONArrayImpl();
	}

	@Override
	public JSONArray createJSONArray(String json) throws JSONException {
		return new JSONArrayImpl(json);
	}

	@Override
	public <T> JSONDeserializer<T> createJSONDeserializer() {
		return new JSONDeserializerImpl<T>();
	}

	@Override
	public JSONObject createJSONObject() {
		return new JSONObjectImpl();
	}

	@Override
	public JSONObject createJSONObject(String json) throws JSONException {
		return new JSONObjectImpl(json);
	}

	@Override
	public JSONSerializer createJSONSerializer() {
		return new JSONSerializerImpl();
	}

	@Override
	public Object deserialize(JSONObject jsonObj) {
		return deserialize(jsonObj.toString());
	}

	@Override
	public Object deserialize(String json) {
		try {
			return _jsonSerializer.fromJSON(json);
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e, e);
			}

			throw new IllegalStateException("Unable to deserialize object", e);
		}
	}

	@Override
	public String getNullJSON() {
		return _NULL_JSON;
	}

	@Override
	public JSONObject getUnmodifiableJSONObject() {
		return _unmodifiableJSONObject;
	}

	@Override
	public Object looseDeserialize(String json) {
		try {
			JSONDeserializer<?> jsonDeserializer = createJSONDeserializer();

			return jsonDeserializer.deserialize(json);
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e, e);
			}

			throw new IllegalStateException("Unable to deserialize object", e);
		}
	}

	@Override
	public <T> T looseDeserialize(String json, Class<T> clazz) {
		JSONDeserializer<?> jsonDeserializer = createJSONDeserializer();

		jsonDeserializer.use(null, clazz);

		return (T)jsonDeserializer.deserialize(json);
	}

	@Override
	public Object looseDeserializeSafe(String json) {
		try {
			JSONDeserializer<?> jsonDeserializer = createJSONDeserializer();

			jsonDeserializer.safeMode(true);

			return jsonDeserializer.deserialize(json);
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e, e);
			}

			throw new IllegalStateException("Unable to deserialize object", e);
		}
	}

	@Override
	public <T> T looseDeserializeSafe(String json, Class<T> clazz) {
		JSONDeserializer<?> jsonDeserializer = createJSONDeserializer();

		jsonDeserializer.safeMode(true);

		jsonDeserializer.use(null, clazz);

		return (T)jsonDeserializer.deserialize(json);
	}

	@Override
	public String looseSerialize(Object object) {
		JSONSerializer jsonSerializer = createJSONSerializer();

		return jsonSerializer.serialize(object);
	}

	@Override
	public String looseSerialize(
		Object object, JSONTransformer jsonTransformer, Class<?> clazz) {

		JSONSerializer jsonSerializer = createJSONSerializer();

		jsonSerializer.transform(jsonTransformer, clazz);

		return jsonSerializer.serialize(object);
	}

	@Override
	public String looseSerialize(Object object, String... includes) {
		JSONSerializer jsonSerializer = createJSONSerializer();

		jsonSerializer.include(includes);

		return jsonSerializer.serialize(object);
	}

	@Override
	public String looseSerializeDeep(Object object) {
		JSONSerializer jsonSerializer = createJSONSerializer();

		return jsonSerializer.serializeDeep(object);
	}

	@Override
	public String looseSerializeDeep(
		Object object, JSONTransformer jsonTransformer, Class<?> clazz) {

		JSONSerializer jsonSerializer = createJSONSerializer();

		jsonSerializer.transform(jsonTransformer, clazz);

		return jsonSerializer.serializeDeep(object);
	}

	@Override
	public String serialize(Object object) {
		try {
			return _jsonSerializer.toJSON(object);
		}
		catch (MarshallException me) {
			if (_log.isWarnEnabled()) {
				_log.warn(me, me);
			}

			throw new IllegalStateException("Unable to serialize oject", me);
		}
	}

	@Override
	public String serializeException(Exception exception) {
		JSONObject jsonObject = createJSONObject();

		String message = null;

		if (exception instanceof InvocationTargetException) {
			Throwable cause = exception.getCause();

			message = cause.toString();
		}
		else {
			message = exception.getMessage();
		}

		if (Validator.isNull(message)) {
			message = exception.toString();
		}

		jsonObject.put("exception", message);

		return jsonObject.toString();
	}

	@Override
	public String serializeThrowable(Throwable throwable) {
		if (throwable instanceof Exception) {
			return serializeException((Exception)throwable);
		}

		JSONObject jsonObject = createJSONObject();

		String message = throwable.getMessage();

		if (Validator.isNull(message)) {
			message = throwable.toString();
		}

		jsonObject.put("throwable", message);

		return jsonObject.toString();
	}

	private static final String _NULL_JSON = "{}";

	private static Log _log = LogFactoryUtil.getLog(JSONFactoryImpl.class);

	private org.jabsorb.JSONSerializer _jsonSerializer;
	private JSONObject _unmodifiableJSONObject =
		new UnmodifiableJSONObjectImpl();

}