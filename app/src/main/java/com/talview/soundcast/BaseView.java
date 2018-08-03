package com.talview.soundcast;

/**
 * Base View, All View class must extend this
 *
 * @author Rolbin
 */
public interface BaseView {

	/**
	 * show loading
	 */
	void showProgress();

	/**
	 * hide loading
	 */
	void hideProgress();
}
