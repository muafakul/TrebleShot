package com.genonbeta.TrebleShot.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.genonbeta.TrebleShot.R;
import com.genonbeta.TrebleShot.app.Activity;
import com.genonbeta.TrebleShot.config.AppConfig;
import com.genonbeta.TrebleShot.fragment.ApplicationListFragment;
import com.genonbeta.TrebleShot.fragment.MusicListFragment;
import com.genonbeta.TrebleShot.fragment.NetworkDeviceListFragment;
import com.genonbeta.TrebleShot.fragment.PendingTransferListFragment;
import com.genonbeta.TrebleShot.fragment.ReceivedFilesFragment;
import com.genonbeta.TrebleShot.fragment.TextShareFragment;
import com.genonbeta.TrebleShot.fragment.VideoListFragment;
import com.genonbeta.TrebleShot.helper.FileUtils;
import com.genonbeta.TrebleShot.support.FragmentTitle;

import java.io.File;

import velitasali.updatewithgithub.GitHubUpdater;

public class TrebleShotActivity extends Activity implements NavigationView.OnNavigationItemSelectedListener
{
	public static final String ACTION_OPEN_RECEIVED_FILES = "genonbeta.intent.action.OPEN_RECEIVED_FILES";
	public static final String ACTION_OPEN_ONGOING_LIST = "genonbeta.intent.action.OPEN_ONGOING_LIST";

	public static final int REQUEST_PERMISSION_ALL = 1;

	private SharedPreferences mPreferences;
	private NavigationView mNavigationView;
	private GitHubUpdater mUpdater;
	private Fragment mFragmentDeviceList;
	private Fragment mFragmentReceivedFiles;
	private Fragment mFragmentOnGoingProcessList;
	private Fragment mFragmentShareApplication;
	private Fragment mFragmentShareMusic;
	private Fragment mFragmentShareVideo;
	private Fragment mFragmentShareText;

