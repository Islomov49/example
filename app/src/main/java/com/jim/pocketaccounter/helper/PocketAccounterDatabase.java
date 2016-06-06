package com.jim.pocketaccounter.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jim.pocketaccounter.R;
import com.jim.pocketaccounter.debt.DebtBorrow;
import com.jim.pocketaccounter.finance.Account;
import com.jim.pocketaccounter.finance.Currency;
import com.jim.pocketaccounter.finance.CurrencyCost;
import com.jim.pocketaccounter.finance.FinanceRecord;
import com.jim.pocketaccounter.finance.RootCategory;
import com.jim.pocketaccounter.finance.SubCategory;

public class PocketAccounterDatabase extends SQLiteOpenHelper {
	
	private Context context;
	private SimpleDateFormat dateFormat = null;
	public PocketAccounterDatabase(Context context) {
		super(context, "PocketAccounterDatabase", null, 2);
		this.context = context;
		dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	}
	
	//onCreate method 
	//Here was created all tables which needs for finance department	
	@Override
	public void onCreate(SQLiteDatabase db) {
		//currency_table
		db.execSQL("create table currency_table ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "currency_name TEXT,"
				+ "currency_sign TEXT,"
				+ "currency_id TEXT,"
				+ "currency_main INTEGER"
				+ ");");

		//currency costs
		db.execSQL("create table currency_costs_table ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "currency_id TEXT,"
				+ "date TEXT,"
				+ "cost REAL"
				+ ");");

		//category_table
		db.execSQL("create table category_table ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "category_name TEXT,"
				+ "category_id TEXT,"
				+ "category_type INTEGER,"
				+ "icon INTEGER"
				+ ");");

		//subcategries table
		db.execSQL("create table subcategory_table ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "subcategory_name TEXT,"
				+ "subcategory_id TEXT,"
				+ "category_id TEXT"
				+ ");");

		//account table
		db.execSQL("CREATE TABLE account_table ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "account_name TEXT,"
				+ "account_id TEXT,"
				+ "icon INTEGER"
				+ ");");
		//daily_record_table
		db.execSQL("CREATE TABLE daily_record_table ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "date TEXT,"
				+ "category_id TEXT,"
				+ "subcategory_id TEXT,"
				+ "account_id TEXT,"
				+ "currency_id TEXT,"
				+ "amount REAL"
				+ ");");
		//debt_borrow_table
		db.execSQL("CREATE TABLE debt_borrow_table ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "person_name TEXT,"
				+ "person_number TEXT,"
				+ "taken_date TEXT,"
				+ "return_date TEXT,"
				+ "remind INTEGER,"
				+ "type INTEGER,"
				+ "account_id TEXT,"
				+ "currency_id TEXT,"
				+ "amount REAL,"
				+ "debt_id TEXT,"
				+ "photo_id TEXT"
				+ ");");
		initDefault(db);
	}

	private void initDefault(SQLiteDatabase db) {
		String[] catValues = context.getResources().getStringArray(R.array.cat_values);
		String[] catTypes = context.getResources().getStringArray(R.array.cat_types);
		String[] catIcons = context.getResources().getStringArray(R.array.cat_icons);
		for (int i=0; i<catValues.length; i++) {
			int resId = context.getResources().getIdentifier(catValues[i], "string", context.getPackageName());
			int iconId = context.getResources().getIdentifier(catIcons[i], "drawable", context.getPackageName());
			ContentValues values = new ContentValues();
			values.put("category_name", context.getResources().getString(resId));
			values.put("category_id", catValues[i]);
			values.put("category_type", catTypes[i]);
			values.put("icon", iconId);
			db.insert("category_table", null, values);
			int arrayId = context.getResources().getIdentifier(catValues[i], "array", context.getPackageName());
			String[] subCats = context.getResources().getStringArray(arrayId);
			for (int j=0; j<subCats.length; j++) {
				values.clear();
				values.put("subcategory_name", subCats[j]);
				values.put("subcategory_id", subCats[j]);
				values.put("category_id", catValues[i]);
				db.insert("subcategory_table", null, values);
			}
		}
	}

