/*
 * Copyright (C) 2010  Jean Marie PASCAL
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

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

public class FileSystemUtils {

	
	public static boolean rename(File file, String newName){
		if (file.exists()){
			return file.renameTo(new File(file.getParent(), newName));
		} else {
			return false;
		}
	}
	
	public static boolean delete(File file){
		if (file.exists()){
			if (file.isDirectory()) {
				return recursiveDelete(file);
			} else {
				return file.delete();
			}
		} else {
			return true;
		}
	}
	
	public static void open(Activity activity, File file){
		if (file.exists()){
			ActionUtils.openDocument(activity, file);
		}
	}
	
	private static boolean recursiveDelete(File file) {
		// Recursively delete all contents.
		File[] files = file.listFiles();
		
		for (int x=0; x<files.length; x++) {
			File childFile = files[x];
			if (childFile.isDirectory()) {
				if (!recursiveDelete(childFile)) {
					return false;
				}
			} else {
				if (!childFile.delete()) {
					return false;
				}
			}
		}
		
		if (!file.delete()) {
			return false;
		}
		
		return true;
	}
	
	public static boolean createNewFolder(File currentDirectory, String foldername) {
		if (!TextUtils.isEmpty(foldername)) {
			File file = new File(currentDirectory, foldername);
			if (file.mkdirs()){
				return true;
			} else {
				return false;
			}
		}  else {
			return false;
		}
	}
	
	
}
