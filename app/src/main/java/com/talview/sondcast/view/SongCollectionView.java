package com.talview.sondcast.view;


import com.talview.sondcast.BaseView;
import com.talview.sondcast.service.model.SongDetails;

import java.util.List;

public interface SongCollectionView extends BaseView {

	void showSongCollection(List<SongDetails> songCollection);

	void showError();

}
