package com.talview.soundcast;

import android.content.Context;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Root;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import com.talview.soundcast.view.utils.Utility;

import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class UtilityInstrumentedTest {

	@Test
	public void showError() {
		// Context of the app under test.
		Context appContext = InstrumentationRegistry.getTargetContext();
		Utility.showError(appContext, "Invalid Name");

		onView(withText("Invalid Name")).inRoot(new ToastMatcher())
				.check(matches(withText("Invalid Name")));
	}

	public class ToastMatcher extends TypeSafeMatcher<Root> {

		@Override
		public boolean matchesSafely(Root root) {
			int type = root.getWindowLayoutParams().get().type;
			if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
				IBinder windowToken = root.getDecorView().getWindowToken();
				IBinder appToken = root.getDecorView().getApplicationWindowToken();
				if (windowToken == appToken) {
					//means this window isn't contained by any other windows.
				}
			}
			return false;
		}

		@Override
		public void describeTo(org.hamcrest.Description description) {
			description.appendText("is toast");
		}
	}
}
