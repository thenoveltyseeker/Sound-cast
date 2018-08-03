package com.talview.soundcast.service;

import android.support.annotation.NonNull;

import com.talview.soundcast.service.model.Song;
import com.talview.soundcast.service.model.SongCollection;
import com.talview.soundcast.service.model.SongDetails;
import com.talview.soundcast.service.retrofit.RetrofitClient;
import com.talview.soundcast.service.retrofit.SongCollectionService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementation class of {@link SongCollectionInteractor}
 *
 * @author Rolbin
 */
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
					// map response into ui model
					for (Song song : collection.getSongs()) {
						SongDetails songDetails = new SongDetails();
						songDetails.setMediaId(String.valueOf(song.getId()));
						songDetails.setMediaTitle(song.getTitle());
						songDetails.setMediaUrl(song.getLink());
						songDetails.setThumbnail(song.getThumbnail());
						songDetailsList.add(songDetails);
					}
					responseListener.onSuccess(songDetailsList);
				}else{
					responseListener.onError();
				}
			}

			@Override
			public void onFailure(@NonNull Call<SongCollection> call, @NonNull Throwable throwable) {
				//Todo handle error message
				responseListener.onError();
			}
		});
	}


}