	public void saveDatasToDebtBorrowTable(ArrayList<DebtBorrow> debtBorrows) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM debt_borrow_table");
		for (int i=0; i<debtBorrows.size(); i++) {
			ContentValues values = new ContentValues();
		}
	}

	public void saveDatasToCurrencyTable(ArrayList<Currency> currencies) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM currency_table");
		db.execSQL("DELETE FROM currency_costs_table");
		for (int i=0; i<currencies.size(); i++) {
			ContentValues values = new ContentValues();
			values.put("currency_name", currencies.get(i).getName());
			values.put("currency_sign", currencies.get(i).getAbbr());
			values.put("currency_id", currencies.get(i).getId());
			values.put("currency_main", currencies.get(i).getMain());
			db.insert("currency_table", null, values);
			for (int j=0; j<currencies.get(i).getCosts().size(); j++) {
				values.clear();
				values.put("currency_id", currencies.get(i).getId());
				values.put("date", dateFormat.format(currencies.get(i).getCosts().get(j).getDay().getTime()));
				values.put("cost", currencies.get(i).getCosts().get(j).getCost());
				db.insert("currency_costs_table", null, values);
			}
		}
		db.close();
	}

	//loading currencies
	public ArrayList<Currency> loadCurrencies() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor curCursor = db.query("currency_table", null, null, null, null, null, null, null);
		Cursor curCostCursor = db.query("currency_costs_table", null, null, null, null, null, null, null);
		ArrayList<Currency> result = new ArrayList<Currency>();
		curCursor.moveToFirst();
		while (!curCursor.isAfterLast()) {
			Currency newCurrency = new Currency(curCursor.getString(curCursor.getColumnIndex("currency_name")));
			newCurrency.setAbbr(curCursor.getString(curCursor.getColumnIndex("currency_sign")));
			String currId = curCursor.getString(curCursor.getColumnIndex("currency_id"));
			newCurrency.setId(currId);
			newCurrency.setMain(curCursor.getInt(curCursor.getColumnIndex("currency_main"))!=0);
			curCostCursor.moveToFirst();
			while(!curCostCursor.isAfterLast()) {
				if (curCostCursor.getString(curCostCursor.getColumnIndex("currency_id")).matches(currId)) {
					CurrencyCost newCurrencyCost = new CurrencyCost();
					try {
						Calendar day = Calendar.getInstance();
						day.setTime(dateFormat.parse(curCostCursor.getString(curCostCursor.getColumnIndex("date"))));
						newCurrencyCost.setDay(day);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					newCurrencyCost.setCost(curCostCursor.getDouble(curCostCursor.getColumnIndex("cost")));
					newCurrency.getCosts().add(newCurrencyCost);
				}
				curCostCursor.moveToNext();
			}
			result.add(newCurrency);
			curCursor.moveToNext();
		}
		curCostCursor.close();
		curCursor.close();
		return result;
	}
	//----------Currencies end -------------

	//----------Categories begin------------
	//saving categories
	public void saveDatasToCategoryTable(ArrayList<RootCategory> categories) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM category_table");
		db.execSQL("DELETE FROM subcategory_table");
		for (int i=0; i<categories.size(); i++) {
			ContentValues values = new ContentValues();
			values.put("category_name", categories.get(i).getName());
			values.put("category_id", categories.get(i).getId());
			values.put("category_type", categories.get(i).getType());
			values.put("icon", categories.get(i).getIcon());
			db.insert("category_table", null, values);
			for (int j=0; j<categories.get(i).getSubCategories().size(); j++) {
				values.clear();
				values.put("subcategory_name", categories.get(i).getSubCategories().get(j).getName());
				values.put("subcategory_id", categories.get(i).getSubCategories().get(j).getId());
				values.put("category_id", categories.get(i).getId());
				db.insert("subcategory_table", null, values);
			}
		}
		db.close();
	}
	//loading categories
	public ArrayList<RootCategory> loadCategories() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor catCursor = db.query("category_table", null, null, null, null, null, null);
		Cursor subcatCursor = db.query("subcategory_table", null, null, null, null, null, null);
		ArrayList<RootCategory> result = new ArrayList<RootCategory>();
		catCursor.moveToFirst();
		while(!catCursor.isAfterLast()) {
			RootCategory newCategory = new RootCategory();
			newCategory.setName(catCursor.getString(catCursor.getColumnIndex("category_name")));
			String catId = catCursor.getString(catCursor.getColumnIndex("category_id"));
			newCategory.setId(catId);
			newCategory.setType(catCursor.getInt(catCursor.getColumnIndex("category_type")));
			newCategory.setIcon(catCursor.getInt(catCursor.getColumnIndex("icon")));
			subcatCursor.moveToFirst();
			ArrayList<SubCategory> subCats = new ArrayList<SubCategory>();
			while(!subcatCursor.isAfterLast()) {
				if (subcatCursor.getString(subcatCursor.getColumnIndex("category_id")).matches(catId)) {
					SubCategory newSubCategory = new SubCategory();
					newSubCategory.setName(subcatCursor.getString(subcatCursor.getColumnIndex("subcategory_name")));
					newSubCategory.setId(subcatCursor.getString(subcatCursor.getColumnIndex("subcategory_id")));
					newSubCategory.setParentId(catId);
					subCats.add(newSubCategory);
				}
				subcatCursor.moveToNext();
			}
			newCategory.setSubCategories(subCats);
			result.add(newCategory);
			catCursor.moveToNext();
		}
		catCursor.close();
		subcatCursor.close();
		return result;
	}
	//----------Categories end--------------

	//----------Purses begin----------------
	//saving purses
	public void saveDatasToAccountTable(ArrayList<Account> purses) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM account_table");
		for (int i=0; i<purses.size(); i++) {
			ContentValues values = new ContentValues();
			values.put("account_name", purses.get(i).getName());
			values.put("account_id", purses.get(i).getId());
			values.put("icon", purses.get(i).getIcon());
			db.insert("account_table", null, values);
		}
	}
	//get all purses
	public ArrayList<Account> loadAccounts() {
		ArrayList<Account> result = new ArrayList<Account>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query("account_table", null, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {
			Account newAccount = new Account();
			newAccount.setName(cursor.getString(cursor.getColumnIndex("account_name")));
			newAccount.setId(cursor.getString(cursor.getColumnIndex("account_id")));
			newAccount.setIcon(cursor.getInt(cursor.getColumnIndex("icon")));
			result.add(newAccount);
			cursor.moveToNext();
		}
		return result;
	}
	//----------Purses end------------------
	//----------daily records begin---------
	//	saving daily_record_table
	public void saveDatasToDailyRecordTable(ArrayList<FinanceRecord> records) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM daily_record_table");
		ContentValues values = new ContentValues();
		for (int i=0; i<records.size(); i++) {
			Calendar cal = records.get(i).getDate();
			String date = dateFormat.format(cal.getTime());
			values.put("date", date);
			values.put("category_id", records.get(i).getCategory().getId());
			values.put("subcategory_id", records.get(i).getSubCategory().getId());
			values.put("account_id", records.get(i).getAccount().getId());
			values.put("currency_id", records.get(i).getCurrency().getId());
			values.put("amount", records.get(i).getAmount());
			db.insert("daily_record_table", null, values);
		}
		db.close();
	}
	//laoding datas from daily_record_compound_table
	public ArrayList<FinanceRecord> loadDailyRecords() {
		ArrayList<Currency> currencies = loadCurrencies();
		ArrayList<RootCategory> categories = loadCategories();
		ArrayList<Account> purses = loadAccounts();
		ArrayList<FinanceRecord> result = new ArrayList<FinanceRecord>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query("daily_record_table", null, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			FinanceRecord newRecord = new FinanceRecord();
			Calendar cal = Calendar.getInstance();
			String date = cursor.getString(cursor.getColumnIndex("date"));
			try {
				cal.setTime(dateFormat.parse(date));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			newRecord.setDate(cal);
			for (int i=0; i<categories.size(); i++) {
				if (cursor.getString(cursor.getColumnIndex("category_id")).equals(categories.get(i).getId())) {
					newRecord.setCategory(categories.get(i));
					if (cursor.getString(cursor.getColumnIndex("subcategory_id")).matches(""))
						break;
					for (int j=0; j<categories.get(i).getSubCategories().size(); j++) {
						if (cursor.getString(cursor.getColumnIndex("subcategory_id")).matches(categories.get(i).getSubCategories().get(j).getId()))
							newRecord.setSubCategory(categories.get(i).getSubCategories().get(j));
					}
					break;
				}
			}
			for (int i=0; i<purses.size(); i++) {
				if (cursor.getString(cursor.getColumnIndex("account_id")).matches(purses.get(i).getId()))
					newRecord.setAccount(purses.get(i));
			}
			for (int i=0; i<currencies.size(); i++) {
				if (cursor.getString(cursor.getColumnIndex("currency_id")).matches(currencies.get(i).getId()))
					newRecord.setCurrency(currencies.get(i));
			}
			newRecord.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
			result.add(newRecord);
			cursor.moveToNext();
		}
		return result;
	}
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}
}