package com.talview.sondcast.service.retrofit;

import com.talview.sondcast.service.model.SongCollection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface SongCollectionService {

	@GET("things/zKWW.json")
	Call<SongCollection> getSongsList();

	@GET
	Call<ResponseBody> getSongThumbnail(@Url String thumbnailUrl);
}