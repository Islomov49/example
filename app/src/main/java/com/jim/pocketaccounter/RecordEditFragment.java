package com.jim.pocketaccounter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ActionBarOverlayLayout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.pocketaccounter.finance.Account;
import com.jim.pocketaccounter.finance.AccountAdapter;
import com.jim.pocketaccounter.finance.CategoryAdapter;
import com.jim.pocketaccounter.finance.Currency;
import com.jim.pocketaccounter.finance.FinanceRecord;
import com.jim.pocketaccounter.finance.IconAdapter;
import com.jim.pocketaccounter.finance.RecordAccountAdapter;
import com.jim.pocketaccounter.finance.RecordCategoryAdapter;
import com.jim.pocketaccounter.finance.RecordSubCategoryAdapter;
import com.jim.pocketaccounter.finance.RootCategory;
import com.jim.pocketaccounter.finance.SubCategory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

@SuppressLint("ValidFragment")
public class RecordEditFragment extends Fragment implements OnClickListener {
    private RelativeLayout rlZero, rlOne, rlTwo, rlThree, rlFour, rlFive, rlSix, rlSeven, rlEight, rlNine, rlDot, rlEqualSign,
            rlPlusSign, rlMinusSign, rlMultipleSign, rlDivideSign, rlClearSign, rlBackspaceSign, rlCategory, rlSubCategory;
    private TextView tvRecordEditDisplay;
    private ImageView ivToolbarMostRight, ivRecordEditCategory, ivRecordEditSubCategory;
    private Spinner spRecordEdit, spToolbar;
    private byte operation = -1;
    private String s = "";
    private String s2 = "";
    private RootCategory category;
    private SubCategory subCategory;
    private FinanceRecord record;
    private Currency currency;
    private Account account;
    private Calendar date;
    private boolean tek = false,
            tek2 = false,
            calc = false,
            sequence = false;

