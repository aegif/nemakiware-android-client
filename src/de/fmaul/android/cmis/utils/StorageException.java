package de.fmaul.android.cmis.utils;

public class StorageException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 329581492462074017L;
	
	
	StorageException(String erreur, Exception e){
		super(erreur, e);
	}


	public StorageException(String erreur) {
		super(erreur);
	}

}
