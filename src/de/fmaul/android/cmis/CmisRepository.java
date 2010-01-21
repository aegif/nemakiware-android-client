package de.fmaul.android.cmis;

/**
 * NOT USED YET... Should contain all information about the repo
 * 
 * @author Florian
 *
 */
public class CmisRepository {
	
	
	private String feedRootCollection;
	private String feedQueryCollection;

	private CmisRepository() {
	}
	
	private void connect(String repositoryUrl, String user, String password) {
		

	}
	

	public String getFeedRootCollection() {
		return feedRootCollection;
	}
	
	public String getFeedQueryCollection() {
		return feedQueryCollection;
	}
}