    @SuppressLint("ValidFragment")
    public RecordEditFragment(RootCategory category, Calendar date, FinanceRecord record) {
        if (category != null) {
            for (int i=0; i<PocketAccounter.financeManager.getCategories().size(); i++) {
                if (category.getId().matches(PocketAccounter.financeManager.getCategories().get(i).getId()))
                    this.category = PocketAccounter.financeManager.getCategories().get(i);
            }
            this.subCategory = null;
        }
        else {
            this.category = record.getCategory();
            this.subCategory = record.getSubCategory();
        }
        this.date = (Calendar) date.clone();
        this.record = record;
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.record_edit, container, false);
        ((PocketAccounter)getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((PocketAccounter)getContext()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_button);
        PocketAccounter.toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PocketAccounter)getContext()).replaceFragment(new RecordFragment(Calendar.getInstance()));
            }
        });
        PocketAccounter.toolbar.setTitle("");
        PocketAccounter.toolbar.setSubtitle("");
        spRecordEdit = (Spinner) rootView.findViewById(R.id.spRecordEdit);
        spToolbar = (Spinner) PocketAccounter.toolbar.findViewById(R.id.spToolbar);
        spToolbar.setVisibility(View.VISIBLE);
        RecordAccountAdapter accountAdapter = new RecordAccountAdapter(getContext(), PocketAccounter.financeManager.getAccounts());
        spToolbar.setAdapter(accountAdapter);
        spToolbar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                account = PocketAccounter.financeManager.getAccounts().get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        final String[] currencies = new String[PocketAccounter.financeManager.getCurrencies().size()];
        for (int i=0; i<PocketAccounter.financeManager.getCurrencies().size(); i++)
            currencies[i] = PocketAccounter.financeManager.getCurrencies().get(i).getAbbr();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_single_item, currencies);
        spRecordEdit.setAdapter(adapter);
        for (int i=0; i<PocketAccounter.financeManager.getCurrencies().size(); i++) {
            if (PocketAccounter.financeManager.getCurrencies().get(i).getId().matches(PocketAccounter.financeManager.getMainCurrency().getId())) {
                spRecordEdit.setSelection(i);
                break;
            }
        }
        spRecordEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currency = PocketAccounter.financeManager.getCurrencies().get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        ivToolbarMostRight = (ImageView) PocketAccounter.toolbar.findViewById(R.id.ivToolbarMostRight);
        ivToolbarMostRight.setVisibility(View.VISIBLE);
        ivToolbarMostRight.setImageResource(R.drawable.check_sign);
        ivToolbarMostRight.setOnClickListener(this);
        ivRecordEditCategory = (ImageView) rootView.findViewById(R.id.ivRecordEditCategory);
        ivRecordEditSubCategory = (ImageView) rootView.findViewById(R.id.ivRecordEditSubCategory);
        if (category != null) {
            ivRecordEditSubCategory.setImageResource(R.drawable.category_not_selected);
            ivRecordEditCategory.setImageResource(category.getIcon());
        }
        if (record != null) {
            ivRecordEditSubCategory.setImageResource(record.getCategory().getIcon());
            ivRecordEditCategory.setImageResource(record.getCategory().getIcon());
        }
        rlZero = (RelativeLayout) rootView.findViewById(R.id.rlZero);
        rlZero.setOnClickListener(this);
        rlOne = (RelativeLayout) rootView.findViewById(R.id.rlOne);
        rlOne.setOnClickListener(this);
        rlTwo = (RelativeLayout) rootView.findViewById(R.id.rlTwo);
        rlTwo.setOnClickListener(this);
        rlThree = (RelativeLayout) rootView.findViewById(R.id.rlThree);
        rlThree.setOnClickListener(this);
        rlFour = (RelativeLayout) rootView.findViewById(R.id.rlFour);
        rlFour.setOnClickListener(this);
        rlFive = (RelativeLayout) rootView.findViewById(R.id.rlFive);
        rlFive.setOnClickListener(this);
        rlSix = (RelativeLayout) rootView.findViewById(R.id.rlSix);
        rlSix.setOnClickListener(this);
        rlSeven = (RelativeLayout) rootView.findViewById(R.id.rlSeven);
        rlSeven.setOnClickListener(this);
        rlEight = (RelativeLayout) rootView.findViewById(R.id.rlEight);
        rlEight.setOnClickListener(this);
        rlNine = (RelativeLayout) rootView.findViewById(R.id.rlNine);
        rlNine.setOnClickListener(this);
        rlDot = (RelativeLayout) rootView.findViewById(R.id.rlDot);
        rlDot.setOnClickListener(this);
        OperationHandler handler = new OperationHandler();
        rlEqualSign = (RelativeLayout) rootView.findViewById(R.id.rlEqualSign);
        rlEqualSign.setOnClickListener(handler);
        rlPlusSign = (RelativeLayout) rootView.findViewById(R.id.rlPlusSign);
        rlPlusSign.setOnClickListener(handler);
        rlMinusSign = (RelativeLayout) rootView.findViewById(R.id.rlMinusSign);
        rlMinusSign.setOnClickListener(handler);
        rlMultipleSign = (RelativeLayout) rootView.findViewById(R.id.rlMultipleSign);
        rlMultipleSign.setOnClickListener(handler);
        rlDivideSign = (RelativeLayout) rootView.findViewById(R.id.rlDivideSign);
        rlDivideSign.setOnClickListener(handler);
        rlClearSign = (RelativeLayout) rootView.findViewById(R.id.rlCancelSign);
        rlClearSign.setOnClickListener(handler);
        rlBackspaceSign = (RelativeLayout) rootView.findViewById(R.id.rlBackspaceSign);
        rlBackspaceSign.setOnClickListener(this);
        rlCategory = (RelativeLayout) rootView.findViewById(R.id.rlCategory);
        rlCategory.setOnClickListener(this);
        rlSubCategory = (RelativeLayout) rootView.findViewById(R.id.rlSubcategory);
        rlSubCategory.setOnClickListener(this);
        tvRecordEditDisplay = (TextView) rootView.findViewById(R.id.tvRecordEditDisplay);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (!calc) {
            sequence = false;
            if (tek ? s2.length() < 18 : s.length() < 18) {
                switch (view.getId()) {
                    case R.id.rlZero:
                        if (tek) {
                            if (tek2) {
                                s2 = "";
                                tek2 = false;
                            }
                            s2 += "0";
                            tvRecordEditDisplay.setText(s2);
                        } else {
                            s += "0";
                            tvRecordEditDisplay.setText(s);
                        }
                        break;
                    case R.id.rlOne:
                        if (tek) {
                            if (tek2) {
                                s2 = "";
                                tek2 = false;
                            }
                            s2 += "1";
                            tvRecordEditDisplay.setText(s2);
                        } else {
                            s += "1";
                            tvRecordEditDisplay.setText(s);
                        }
                        break;
                    case R.id.rlTwo:
                        if (tek) {
                            if (tek2) {
                                s2 = "";
                                tek2 = false;
                            }
                            s2 += "2";
                            tvRecordEditDisplay.setText(s2);
                        } else {
                            s += "2";
                            tvRecordEditDisplay.setText(s);
                        }
                        break;
                    case R.id.rlThree:
                        if (tek) {
                            if (tek2) {
                                s2 = "";
                                tek2 = false;
                            }
                            s2 += "3";
                            tvRecordEditDisplay.setText(s2);
                        } else {
                            s += "3";
                            tvRecordEditDisplay.setText(s);
                        }
                        break;
                    case R.id.rlFour:
                        if (tek) {
                            if (tek2) {
                                s2 = "";
                                tek2 = false;
                            }
                            s2 += "4";
                            tvRecordEditDisplay.setText(s2);
                        } else {
                            s += "4";
                            tvRecordEditDisplay.setText(s);
                        }
                        break;
                    case R.id.rlFive:
                        if (tek) {
                            if (tek2) {
                                s2 = "";
                                tek2 = false;
                            }
                            s2 += "5";
                            tvRecordEditDisplay.setText(s2);
                        } else {
                            s += "5";
                            tvRecordEditDisplay.setText(s);
                        }
                        break;
                    case R.id.rlSix:
                        if (tek) {
                            if (tek2) {
                                s2 = "";
                                tek2 = false;
                            }
                            s2 += "6";
                            tvRecordEditDisplay.setText(s2);
                        } else {
                            s += "6";
                            tvRecordEditDisplay.setText(s);
                        }
                        break;
                    case R.id.rlSeven:
                        if (tek) {
                            if (tek2) {
                                s2 = "";
                                tek2 = false;
                            }
                            s2 += "7";
                            tvRecordEditDisplay.setText(s2);
                        } else {
                            s += "7";
                            tvRecordEditDisplay.setText(s);
                        }
                        break;
                    case R.id.rlEight:
                        if (tek) {
                            if (tek2) {
                                s2 = "";
                                tek2 = false;
                            }
                            s2 += "8";
                            tvRecordEditDisplay.setText(s2);
                        } else {
                            s += "8";
                            tvRecordEditDisplay.setText(s);
                        }
                        break;
                    case R.id.rlNine:
                        if (tek) {
                            if (tek2) {
                                s2 = "";
                                tek2 = false;
                            }
                            s2 += "9";
                            tvRecordEditDisplay.setText(s2);
                        } else {
                            s += "9";
                            tvRecordEditDisplay.setText(s);
                        }
                        break;
                    case R.id.rlDot:
                        if (tek) {
                            if (s2.indexOf('.') == -1) {
                                if (tek2 && s2.isEmpty()) {
                                    s2 = "0";
                                    tek2 = false;
                                }
                                s2 += ".";
                                tvRecordEditDisplay.setText(s2);
                            }
                        } else {
                            if (s.indexOf('.') == -1) {
                                if (s.isEmpty()) s = "0";
                                s += ".";
                                tvRecordEditDisplay.setText(s);
                            }
                        }
                        break;
                    case R.id.rlBackspaceSign: {
                        if (tek ? s2.length() > 0 : s.length() > 0) {
                            if (tek) {
                                if (tek2) {
                                    s2 = "0";
                                    tek2 = false;
                                }
                                s2 = s2.substring(0, s2.length() - 1);
                                tvRecordEditDisplay.setText(s2.length() > 0 ? s2 : "0");
                            } else {
                                s = s.substring(0, s.length() - 1);
                                tvRecordEditDisplay.setText(s.length() > 0 ? s : "0");
                            }
                        }
                        break;
                    }
                    case R.id.rlCategory:
                        openCategoryDialog();
                        break;
                    case R.id.rlSubcategory:
                        openSubCategoryDialog();
                        break;
                    case R.id.ivToolbarMostRight:
                        createNewRecord();
                        break;
                }
            }
        }
    }
    private void createNewRecord() {
        if (record != null) {
            record.setCategory(category);
            record.setSubCategory(subCategory);
            record.setDate(date);
            record.setAccount(account);
            record.setCurrency(currency);
            record.setAmount(Double.parseDouble(tvRecordEditDisplay.getText().toString()));
        }
        else {
            FinanceRecord newRecord = new FinanceRecord();
            newRecord.setCategory(category);
            newRecord.setSubCategory(subCategory);
            newRecord.setDate(date);
            newRecord.setAccount(account);
            newRecord.setCurrency(currency);
            newRecord.setAmount(Double.parseDouble(tvRecordEditDisplay.getText().toString()));
            PocketAccounter.financeManager.getRecords().add(newRecord);
        }
        ((PocketAccounter)getContext()).replaceFragment(new RecordFragment(date));
    }
    private void openCategoryDialog() {
        final Dialog dialog=new Dialog(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.category_choose_list, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        ListView lvCategoryChoose = (ListView) dialogView.findViewById(R.id.lvCategoryChoose);
        RecordCategoryAdapter adapter = new RecordCategoryAdapter(getContext(), PocketAccounter.financeManager.getCategories());
        lvCategoryChoose.setAdapter(adapter);
        lvCategoryChoose.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ivRecordEditCategory.setImageResource(PocketAccounter.financeManager.getCategories().get(position).getIcon());
                ivRecordEditSubCategory.setImageResource(R.drawable.category_not_selected);
                category = PocketAccounter.financeManager.getCategories().get(position);
                dialog.dismiss();
            }
        });
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        dialog.getWindow().setLayout(8*width/9, ActionBarOverlayLayout.LayoutParams.MATCH_PARENT);
        dialog.show();
    }
    private void openSubCategoryDialog(){
        final Dialog dialog=new Dialog(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.category_choose_list, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        ListView lvCategoryChoose = (ListView) dialogView.findViewById(R.id.lvCategoryChoose);
        final ArrayList<SubCategory> subCategories = new ArrayList<SubCategory>();
        SubCategory noSubCategory = new SubCategory();
        noSubCategory.setIcon(R.drawable.category_not_selected);
        noSubCategory.setName(getResources().getString(R.string.no_category_name));
        noSubCategory.setId(getResources().getString(R.string.no_category));
        subCategories.add(noSubCategory);
        for (int i=0; i<category.getSubCategories().size(); i++)
            subCategories.add(category.getSubCategories().get(i));
        RecordSubCategoryAdapter adapter = new RecordSubCategoryAdapter(getContext(), subCategories);
        lvCategoryChoose.setAdapter(adapter);
        lvCategoryChoose.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (subCategories.get(position).getId().matches(getResources().getString(R.string.no_category)))
                    subCategory = null;
                else
                    subCategory = subCategories.get(position);
                ivRecordEditSubCategory.setImageResource(subCategories.get(position).getIcon());
                dialog.dismiss();
            }
        });
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        dialog.getWindow().setLayout(8*width/9, ActionBarOverlayLayout.LayoutParams.MATCH_PARENT);
        dialog.show();
    }
    private void calculate() {
        if (operation != -1) {
            if (!sequence) {
                calc = false;
            } else {
                calc = true;
            }
            if (s.isEmpty())
                s = "0";
            if (s.indexOf('.') != -1 && s.substring(s.indexOf('.') + 1, s.length()).isEmpty())
                s += "0";
            if (s2.isEmpty()) {
                switch (operation) {
                    case 0:
                    case 1: {
                        s2 = s;
                        s = "0";
                        break;
                    }
                    case 2: {
                        s2 = s;
                        break;
                    }
                    case 3: {
                        s2 = s;
                        s = "1";
                        break;
                    }
                }
            }
            if (s2.indexOf('.') != -1 && s2.substring(s2.indexOf('.') + 1, s2.length()).isEmpty())
                s2 += "0";
            BigDecimal a = new BigDecimal(s);
            BigDecimal b = new BigDecimal(s2);
            switch (operation) {
                case 0: {
                    a = a.add(b);
                    break;
                }
                case 1: {
                    a = a.subtract(b);
                    break;
                }
                case 2: {
                    a = a.multiply(b);
                    break;
                }
                case 3: {
                    if (b.equals(new BigDecimal("0"))) {
                        tvRecordEditDisplay.setText(getResources().getString(R.string.divide_null));
                        s = "";
                        s2 = "";
                        tek = false;
                        calc = true;
                    } else {
                        a = a.divide(b);
                    }
                    break;
                }
            }
            if (!b.equals(new BigDecimal("0"))) {
                String text = "";
                if ((a.toString()).length() > 18) {
                    s = a.toString();
                    text = (a.toString()).substring(0, 18);
                } else {
                    s = a.toString();
                    text = s;
                }
                if (text.substring(text.indexOf('.') + 1, text.length()).equals("0"))
                    tvRecordEditDisplay.setText(text.substring(0, text.indexOf('.')));
                else tvRecordEditDisplay.setText(text);
            }
            tek2 = true;
        }
    }

    class OperationHandler implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (!calc && !sequence) {
                calculate();
                sequence = true;
            }
            switch (v.getId()) {
                case R.id.rlPlusSign: {
                    operation = 0;
                    tek = true;
                    tek2 = true;
                    calc = false;
                    break;
                }
                case R.id.rlMinusSign: {
                    operation = 1;
                    tek2 = true;
                    calc = false;
                    tek = true;
                    break;
                }
                case R.id.rlMultipleSign: {
                    operation = 2;
                    tek2 = true;
                    tek = true;
                    calc = false;
                    break;
                }
                case R.id.rlDivideSign: {
                    operation = 3;
                    tek = true;
                    tek2 = true;
                    calc = false;
                    break;
                }
                case R.id.rlEqualSign: {
                    if (calc) {
                        calculate();
                    }
                    calc = true;
                    break;
                }
                case R.id.rlCancelSign: {
                    s = "";
                    s2 = "";
                    tvRecordEditDisplay.setText("0");
                    operation = -1;
                    tek = false;
                    tek2 = false;
                    calc = false;
                    break;
                }
            }
        }
    }
}