package io.anyline.examples.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import io.anyline.examples.model.Customer;
import io.anyline.examples.model.Order;
import io.anyline.examples.model.Reading;

//database class used for meter reading processes
public class DataBaseProcessesAdapter
{
    protected static final String TAG = "DataAdapter";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DataBaseProcessesHelper mDbHelper;


    public DataBaseProcessesAdapter(Context context) throws IOException {
        this.mContext = context;
        mDbHelper = new DataBaseProcessesHelper(mContext);
    }

    //get list of orders
    public List<Order> getOrders() {

        List<Order> orders = new ArrayList<Order>();
        Order modelOrder = null;

        String query ="SELECT * FROM `" + Order.TABLE_NAME + "`";
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                modelOrder = new Order();
                modelOrder.setId(Integer.parseInt(cursor.getString(0)));
                modelOrder.setDate(cursor.getString(1));
                orders.add(modelOrder);

            } while (cursor.moveToNext());
        }

        return orders;

    }

    //search if there are unsync readings
    public int unsyncCustomers() {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int cnt  = (int) DatabaseUtils.queryNumEntries(db, Customer.TABLE_NAME, "isSynced = 0");
        db.close();
        return cnt;
    }

    public int completedCustomers(int orderId) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int cnt  = (int) DatabaseUtils.queryNumEntries(db, Customer.TABLE_NAME, "isCompleted = 1 and orderId = " + orderId);
        db.close();
        return cnt;
    }

    public int countCustomersByOrderId(int orderId) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int cnt  = (int) DatabaseUtils.queryNumEntries(db, Customer.TABLE_NAME, "orderId = " + orderId);
        db.close();
        return cnt;
    }

    public List<Customer> getCustomersByOrderId(int orderId) {

        List<Customer> customers = new ArrayList<Customer>();
        //Reading reading = null;
        Customer customerModel = null;
        String query ="SELECT * FROM " + Customer.TABLE_NAME +
                     // "` INNER JOIN `" + Reading.TABLE_NAME + "` on " + Customer.TABLE_NAME + "." + Customer.COLUMN_ID  + " = " + Reading.TABLE_NAME + "." + Reading.COLUMN_CUSTOMER_ID +
                      " WHERE " + Customer.COLUMN_ORDER_ID + " = " + orderId;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                customerModel = new Customer();
                customerModel.setId(Integer.parseInt(cursor.getString(0)));
                customerModel.setName(cursor.getString(1));
                customerModel.setAddress(cursor.getString(2));
                customerModel.setAnnualConsumption(Long.parseLong(cursor.getString(3)));
                customerModel.setOrderId(Integer.parseInt(cursor.getString(4)));
                customerModel.setMeterId(Long.parseLong(cursor.getString(5)));
                customerModel.setMeterType(cursor.getString(6));
                customerModel.setIsSynced(cursor.getInt(7));
                customerModel.setIsCompleted(cursor.getInt(8));

                customers.add(customerModel);

            } while (cursor.moveToNext());
        }

        return customers;
    }

    public List<Reading> getHistorySelfScan() {

        List<Reading> readingList = new ArrayList<Reading>();
        Reading readingModel = null;
        String query ="SELECT * FROM " + Reading.TABLE_NAME +
                      // "` INNER JOIN `" + Reading.TABLE_NAME + "` on " + Customer.TABLE_NAME + "." + Customer.COLUMN_ID  + " = " + Reading.TABLE_NAME + "." + Reading.COLUMN_CUSTOMER_ID +
                      " WHERE " + Reading.COLUMN_SCANNED + " = 1";
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                readingModel = new Reading();
                readingModel.setId(Integer.parseInt(cursor.getString(0)));
                readingModel.setCustomerId(cursor.getInt(1));
                readingModel.setLastReadingDate(cursor.getString(3));
                readingModel.setLastReadingValue(cursor.getString(4));
                readingModel.setFullImageLocalPath(cursor.getString(5));

                readingList.add(readingModel);

            } while (cursor.moveToNext());
        }

        return readingList;
    }

    public Customer getCustomersByOrderIdAndMeterId(String meterId, long orderId) {

        Customer customer = null;
        Customer customerModel = null;
        String query ="SELECT * FROM " + Customer.TABLE_NAME +
                      // "` INNER JOIN `" + Reading.TABLE_NAME + "` on " + Customer.TABLE_NAME + "." + Customer.COLUMN_ID  + " = " + Reading.TABLE_NAME + "." + Reading.COLUMN_CUSTOMER_ID +
                      " WHERE " /*+ Customer.COLUMN_ORDER_ID + " = " + orderId + " AND " */ + Customer.COLUMN_METER_ID + " LIKE'%" + meterId + "%' " +
                      " AND " /*+ Customer.COLUMN_ORDER_ID + " = " + orderId + " AND " */ + Customer.COLUMN_ORDER_ID + " = " + orderId;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                customerModel = new Customer();
                customerModel.setId(Integer.parseInt(cursor.getString(0)));
                customerModel.setName(cursor.getString(1));
                customerModel.setAddress(cursor.getString(2));
                customerModel.setAnnualConsumption(Long.parseLong(cursor.getString(3)));
                customerModel.setOrderId(Integer.parseInt(cursor.getString(4)));
                customerModel.setMeterId(Long.parseLong(cursor.getString(5)));
                customerModel.setMeterType(cursor.getString(6));
                customerModel.setIsSynced(cursor.getInt(7));
                customerModel.setIsCompleted(cursor.getInt(8));

                customer = customerModel;

            } while (cursor.moveToNext());
        }

        return customer;
    }

    public Customer getCustomersByMeterId(String meterId) {

        Customer customer = null;
        Customer customerModel = null;
        String query ="SELECT * FROM " + Customer.TABLE_NAME +
                      // "` INNER JOIN `" + Reading.TABLE_NAME + "` on " + Customer.TABLE_NAME + "." + Customer.COLUMN_ID  + " = " + Reading.TABLE_NAME + "." + Reading.COLUMN_CUSTOMER_ID +
                      " WHERE " /*+ Customer.COLUMN_ORDER_ID + " = " + orderId + " AND " */ + Customer.COLUMN_METER_ID + " = '" + meterId + "'" + " ORDER BY " + Customer.COLUMN_ORDER_ID + " DESC";

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                customerModel = new Customer();
                customerModel.setId(Integer.parseInt(cursor.getString(0)));
                customerModel.setName(cursor.getString(1));
                customerModel.setAddress(cursor.getString(2));
                customerModel.setAnnualConsumption(Long.parseLong(cursor.getString(3)));
                customerModel.setOrderId(Integer.parseInt(cursor.getString(4)));
                customerModel.setMeterId(Long.parseLong(cursor.getString(5)));
                customerModel.setMeterType(cursor.getString(6));
                customerModel.setIsSynced(cursor.getInt(7));
                customerModel.setIsCompleted(cursor.getInt(8));

                customer = customerModel;

            } while (cursor.moveToNext());
        }

        return customer;
    }

    public List<String> getCustomerMeterIdListByOrderId(int orderId) {

        List<String> customerMeterIdList = new ArrayList<>();
        Customer customer = null;
        Customer customerModel = null;
        String query ="SELECT * FROM " + Customer.TABLE_NAME +
                 " WHERE " +  Customer.COLUMN_ORDER_ID + " = " + orderId + "";

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                customerMeterIdList.add(String.valueOf(Long.parseLong(cursor.getString(5))));

            } while (cursor.moveToNext());
        }

        return customerMeterIdList;
    }

    public List<String> getCustomerMeterIdList() {

        List<String> customerMeterIdList = new ArrayList<>();
        String query ="SELECT * FROM " + Customer.TABLE_NAME + " ORDER BY " + Customer.COLUMN_ORDER_ID + " ASC";

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                customerMeterIdList.add(String.valueOf(Long.parseLong(cursor.getString(5))));

            } while (cursor.moveToNext());
        }

        return customerMeterIdList;
    }

    public Customer getCustomerById (int id) {

        Customer customer = null;
        Customer customerModel = null;
        String query ="SELECT * FROM " + Customer.TABLE_NAME +
                      // "` INNER JOIN `" + Reading.TABLE_NAME + "` on " + Customer.TABLE_NAME + "." + Customer.COLUMN_ID  + " = " + Reading.TABLE_NAME + "." + Reading.COLUMN_CUSTOMER_ID +
                      " WHERE " /*+ Customer.COLUMN_ORDER_ID + " = " + orderId + " AND " */ + Customer.COLUMN_ID + " = " + id;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                customerModel = new Customer();
                customerModel.setId(Integer.parseInt(cursor.getString(0)));
                customerModel.setName(cursor.getString(1));
                customerModel.setAddress(cursor.getString(2));
                customerModel.setAnnualConsumption(Long.parseLong(cursor.getString(3)));
                customerModel.setOrderId(Integer.parseInt(cursor.getString(4)));
                customerModel.setMeterId(Long.parseLong(cursor.getString(5)));
                customerModel.setMeterType(cursor.getString(6));
                customerModel.setIsSynced(cursor.getInt(7));
                customerModel.setIsCompleted(cursor.getInt(8));

                customer = customerModel;

            } while (cursor.moveToNext());
        }

        return customer;
    }

    public Reading getLastReadingByCustomerId(int customerId) {

        Reading reading = null;
        Reading readingModel = null;
        //Reading reading = null;
        String query ="SELECT * FROM " + Reading.TABLE_NAME +
                      // "` INNER JOIN `" + Reading.TABLE_NAME + "` on " + Customer.TABLE_NAME + "." + Customer.COLUMN_ID  + " = " + Reading.TABLE_NAME + "." + Reading.COLUMN_CUSTOMER_ID +
                      " WHERE " + Reading.COLUMN_CUSTOMER_ID + " = " + customerId + " ORDER BY dateTime(" + Reading.COLUMN_LAST_READING_DATE + ") DESC LIMIT 1";
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                readingModel = new Reading();
                readingModel.setId(cursor.getInt(0));
                readingModel.setCustomerId(customerId);
                //readingModel.setSynced(cursor.getInt(2) == 0 ? false : true);
                readingModel.setIsScanned(cursor.getInt(2) == 0 ? false : true);
                readingModel.setLastReadingDate(cursor.getString(3));
                readingModel.setLastReadingValue(cursor.getString(4));
                readingModel.setFullImageLocalPath(cursor.getString(5));

                reading = readingModel;

            } while (cursor.moveToNext());
        }

        return reading;
    }

    //sync process
    public void syncCustomers(){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String query ="UPDATE Customer SET isSynced = 1";
        db.execSQL(query);

        db.close();
    }

    //update Reading in order to sync it
    public void updateReading(Reading reading) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(reading.COLUMN_LAST_READING_DATE, reading.getLastReadingDate());
        values.put(reading.COLUMN_LAST_READING_VALUE , reading.getLastReadingValue());
        values.put(reading.COLUMN_IMAGE_PATH, reading.getFullImageLocalPath());
       // values.put(reading.COLUMN_SYNC, reading.isSynced()? 1 : 0);
        values.put(reading.COLUMN_SCANNED, reading.isScanned()? 1 : 0);

        int i = db.update("Reading", // table
                          values, // column/value
                          "customerId = ?", // selections
                          new String[] { String.valueOf(reading.getCustomerId()) });


        db.close();
    }

    public void insertReading(Reading reading){

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(reading.COLUMN_CUSTOMER_ID, reading.getCustomerId());
        values.put(reading.COLUMN_SCANNED , reading.isScanned() ? 1 : 0);
        values.put(reading.COLUMN_LAST_READING_DATE, reading.getLastReadingDate());
        values.put(reading.COLUMN_LAST_READING_VALUE , reading.getLastReadingValue());
        values.put(reading.COLUMN_IMAGE_PATH, reading.getFullImageLocalPath());

        try {
            database.insert(Reading.TABLE_NAME, null, values);
        }catch (Exception ex){
            throw  ex;
        }
        database.close();
    }

    public void updateCustomer(Customer customer) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(customer.COLUMN_SYNC, customer.getIsSynced());
        values.put(customer.COLUMN_COMPLETE, customer.getIsCompleted());

        int i = db.update(Customer.TABLE_NAME, // table
                          values, // column/value
                          "id = ?", // selections
                          new String[] { String.valueOf(customer.getId()) });


        db.close();
    }
}
