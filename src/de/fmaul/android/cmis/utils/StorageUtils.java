/*
 * Copyright (C) 2010 Florian Maul
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fmaul.android.cmis.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.fmaul.android.cmis.CmisApp;

import android.app.Application;
import android.os.Environment;

public class StorageUtils {

	public static final String TYPE_FEEDS = "cache";
	public static final String TYPE_CONTENT = "files";
	public static final String TYPE_DOWNLOAD = "download";

	public static boolean isFeedInCache(Application app, String url, String workspace) throws StorageException {
		File cacheFile = getFeedFile(app, workspace, md5(url));
		return cacheFile != null && cacheFile.exists();
	}

	public static Document getFeedFromCache(Application app, String url, String workspace) throws StorageException {
		File cacheFile = getFeedFile(app, workspace, md5(url));
		Document document = null;
		SAXReader reader = new SAXReader(); // dom4j SAXReader
		try {
			document = reader.read(cacheFile);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // dom4j Document

		return document;
	}

	private static File getFeedFile(Application app, String repoId, String feedHash) throws StorageException {
		return getStorageFile(app, repoId, TYPE_FEEDS, null, feedHash + ".xml");
	}

	public static void storeFeedInCache(Application app, String url, Document doc, String workspace) throws StorageException {
		File cacheFile = getFeedFile(app, workspace, md5(url));
		ensureOrCreatePathAndFile(cacheFile);

		try {
			XMLWriter writer = new XMLWriter(new FileOutputStream(cacheFile));
			writer.write(doc);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static File getStorageFile(Application app, String saveFolder, String filename) throws StorageException {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			StringBuilder builder = new StringBuilder();
			builder.append(saveFolder);
			builder.append("/");
			builder.append(((CmisApp) app).getRepository().getServer().getName());
			if (filename != null) {
				builder.append("/");
				builder.append(filename);
			}
			return new File(builder.toString());
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			throw new StorageException("Storage in Read Only Mode");
		} else {
			throw new StorageException("Storage is unavailable");
		}
	}
	

	public static File getStorageFile(Application app, String repoId, String storageType, String itemId, String filename) throws StorageException {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			StringBuilder builder = new StringBuilder();
			builder.append(Environment.getExternalStorageDirectory());
			builder.append("/");
			builder.append("Android");
			builder.append("/");
			builder.append("data");
			builder.append("/");
			builder.append(app.getPackageName());
			builder.append("/");
			if (storageType != null) {
				builder.append("/");
				builder.append(storageType);
			}
			if (repoId != null) {
				builder.append("/");
				builder.append(repoId);
			}
			if (itemId != null) {
				builder.append("/");
				builder.append(itemId.replaceAll(":", "_"));
			}
			if (filename != null) {
				builder.append("/");
				builder.append(filename);
			}
			return new File(builder.toString());
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			throw new StorageException("Storage in Read Only Mode");
		} else {
			throw new StorageException("Storage is unavailable");
		}
	}

	private static void ensureOrCreatePathAndFile(File contentFile) {
		try {
			contentFile.getParentFile().mkdirs();
			contentFile.createNewFile();
		} catch (IOException iox) {
			throw new RuntimeException(iox);
		}
	}

	public static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static boolean deleteRepositoryFiles(Application app, String repoId) throws StorageException {
		File repoDir = getStorageFile(app, repoId, TYPE_FEEDS, null, null);
		try {
			FileUtils.deleteDirectory(repoDir);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static boolean deleteCacheFolder(Application app) throws StorageException {
		File contentDir = getStorageFile(app, null, TYPE_CONTENT, null, null);
		File feedsDir = getStorageFile(app, null, TYPE_FEEDS, null, null);
		try {
			FileUtils.deleteDirectory(contentDir);
			FileUtils.deleteDirectory(feedsDir);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	
	public static boolean deleteRepositoryCacheFiles(Application app, String repoId) throws StorageException {
		File contentDir = getStorageFile(app, repoId, TYPE_CONTENT, null, null);
		File feedsDir = getStorageFile(app, repoId, TYPE_FEEDS, null, null);
		try {
			FileUtils.deleteDirectory(contentDir);
			FileUtils.deleteDirectory(feedsDir);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public static boolean deleteFeedFile(Application app, String repoId, String url) throws StorageException {
		File feedFile = getStorageFile(app, repoId, TYPE_FEEDS, null,  md5(url));
		try {
			FileUtils.deleteDirectory(feedFile);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
