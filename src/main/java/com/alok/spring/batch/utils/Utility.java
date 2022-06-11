package com.alok.spring.batch.utils;

import com.alok.spring.utils.UploadType;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class Utility {

    static Map<String, List<String>> categoryStringMap = new HashMap<>();


    static {
        categoryStringMap.put(
                "Bills", Arrays.asList("recharge", "bill", "electricity", "gas", "indane", "tatasky",
                        "broadband", "airtel", "bescom", "tata sky", "insurance", "subscription",
                        "netflix", "membership", "racharge")
        );
        categoryStringMap.put(
                "Fuel", Arrays.asList("fuel", "petrol", "patrol", "engine oil", "diesel")
        );
        categoryStringMap.put(
                "Milk", Arrays.asList("milk", "doodh")
        );
        categoryStringMap.put(
                "Maintenance", Arrays.asList( "maintenance", "plumber", "plumb", "maintance", "service",
                        "bulb", "acqua", "repair", "aquaguard", "carpenter", "electrician", "lamp",
                        "byke", "plant", "door lock", "lights", "paint", "shower head", "curtain",
                        "hanger", "door")
        );
        categoryStringMap.put(
                "Travel", Arrays.asList("travel", "taxi","ola", "uber", "auto", "train", "flight",
                        "ticket", "hotel", "toll", "tip", "travel", "fasttag", "fastag")
        );
        categoryStringMap.put(
                "House Help", Arrays.asList("househelp", "cook", "ranjit", "ranjeet", "basantha",
                        "basanta", "maid", "byke wash", "bykewash", "byke clean", "bykeclean",
                        "vasantha", "iron", "car wash", "scooty cleaner", "wheeler cleaner",
                        "laundry", "car cleaner", "garbage")
        );
        categoryStringMap.put(
                "Food Outside", Arrays.asList("food", "pizza", "burger", "swigy", "lunch", "dinner",
                        "breakfast", "snax", "sweets", "restaurant", "idli", "southin", "party",
                        "kachori", "cookies", "dosa", "chai", "gabbar", "sanx", "vada", "mc donald",
                        "picnic", "kfc", "food", "laddoo", "beer", "wine", "vodka", "water", "cake",
                        "sweet", "icecream", "snacks")
        );
        categoryStringMap.put(
                "Accessories", Arrays.asList("accessories", "hoodie", "necklace", "sunglasses", "t-shirt",
                        "hat", "bag", "shirt", "shoe", "dress", "towel", "bedsheet", "pillow", "jeans",
                        "kurti", "belt", "tailor", "decathlon", "saree", "cloth", "sandal", "shopping",
                        "underwear", "trousers", "central", "kurti", "myntra", "juttis", "blanket",
                        "socks", "sleepers", "jacket", "bed sheet", "track pant", "frock")
        );
        categoryStringMap.put(
                "Grocery", Arrays.asList("grossary", "veg", "bazar", "groffer", "grofer", "chicken",
                        "mutton", "fish", "egg", "total", "pooja", "paneer", "butter", "curd", "baazar",
                        "bazzar", "bazaar", "prawns", "groffars", "oil", "salt", "tea", "coffee",
                        "biscuit", "deodorant", "jamun", "mango", "retail", "finger", "grossary",
                        "tissue", "ladoo", "fruits", "shampoo", "fruit", "sugar", "groceries",
                        "grocerie", "grocery", "coconut", "sanitizer", "sanitiser", "puja samagri",
                        "nariyal", "pediasure", "pickle")
        );
        categoryStringMap.put(
                "Education", Arrays.asList("book", "pen", "exam", "xerox", "copy", "tution", "unacedmy",
                        "ugc", "form", "economics", "aws", "stationary", "unacademy", "education")
        );
        categoryStringMap.put(
                "Medical", Arrays.asList("medical", "medicine", "test", "doctor", "colombia", "scan",
                        "annama", "hospital", "physiotherapy", "consultation", "Physiothreapy",
                        "Physiotherepist", "ice pack", "resistance", "physical", "protein", "glucometer",
                        "nursing", "consulting", "consultancy", "consultation", "vaccine", "injection")
        );
        categoryStringMap.put(
                "Grooming", Arrays.asList("grooming", "parlor", "facial", "Manicure", "hair", "pedicure",
                        "cream", "kaya", "lotion", "urban clap", "saloon", "makeup", "trimmer", "parlour",
                        "cosmetics", "cosmetic", "facepack", "make up", "face pack")
        );
        categoryStringMap.put(
                "Appliances", Arrays.asList("appliances", "Oven", "borosil", "phone", "urban ladder",
                        "room heater", "mixer", "watch", "steriliser")
        );
        categoryStringMap.put(
                "Gift", Arrays.asList("bablu", "gift")
        );
        categoryStringMap.put(
                "Entertainment", Arrays.asList("entertainment", "movie", "cinema", "pvr", "inox",
                        "theatre", "firestick")
        );
        categoryStringMap.put(
                "Baby Care", Arrays.asList("baby", "firstcry", "toy", "diaper")
        );
        categoryStringMap.put(
                "Baby Care", Arrays.asList("urban ladder", "chair", "table", "sofa", "furniture")
        );
        categoryStringMap.put(
                "Other", Arrays.asList("warmer")
        );
    }

    static public boolean isSalaryTransaction(String transation) {
        if (transation.toLowerCase().matches(".*salary.*|.*evolving.*|.*wipro.*|.*yodlee.*|.*bosch.*|.*j.p. morgan services.*")) {
            if (!transation.toLowerCase().matches(".*reimbursement.*|.*withdrawal.*" +
                    "|.*corp.trf.*|.*trip.*|.*hotel.*|.*ref: .*|.*imps.*")) {
                return true;
            }
        }
        return false;
    }

    static public boolean isFamilyTransaction(String transation) {
        if (transation.toLowerCase().matches(".*ramawatar.*|.*avinash.*|.*avin.*|.*gopal.*|.*papa.*|.*31987667084.*|.*PUNBX0113.*" +
                "|.*3209010000019.*|.*kharagpur.*|.*mb hr.*|.*kumari  jyoti.*|.*pankaj  kumar.*|.*bihar.*" +
                "|.*yogendra  narayan.*|.*shailendra  singh.*|.*manju  devi.*|.*vivekanand  singh.*")) {
            if (!transation.toLowerCase().matches(".*rachna.*|.*withdrawal.*|.*9916661247@.*"
            )) {
                return true;
            }
        }
        return false;
    }

    static public boolean isReversalTransaction(String transaction) {
        if (transaction.toLowerCase().matches(".*outward  rev.*") ||
                transaction.toLowerCase().matches(".*rev:imps.*") ||
                transaction.toLowerCase().matches(".*received from.*")
        ) {
            if (!transaction.toLowerCase().matches(".*withdrawal.*")) {
                return true;
            }
        }
        return false;
    }

    static public String getExpenseCategory(String expenseHead, String expenseComment) {
        if (expenseHead.length() == 0) {
            return "";
        }
        String stringToSearch = expenseHead.toLowerCase();

        //loop through each category until a match is reached then return the category
        for (Map.Entry<String, List<String>> entry: categoryStringMap.entrySet()) {
            for (String string: entry.getValue()) {
                if (stringToSearch.contains(string))
                    return entry.getKey();
            }
        }

        return "Other";
    }

    static public String getDateFormat(String dateString) {

        if (dateString.trim().matches("....\\/...\\/..")) {
            return "yyyy/MMM/dd";
        }

        if (dateString.trim().matches("..-..-....")) {
            return "dd-MM-yyyy";
        }

        if (dateString.trim().matches("..\\/..\\/..")) {
            return "dd/MM/yy";
        }

        return "dd/MM/yyyy";
    }

    static public UploadType getUploadType(String fileName) {

        if (fileName.matches("^Report-.*.csv$|^KM52025632.*.csv$"))
            return UploadType.KotakExportedStatement;

        if (fileName.matches("^190992811_.*.txt$"))
            return UploadType.HDFCExportedStatement;

        return null;
    }
}
