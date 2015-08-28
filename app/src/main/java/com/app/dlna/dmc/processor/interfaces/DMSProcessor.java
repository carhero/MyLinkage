package com.app.dlna.dmc.processor.interfaces;

import org.fourthline.cling.support.model.DIDLObject;

import java.util.List;
import java.util.Map;



public interface DMSProcessor {
	void browse(String objectID);

	void dispose();

	void addListener(DMSProcessorListener listener);

	void removeListener(DMSProcessorListener listener);

	public interface DMSProcessorListener {
		void onBrowseComplete(Map<String, List<? extends DIDLObject>> result);

		void onBrowseFail(String message);
	}
}
