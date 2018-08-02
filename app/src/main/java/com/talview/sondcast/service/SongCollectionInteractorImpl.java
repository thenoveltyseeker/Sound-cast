package com.talview.sondcast.service;

import android.support.annotation.NonNull;

import com.talview.sondcast.service.model.Song;
import com.talview.sondcast.service.model.SongCollection;
import com.talview.sondcast.service.model.SongDetails;
import com.talview.sondcast.service.retrofit.RetrofitClient;
import com.talview.sondcast.service.retrofit.SongCollectionService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongCollectionInteractorImpl implements SongCollectionInteractor {


	@Override
	public void getSongsList(final ResponseListener responseListener) {
		SongCollectionService service = RetrofitClient.getRetrofit().create(SongCollectionService.class);
		Call<SongCollection> songCollectionCall = service.getSongsList();
		songCollectionCall.enqueue(new Callback<SongCollection>() {
			@Override
			public void onResponse(@NonNull Call<SongCollection> call, @NonNull Response<SongCollection> response) {
				SongCollection collection = response.body();
				if (collection != null && collection.getSongs() != null) {
					List<SongDetails> songDetailsList = new ArrayList<>();
					for (Song song : collection.getSongs()) {
						SongDetails songDetails = new SongDetails();
						songDetails.setMediaId(String.valueOf(song.getId()));
						songDetails.setMediaTitle(song.getTitle());
						songDetails.setMediaUrl(song.getLink());
						songDetails.setThumbnail(song.getThumbnail());
						songDetailsList.add(songDetails);
					}
					responseListener.onSuccess(songDetailsList);
				}
			}

			@Override
			public void onFailure(@NonNull Call<SongCollection> call, @NonNull Throwable throwable) {
				responseListener.onError();
			}
		});
	}


}
