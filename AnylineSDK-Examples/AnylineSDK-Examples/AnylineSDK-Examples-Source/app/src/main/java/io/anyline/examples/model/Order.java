package io.anyline.examples.model;

import java.util.List;

public class Order {

    public static final String TABLE_NAME = "Orders";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DATE = "date";

    private int id;
    private String date;
    private List<Customer> customers;
    private String json;

    public Order() {
    }

    public Order(String date, List<Customer> customers) {
        this.date = date;
        this.customers = customers;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

//    public boolean isSynced() {
//       return (getUnsyncedCount() == 0);
//    }
//
//    public int getUnsyncedCount() {
//        int unsynced = 0;
//        for(Reading reading: getReadings()){
//            //if(!reading.isSynced())
//                unsynced++;
//        }
//        return unsynced;
//    }
//
//    public void setSynced() {
//        for(Reading reading: getReadings()){
//            //reading.setSynced(true);
//        }
//    }

//    public int getScanned() {
//        if(readings == null)
//            return 0;
//
//        int scanned = 0;
//
//        for(Reading reading : readings) {
//         //   if(reading.isScanned())
//                scanned++;
//        }
//
//        return scanned;
//    }


//
//    public String getJson() {
//        return json;
//    }
//
//    public void setJson(String json) {
//        this.json = json;
//    }
//
//    public String toJson(Order order){
//        //Order = new User();
//        Gson gson = new Gson();
//        String jsonString = gson.toJson(order);
//        try {
//            JSONObject request = new JSONObject(jsonString);
//        } catch (JSONException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return jsonString;
//    }

//    public String toJson() {
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//
//        try {
//            return mapper.writeValueAsString(this);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return super.toString();
//    }
}