	private long mExitPressTime;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.text_navigationDrawerOpen, R.string.text_navigationDrawerClose);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		mUpdater = new GitHubUpdater(this, AppConfig.APP_UPDATE_REPO, R.style.AppTheme);
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mNavigationView = (NavigationView) findViewById(R.id.nav_view);
		mNavigationView.setNavigationItemSelectedListener(this);

		mFragmentDeviceList = Fragment.instantiate(this, NetworkDeviceListFragment.class.getName());
		mFragmentReceivedFiles = Fragment.instantiate(this, ReceivedFilesFragment.class.getName());
		mFragmentOnGoingProcessList = Fragment.instantiate(this, PendingTransferListFragment.class.getName());
		mFragmentShareApplication = Fragment.instantiate(this, ApplicationListFragment.class.getName());
		mFragmentShareMusic = Fragment.instantiate(this, MusicListFragment.class.getName());
		mFragmentShareVideo = Fragment.instantiate(this, VideoListFragment.class.getName());
		mFragmentShareText = Fragment.instantiate(this, TextShareFragment.class.getName());

		changeFragment(mFragmentDeviceList);
		checkCurrentRequestedFragment(getIntent());

		if (mPreferences.contains("availableVersion") && mUpdater.isNewVersion(mPreferences.getString("availableVersion", null)))
			highlightUpdater(mPreferences.getString("availableVersion", null));
		else
			mUpdater.checkForUpdates(false, new GitHubUpdater.OnInfoAvailableListener()
			{
				@Override
				public void onInfoAvailable(boolean newVersion, String versionName, String title, String description, String releaseDate)
				{
					mPreferences
							.edit()
							.putString("availableVersion", versionName)
							.apply();

					if (newVersion)
						highlightUpdater(versionName);
				}
			});
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item)
	{
		if (R.id.menu_activity_main_device_list == item.getItemId())
		{
			changeFragment(mFragmentDeviceList);
		}
		else if (R.id.menu_activity_main_received_files == item.getItemId())
		{
			changeFragment(mFragmentReceivedFiles);
		}
		else if (R.id.menu_activity_main_ongoing_process == item.getItemId())
		{
			changeFragment(mFragmentOnGoingProcessList);
		}
		else if (R.id.menu_activity_main_share_app == item.getItemId())
		{
			changeFragment(mFragmentShareApplication);
		}
		else if (R.id.menu_activity_main_share_music == item.getItemId())
		{
			changeFragment(mFragmentShareMusic);
		}
		else if (R.id.menu_activity_main_share_video == item.getItemId())
		{
			changeFragment(mFragmentShareVideo);
		}
		else if (R.id.menu_activity_main_share_text == item.getItemId())
		{
			changeFragment(mFragmentShareText);
		}
		else if (R.id.menu_activity_main_about == item.getItemId())
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle(R.string.text_about);
			builder.setMessage(R.string.text_aboutSummary);
			builder.setNegativeButton(R.string.butn_close, null);
			builder.setPositiveButton(R.string.butn_seeSourceCode, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(AppConfig.APPLICATION_REPO)));
				}
			});

			builder.show();
		}
		else if (R.id.menu_activity_main_send_application == item.getItemId())
		{
			sendThisApplication();
		}
		else if (R.id.menu_activity_main_preferences == item.getItemId())
		{
			startActivity(new Intent(this, PreferencesActivity.class));
		}
		else if (R.id.menu_activity_main_check_for_updates == item.getItemId())
		{
			mUpdater.checkForUpdates(true, null);
		}
		else
			return false;

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);

		return true;
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		checkCurrentRequestedFragment(intent);
	}

	@Override
	public void onBackPressed()
	{
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

		if (drawer.isDrawerOpen(GravityCompat.START))
			drawer.closeDrawer(GravityCompat.START);
		else
		{
			if ((System.currentTimeMillis() - mExitPressTime) < 2000)
				finish();
			else
			{
				mExitPressTime = System.currentTimeMillis();
				Toast.makeText(this, R.string.mesg_secureExit, Toast.LENGTH_SHORT).show();
			}
		}

	}

	public void changeFragment(Fragment fragment)
	{
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		ft.replace(R.id.content_frame, fragment);
		ft.commit();

		if (fragment instanceof FragmentTitle)
			setTitle(((FragmentTitle) fragment).getFragmentTitle(this));
		else
			setTitle(R.string.text_appName);
	}

	public void checkCurrentRequestedFragment(Intent intent)
	{
		if (intent != null)
			if (ACTION_OPEN_RECEIVED_FILES.equals(intent.getAction()))
			{
				changeFragment(mFragmentReceivedFiles);
				mNavigationView.setCheckedItem(R.id.menu_activity_main_received_files);
			}
			else if (ACTION_OPEN_ONGOING_LIST.equals(intent.getAction()))
			{
				changeFragment(mFragmentOnGoingProcessList);
				mNavigationView.setCheckedItem(R.id.menu_activity_main_ongoing_process);
			}
	}

	private void highlightUpdater(String availableVersion)
	{
		MenuItem item = mNavigationView.getMenu().findItem(R.id.menu_activity_main_check_for_updates);

		item.setChecked(true);
		item.setTitle(R.string.text_newVersionAvailable);
	}

	private void sendThisApplication()
	{
		File apkFile = new File(getPackageCodePath());
		Intent sendIntent = new Intent(Intent.ACTION_SEND);

		sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(apkFile));
		sendIntent.setType(FileUtils.getFileContentType(apkFile.getAbsolutePath()));

		try
		{
			PackageManager pm = getPackageManager();
			PackageInfo packageInfo = pm.getPackageInfo(getApplicationInfo().packageName, 0);

			sendIntent.putExtra(ShareActivity.EXTRA_FILENAME_LIST, packageInfo.applicationInfo.loadLabel(pm) + "_" + packageInfo.versionName + ".apk");
		} catch (PackageManager.NameNotFoundException e)
		{
			e.printStackTrace();
		}

		startActivity(Intent.createChooser(sendIntent, getString(R.string.text_fileShareAppChoose)));
	}
}
