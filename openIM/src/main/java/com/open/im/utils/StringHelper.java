package com.open.im.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.util.ByteArrayBuffer;

public class StringHelper {
	public static String parseStream(InputStream inputStream) throws IOException {
		ByteArrayBuffer bab = new ByteArrayBuffer(inputStream.available());
		int len = -1;
		byte[] bs = new byte[1024];
		while ((len = inputStream.read(bs)) != -1) {
			bab.append(bs, 0, len);
		}
		return new String(bab.toByteArray(), "UTF-8");
	}
}
