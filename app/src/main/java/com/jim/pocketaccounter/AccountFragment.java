package com.jim.pocketaccounter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import com.jim.pocketaccounter.credit.ReckingCredit;
import com.jim.pocketaccounter.debt.DebtBorrow;
import com.jim.pocketaccounter.debt.Recking;
import com.jim.pocketaccounter.finance.Account;
import com.jim.pocketaccounter.finance.AccountAdapter;
import com.jim.pocketaccounter.finance.FinanceRecord;
import com.jim.pocketaccounter.finance.IconAdapter;
import com.jim.pocketaccounter.helper.FABIcon;
import com.jim.pocketaccounter.helper.FloatingActionButton;
import com.jim.pocketaccounter.helper.PocketAccounterGeneral;
import com.jim.pocketaccounter.helper.ScrollDirectionListener;

import java.util.UUID;

@SuppressLint("InflateParams")
public class AccountFragment extends Fragment implements OnClickListener, OnItemClickListener {
	private FloatingActionButton fabAccountAdd;
	private boolean[] selected;
	private int mode = PocketAccounterGeneral.NORMAL_MODE, selectedIcon;
	private ListView lvAccounts;
	private ImageView ivToolbarMostRight;
	private Spinner spToolbar;
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.account_layout, container, false);
		fabAccountAdd = (FloatingActionButton) rootView.findViewById(R.id.fabAccountAdd);
		fabAccountAdd.setOnClickListener(this);
		lvAccounts = (ListView) rootView.findViewById(R.id.lvAccounts);
		PocketAccounter.toolbar.findViewById(R.id.ivToolbarExcel).setVisibility(View.GONE);
		lvAccounts.setOnItemClickListener(this);
		PocketAccounter.toolbar.setTitle(R.string.accounts);
		PocketAccounter.toolbar.setSubtitle("");
		fabAccountAdd.attachToListView(lvAccounts, new ScrollDirectionListener() {
			@Override
			public void onScrollUp() {
				if (mode == PocketAccounterGeneral.EDIT_MODE) return;
				if (fabAccountAdd.getVisibility() == View.GONE) return;
				Animation down = AnimationUtils.loadAnimation(getContext(), R.anim.fab_down);
				synchronized (down) {
					down.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
							fabAccountAdd.setClickable(false);
							fabAccountAdd.setVisibility(View.GONE);
						}
						@Override
						public void onAnimationEnd(Animation animation) {
						}
						@Override
						public void onAnimationRepeat(Animation animation) {
						}
					});
					fabAccountAdd.startAnimation(down);
				}
			}
			@Override
			public void onScrollDown() {
				if (mode == PocketAccounterGeneral.EDIT_MODE) return;
				if (fabAccountAdd.getVisibility() == View.VISIBLE) return;
				Animation up = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_up);
				synchronized (up) {
					up.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
							fabAccountAdd.setVisibility(View.VISIBLE);
							fabAccountAdd.setClickable(true);
						}
						@Override
						public void onAnimationEnd(Animation animation) {
						}
						@Override
						public void onAnimationRepeat(Animation animation) {
						}
					});
					fabAccountAdd.startAnimation(up);
				}
			}
		});
		((PocketAccounter)getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		ivToolbarMostRight = (ImageView) PocketAccounter.toolbar.findViewById(R.id.ivToolbarMostRight);
		ivToolbarMostRight.setVisibility(View.VISIBLE);
		ivToolbarMostRight.setImageResource(R.drawable.pencil);
		ivToolbarMostRight.setOnClickListener(this);
		spToolbar = (Spinner) PocketAccounter.toolbar.findViewById(R.id.spToolbar);
		spToolbar.setVisibility(View.GONE);
		refreshList(mode);
		return rootView;
	}
	private void refreshList(int mode) {
		AccountAdapter adapter = new AccountAdapter(getActivity(), PocketAccounter.financeManager.getAccounts(), selected, mode);
		lvAccounts.setAdapter(adapter);
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mode == PocketAccounterGeneral.NORMAL_MODE)
			openAccountsAddDialog(PocketAccounter.financeManager.getAccounts().get(position));
		else {
			CheckBox chbAccountListItem = (CheckBox) view.findViewById(R.id.chbAccountListItem);
			chbAccountListItem.setChecked(!chbAccountListItem.isChecked());
			selected[position] = chbAccountListItem.isChecked();
		}
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.ivToolbarMostRight:
				if (mode == PocketAccounterGeneral.NORMAL_MODE) {
					mode = PocketAccounterGeneral.EDIT_MODE;
					ivToolbarMostRight.setImageDrawable(null);
					ivToolbarMostRight.setImageResource(R.drawable.ic_delete_black);
					selected = new boolean[PocketAccounter.financeManager.getAccounts().size()];
					refreshList(mode);
				}
				else {
					boolean isAnySelection = false;
					for (int i=0; i<selected.length; i++) {
						if (selected[i]) {
							isAnySelection = true;
							break;
						}
					}
					if (isAnySelection) {
						final Dialog dialog=new Dialog(getActivity());
						View dialogView = getActivity().getLayoutInflater().inflate(R.layout.warning_dialog, null);
						dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
						dialog.setContentView(dialogView);
						TextView tv = (TextView) dialogView.findViewById(R.id.tvWarningText);
						tv.setText(getString(R.string.account_delete_warning));
						Button btnYes = (Button) dialogView.findViewById(R.id.btnWarningYes);
						btnYes.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								mode = PocketAccounterGeneral.NORMAL_MODE;
								ivToolbarMostRight.setImageDrawable(null);
								ivToolbarMostRight.setImageResource(R.drawable.pencil);
								deleteAccounts();
								selected = null;
								refreshList(mode);
								dialog.dismiss();
							}
						});
						Button btnNo = (Button) dialogView.findViewById(R.id.btnWarningNo);
						btnNo.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();

							}
						});
						dialog.show();
					}
					else {
						mode = PocketAccounterGeneral.NORMAL_MODE;
						ivToolbarMostRight.setImageDrawable(null);
						ivToolbarMostRight.setImageResource(R.drawable.pencil);
						selected = null;
						refreshList(mode);
					}
				}
				break;
			case R.id.fabAccountAdd:
				mode = PocketAccounterGeneral.NORMAL_MODE;
				ivToolbarMostRight.setImageDrawable(null);
				ivToolbarMostRight.setImageResource(R.drawable.pencil);
				refreshList(mode);
				openAccountsAddDialog(null);
				break;
		}
	}
	private void openAccountsAddDialog(final Account account) {
		final Dialog dialog=new Dialog(getActivity());
		View dialogView = getActivity().getLayoutInflater().inflate(R.layout.account_edit_layout, null);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(dialogView);
		final EditText etAccountEditName = (EditText) dialogView.findViewById(R.id.etAccountEditName);
		final FABIcon fabAccountIcon = (FABIcon) dialogView.findViewById(R.id.fabAccountIcon);
		String[] tempIcons = getResources().getStringArray(R.array.icons);
		final int[] icons = new int[tempIcons.length];
		for (int i=0; i<tempIcons.length; i++)
			icons[i] = getResources().getIdentifier(tempIcons[i], "drawable", getActivity().getPackageName());
		selectedIcon = icons[0];
		if (account != null) {
			etAccountEditName.setText(account.getName());
			selectedIcon = account.getIcon();
		}
		Bitmap temp = BitmapFactory.decodeResource(getResources(), selectedIcon);
		Bitmap icon = Bitmap.createScaledBitmap(temp, (int)getResources().getDimension(R.dimen.twentyfive_dp), (int)getResources().getDimension(R.dimen.twentyfive_dp), false);
		fabAccountIcon.setImageBitmap(icon);
		fabAccountIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Dialog dialog=new Dialog(getActivity());
				View dialogView = getActivity().getLayoutInflater().inflate(R.layout.cat_icon_select, null);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(dialogView);
				GridView gvIcon = (GridView) dialogView.findViewById(R.id.gvCategoryIcons);
				IconAdapter adapter = new IconAdapter(getActivity(), icons, selectedIcon);
				gvIcon.setAdapter(adapter);
				gvIcon.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						Bitmap temp = BitmapFactory.decodeResource(getResources(), icons[position]);
						Bitmap icon = Bitmap.createScaledBitmap(temp, (int)getResources().getDimension(R.dimen.twentyfive_dp), (int)getResources().getDimension(R.dimen.twentyfive_dp), false);
						fabAccountIcon.setImageBitmap(icon);
						selectedIcon = icons[position];
						dialog.dismiss();
					}
				});
				DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
				int width = displayMetrics.widthPixels;
				dialog.getWindow().setLayout(7*width/8, LayoutParams.WRAP_CONTENT);
				dialog.show();
			}
		});
		ImageView ivAccountSave = (ImageView) dialogView.findViewById(R.id.ivAccountSave);
		ivAccountSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				if (etAccountEditName.getText().toString().matches("")) {
					etAccountEditName.setError(getResources().getString(R.string.account_name_error));
					return;
				}
				if (account != null) {
					account.setName(etAccountEditName.getText().toString());
					account.setIcon(selectedIcon);
				}
				else {
					Account newAccount = new Account();
					newAccount.setName(etAccountEditName.getText().toString());
					newAccount.setIcon(selectedIcon);
					newAccount.setId("account_"+UUID.randomUUID().toString());
					PocketAccounter.financeManager.getAccounts().add(newAccount);
				}
				dialog.dismiss();
				refreshList(mode);
			}
		});
		ImageView ivAccountClose = (ImageView) dialogView.findViewById(R.id.ivAccountClose);
		ivAccountClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		int width = displayMetrics.widthPixels;
		dialog.getWindow().setLayout(7*width/8, LayoutParams.WRAP_CONTENT);
		dialog.show();
	}
	private void deleteAccounts() {
		boolean allSelected = true;
		for (int i=0; i<selected.length; i++) {
			if (!selected[i]) {
				allSelected = false;
				break;
			}
		}
		int begPos = 0;
		if (allSelected)
			begPos = 1;
		else
			begPos = 0;
		if (allSelected) {
			for (int i=0; i<PocketAccounter.financeManager.getRecords().size(); i++) {
				FinanceRecord record = PocketAccounter.financeManager.getRecords().get(i);
				if (!record.getAccount().getId().matches(PocketAccounter.financeManager.getAccounts().get(0).getId())) {
					PocketAccounter.financeManager.getRecords().remove(i);
					i--;
				}
			}
			for (int i=0; i<PocketAccounter.financeManager.getDebtBorrows().size(); i++) {
				DebtBorrow debtBorrow = PocketAccounter.financeManager.getDebtBorrows().get(i);
				if (!debtBorrow.getAccount().getId().matches(PocketAccounter.financeManager.getAccounts().get(0).getId())) {
					PocketAccounter.financeManager.getDebtBorrows().remove(i);
					i--;
					continue;
				}
				for (int j=0; j<PocketAccounter.financeManager.getDebtBorrows().get(i).getReckings().size(); j++) {
					Recking recking = PocketAccounter.financeManager.getDebtBorrows().get(i).getReckings().get(j);
					if (!recking.getAccountId().matches(PocketAccounter.financeManager.getAccounts().get(0).getId())) {
						PocketAccounter.financeManager.getDebtBorrows().get(i).getReckings().remove(j);
						j--;
					}
				}
			}
			for (int i=0; i<PocketAccounter.financeManager.getCredits().size(); i++) {
				for (int j=0; j<PocketAccounter.financeManager.getCredits().get(i).getReckings().size(); j++) {
					ReckingCredit recking = PocketAccounter.financeManager.getCredits().get(i).getReckings().get(j);
					if (!recking.getAccountId().matches(PocketAccounter.financeManager.getAccounts().get(0).getId())) {
						PocketAccounter.financeManager.getCredits().get(i).getReckings().remove(j);
						j--;
					}
				}
			}
		}
		else {
			for (int i=0; i<selected.length; i++) {
				if (!selected[i]) continue;
				Account account = PocketAccounter.financeManager.getAccounts().get(i);
				for (int j=0; j<PocketAccounter.financeManager.getRecords().size(); j++) {
					if (PocketAccounter.financeManager.getRecords().get(j).getAccount().getId().matches(account.getId())) {
						PocketAccounter.financeManager.getRecords().remove(j);
						j--;
					}
				}
				for (int j=0; j<PocketAccounter.financeManager.getDebtBorrows().size(); j++) {
					if(PocketAccounter.financeManager.getDebtBorrows().get(j).getAccount().getId().matches(account.getId())) {
						PocketAccounter.financeManager.getDebtBorrows().remove(j);
						j--;
						continue;
					}
					for (int k=0; k<PocketAccounter.financeManager.getDebtBorrows().get(j).getReckings().size(); k++) {
						Recking recking = PocketAccounter.financeManager.getDebtBorrows().get(j).getReckings().get(k);
						if (recking.getAccountId().matches(account.getId())) {
							PocketAccounter.financeManager.getDebtBorrows().get(j).getReckings().remove(k);
							k--;
						}
					}
				}
				for (int j=0; j<PocketAccounter.financeManager.getCredits().size(); j++) {
					for (int k=0; k<PocketAccounter.financeManager.getCredits().get(j).getReckings().size(); k++) {
						if (PocketAccounter.financeManager.getCredits().get(j).getReckings().get(k).getAccountId().matches(account.getId())) {
							PocketAccounter.financeManager.getCredits().get(j).getReckings().remove(k);
							k--;
						}
					}
				}
			}
		}
		for (int i=begPos; i<selected.length; i++) {
			if (selected[i])
				PocketAccounter.financeManager.getAccounts().set(i, null);
		}
		for (int i = 0; i< PocketAccounter.financeManager.getAccounts().size(); i++) {
			if (PocketAccounter.financeManager.getAccounts().get(i) == null) {
				PocketAccounter.financeManager.getAccounts().remove(i);
				i--;
			}
		}
	}
	@Override
	public void onStop() {
		super.onStop();
		PocketAccounter.financeManager.saveAccounts();
	}
}
