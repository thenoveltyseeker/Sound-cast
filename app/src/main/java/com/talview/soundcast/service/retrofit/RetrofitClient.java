package com.talview.soundcast.service.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit builder class
 *
 * @author Rolbin
 */
public class RetrofitClient {
	private static final String sBaseUrl = "https://www.jasonbase.com/";
	private static Retrofit sRetrofit;

	public static Retrofit getRetrofit() {
		if (sRetrofit == null) {
			sRetrofit = new Retrofit.Builder()
					.baseUrl(sBaseUrl)
					.addConverterFactory(GsonConverterFactory.create())
					.build();
		}
		return sRetrofit;
	}
}
