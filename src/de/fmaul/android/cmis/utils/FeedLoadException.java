package de.fmaul.android.cmis.utils;

/**
 * Runtime excpetion that is thrown in case a CMIS feed can not be loaded.
 * Usually contains the cause as inner exception.
 * 
 * @author Florian Maul
 */
public class FeedLoadException extends RuntimeException {

	public FeedLoadException(Throwable e) {
		super(e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7034486177281832030L;

}
