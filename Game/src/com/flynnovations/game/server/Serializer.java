package com.flynnovations.game.server;

import java.io.*;
import java.util.Base64;

public final class Serializer {
	
	/**
	 * Serialize the given object to string
	 * @param o Object to serialize
	 * @return a Serialized object in a Base64 string
	 * @throws IOException if object cannot be serialized
	 */
	public static String serialize(Object o) throws IOException {
		String serializedObject;
		
		//create stream to write to
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		//create serializer
		ObjectOutputStream oos = new ObjectOutputStream(stream);
		
		//serialize object, and write to our stream
		oos.writeObject(o);
		oos.flush();

		//encode the byte array to a Base64 String
		serializedObject = new String(Base64.getEncoder().encode(stream.toByteArray()));

		return serializedObject;
	}

	/**
	 * Deserailize a serialized object
	 * @param string Serialized object represented as a Base64 String
	 * @return Deserialized object of type T
	 */
	@SuppressWarnings("unchecked")
	public static <t extends Serializable, T>T deserialize(String string, Class<t> c) throws Exception {
		//convert base64 string to byte array
		byte[] bytes = Base64.getDecoder().decode((string.getBytes()));

		T object = null;
		
		try {
			//create deserializer
			ObjectInputStream inputStream = new ObjectInputStream( new ByteArrayInputStream(bytes) );
			//deserialize string
			object = (T) inputStream.readObject();
		} catch (Exception e) {
			//unable to deserialize, throw exception
			e.printStackTrace();
			throw e;
		}
		
		return object;
	}
}
