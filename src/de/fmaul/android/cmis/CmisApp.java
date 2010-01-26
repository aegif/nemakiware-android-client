package de.fmaul.android.cmis;

import android.app.Application;
import de.fmaul.android.cmis.repo.CmisRepository;

public class CmisApp extends Application {

	private CmisRepository repository;
	
	public CmisRepository getRepository() {
		return repository;
	}
	
	public void setRepository(CmisRepository repository) {
		this.repository = repository;
	}
	
	
	
}
