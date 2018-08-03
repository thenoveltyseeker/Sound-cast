package com.talview.soundcast.service.retrofit;

import com.talview.soundcast.service.model.SongCollection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Retrofit service list
 * use {@link RetrofitClient} to create service class
 *
 * @author Rolbin
 */
public interface SongCollectionService {

	@GET("things/zKWW.json")
	Call<SongCollection> getSongsList();

	@GET
	Call<ResponseBody> getSongThumbnail(@Url String thumbnailUrl);
}