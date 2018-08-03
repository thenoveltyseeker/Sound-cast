package com.talview.soundcast;

/**
 * Base presenter, All presenter class must extend this
 *
 * @author Rolbin
 */
public interface BasePresenter {

	/**
	 * Release everything in this method
	 */
	void onDestroy();
}
